package sse.tongji.coidea.view.formUI;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.JBUI;
import dev.mtage.eyjaot.client.inter.view.IBasicCollaborationInfoView;
import dev.mtage.eyjaot.client.inter.view.INotificationView;
import dev.mtage.eyjaot.core.CoUser;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;
import sse.tongji.coidea.view.SimpleNotifyInfoView;

import javax.swing.*;
import java.util.Collection;

public class NativeCollaborationView extends SimpleToolWindowPanel implements INotificationView, IBasicCollaborationInfoView {
    @Setter
    private LocalRepositoryPresenter localRepositoryPresenter;

    private SimpleNotifyInfoView simpleNotifyInfoView;

    private MainContentInterface mainContent;
    private CollaborationPanel collaborationPanel;
    private TeamPanel teamContent = new TeamPanel();

    private boolean connectEnable = true;

    public NativeCollaborationView(SimpleNotifyInfoView simpleNotifyInfoView) {
        super(true, true);
        setToolbar(createToolbarPanel());
//        directToLoginPage();
        directToCollaborationPage();
        this.simpleNotifyInfoView = simpleNotifyInfoView;
    }

    @Override
    public void displayUserName(String userName) {
        updateUISynchronously(() -> {
            this.collaborationPanel.userNameField.setText(userName);
        });
    }

    @Override
    public void displayRepoId(String repoId) {
        updateUISynchronously(() -> {
            this.collaborationPanel.repoIdTextField.setText(repoId);
        });
    }

    @Override
    public void displayConnErr(String errMsg) {
        updateUISynchronously(() -> {
//            sysNotify("Error occurs: " + errMsg);
            simpleNotifyInfoView.displayConnErr(errMsg);
        });
    }

    @Override
    public void displayConnSuccess() {
        updateUISynchronously(() -> {
//            connectServerButton.setText(CoIDEAUIString.DISCONNECT);
            this.connectEnable = false;
            simpleNotifyInfoView.displayConnSuccess();
        });
    }

    @Override
    public void displayConnBroken(String msg) {
        updateUISynchronously(() -> {
//            connectServerButton.setText(CoIDEAUIString.CONNECT);
            this.connectEnable = true;
            simpleNotifyInfoView.displayConnBroken(msg);
        });
    }

    @Override
    public void displayCollaborators(Collection<CoUser> coUsers) {
        updateUISynchronously(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) this.collaborationPanel.collaboratorList.getModel();
            model.clear();
            coUsers.forEach(u -> model.addElement(u.getUserName()));
        });
    }

    @Override
    public void removeDisplayCollaborator(CoUser coUser) {
        updateUISynchronously(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) this.collaborationPanel.collaboratorList.getModel();
            model.removeElement(coUser.getUserName());
        });
    }

    @Override
    public void addDisplayCollaborator(CoUser coUser) {
        updateUISynchronously(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) this.collaborationPanel.collaboratorList.getModel();
            model.addElement(coUser.getUserName());
        });
    }

    @Override
    public void sysNotify(String msg) {
        simpleNotifyInfoView.sysNotify(msg);
    }

    private synchronized void updateUISynchronously(Runnable runnable) {
        runnable.run();
    }

    private JPanel createToolbarPanel() {
        final DefaultActionGroup group = new DefaultActionGroup();
//        group.add(new UserAction());
//        group.add(new TeamAction());
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
            localRepositoryPresenter.onConnectDisconnectClicked(e);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            // todo: 设置按钮enable/disable
            e.getPresentation().setEnabled(connectEnable);
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
            e.getPresentation().setEnabled(!connectEnable);
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
        this.collaborationPanel = new CollaborationPanel();
//        newCp.logoutButton.addActionListener(e -> {
//            directToLoginPage();
//        });
        setMainContent(this.collaborationPanel);
    }
}
