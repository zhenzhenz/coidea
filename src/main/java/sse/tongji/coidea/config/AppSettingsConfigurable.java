package sse.tongji.coidea.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 允许用户自定义配置
 * @author mtage
 * @since 2021/4/13 19:33
 */
public class AppSettingsConfigurable implements Configurable {
    private AppSettingsComponent mySettingsComponent;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "CoIDEA: App Settings";
    }

    @Override
    public @Nullable JComponent createComponent() {
        mySettingsComponent = new AppSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        AppSettingsState settings = AppSettingsState.getInstance();
        boolean modified = !mySettingsComponent.getDalOpenCheckBox().isSelected() == settings.isDalOpen();
        modified |= mySettingsComponent.getDepthOpenCheckBox().isSelected() != settings.ideaStatus;
        modified |= mySettingsComponent.getTimeoutField().getValue() != settings.getTimeout();
        modified |= mySettingsComponent.getMethodDepthField().getValue() != settings.getMethodDepth();
        modified |= mySettingsComponent.getFieldDepthField().getValue() != settings.getFieldDepth();

        return modified;
    }

    @Override
    public void apply() {
        AppSettingsState settings = AppSettingsState.getInstance();
        settings.setDalOpen(mySettingsComponent.getDalOpenCheckBox().isSelected());
        settings.setDepthOpen(mySettingsComponent.getDepthOpenCheckBox().isSelected());
        settings.setMethodDepth(mySettingsComponent.getMethodDepthField().getValue());
        settings.setFieldDepth(mySettingsComponent.getFieldDepthField().getValue());
        settings.setTimeout(mySettingsComponent.getTimeoutField().getValue());
    }

    @Override
    public void reset() {
        AppSettingsState settings = AppSettingsState.getInstance();
        mySettingsComponent.getDalOpenCheckBox().setSelected(settings.isDalOpen());
        mySettingsComponent.getDepthOpenCheckBox().setSelected(settings.isDepthOpen());
        mySettingsComponent.getMethodDepthField().setValue(settings.getMethodDepth());
        mySettingsComponent.getFieldDepthField().setValue(settings.getFieldDepth());
        mySettingsComponent.getTimeoutField().setValue(settings.getTimeout());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
