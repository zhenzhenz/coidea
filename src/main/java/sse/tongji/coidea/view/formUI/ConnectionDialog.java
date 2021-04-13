package sse.tongji.coidea.view.formUI;

import javax.swing.*;

public class ConnectionDialog {
    JPanel connectionPanel;

    JRadioButton createRepoOption;
    JRadioButton joinRepoOption;
    JPanel newRopePanel;
    JTextField repoIdTextField;
    JLabel repoIdLabel;
    JComboBox repoIdComboBox;
    JLabel repoIdLabel2;
    JPanel optionalBlock;
    JCheckBox isDALOpen;
    JCheckBox isFieldsLocked;
    JLabel lockDepth;
    JTextField lockDepthField;
    JCheckBox isMethodsLocked;
    JPanel existRepoPanel;
    JPanel optionPanel;
    JPanel teamPanel;
    JLabel teamLabel;
    JComboBox teamComboBox;

    public ConnectionDialog(){
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(createRepoOption);
        btnGroup.add(joinRepoOption);
        createRepoOption.setSelected(true);
        existRepoPanel.setVisible(false);
        createRepoOption.addActionListener(e->{
            this.newRopePanel.setVisible(this.createRepoOption.isSelected());
            this.existRepoPanel.setVisible(this.joinRepoOption.isSelected());
        });
        joinRepoOption.addActionListener(e->{
            this.newRopePanel.setVisible(this.createRepoOption.isSelected());
            this.existRepoPanel.setVisible(this.joinRepoOption.isSelected());
        });
        Box vBox = Box.createVerticalBox();
        vBox.add(teamPanel);
        vBox.add(optionPanel);
        vBox.add(newRopePanel);
        vBox.add(existRepoPanel);
        vBox.add(optionalBlock);

        connectionPanel.add(vBox);
    }

    public JPanel getContent(){
        return connectionPanel;
    }

}
