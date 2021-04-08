package sse.tongji.coidea.view.formUI;

import javax.swing.*;

public class CollaborationPanel implements MainContentInterface {
    JPanel myToolWindowContent;

    JPanel collaborationInformationPanel;
    JList<String> collaboratorList;
    JTextField userNameField;
    JPanel serverPanel;
    JLabel serverAddressLabel;
    JTextField serverAddressTextField;
    JLabel userNameLabel;
    JPanel noProjectPanel;
    JPanel usernamePanel;
    JPanel teamPanel;
    JLabel teamIdLabel;
    JPanel projectPanel;
    JLabel projectIdLabel;
    JPanel collaboratorPanel;
    JButton logoutButton;
    JPanel logoutPanel;

    public CollaborationPanel(){
        Box vBox = Box.createVerticalBox();
        vBox.add(usernamePanel);
        vBox.add(serverPanel);
        vBox.add(noProjectPanel);
        Box vBox2 = Box.createVerticalBox();
        vBox2.add(teamPanel);
        vBox2.add(projectPanel);
        vBox2.add(collaboratorPanel);
        collaborationInformationPanel.add(vBox2);
        vBox.add(collaborationInformationPanel);
        vBox.add(logoutPanel);
        myToolWindowContent.add(vBox);
        connectSuccess(false);
    }

    @Override
    public JPanel getContent() {
        return myToolWindowContent;
    }

    public void connectSuccess(boolean isConnect){
        noProjectPanel.setVisible(!isConnect);
        collaborationInformationPanel.setVisible(isConnect);
    }
}