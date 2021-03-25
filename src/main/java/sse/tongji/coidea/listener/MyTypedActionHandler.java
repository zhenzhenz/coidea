package sse.tongji.coidea.listener;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.editor.impl.EditorImpl;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

import java.util.Objects;

/**
 * 该Handler只能监听到字符输入，backspace enter等都不行
 * @author mtage
 * @since 2021/3/15 10:10
 */
@Deprecated
public class MyTypedActionHandler implements TypedActionHandler {
    private final Logger log = LoggerFactory.getLogger(MyTypedActionHandler.class);
    private TypedActionHandler oldHandler;
    private LocalRepositoryPresenter localRepositoryPresenter;

    public MyTypedActionHandler(LocalRepositoryPresenter localRepositoryPresenter) {
        this.localRepositoryPresenter = localRepositoryPresenter;
    }

    public void setOldHandler(TypedActionHandler oldHandler) {
        this.oldHandler = oldHandler;
    }

    @Override
    public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        log.info("doc{} charTyped:{}", editor.getDocument(), charTyped);
        localRepositoryPresenter.onLocalKeyPressed(((EditorImpl) editor).getVirtualFile(), charTyped);
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            log.error("InterruptedException ", e);
//        }
        if (Objects.nonNull(oldHandler)) {
            oldHandler.execute(editor, charTyped, dataContext);
        }
//        if (editor.isViewer()) {
//            return;
//        }
    }
}
