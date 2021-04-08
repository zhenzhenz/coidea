package sse.tongji.coidea.view;

import com.intellij.openapi.wm.ToolWindow;
import dev.mtage.eyjaot.client.inter.util.DateUtil;
import dev.mtage.eyjaot.client.inter.view.IBasicCollaborationInfoView;
import dev.mtage.eyjaot.client.inter.view.INotificationView;
import dev.mtage.eyjaot.core.CoUser;
import lombok.Setter;
import sse.tongji.coidea.config.CoIDEAUIString;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

import javax.swing.*;
import java.util.Collection;

@SuppressWarnings("jol")
public class CollaborationPanel implements INotificationView, IBasicCollaborationInfoView {
    @Setter
    private LocalRepositoryPresenter localRepositoryPresenter;

    private SimpleNotifyInfoView simpleNotifyInfoView;

    private JButton hideToolWindowButton;
    private JPanel myToolWindowContent;
    private JPanel controlBlock;
    private JButton connectServerButton;
    private JButton refreshInformationButton;
    private JPanel collaborationInformationBlock;
    private JPanel userNameBlock;
    private JList<String> collaboratorList;
    private JPanel collaboratorBlock;
    private JList<String> notificationList;
    private JPanel notificationBlock;
    private JPanel repositoryBlock;
    private JLabel userNameLabel;
    private JLabel repositoryIdLabel;
    private JLabel helpLabel;
    private JPanel logoBlock;

    public CollaborationPanel(){}

    public CollaborationPanel(ToolWindow toolWindow, SimpleNotifyInfoView simpleNotifyInfoView) {
        this.simpleNotifyInfoView = simpleNotifyInfoView;
        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
        connectServerButton.addActionListener(e -> {
//            localRepositoryPresenter.onConnectDisconnectClicked(e);
        });
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    @Override
    public void displayUserName(String userName) {
        updateUISynchronously(() -> {
            this.userNameLabel.setText(userName);
        });
    }

    @Override
    public void displayRepoId(String repoId) {
        updateUISynchronously(() -> {
            this.repositoryIdLabel.setText(repoId);
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
            connectServerButton.setText(CoIDEAUIString.DISCONNECT);
            simpleNotifyInfoView.displayConnSuccess();
        });
    }

    @Override
    public void displayConnBroken(String msg) {
        updateUISynchronously(() -> {
            connectServerButton.setText(CoIDEAUIString.CONNECT);
            simpleNotifyInfoView.displayConnBroken(msg);
        });
    }

    @Override
    public void displayCollaborators(Collection<CoUser> coUsers) {
        updateUISynchronously(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) this.collaboratorList.getModel();
            model.clear();
            coUsers.forEach(u -> model.addElement(u.getUserName()));
        });
    }

    @Override
    public void removeDisplayCollaborator(CoUser coUser) {
        updateUISynchronously(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) this.collaboratorList.getModel();
            model.removeElement(coUser.getUserName());
        });
    }

    @Override
    public void addDisplayCollaborator(CoUser coUser) {
        updateUISynchronously(() -> {
            DefaultListModel<String> model = (DefaultListModel<String>) this.collaboratorList.getModel();
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
}