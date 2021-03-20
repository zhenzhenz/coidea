package listeners;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author mtage
 * @since 2021/3/15 10:10
 */
public class MyTypedActionHandler implements TypedActionHandler {
    private final Logger log = LoggerFactory.getLogger(MyTypedActionHandler.class);
    private TypedActionHandler oldHandler;

    public void setOldHandler(TypedActionHandler oldHandler) {
        this.oldHandler = oldHandler;
    }

    @Override
    public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        log.info("doc text: {} charTyped:{} context:{}",
                editor.getDocument().getText(), charTyped, dataContext);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error("InterruptedException ", e);
        }
        if (Objects.nonNull(oldHandler)) {
            oldHandler.execute(editor, charTyped, dataContext);
        }
//        if (editor.isViewer()) {
//            return;
//        }
    }
}
