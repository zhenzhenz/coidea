package sse.tongji.coidea.view;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import dev.mtage.eyjaot.client.inter.view.GeneralUIStringConfig;
import dev.mtage.eyjaot.client.inter.view.IBasicCollaborationInfoView;
import dev.mtage.eyjaot.client.inter.view.INotificationView;
import dev.mtage.eyjaot.core.CoUser;
import sse.tongji.coidea.config.CoIDEAUIString;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 简单地推送通知
 * @author mtage
 * @since 2021/3/17 20:44
 */
public class SimpleNotifyInfoView implements INotificationView, IBasicCollaborationInfoView {
    private Project localProject;
    private final NotificationGroup errorGroup = NotificationGroup.balloonGroup("CoIDEA Error");
    private final NotificationGroup infoGroup = NotificationGroup.balloonGroup("CoIDEA Info");
    private String userName;
    private String repoId;

    public SimpleNotifyInfoView(Project localProject) {
        this.localProject = localProject;
    }


    @Override
    public void displayUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void displayRepoId(String repoId) {
        this.repoId = repoId;
    }

    @Override
    public void displayConnErr(String errMsg) {
        Notification notification = errorGroup.createNotification(CoIDEAUIString.GENERAL_TITLE, errMsg,
                NotificationType.ERROR, null);
        notification.notify(localProject);
    }

    @Override
    public void displayConnSuccess() {
        Notification notification = infoGroup.createNotification(CoIDEAUIString.GENERAL_TITLE, GeneralUIStringConfig.CONNECTED,
                NotificationType.INFORMATION, null);
        notification.notify(localProject);
    }

    @Override
    public void displayCollaborators(Collection<CoUser> coUsers) {
        sysNotify("Collaborators: " + coUsers.stream().map(CoUser::getUserName).collect(Collectors.joining(", ")));
    }

    @Override
    public void displayConnBroken(String msg) {
        Notification notification = errorGroup.createNotification(CoIDEAUIString.GENERAL_TITLE, msg,
                NotificationType.WARNING, null);
        notification.notify(localProject);
    }

    @Override
    public void sysNotify(String msg) {
        Notification notification = infoGroup.createNotification(CoIDEAUIString.GENERAL_TITLE, msg,
                NotificationType.INFORMATION, null);
        notification.notify(localProject);
    }
}
