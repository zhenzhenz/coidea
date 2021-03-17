package sse.tongji.coidea.view;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.project.Project;
import dev.mtage.eyjaot.client.inter.view.IBasicCollaborationInfoView;
import dev.mtage.eyjaot.client.inter.view.INotificationView;

/**
 * 简单地推送通知
 * @author mtage
 * @since 2021/3/17 20:44
 */
public class SimpleNotifyInfoView implements INotificationView, IBasicCollaborationInfoView {
    private Project project;
    private final NotificationGroup errorGroup = NotificationGroup.balloonGroup("CoIDEA Error");
    private final NotificationGroup infoGroup = NotificationGroup.balloonGroup("CoIDEA Info");

    public SimpleNotifyInfoView(Project project) {
        this.project = project;
    }


    @Override
    public void displayUserName(String userName) {

    }

    @Override
    public void displayRepoId(String repoId) {

    }

    @Override
    public void displayConnErr(String errMsg) {

    }

    @Override
    public void displayConnSuccess() {

    }

    @Override
    public void displayConnBroken(String msg) {

    }

    @Override
    public void sysNotify(String msg) {

    }
}
