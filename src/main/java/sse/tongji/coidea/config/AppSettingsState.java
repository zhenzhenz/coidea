package sse.tongji.coidea.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author mtage
 * @since 2021/4/13 19:37
 */
@State(
        name = "sse.tongji.coidea.config.AppSettingsState",
        storages = {@Storage("CoIDEASettingsPlugin.xml")}
)
public class AppSettingsState implements PersistentStateComponent<AppSettingsState> {
    public boolean ideaStatus = false;

    @Getter
    @Setter
    private boolean dalOpen = false;
    @Getter
    @Setter
    private boolean depthOpen = false;
    @Getter
    @Setter
    private int timeout;
    @Getter
    @Setter
    private int fieldDepth;
    @Getter
    @Setter
    private int methodDepth;

    public static AppSettingsState getInstance() {
        return ServiceManager.getService(AppSettingsState.class);
    }

    @Override
    public @Nullable AppSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull AppSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
