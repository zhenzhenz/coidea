package sse.tongji.coidea.view.formUI;

import javax.swing.*;

public class TeamPanel {
    JPanel teamPanel;

    JPanel createPanel;
    JPanel existTeamPanel;
    JLabel teamIdLabel;
    JComboBox teamIdComboBox;
    JButton createButton;
    JPanel memberPanel;
    JList memberList;
    JPanel controlPanel;
    JButton addMemberButton;
    JButton rmMemberButton;

    public TeamPanel() {
        Box vBox = Box.createVerticalBox();
        vBox.add(existTeamPanel);
        vBox.add(createPanel);
        vBox.add(memberPanel);
        vBox.add(controlPanel);
        teamPanel.add(vBox);
    }

    public JPanel getContent(){
        return teamPanel;
    }
}
