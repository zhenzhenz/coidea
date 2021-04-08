package sse.tongji.coidea.view.formUI;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class NativeCollaborationView extends SimpleToolWindowPanel {

    private MainContentInterface mainContent;
    private TeamPanel teamContent = new TeamPanel();

    public NativeCollaborationView() {
        super(true, true);
        setToolbar(createToolbarPanel());
        directToLoginPage();
    }

    private JPanel createToolbarPanel() {
        final DefaultActionGroup group = new DefaultActionGroup();
        group.add(new UserAction());
        group.add(new TeamAction());
        group.add(new ConnectAction());
        group.add(new DisConnectAction());

        final ActionToolbar actionToolBar = ActionManager.getInstance().createActionToolbar("co idea tool bar", group, true);
        return JBUI.Panels.simplePanel(actionToolBar.getComponent());
    }

    private final class UserAction extends AnAction {
        public UserAction() {
            super("User information", "Show user information", AllIcons.General.User);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            setContent(mainContent.getContent());
        }
    }

    private final class TeamAction extends AnAction {
        public TeamAction() {
            super("Team information", "Show team information", AllIcons.Debugger.Db_array);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            setContent(teamContent.getContent());
        }
    }

    private final class ConnectAction extends AnAction {
        public ConnectAction() {
            super("Connect", "Connect to a project", AllIcons.General.Add);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            // <html>No team in your account, please create a new team in team page.</html>
            // todo: 弹出连接窗
            if (new ConnectionDialogWrapper().showAndGet()) {
                // user pressed OK
                ((CollaborationPanel)mainContent).connectSuccess(true); // 显示连接信息
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            // todo: 设置按钮enable/disable
            e.getPresentation().setEnabled(true);
        }
    }

    private final class DisConnectAction extends AnAction {
        public DisConnectAction() {
            super("Disconnect", "Disconnect to a project", AllIcons.Actions.Cancel);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            // todo: 断开连接
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            // todo: 设置按钮enable/disable
            e.getPresentation().setEnabled(false);
        }
    }

    private void setMainContent(MainContentInterface mainContent) {
        this.mainContent = mainContent;
        setContent(mainContent.getContent());
    }

    private void directToLoginPage(){
        LoginPanel login = new LoginPanel();
        login.loginButton.addActionListener(e -> {
            // true
            directToCollaborationPage();
            // todo: false -> error dialog
        });
        login.signupButton.addActionListener(e -> {
            // true
            directToCollaborationPage();
            // todo: false -> error dialog
        });
        setMainContent(login);
    }

    private void directToCollaborationPage(){
        CollaborationPanel newCp = new CollaborationPanel();
        newCp.logoutButton.addActionListener(e -> {
            directToLoginPage();
        });
        setMainContent(newCp);
    }
}
