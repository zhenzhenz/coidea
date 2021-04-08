package sse.tongji.coidea.view;

import lombok.Getter;

import javax.swing.*;

@SuppressWarnings("jol")
@Getter
public class ConfigureDialog extends JDialog {
    private JPanel contentPane;
    private JPanel controlBlock;
    private JTextField serverAddressTextField;
    private JTextField userNameTextField;
    private JTextField repoIdTextField;
    private JRadioButton createRepoOption;
    private JRadioButton joinRepoOption;
    private JLabel serverAddressLabel;
    private JLabel userNameLabel;
    private JLabel repoIdLabel;

    public JPanel getContent() {
        ButtonGroup btnGroup = new ButtonGroup();
        btnGroup.add(createRepoOption);
        btnGroup.add(joinRepoOption);
        return contentPane;
    }

}
