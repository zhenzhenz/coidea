package sse.tongji.coidea.presenter;

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
import dev.mtage.eyjaot.client.inter.ILocalFileEditor;
import dev.mtage.eyjaot.client.inter.presenter.GeneralLocalRepositoryPresenter;
import dev.mtage.eyjaot.client.inter.presenter.GeneralUnopenedFilePresenter;
import dev.mtage.eyjaot.client.inter.presenter.IFilePresenter;
import dev.mtage.eyjaot.client.inter.util.FilePathUtil;
import dev.mtage.eyjaot.client.inter.util.GeneralFileIgnoreUtil;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import dev.mtage.eyjaot.client.inter.view.*;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.action.file.FileContentInsertAction;
import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import dev.mtage.eyjaot.core.util.EditOperationSourceEnum;
import org.apache.commons.collections4.CollectionUtils;
import sse.tongji.coidea.config.AppSettingsState;
import sse.tongji.coidea.config.CoIDEAUIString;
import sse.tongji.coidea.config.ConnectionConfig;
import sse.tongji.coidea.listener.MyFileOpenCloseListener;
import sse.tongji.coidea.listener.MyRepositoryListener;
import sse.tongji.coidea.util.CoIDEAFilePathUtil;
import sse.tongji.coidea.view.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
                                    INotificationView notificationView, IRepositoryEditorView repositoryView,
                                    Project project) {
        super(connConfigureView, collaborationInfoView, notificationView, repositoryView);
        this.project = project;
    }

    public LocalRepositoryPresenter(IBasicCollaborationInfoView collaborationInfoView, INotificationView notificationView,
                                    IRepositoryEditorView repositoryView) {
        super(collaborationInfoView, notificationView, repositoryView);
    }

    /**
     * 构建简单的通知View
     * @param project
     * @return
     */
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

    public static LocalRepositoryPresenter fromProject(IBasicCollaborationInfoView collaborationInfoView,
                                                       INotificationView notificationView,
                                                       Project project) {
        if (projectIdPresenterMap.containsKey(project.getBasePath())) {
            return projectIdPresenterMap.get(project.getBasePath());
        }
        LocalRepositoryPresenter presenter = new LocalRepositoryPresenter(new ConnConfigurationDialog(project),
                collaborationInfoView, notificationView, new RepositoryEditorImpl(project), project);
        projectIdPresenterMap.put(project.getBasePath(), presenter);
        return presenter;
    }

    /**
     * 菜单栏按下connect
     * @param e
     */
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

    /**
     * 侧边控制面板按下
     * @param e
     */
    public void onConnectDisconnectClicked(AnActionEvent e) {
        if (Objects.isNull(this.otClient) || !this.otClient.isConnected()) {
            ConfigureDialogWrapper connConfigureView = new ConfigureDialogWrapper();
            if (connConfigureView.showAndGet()) {
                this.connConfigureView = connConfigureView;
                ConnConfigurationInput conf = this.connConfigureView.readConfigurationInput();
                this.connConfigureView.close();
                connectAndSync(conf);
            }
        } else {
            onDisconnectClicked();
            this.collaborationInfoView.displayConnBroken("Disconnected");
        }
    }

    public class LocalUnOpenFilePresenter extends GeneralUnopenedFilePresenter {

        public LocalUnOpenFilePresenter(String path) {
            super(path);
        }

        @Override
        protected Path getAbsolutePath() {
//            log.info("{0} absolutePath:{1}", getPath(), Paths.get(CoEclipseFilePathUtil.getStandardAbsolutePath(getPath(), repositoryView.getDefaultProjectPath())));
            return Paths.get(CoIDEAFilePathUtil.getStandardAbsolutePath(getPath(), repositoryView.getDefaultProjectPath()));
        }

        @Override
        public void onInsert(FileContentInsertAction operation, EditOperationSourceEnum source, CoUser coUser) throws IOException {
            log.info("本地未打开的文件插入 {0}", operation.getContent());
            super.onInsert(operation, source, coUser);
        }
    }

    @Override
    public ILocalFileEditor getLocalFileEditor(String path) {
        if (openedFilePresenters.containsKey(path)) {
            return openedFilePresenters.get(path);
        }
        log.info("构建未打开文件 {0} 的presenter", path);
        return new LocalUnOpenFilePresenter(path);
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
                                    .dalOpen(conf.isOpenDal())
                                    .fieldDepth(conf.getDepthOfLocking())
                                    .methodDepth(conf.getDepthOfLocking())
                                    .depthOpen(conf.isLockFields())
                                    .build()
                            , this);
                    //TODO 改成自动的从UI获取
                    this.repositoryListener = new MyRepositoryListener(this);
                    MyRepositoryListener.resumeListening();
                    VirtualFileManager.getInstance().addVirtualFileListener(repositoryListener);
                    collaborationInfoView.displayCollaborators(Collections.singleton(otClient.getLocalUser()));
                } catch (IOException e) {
                    collaborationInfoView.displayConnErr("Error when compressing your repository: " + e.getMessage());
                    log.error("read repo all data IOException", e);
                }

            } else {
                otClient.joinRepo(conf.getRepoId(), conf.getUserName(), this);
            }

            // 设置全项目监听
            this.fileEditorManagerListener = new MyFileOpenCloseListener(this);
            this.project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                   this.fileEditorManagerListener);
//            this.typedActionHandler = new MyTypedActionHandler(this);
//            this.typedActionHandler.setOldHandler(TypedAction.getInstance().setupRawHandler(typedActionHandler));

            collaborationInfoView.displayUserName(conf.getUserName());
            collaborationInfoView.displayRepoId(conf.getRepoId());
            collaborationInfoView.displayConnSuccess();
            log.info("localUser {0}", otClient.getLocalUser().toString());

            AppSettingsState settingsState = AppSettingsState.getInstance();
            log.info("读取DAL用户自定义配置 :open={0} depthOpen={1} timeOut={2} depth={3}/{4}",
                    settingsState.isDalOpen(), settingsState.isDepthOpen(), settingsState.getTimeout(), settingsState.getMethodDepth(), settingsState.getFieldDepth());
            otClient.updatePersonalSettings(new CoUser.PersonalSettings(DalPolicySettings.builder()
                    .dalOpen(settingsState.isDalOpen())
                    .depthOpen(settingsState.isDepthOpen())
                    .methodDepth(settingsState.getMethodDepth())
                    .fieldDepth(settingsState.getFieldDepth())
                    .timeoutSecond(settingsState.getTimeout())
                    .build()));
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

    /**
     * 不可用
     * @see LocalRepositoryPresenter#onLocalFileOpen(FileEditorManager, VirtualFile)
     * @param absolutePath
     * @param fileName
     */
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
        acquireSemaphore();
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
        releaseSemaphore();
    }

    @Override
    public void onDeleteFile(String path, CoUser user) throws IOException {
        // TODO 对项目级别锁的获取可以考虑放在inter @周泓光
        acquireSemaphore();
        super.onDeleteFile(path, user);
        releaseSemaphore();
    }

    public void onLocalFileOpen(FileEditorManager source, VirtualFile file) {
        log.info("local file open {0}", file.getPath());
        if (GeneralFileIgnoreUtil.isIgnored(file.getName())) {
            log.info("local file opened and ignored {0}", file.getPath());
            return;
        }
        LocalFilePresenter localFilePresenter = new LocalFilePresenter(project, file, otClient);
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
    public void onCreateDir(String path, CoUser user) throws IOException {
        MyRepositoryListener.pauseListening();
        super.onCreateDir(path, user);
        MyRepositoryListener.resumeListening();
    }

    @Override
    public void onCreateFile(String path, String content, CoUser user) throws IOException {
        MyRepositoryListener.pauseListening();
        super.onCreateFile(path, content, user);
        MyRepositoryListener.resumeListening();
    }

    @Override
    public void onDeleteDir(String path, CoUser user) throws IOException {
        MyRepositoryListener.pauseListening();
        super.onDeleteDir(path, user);
        MyRepositoryListener.resumeListening();
    }

    private boolean connected() {
        return otClient.isConnected();
    }

}
