package sse.tongji.coidea.view;

import com.intellij.openapi.project.Project;
import dev.mtage.eyjaot.client.inter.util.FilePathUtil;
import dev.mtage.eyjaot.client.inter.util.ZipUtil;
import dev.mtage.eyjaot.client.inter.view.IRepositoryEditor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author mtage
 * @since 2021/3/18 14:52
 */
public class RepositoryEditorImpl implements IRepositoryEditor {
    private Project project;

    @Override
    public String getDefaultProjectPath() {
        return project.getBasePath();
    }

    @Override
    public String getDefaultProjectName() {
        return project.getName();
    }

    @Override
    public byte[] readDefaultProjectAllData() throws IOException {
        return Files.readAllBytes(ZipUtil.zipAll(getDefaultProjectPath(), getDefaultProjectName()).toPath());
    }

    @Override
    public void syncDataToDefault(byte[] repoData) throws IOException {
        ZipUtil.unzip(repoData, getDefaultProjectPath());
    }

    @Override
    public void syncDirCreation(String dirPath) throws IOException {

    }

    @Override
    public void syncFileCreation(String filePath, String initialContent) throws IOException {

    }

    @Override
    public void syncDirDeletion(String dirPath) throws IOException {

    }

    @Override
    public void syncFileDeletion(String filePath) throws IOException {

    }

    private Path getFileAbsolutePath(String relativePath) {
        return Paths.get(FilePathUtil.getStandardAbsolutePath(relativePath, getDefaultProjectPath()));
    }
}
