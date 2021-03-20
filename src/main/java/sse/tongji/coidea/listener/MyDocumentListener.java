package sse.tongji.coidea.listener;

import client.DocumentEditor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalFilePresenter;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyDocumentListener implements DocumentListener {
    private final MyLogger log = MyLogger.getLogger(MyDocumentListener.class);

    private LocalFilePresenter localFilePresenter;

    private final AtomicBoolean isRemotePlaying = new AtomicBoolean(false);

    public MyDocumentListener(LocalFilePresenter localFilePresenter) {
        this.localFilePresenter = localFilePresenter;
    }

    public void remotePlaying() {
        this.isRemotePlaying.set(true);
    }

    public void remotePlayingDone() {
        this.isRemotePlaying.set(false);
    }

    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        System.out.println("beforeDocumentChange");
        if(DocumentEditor.isRemotePlaying.get()){
            System.out.println("isRemotePlaying");
            return;
        }
        System.out.println(event.toString());
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        if (isRemotePlaying.get()) {
            return;
        }
        localFilePresenter.onLocalEdit(event);
    }
}
