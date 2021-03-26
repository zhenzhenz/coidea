package sse.tongji.coidea.presenter;

import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import dev.mtage.error.CommonSysException;
import dev.mtage.eyjaot.client.OtClient;
import dev.mtage.eyjaot.client.entity.ClientCoFile;
import dev.mtage.eyjaot.client.inter.presenter.GeneralLocalRepositoryPresenter;
import dev.mtage.eyjaot.client.inter.presenter.IFilePresenter;
import dev.mtage.eyjaot.client.inter.util.FilePathUtil;
import dev.mtage.eyjaot.client.inter.util.GeneralFileIgnoreUtil;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import dev.mtage.eyjaot.client.inter.view.*;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import org.apache.commons.collections4.CollectionUtils;
import sse.tongji.coidea.config.CoIDEAUIString;
import sse.tongji.coidea.config.ConnectionConfig;
import sse.tongji.coidea.listener.MyAllKeyListener;
import sse.tongji.coidea.listener.MyFileOpenCloseListener;
import sse.tongji.coidea.listener.MyRepositoryListener;
import sse.tongji.coidea.util.CoIDEAFilePathUtil;
import sse.tongji.coidea.view.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private MyFileOpenCloseListener fileEditorManagerListener;
//    private MyTypedActionHandler typedActionHandler;


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
        if (Objects.nonNull(this.connConfig) && Objects.nonNull(this.connConfig.getUserName())) {
            connectAndSync(this.connConfig);
//            e.getPresentation().setEnabled(false);
        }
    }


    @Override
    public void connectAndSync(ConnConfigurationInput conf) {
        try {
            log.info("尝试连接，配置 {0}", conf);
            conf.validate();
            this.otClient = OtClient.createWsClient(conf.getServerAddr()).connect();
            for (int i = 0; i < ConnectionConfig.INIT_CONNECT_MAX; i++) {
                if (otClient.isConnected()) {
                    break;
                }
                try {
                    // 这个地方确实设计得不大好...未来应该考虑异步回调
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    log.error("InterruptedException when waiting for connection", e);
                }
                if (i + 1 >= ConnectionConfig.INIT_CONNECT_MAX) {
                    messageView.messageWithDefaultTitle("Cannot connect to server. Check your Internet connection please.");
                    return;
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
                    this.repositoryListener = new MyRepositoryListener(this);
                    MyRepositoryListener.resumeListening();
                    VirtualFileManager.getInstance().addVirtualFileListener(repositoryListener);
                } catch (IOException e) {
                    collaborationInfoView.displayConnErr("Error when compressing your repository: " + e.getMessage());
                    log.error("read repo all data IOException", e);
                }

            } else {
                otClient.joinRepo(conf.getRepoId(), conf.getUserName(), this);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("InterruptedException", e);
                }
            }

            // 设置全项目监听
            this.fileEditorManagerListener = new MyFileOpenCloseListener(this);
            this.project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                   this.fileEditorManagerListener);
//            this.typedActionHandler = new MyTypedActionHandler(this);
//            this.typedActionHandler.setOldHandler(TypedAction.getInstance().setupRawHandler(typedActionHandler));

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

    @Deprecated
    public void onLocalKeyPressed(VirtualFile file, char charTyped) {
        this.openedFilePresenters.get(getProjectRelativePath(file, project))
                .acquireSemaphore();
    }

    @Override
    public void onLocalFileCreate(String absolutePath, String fileName) {
        log.info("local file created {0}", absolutePath);
        super.onLocalFileCreate(absolutePath, fileName);
    }

    public void onLocalFileCreate(VirtualFile file) {
        String projectRelativePath = FilePathUtil.getProjectRelativePath(file.getPath(),
                repositoryView.getDefaultProjectPath(), repositoryView.getDefaultProjectName());
        if (Files.isDirectory(Paths.get(file.getPath()))) {
            otClient.createDir(projectRelativePath, file.getName());
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            Document document = FileDocumentManager.getInstance().getDocument(file);
            otClient.createFile(projectRelativePath, projectRelativePath, document.getText());
            log.info("send create file request {0} name: {1}", projectRelativePath, file.getName());
        });
    }

    public void onLocalFileOpen(FileEditorManager source, VirtualFile file) {
        log.info("local file open {0}", file.getPath());
        if (GeneralFileIgnoreUtil.isIgnored(file.getName())) {
            log.info("local file opened and ignored {0}", file.getPath());
            return;
        }
        LocalFilePresenter localFilePresenter = new LocalFilePresenter(project, file);
        String fileRelativePath = getProjectRelativePath(file, project);
        ApplicationManager.getApplication().invokeLater(() -> {
            ClientCoFile clientCoFile = otClient.openFile(fileRelativePath, file.getName(), localFilePresenter);
            localFilePresenter.setOtClientCoFile(clientCoFile);
            localFilePresenter.setLocalRepositoryPresenter(this);
            this.openedFilePresenters.put(fileRelativePath, localFilePresenter);
        });
    }

    public void onLocalFileClose(FileEditorManager source, VirtualFile file) {
        log.warn("local file closed {0}", file.getPath());
        if (GeneralFileIgnoreUtil.isIgnored(file.getName())) {
            log.info("local file closed and ignored {0}", file.getPath());
            return;
        }
        onLocalFileClose(CoIDEAFilePathUtil.getProjectRelativePath(file.getPath(), project), file.getName());
    }


    @Override
    public void onInitRepo(byte[] repoData, Collection<CoUser> users, DalPolicySettings dalPolicySettings) {
        log.info("接收到来自服务器的项目文件");
        MyRepositoryListener.pauseListening();
        try {
            log.info("repo listening {0}", MyRepositoryListener.isResourceListening.get());
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

        log.info("接受全项目文件处理完毕");

        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                LocalFileSystem.getInstance().refreshWithoutFileWatcher(false);
                log.info("resume listening...");
                this.repositoryListener = new MyRepositoryListener(this);
                MyRepositoryListener.resumeListening();
                VirtualFileManager.getInstance().addVirtualFileListener(repositoryListener);
            });
        });
    }

    @Override
    public void onError(String code, String message) {
        super.onError(code, message);
//        this.connectAction.getPresentation().setEnabled(true);
    }

    @Override
    public void onCreateDir(String path, CoUser user) {
        MyRepositoryListener.pauseListening();
        super.onCreateDir(path, user);
        MyRepositoryListener.resumeListening();
    }

    @Override
    public void onCreateFile(String path, String content, CoUser user) {
        MyRepositoryListener.pauseListening();
        super.onCreateFile(path, content, user);
        MyRepositoryListener.resumeListening();
    }

    @Override
    public void onDeleteDir(String path, CoUser user) {
        MyRepositoryListener.pauseListening();
        super.onDeleteDir(path, user);
        MyRepositoryListener.resumeListening();
    }

    private boolean connected() {
        return otClient.isConnected();
    }

}
