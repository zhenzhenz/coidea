package sse.tongji.coidea.view;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import dev.mtage.eyjaot.client.inter.util.GeneralFileIgnoreUtil;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import dev.mtage.eyjaot.client.inter.view.GeneralRepositoryEditorImpl;
import org.apache.commons.io.FileUtils;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author mtage
 * @since 2021/3/18 14:52
 */

public class RepositoryEditorImpl extends GeneralRepositoryEditorImpl {
    private final MyLogger log = MyLogger.getLogger(LocalRepositoryPresenter.class);
    private final Project project;

    public RepositoryEditorImpl(Project project) {
        this.project = project;
    }

    @Override
    public Path getProjectPath() {
        return Optional.ofNullable(project.getBasePath())
                .map(Path::of)
                .orElse(null);
    }

    @Override
    public String getProjectName() {
        return project.getName();
    }

    @Override
    public void syncDataToDefault(byte[] repoData) throws IOException {
        for (Path eachFile : Files.list(getProjectPath()).collect(Collectors.toList())) {
            if (GeneralFileIgnoreUtil.isPathIgnored(eachFile)) {
                continue;
            }
            log.info("删除本地文件 {0}", eachFile.toString());
            FileUtils.forceDelete(eachFile.toFile());
        }
        super.syncDataToDefault(repoData);
    }

    @Override
    public void syncDirCreation(String dirPath) throws IOException {
        super.syncDirCreation(dirPath);
        updateFileSystem();
    }

    @Override
    public void syncFileCreation(String filePath, String initialContent) throws IOException {
        super.syncFileCreation(filePath, initialContent);
        updateFileSystem();
    }

    @Override
    public void syncDirDeletion(String dirPath) throws IOException {
        super.syncDirDeletion(dirPath);
        updateFileSystem();
    }

    @Override
    public void syncFileDeletion(String filePath) throws IOException {
        super.syncFileDeletion(filePath);
        updateFileSystem();
    }

    private void updateFileSystem() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                LocalFileSystem.getInstance().refreshWithoutFileWatcher(false);
            });
        });
    }
}
