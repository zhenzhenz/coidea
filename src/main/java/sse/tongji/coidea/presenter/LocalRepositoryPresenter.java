package sse.tongji.coidea.presenter;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import dev.mtage.error.CommonSysException;
import dev.mtage.eyjaot.client.OtClient;
import dev.mtage.eyjaot.client.entity.ClientCoFile;
import dev.mtage.eyjaot.client.inter.presenter.GeneralLocalRepositoryPresenter;
import dev.mtage.eyjaot.client.inter.presenter.IFilePresenter;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import dev.mtage.eyjaot.client.inter.view.*;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import org.apache.commons.collections4.CollectionUtils;
import sse.tongji.coidea.config.CoIDEAUIString;
import sse.tongji.coidea.listener.MyFileEditorManagerListener;
import sse.tongji.coidea.listener.MyRepositoryListener;
import sse.tongji.coidea.listener.MyTypedActionHandler;
import sse.tongji.coidea.view.*;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static sse.tongji.coidea.util.CoIDEAFilePathUtil.getProjectRelativePath;

/**
 * @author mtage
 * @since 2021/3/17 19:15
 */
@SuppressWarnings("jol")
public class LocalRepositoryPresenter extends GeneralLocalRepositoryPresenter {
    private static final Map<String, LocalRepositoryPresenter> projectIdPresenterMap = new HashMap<>();

    private final MyLogger log = MyLogger.getLogger(LocalRepositoryPresenter.class);

    private Project project;

    private DalPolicySettings dalPolicySettings;

    private ConnConfigurationInput connConfig;
    private IMessageView messageView = new SimpleMessageView();
    private AnActionEvent connectAction;

    private MyRepositoryListener repositoryListener;
    private MyFileEditorManagerListener fileEditorManagerListener;
    private MyTypedActionHandler typedActionHandler;


    public LocalRepositoryPresenter(IConnConfigureView connConfigureView, IBasicCollaborationInfoView collaborationInfoView,
                                    INotificationView notificationView, IRepositoryEditor repositoryView,
                                    Project project) {
        super(connConfigureView, collaborationInfoView, notificationView, repositoryView);
        this.project = project;
    }


    public static LocalRepositoryPresenter fromProject(Project project) {
        if (projectIdPresenterMap.containsKey(project.getBasePath())) {
            return projectIdPresenterMap.get(project.getBasePath());
        }
        SimpleNotifyInfoView simpleNotifyInfoView = new SimpleNotifyInfoView(project);

        LocalRepositoryPresenter presenter = new LocalRepositoryPresenter(new ConnConfigurationDialog(project),
                simpleNotifyInfoView, simpleNotifyInfoView, new RepositoryEditorImpl(project), project);
        projectIdPresenterMap.put(project.getBasePath(), presenter);
        return presenter;
    }

    public void onConnectClicked(AnActionEvent e) {
        this.project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            messageView.messageWithDefaultTitle("You have to open a project before using " + CoIDEAUIString.PLUGIN_NAME);
            return;
        }
        this.connConfig = this.connConfigureView.readConfigurationInput();
        connectAndSync(this.connConfig);
        e.getPresentation().setEnabled(false);
    }


    @Override
    public void connectAndSync(ConnConfigurationInput conf) {
        try {
            log.info("尝试连接，配置 {0}", conf);
            conf.validate();
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
            this.repositoryListener = new MyRepositoryListener(this);
            MyRepositoryListener.resumeListening();
            VirtualFileManager.getInstance().addVirtualFileListener(repositoryListener);
            this.fileEditorManagerListener = new MyFileEditorManagerListener(this);
            this.project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                   this.fileEditorManagerListener);
            this.typedActionHandler = new MyTypedActionHandler(this);
            this.typedActionHandler.setOldHandler(TypedAction.getInstance().setupRawHandler(typedActionHandler));

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
        if (Objects.isNull(otClient) || !otClient.isConnected()) {
            notificationView.sysNotify("You have disconnected");
            return;
        }
        openedFilePresenters.values().forEach(IFilePresenter::close);
        MyRepositoryListener.pauseListening();
        VirtualFileManager.getInstance().removeVirtualFileListener(repositoryListener);
        otClient.leaveRepo();
        openedFilePresenters.clear();
    }

    @Override
    public void onLocalFileOpen(String absolutePath, String fileName) {
        throw new UnsupportedOperationException();
    }

    public void onLocalKeyPressed(VirtualFile file, char charTyped) {
        this.openedFilePresenters.get(getProjectRelativePath(file, project))
                .acquireSemaphore();
    }

    public void onLocalFileOpen(FileEditorManager source, VirtualFile file) {
        log.info("local file open {0}", file.getPath());
        LocalFilePresenter localFilePresenter = new LocalFilePresenter(project, file);
        String fileRelativePath = getProjectRelativePath(file, project);
        ClientCoFile clientCoFile = otClient.openFile(fileRelativePath, file.getName(), localFilePresenter);
        localFilePresenter.setOtClientCoFile(clientCoFile);
        localFilePresenter.setLocalRepositoryPresenter(this);
        this.openedFilePresenters.put(fileRelativePath, localFilePresenter);
    }

    public void onLocalFileClose(FileEditorManager source, VirtualFile file) {
        log.warn("local file closed {0}", file.getPath());
        onLocalFileClose(file.getPath(), file.getName());
    }


    @Override
    public void onInitRepo(byte[] repoData, Collection<CoUser> users, DalPolicySettings dalPolicySettings) {
        log.info("接收到来自服务器的项目文件");
        MyRepositoryListener.pauseListening();
        try {
            repositoryView.syncDataToDefault(repoData);
        } catch (IOException e) {
            log.error("IOException", e);
        }
        collaborationInfoView.displayCollaborators(users);
        if (CollectionUtils.isNotEmpty(users)) {
            log.info("项目成员数:{0} 成员: {1}", users.size(),
                    users.stream().map(CoUser::getUserName).collect(Collectors.toList()));
        }
        this.dalPolicySettings = dalPolicySettings;

        log.info("接受全项目文件处理完毕 resume listening...");
        MyRepositoryListener.resumeListening();
    }

    private boolean connected() {
        return otClient.isConnected();
    }

    @Override
    public void onError(String code, String message) {
        super.onError(code, message);
        this.connectAction.getPresentation().setEnabled(true);
    }
}
