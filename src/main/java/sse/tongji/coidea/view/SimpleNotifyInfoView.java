package sse.tongji.coidea.view;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import dev.mtage.eyjaot.client.inter.view.GeneralUIStringConfig;
import dev.mtage.eyjaot.client.inter.view.IBasicCollaborationInfoView;
import dev.mtage.eyjaot.client.inter.view.INotificationView;
import sse.tongji.coidea.config.CoIDEAUIString;

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
        Notification notification = errorGroup.createNotification(errMsg, CoIDEAUIString.GENERAL_TITLE,
                NotificationType.ERROR, null);
        notification.notify(localProject);
    }

    @Override
    public void displayConnSuccess() {
        Notification notification = errorGroup.createNotification(GeneralUIStringConfig.CONNECTED, CoIDEAUIString.GENERAL_TITLE,
                NotificationType.ERROR, null);
        notification.notify(localProject);
    }

    @Override
    public void displayConnBroken(String msg) {
        Notification notification = errorGroup.createNotification(msg, CoIDEAUIString.GENERAL_TITLE,
                NotificationType.WARNING, null);
        notification.notify(localProject);
    }

    @Override
    public void sysNotify(String msg) {
        Notification notification = errorGroup.createNotification(msg, CoIDEAUIString.GENERAL_TITLE,
                NotificationType.INFORMATION, null);
        notification.notify(localProject);
    }
}
