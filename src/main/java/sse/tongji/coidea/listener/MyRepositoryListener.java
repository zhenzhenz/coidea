package sse.tongji.coidea.listener;

import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

import java.util.concurrent.atomic.AtomicBoolean;

public class MyRepositoryListener implements VirtualFileListener {

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
    public void fileCreated(@NotNull VirtualFileEvent event){
        if (!isResourceListening.get())
        {
            return;
        }

    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {
        if (!isResourceListening.get())
        {
            System.out.println("isResourceListening false");
            return;
        }


    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event){
        if (!isResourceListening.get())
        {
            return;
        }

    }
}

