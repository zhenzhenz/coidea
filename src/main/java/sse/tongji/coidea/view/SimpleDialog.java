package sse.tongji.coidea.view;

import javax.swing.*;

public class SimpleDialog extends JDialog {
    private JPanel contentPane;
    private JPanel controlBlock;
    private JPanel optionalBlock;
    private JCheckBox isDALOpen;
    private JCheckBox isFieldsLocked;
    private JTextField textField1;
    private JCheckBox isMethodsLocked;
    private JLabel lockDepth;
    private JTextField serverAddress;
    private JTextField userName;
    private JTextField repoId;
    private JButton connectServerButton;
    private JRadioButton createRepoOption;
    private JRadioButton joinRepoOption;
    private JLabel serverAddressLabel;
    private JLabel userNameLabel;
    private JLabel repoIdLabel;
    private JPanel selectBlock;

    public JPanel getContent() {
        return contentPane;
    }

}
