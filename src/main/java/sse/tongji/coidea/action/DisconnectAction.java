package sse.tongji.coidea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

/**
 * @author mtage
 * @since 2021/3/20 17:07
 */
public class DisconnectAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        // TODO: project might be null
        LocalRepositoryPresenter.fromProject(project).onDisconnectClicked();
    }
}
