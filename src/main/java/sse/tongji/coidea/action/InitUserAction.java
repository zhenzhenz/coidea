package sse.tongji.coidea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.config.CoIDEAUIString;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

/**
 * @author mtage
 * @since 2021/3/18 10:00
 */
public class InitUserAction extends AnAction {
    private LocalRepositoryPresenter localRepositoryPresenter;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        this.localRepositoryPresenter = LocalRepositoryPresenter.fromProject(project);
        this.localRepositoryPresenter.onConnectClicked(e);
    }
}