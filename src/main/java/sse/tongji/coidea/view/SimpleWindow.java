package sse.tongji.coidea.view;

import com.intellij.openapi.wm.ToolWindow;
import javax.swing.*;

public class SimpleWindow {

    private JButton hideToolWindowButton;
    private JPanel myToolWindowContent;
    private JPanel controlBlock;
    private JButton connectServer;
    private JButton refreshInformation;
    private JPanel collaborationInformationBlock;
    private JPanel userNameBlock;
    private JList collaboratorList;
    private JPanel collaboratorBlock;
    private JList notificationList;
    private JPanel notificationBlock;
    private JPanel repositoryBlock;
    private JLabel userNameLabel;
    private JLabel repositoryIdLabel;
    private JLabel helpLabel;
    private JPanel logoBlock;

    public SimpleWindow(){}

    public SimpleWindow(ToolWindow toolWindow) {
        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
        connectServer.addActionListener(e -> {
            if (new SimpleDialogWrapper().showAndGet()) {
                // user pressed OK
            }
        });
    }

/*    public SimpleWindowView(ToolWindow toolWindow) {

        hideToolWindowButton.addActionListener(e -> toolWindow.hide(null));
        refreshToolWindowButton.addActionListener(e -> currentDateTime());

        this.currentDateTime();
    }

    public void currentDateTime() {
        // Get current date and time
        Calendar instance = Calendar.getInstance();
        currentDate.setText(
                instance.get(Calendar.DAY_OF_MONTH) + "/"
                        + (instance.get(Calendar.MONTH) + 1) + "/"
                        + instance.get(Calendar.YEAR)
        );
        currentDate.setIcon(new ImageIcon(getClass().getResource("/logo.png")));
        int min = instance.get(Calendar.MINUTE);
        String strMin = min < 10 ? "0" + min : String.valueOf(min);
        currentTime.setText(instance.get(Calendar.HOUR_OF_DAY) + ":" + strMin);
        currentTime.setIcon(new ImageIcon(getClass().getResource("/logo.png")));
        // Get time zone
        long gmt_Offset = instance.get(Calendar.ZONE_OFFSET); // offset from GMT in milliseconds
        String str_gmt_Offset = String.valueOf(gmt_Offset / 3600000);
        str_gmt_Offset = (gmt_Offset > 0) ? "GMT + " + str_gmt_Offset : "GMT - " + str_gmt_Offset;
        timeZone.setText(str_gmt_Offset);
        timeZone.setIcon(new ImageIcon(getClass().getResource("/logo.png")));
    }*/

    public JPanel getContent() {
        return myToolWindowContent;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SimpleWindowView");
        frame.setContentPane(new SimpleWindow().myToolWindowContent);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}