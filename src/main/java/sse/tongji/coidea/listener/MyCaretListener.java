package sse.tongji.coidea.listener;

import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalFilePresenter;

/**
 * @author xuwenhua
 */
public class MyCaretListener implements CaretListener {
    private final MyLogger log = MyLogger.getLogger(MyCaretListener.class);
    private LocalFilePresenter localFilePresenter;
    public MyCaretListener(LocalFilePresenter localFilePresenter) {
        this.localFilePresenter = localFilePresenter;
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        localFilePresenter.localCaretMove(event);
    }
}
