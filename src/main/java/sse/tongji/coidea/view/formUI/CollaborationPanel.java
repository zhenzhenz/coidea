package sse.tongji.coidea.view.formUI;

import javax.swing.*;

public class CollaborationPanel implements MainContentInterface {
    JPanel myToolWindowContent;

    JPanel userPanel;
    JTextField userNameField;
    JLabel userNameLabel;
    JLabel repoIdLabel;
    JTextField repoIdTextField;
    JTabbedPane tabbedPane1;
    JList<String> collaboratorList;

    public CollaborationPanel(){
        Box vBox = Box.createVerticalBox();
        vBox.add(userPanel);
        vBox.add(tabbedPane1);
        myToolWindowContent.add(vBox);

//        DefaultListModel<String> model = (DefaultListModel<String>) this.collaboratorList.getModel();
//        model.addElement("asdxcvdfg");
    }

    @Override
    public JPanel getContent() {
        return myToolWindowContent;
    }

}