package sse.tongji.coidea.view.formUI;

import com.intellij.openapi.util.IconLoader;

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
    private JLabel imageLabel;

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

    private void createUIComponents() {
        Icon icon = IconLoader.getIcon("/coidea_icon.svg", CollaborationPanel.class);
        imageLabel = new JLabel(icon);
    }
}