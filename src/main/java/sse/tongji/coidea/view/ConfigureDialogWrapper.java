package sse.tongji.coidea.view;

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
public class ConfigureDialogWrapper extends DialogWrapper implements IConnConfigureView {
    private ConfigureDialog configureDialog;

    public ConfigureDialogWrapper() {
        super(true);
        init();
        setTitle(CoIDEAUIString.CONFIGURE_DIALOG_TITLE);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        this.configureDialog = new ConfigureDialog();
        setOKButtonText(CoIDEAUIString.CONNECT);
        this.configureDialog.getRepoIdTextField().setText(CoIDEAUIString.RANDOM_REPO_PREFIX + new Random().nextInt(1000));
        this.configureDialog.getUserNameTextField().setText(CoIDEAUIString.DEFAULT_USERNAME);
        return this.configureDialog.getContent();
    }

    @Override
    public ConnConfigurationInput readConfigurationInput() {
        return ConnConfigurationInput.builder()
                .serverAddr(CoIDEAUIString.WS_SERVER_PREFIX + configureDialog.getServerAddressTextField().getText())
                .repoId(configureDialog.getRepoIdTextField().getText())
                .newRepo(configureDialog.getCreateRepoOption().isSelected())
                .userName(configureDialog.getUserNameTextField().getText())
                .build();
    }

    @Override
    public void close() {
        super.close(OK_EXIT_CODE);
    }
}
