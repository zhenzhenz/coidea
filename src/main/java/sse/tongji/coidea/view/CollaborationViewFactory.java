package sse.tongji.coidea.view;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;
import sse.tongji.coidea.view.formUI.NativeCollaborationView;

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
//        CollaborationPanel collaborationPanel = new CollaborationPanel(toolWindow, simpleNotifyInfoView);
//        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
//        Content content = contentFactory.createContent(collaborationPanel.getContent(), "", false);
//        toolWindow.getContentManager().addContent(content);
//        collaborationPanel.setLocalRepositoryPresenter(localRepositoryPresenter);

        NativeCollaborationView explorer = new NativeCollaborationView(simpleNotifyInfoView);
        localRepositoryPresenter = LocalRepositoryPresenter.fromProject(explorer, explorer, project);
        explorer.setLocalRepositoryPresenter(localRepositoryPresenter);
        ContentManager contentManager = toolWindow.getContentManager();
        Content content = contentManager.getFactory().createContent(explorer, null, false);
        contentManager.addContent(content);
    }

}