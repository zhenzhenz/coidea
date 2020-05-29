package client;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class NotificationClient {
    public static Project project;
    public static NotificationGroup notificationGroup = new NotificationGroup("NotificationClient.Notify", NotificationDisplayType.BALLOON,false);
    public static NotificationGroup balloonGroup = new NotificationGroup("NotificationClient.Balloon",NotificationDisplayType.STICKY_BALLOON,false);

    public static void notify(String title, String content){
        Notification notification = new Notification("NotificationClient.Notify",title,content, NotificationType.INFORMATION);
        notification.notify(project);
    }
    public static void popup(String content){
        BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(content, MessageType.INFO,null);
        Balloon balloon = balloonBuilder.setFadeoutTime(TimeUnit.SECONDS.toMillis(1)).createBalloon();
        Balloon.Position pos = Balloon.Position.above;
        balloon.show(RelativePoint.fromScreen(new Point(0,0)),pos);

    }

}
