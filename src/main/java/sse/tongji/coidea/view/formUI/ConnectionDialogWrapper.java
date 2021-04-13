package sse.tongji.coidea.view.formUI;

import com.intellij.openapi.ui.DialogWrapper;
import dev.mtage.eyjaot.client.inter.view.ConnConfigurationInput;
import dev.mtage.eyjaot.client.inter.view.IConnConfigureView;
import org.jetbrains.annotations.Nullable;
import sse.tongji.coidea.config.CoIDEAUIString;

import javax.swing.*;
import java.util.Random;


/**
 * @author Qisheng Jiang, mtage
 */
@SuppressWarnings("jol")
public class ConnectionDialogWrapper extends DialogWrapper implements IConnConfigureView {
    private ConnectionDialog connectionDialog;

    public ConnectionDialogWrapper() {
        super(true);
        init();
        setTitle(CoIDEAUIString.CONFIGURE_DIALOG_TITLE);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        this.connectionDialog = new ConnectionDialog();
        setOKButtonText(CoIDEAUIString.CONNECT);
//        this.connectionDialog.getRepoIdTextField().setText(CoIDEAUIString.RANDOM_REPO_PREFIX + new Random().nextInt(1000));
//        this.connectionDialog.getUserNameTextField().setText(CoIDEAUIString.DEFAULT_USERNAME);
        return this.connectionDialog.getContent();
    }

    @Override
    public ConnConfigurationInput readConfigurationInput() {
        return ConnConfigurationInput.builder()
//                .serverAddr(CoIDEAUIString.WS_SERVER_PREFIX + connectionDialog.getServerAddressTextField().getText())
//                .repoId(connectionDialog.getRepoIdTextField().getText())
//                .newRepo(connectionDialog.getCreateRepoOption().isSelected())
//                .userName(connectionDialog.getUserNameTextField().getText())
                .build();
    }

    @Override
    public void close() {
        super.close(OK_EXIT_CODE);
    }
}
