package client;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import config.ApiConfig;
import config.FileConfig;
import dal.DALPolicySettingData;
import dev.mtage.eyjaot.client.OtClient;
import dev.mtage.eyjaot.client.inter.ILocalFileEditor;
import dev.mtage.eyjaot.client.inter.ILocalRepositoryEditor;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import entity.Repository;
import listeners.MyVirtualFileListener;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import util.DateUtil;
import util.FilePathUtil;
import util.MyLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

/**
 * portal 核心逻辑业务类
 * 唉 架构设计什么的真是一团糟
 * @author mtage
 * @since 2020/1/1 20:14
 */
public class CollaborationService {
    private Repository repository;
    private Project project;
    private ZipService zipService;

    private static MyLogger log = MyLogger.getLogger(CollaborationService.class);
    private static CollaborationService serviceInstance;
    private List<String> notificationQueue = Lists.newArrayList();
    private final Semaphore documentSemaphore = new Semaphore(1);
    private static CoServiceStatusEnum serviceStatus = CoServiceStatusEnum.UNCREATED;
    private List<String> userNames = Lists.newArrayList();

    private OtClient otClient;

    private class LocalRepositoryEditor implements ILocalRepositoryEditor {

        @Override
        public void onInitRepo(byte[] bytes, Collection<CoUser> users, DalPolicySettings dalPolicySettings) {
            log.info("接收到来自服务器的项目文件");
            MyVirtualFileListener.isResourceListening.set(false);
            if (CollectionUtils.isNotEmpty(users)) {
                log.info("项目成员数:{0} 成员: {1}", users.size(),
                        users.stream().map(CoUser::getUserName).collect(Collectors.toList()));
            }
            zipService.unzip(bytes);
            log.info("解压缩完成");
            LocalFileSystem.getInstance().refreshWithoutFileWatcher(false);
//            Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
//                    VirtualFileManager.getInstance().asyncRefresh(
//                            new Runnable(){
//                                @Override
//                                public void run() {
//                                    MyVirtualFileListener.isResourceListening.set(true);
//                                }
//                            }
//                    );
//                }
//            };
//            WriteCommandAction.runWriteCommandAction(project, runnable);

            MyVirtualFileListener.isResourceListening.set(true);
            displayConnSuccessInfo();
            displayMembers();
            displayPortalId();

            log.info("设置dal相关 " + dalPolicySettings);
            DALPolicySettingData settingData = new DALPolicySettingData();
            settingData.isLockingFields = dalPolicySettings.isLockingFields();
            settingData.isLockingMethods = dalPolicySettings.isLockingMethods();
            settingData.isTimeoutRelease = dalPolicySettings.isTimeoutRelease();
            settingData.deepOfLocking = dalPolicySettings.getDeepOfLocking();
            settingData.timeoutsecond = dalPolicySettings.getTimeoutSecond();
            repository.setDal(dalPolicySettings.isDalOpen());
            repository.setDalPolicySettingData(settingData);

            serviceStatus = CoServiceStatusEnum.INITED;
        }

        @Override
        public void onCreateDir(String path, CoUser user) {
            remoteCreateFile(path, true, user.getUserName(), null);
        }

        @Override
        public void onCreateFile(String path, String content, CoUser user) {
            remoteCreateFile(path, false, user.getUserName(), content);
        }

        @Override
        public void onDeleteDir(String path, CoUser user) {
            remoteDeleteFile(path, user.getUserName());
        }

        @Override
        public void onDeleteFile(String path, CoUser user) {
            remoteDeleteFile(path, user.getUserName());
        }

        @Override
        public void onUserJoined(CoUser user) {
            userJoined(user);
        }

        @Override
        public void onUserLeft(CoUser user, Date leaveTime) {
            userLeft(user);
        }

        @Override
        public void onConnClose(int code, String reason) {
            displayConnFailInfo("The connection has broken. " + reason + " " + code);
        }

        @Override
        public void onError(String code, String message) {
            displayErrorInfo("ERROR " + message + " [" + code + "]");
        }
    }

    private CollaborationService(Project project, Repository repository) {
        this.repository = repository;
        this.project = project;
        zipService = new ZipService(project);
        log.info("初始化与服务器的连接...");
        otClient = OtClient.createWsClient(ApiConfig.WS_SERVER_PREFIX + repository.getServerAddr()).connect();
        serviceStatus = CoServiceStatusEnum.CONNECTED;
        if (repository.isInitNew()) {
            File zipFile = zipService.zipAll();
            try {
                log.info("new repo" + Arrays.toString(Files.readAllBytes(zipFile.toPath())));
                DALPolicySettingData dalPolicySettingData = repository.getDalPolicySettingData();
                DalPolicySettings dalPolicySettings = DalPolicySettings.builder()
                        .isDalOpen(repository.isDal())
                        .deepOfLocking(dalPolicySettingData.deepOfLocking)
                        .isLockingFields(dalPolicySettingData.isLockingFields)
                        .isLockingMethods(dalPolicySettingData.isLockingMethods)
                        .isTimeoutRelease(dalPolicySettingData.isTimeoutRelease)
                        .timeoutSecond(dalPolicySettingData.timeoutsecond)
                        .build();

                otClient.newRepo(repository.getRepositoryId(), repository.getUserId(),
                        Files.readAllBytes(zipFile.toPath()), dalPolicySettings, new LocalRepositoryEditor());
                serviceStatus = CoServiceStatusEnum.INITED;
            } catch (IOException e) {
                log.error("read zip file exception", e);
            }
            displayConnSuccessInfo();
        } else {
            otClient.joinRepo(repository.getRepositoryId(), repository.getUserId(), new LocalRepositoryEditor());
        }
    }

    private CollaborationService() {

    }

    public static CollaborationService getInstance() {
        if (serviceInstance == null) {
            log.error("PortalService called before init");
            return new CollaborationService();
        }
        return serviceInstance;
    }

    public static void createInstance(Project project, Repository repository) {
        if (Objects.nonNull(serviceInstance)) {
            serviceInstance.closeInstance();
        }
        serviceInstance = new CollaborationService(project, repository);
    }

    public static CoServiceStatusEnum getStatus() {
        return serviceStatus;
    }

    public void closeInstance() {
        if (Objects.isNull(serviceInstance)) {
            return;
        }
        log.info("即将销毁原portalService");
//        apiService.close();
        otClient.leaveRepo();
        log.info("Disconnect from server " + repository.getServerAddr());
        serviceStatus = CoServiceStatusEnum.UNCREATED;
        serviceInstance = null;
    }


    /**
     * 响应打开文件
     *
     * @param docEditor
     */
    public void openFile(ILocalFileEditor docEditor) {
        if (Objects.nonNull(docEditor)) {
            otClient.openFile(docEditor.getPath(), docEditor.getName(), docEditor);
            log.info("项目{0} 打开文件{1}", project.getName(), docEditor.getPath());
        }
    }

    /**
     * 响应关闭文件
     * @param docEditor
     */
    public void closeFile(ILocalFileEditor docEditor) {
        if (Objects.nonNull(docEditor) && otClient.isOpened(docEditor.getPath())) {
            otClient.closeFile(docEditor.getPath(), docEditor.getName());
            log.info("项目{0} 关闭文件{1}", project.getName(), docEditor.getPath());
        }
    }

    /**
     * 响应远端创建文件
     */
    public void remoteCreateFile(String filePath, boolean isFolder, String userName, String content) {
        log.info("User {0} Create File {1} Create isFolder: {2}", userName, filePath, isFolder);
        File file = new File(getFullPath(filePath));
        if (file.exists()) {
            log.info("File {0} exists, skipped", file.getAbsolutePath());
        }
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            log.info("  try create dirs " + file.getAbsolutePath());
            parentFile.mkdirs();
        }
        try {
            if (isFolder) {
                log.info("  try create dirs " + file.getAbsolutePath());
                file.mkdirs();
            } else {
                log.info("  try create file " + file.getAbsolutePath());
                file.createNewFile();
            }
            if (!isFolder && StringUtils.isNotBlank(content)) {
                Files.write(file.toPath(), content.getBytes());
            }
        } catch (IOException e) {
            log.error("IOException when create remote file {0} msg: {1}", filePath, e.getMessage());
        }
        forceUpdateResource();
        displayNotification(String.format("User %s created file %s", userName, FilenameUtils.separatorsToSystem(filePath)));
    }

    /**
     * 响应删除文件
     * @param filePath
     */
    public void remoteDeleteFile(String filePath, String userName) {
        log.info("User {0} Delete File {1}", userName, filePath);
        File file = new File(getFullPath(filePath));
        log.info("  try to delete file " + file.getAbsolutePath());
        if (file.exists()) {
            file.delete();
        }
        forceUpdateResource();
        displayNotification(String.format("User %s deleted file %s", userName, FilenameUtils.separatorsToSystem(filePath)));
    }

    public void userJoined(CoUser user) {
        userNames.add(user.getUserName());
        displayNotification("User '" + user.getUserName() + " Joined");
        displayMembers();
    }

    public void userLeft(CoUser user) {
        int pos = -1;
        for (int i = 0; i < userNames.size(); ++i) {
            if(userNames.get(i) == user.getUserName()) {
                pos = i;
                break;
            }
        }
        if (pos != -1) {
            userNames.remove(pos);
        }
        displayNotification("User '" + user.getUserName() + " Left");
        log.info("当前用户数量 " + otClient.getAllUser().size());
        displayMembers();
    }

    /**
     * 响应本地创建文件
     */
    public void localCreateFile(File localFile) {
        if (FileConfig.isIgnored(localFile.getName())) {
            return;
        }
        try {
            byte[] bytes = Files.readAllBytes(localFile.toPath());
            otClient.createFile(getRelativePath(localFile.getAbsolutePath()), localFile.getName(), new String(bytes));
        } catch (IOException e) {
            log.error("read new file content exception", e);
        }
        log.info("send create file request " + localFile.getPath());
    }

    /**
     * 响应本地删除文件
     */
    public void localDeleteFile(File file) {
        if (FileConfig.isIgnored(file.getName())) {
            return;
        }
        otClient.deleteFile(getRelativePath(file.getAbsolutePath()), file.getName(), file.isDirectory());
        log.info("send delete file request");
    }

    /**
     * 响应刷新文件
     */
    public void localRefreshDocs() {
        log.info("开始尝试刷新所有打开的文件");
//        JsEngine.getInstance().invoke("rewriteAll");
    }

    public void localDelete(String path, int lower, int upper) {
        if (StringUtils.isNotBlank(path)) {
            otClient.getOpenedFile(FilePathUtil.getProjectRelativePath(path)).localDelete(lower, upper);
        }
    }

    public void localInsert(String path, int position, String content) {
        if (StringUtils.isNotBlank(path)) {
            otClient.getOpenedFile(FilePathUtil.getProjectRelativePath(path)).localInsert(position, content);
        }
    }

    public void localCaretMove(String path, int offset, String username) {
        if (StringUtils.isNotBlank(path)) {
            System.out.println("path:" + path);
            System.out.println("offset: " + offset);
            otClient.getOpenedFile(FilePathUtil.getProjectRelativePath(path)).localCaretMove(offset);
            log.info("localCaretMove: " + path + " " + offset);
        }
    }

    public void displayNotification(String text) {
        log.info("[SYSTEM] " + DateUtil.getCurrentSimpleTime() + text);
        NotificationClient.notify("Hi ! ", text);

    }

    public void displayConnSuccessInfo() {
        serviceStatus = CoServiceStatusEnum.CONNECTED;
        log.info("Your name: " + repository.getUserId());
        NotificationClient.notify("CoIdea Init Successed!", "Username: " + repository.getUserId() + "\nReposID: " + repository.getRepositoryId());
    }

    public void displayErrorInfo(String text) {
        log.info("Error Occurs");
        log.info(text);
        NotificationClient.notify("Error Occurs", text);

    }

    public void displayConnFailInfo(String text) {
        log.info("Connect Error\n" + text);
        serviceStatus = CoServiceStatusEnum.CREATED;
    }

    public void displayPortalId() {
        log.info(repository.getRepositoryId());
    }

    public void displayUserName() {
        log.info(repository.getUserId());
    }

    public void displayMembers() {
        otClient.getAllUser().forEach(coUser -> {
            log.info(coUser.getUserName());
        });
    }

    /**
     * 获取文档编辑的信号量
     * @return
     */
    public Semaphore getDocumentSemaphore() {
        return documentSemaphore;
    }

    public boolean isDalOpen() {
        return this.repository.isDal();
    }

    public DALPolicySettingData getDALPolicySettingData() {
        return this.repository.getDalPolicySettingData();
    }

    public List<String> UsernameList() {
        return this.userNames;
    }

    private String getRelativePath(String fullPath) {
        return FilenameUtils.separatorsToUnix(fullPath.substring(project.getBasePath().length() - project.getName().length()));
    }

    private String getFullPath(String relativePath) {
        relativePath = FilenameUtils.separatorsToSystem(relativePath);
//        log.info(FilenameUtils.separatorsToSystem("/" + relativePath));
//        log.info(project.getBasePath() + FilenameUtils.separatorsToSystem("/" + relativePath));
        return FilenameUtils.separatorsToSystem(FileSystems.getDefault().getPath(project.getBasePath()).getParent().resolve(relativePath).toString());
    }

    private void forceUpdateResource() {
//        MyVirtualFileListener.isResourceListening.set(false);
//        try {
//            ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
//        } catch (CoreException e) {
//            log.error("CoreException ", e);
//        }
//        MyVirtualFileListener.isResourceListening.set(true);
    }

}
