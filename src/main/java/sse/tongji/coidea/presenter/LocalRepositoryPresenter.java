package sse.tongji.coidea.presenter;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import dev.mtage.error.CommonSysException;
import dev.mtage.eyjaot.client.OtClient;
import dev.mtage.eyjaot.client.inter.ILocalRepositoryEditor;
import dev.mtage.eyjaot.client.inter.presenter.GeneralLocalRepositoryPresenter;
import dev.mtage.eyjaot.client.inter.view.*;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import sse.tongji.coidea.listener.MyFileEditorManagerListener;
import sse.tongji.coidea.listener.MyRepositoryListener;
import sse.tongji.coidea.view.*;
import util.MyLogger;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

/**
 * @author mtage
 * @since 2021/3/17 19:15
 */
public class LocalRepositoryPresenter extends GeneralLocalRepositoryPresenter {
    private final MyLogger log = MyLogger.getLogger(LocalRepositoryPresenter.class);

    private Project project;

    private ConnConfigurationInput connConfig;
    private IMessageView messageView = new SimpleMessageView();

    public LocalRepositoryPresenter(IBasicCollaborationInfoView collaborationInfoView,
                                    INotificationView notificationView, IRepositoryEditor repositoryView) {
        super(collaborationInfoView, notificationView, repositoryView);
    }

    public void onConnectClicked(AnActionEvent e) {
        this.project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            messageView.messageWithDefaultTitle("You have to open a project before using CoIDEA");
            return;
        }
        if (Objects.nonNull(otClient) && otClient.isConnected()) {
            notificationView.sysNotify("You have connected to" + connConfig.getRepoId());
            return;
        }
        initSimpleNotifyView(this.project);
        this.connConfig = this.connConfigureView.readConfigurationInput();
        connectAndSync(this.connConfig);
    }


    @Override
    public void connectAndSync(ConnConfigurationInput conf) {
        try {
            log.info("尝试连接，配置 {0}", conf);
            this.otClient = OtClient.createWsClient(conf.getServerAddr()).connect();
            while (!otClient.isConnected()) {
                try {
                    // 这个地方确实设计得不大好...未来应该考虑异步回调
                    //noinspection BusyWait
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    log.error("InterruptedException when waiting for connection", e);
                }
            }
            if (conf.isNewRepo()) {
                try {
                    byte[] repoData = repositoryView.readDefaultProjectAllData();
                    otClient.newRepo(conf.getRepoId(), conf.getUserName(), repoData,
                            DalPolicySettings.builder()
                                    .isDalOpen(conf.isOpenDal())
                                    .deepOfLocking(conf.getDepthOfLocking())
                                    .isLockingFields(conf.isLockFields())
                                    .isLockingMethods(conf.isLockMethods())
                                    .build()
                            , this);
                } catch (IOException e) {
                    collaborationInfoView.displayConnErr("Error when compressing your repository: " + e.getMessage());
                    log.error("read repo all data IOException", e);
                }
            } else {
                otClient.joinRepo(conf.getRepoId(), conf.getUserName(), this);
            }

            // 设置全项目监听
            MyRepositoryListener repositoryListener = new MyRepositoryListener(this);
            MyRepositoryListener.resumeListening();
            VirtualFileManager.getInstance().addVirtualFileListener(repositoryListener);
            this.project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                    new MyFileEditorManagerListener(this)
            );

            collaborationInfoView.displayConnSuccess();
            log.info("localUser {0}", otClient.getLocalUser().toString());
        } catch (CommonSysException e) {
            collaborationInfoView.displayConnErr(e.getMessage());
            log.error("connect error", e);
        } finally {
            log.info("conn configuration view {0}", this.connConfigureView);
            this.connConfigureView.close();
        }
    }

    @Override
    public void onDisconnectClicked() {

    }

    @Override
    public void onLocalFileOpen(String absolutePath, String fileName) {

    }


    private void initSimpleNotifyView(Project localProject) {
        SimpleNotifyInfoView simpleNotifyInfoView = new SimpleNotifyInfoView(localProject);
        this.collaborationInfoView = simpleNotifyInfoView;
        this.notificationView = simpleNotifyInfoView;
        this.connConfigureView = new ConnConfigurationDialog(localProject);
        this.repositoryView = new RepositoryEditorImpl(localProject);
    }

    @Override
    public void onInitRepo(byte[] repoData, Collection<CoUser> users, DalPolicySettings dalPolicySettings) {

    }

}
