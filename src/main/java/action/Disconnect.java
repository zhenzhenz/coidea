package action;

import client.CollaborationService;
import client.NotificationClient;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class Disconnect extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        CollaborationService.getInstance().closeInstance();
        NotificationClient.notify("","Disconnect From Collaborative Editin");
    }
}
