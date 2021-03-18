package sse.tongji.coidea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.config.CoIDEAUIString;

/**
 * @author mtage
 * @since 2021/3/18 10:00
 */
@Slf4j
public class InitUserAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);




    }
}