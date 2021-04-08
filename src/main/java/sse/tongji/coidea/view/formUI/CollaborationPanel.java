package sse.tongji.coidea.view.formUI;

import javax.swing.*;

public class CollaborationPanel implements MainContentInterface {
    JPanel myToolWindowContent;

    JPanel collaborationInformationPanel;
    JList<String> collaboratorList;
    JTextField userNameField;
    JPanel serverPanel;
    JLabel userNameLabel;
    JPanel usernamePanel;
    JPanel repoPanel;
    JLabel repoIdLabel;
    JTextField repoIdTextField;
    private JTabbedPane tabbedPane1;
    JPanel collaboratorPanel;

    public CollaborationPanel(){
        Box vBox = Box.createVerticalBox();
        vBox.add(usernamePanel);
        vBox.add(serverPanel);
//        vBox.add(repoPanel);
//        vBox.add(noProjectPanel);
        Box vBox2 = Box.createVerticalBox();
//        vBox2.add(projectPanel);
        vBox2.add(tabbedPane1);
        vBox.add(vBox2);
//        vBox.add(logoutPanel);
        myToolWindowContent.add(vBox);
//        connectSuccess(true);

//        DefaultListModel<String> model = (DefaultListModel<String>) this.collaboratorList.getModel();
//        model.addElement("asdxcvdfg");
    }

    @Override
    public JPanel getContent() {
        return myToolWindowContent;
    }

    public void connectSuccess(boolean isConnect){
//        noProjectPanel.setVisible(!isConnect);
        collaborationInformationPanel.setVisible(isConnect);
    }
}