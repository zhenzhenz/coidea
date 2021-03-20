package sse.tongji.coidea.view;

import com.intellij.openapi.project.Project;
import dev.mtage.eyjaot.client.inter.view.GeneralRepositoryEditorImpl;

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
}
