package sse.tongji.coidea.presenter;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import dev.mtage.eyjaot.client.OtClient;
import dev.mtage.eyjaot.client.inter.ILocalRepositoryEditor;
import dev.mtage.eyjaot.client.inter.view.*;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import lombok.extern.slf4j.Slf4j;
import sse.tongji.coidea.config.CoIDEAUIString;
import sse.tongji.coidea.view.ConnConfigurationDialog;
import sse.tongji.coidea.view.IMessageView;
import sse.tongji.coidea.view.SimpleMessageView;
import sse.tongji.coidea.view.SimpleNotifyInfoView;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;

/**
 * @author mtage
 * @since 2021/3/17 19:15
 */
public class LocalRepositoryPresenter implements ILocalRepositoryEditor {
    private OtClient otClient;
    private IBasicCollaborationInfoView collaborationInfoView;
    private IConnConfigureView connConfigureView;
    private IRepositoryEditor repositoryView;
    private INotificationView notificationView;
    private IMessageView messageView = new SimpleMessageView();

    private Project project;

    private ILocalRepositoryEditor repositoryEditor;

    private ConnConfigurationInput connConfig;

    public LocalRepositoryPresenter() {

    }

    public void onConnectClicked(AnActionEvent e) {
        this.project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null) {
            // TODO 理论上来说，这一点也是UI部分应该移动到view
            messageView.messageWithDefaultTitle("You have to open a project before using CoIDEA");
            return;
        }
        if (Objects.nonNull(otClient) && otClient.isConnected()) {
            notificationView.sysNotify("You have connected to" + connConfig.getRepoId());
            return;
        }
        initSimpleNotifyView(this.project);
        this.connConfig = this.connConfigureView.readConfigurationInput();
        if (connConfig.isNewRepo()) {

        }
    }


    private void initSimpleNotifyView(Project localProject) {
        SimpleNotifyInfoView simpleNotifyInfoView = new SimpleNotifyInfoView(localProject);
        this.collaborationInfoView = simpleNotifyInfoView;
        this.notificationView = simpleNotifyInfoView;
        this.connConfigureView = new ConnConfigurationDialog(localProject);
    }

    @Override
    public void onInitRepo(byte[] repoData, Collection<CoUser> users, DalPolicySettings dalPolicySettings) {

    }

    @Override
    public void onCreateDir(String path, CoUser user) {

    }

    @Override
    public void onCreateFile(String path, String content, CoUser user) {

    }

    @Override
    public void onDeleteDir(String path, CoUser user) {

    }

    @Override
    public void onDeleteFile(String path, CoUser user) {

    }

    @Override
    public void onUserJoined(CoUser user) {
        notificationView.sysNotify(user.getUserName() + " have joined");
    }

    @Override
    public void onUserLeft(CoUser user, Date leaveTime) {
        notificationView.sysNotify(user.getUserName() + " have left");
    }

    @Override
    public void onConnClose(int code, String reason) {
        collaborationInfoView.displayConnBroken("You have left from " + connConfig.getRepoId() + " msg:" + reason);
    }

    @Override
    public void onError(String code, String message) {
        collaborationInfoView.displayConnErr(message);
    }
}
