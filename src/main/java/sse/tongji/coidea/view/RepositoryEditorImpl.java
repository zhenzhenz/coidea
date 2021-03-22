package sse.tongji.coidea.view;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import dev.mtage.eyjaot.client.inter.view.GeneralRepositoryEditorImpl;
import sse.tongji.coidea.listener.MyRepositoryListener;

import java.io.IOException;

/**
 * @author mtage
 * @since 2021/3/18 14:52
 */
public class RepositoryEditorImpl extends GeneralRepositoryEditorImpl {
    private final Project project;

    public RepositoryEditorImpl(Project project) {
        this.project = project;
    }

    @Override
    public String getDefaultProjectPath() {
        return project.getBasePath();
    }

    @Override
    public String getDefaultProjectName() {
        return project.getName();
    }

    @Override
    public void syncDataToDefault(byte[] repoData) throws IOException {
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
