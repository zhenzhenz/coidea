package sse.tongji.coidea.listener;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import dev.mtage.eyjaot.client.inter.util.GeneralFileIgnoreUtil;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyRepositoryListener implements VirtualFileListener {
    private MyLogger log = MyLogger.getLogger(MyRepositoryListener.class);

    public static AtomicBoolean isResourceListening = new AtomicBoolean(true);

    public static void pauseListening() {
        isResourceListening.set(false);
    }

    public static void resumeListening() {
        isResourceListening.set(true);
    }

    private LocalRepositoryPresenter localRepositoryPresenter;

    public MyRepositoryListener(LocalRepositoryPresenter localRepositoryPresenter) {
        this.localRepositoryPresenter = localRepositoryPresenter;
    }

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event) {
        if (!isResourceListening.get() || GeneralFileIgnoreUtil.isIgnored(event.getFileName())) {
            return;
        }
        log.info("isResourceListening {0}", isResourceListening.get());
        localRepositoryPresenter.onLocalFileCreate(event.getFile());
    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        if (!isResourceListening.get() || GeneralFileIgnoreUtil.isIgnored(event.getFileName())) {
            System.out.println("isResourceListening false");
            return;
        }
        localRepositoryPresenter.onLocalFileDelete(event.getFile().getPath(), event.getFileName());
    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event) {
        if (!isResourceListening.get()) {
            return;
        }
//        log.info("{0} file content changed", event.getFile().getPath());
    }
}

