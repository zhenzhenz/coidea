package sse.tongji.coidea.view.formUI;

import javax.swing.*;

public class LoginPanel implements MainContentInterface {

    JPanel loginPanel;

    JTextField usernameField;
    JPasswordField passwordField;
    JLabel usernameLabel;
    JLabel passwordLabel;
    JButton loginButton;
    JButton signupButton;
    JPanel buttonPanel;
    JPanel infoPanel;

    public LoginPanel() {
        Box vBox = Box.createVerticalBox();
        vBox.add(infoPanel);
        vBox.add(buttonPanel);
        loginPanel.add(vBox);
    }

    @Override
    public JPanel getContent(){
        return loginPanel;
    }

}
