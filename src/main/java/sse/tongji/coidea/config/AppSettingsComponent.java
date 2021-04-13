package sse.tongji.coidea.config;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.fields.IntegerField;
import com.intellij.util.ui.FormBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * 用户自定义配置 配置界面
 *
 * @author mtage
 * @since 2021/4/13 19:36
 */
@Getter
public class AppSettingsComponent {
    private final JPanel myMainPanel;
    private final JBTextField myUserNameText = new JBTextField();
    private final JBCheckBox dalOpenCheckBox = new JBCheckBox("Open DAL");
    private final JBCheckBox depthOpenCheckBox = new JBCheckBox("Open Depth");
    private final IntegerField timeoutField = new IntegerField();
    private final IntegerField fieldDepthField = new IntegerField();
    private final IntegerField methodDepthField = new IntegerField();

    public AppSettingsComponent() {
        refreshEnable();
        dalOpenCheckBox.addChangeListener(e -> refreshEnable());
        depthOpenCheckBox.addChangeListener(e -> refreshEnable());
        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(dalOpenCheckBox, 1)
                .addComponent(depthOpenCheckBox)
                .addLabeledComponent(new JBLabel("Timeout(seconds): "), timeoutField)
                .addLabeledComponent(new JBLabel("Field Depth: "), fieldDepthField)
                .addLabeledComponent(new JBLabel("Method Depth: "), methodDepthField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    private void refreshEnable() {
        depthOpenCheckBox.setEnabled(dalOpenCheckBox.isSelected());
        fieldDepthField.setEnabled(dalOpenCheckBox.isSelected() && depthOpenCheckBox.isSelected());
        methodDepthField.setEnabled(dalOpenCheckBox.isSelected() && depthOpenCheckBox.isSelected());
        timeoutField.setEnabled(dalOpenCheckBox.isSelected());
    }
}
