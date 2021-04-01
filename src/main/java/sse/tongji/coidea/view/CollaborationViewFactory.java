package sse.tongji.coidea.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

public class CollaborationViewFactory implements ToolWindowFactory {
    private LocalRepositoryPresenter localRepositoryPresenter;

    /**
     * Create the tool window content.
     *
     * @param project    current project
     * @param toolWindow current tool window
     */
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SimpleNotifyInfoView simpleNotifyInfoView = new SimpleNotifyInfoView(project);
        CollaborationPanel collaborationPanel = new CollaborationPanel(toolWindow, simpleNotifyInfoView);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(collaborationPanel.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
        localRepositoryPresenter = LocalRepositoryPresenter.fromProject(collaborationPanel, collaborationPanel, project);
        collaborationPanel.setLocalRepositoryPresenter(localRepositoryPresenter);
    }

}