package sse.tongji.coidea.listener;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

/**
 * 文件打开关闭的监听
 * @author mtage
 * @since 2021/3/19 20:00
 */
public class MyFileOpenCloseListener implements FileEditorManagerListener {
    private LocalRepositoryPresenter localRepositoryPresenter;

    public MyFileOpenCloseListener(LocalRepositoryPresenter localRepositoryPresenter) {
        this.localRepositoryPresenter = localRepositoryPresenter;
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        file.setDetectedLineSeparator("\n");
        localRepositoryPresenter.onLocalFileOpen(source, file);
    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        localRepositoryPresenter.onLocalFileClose(source, file);
    }

}
