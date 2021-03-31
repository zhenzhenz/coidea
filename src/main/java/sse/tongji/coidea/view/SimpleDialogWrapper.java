package sse.tongji.coidea.view;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SimpleDialogWrapper extends DialogWrapper {

    public SimpleDialogWrapper() {
        super(true);
        init();
        setTitle("Connect to Co IDEA Server");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        SimpleDialog dialogPanel = new SimpleDialog();
        return dialogPanel.getContent();
    }

}
