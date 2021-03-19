package sse.tongji.coidea.listener;

import client.CoServiceStatusEnum;
import client.CollaborationService;
import client.DocumentEditor;
import client.FileClient;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import entity.EclipseDocEditorFactory;
import listeners.MyDocumentListener;
import listeners.MyTypedActionHandler;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.presenter.LocalRepositoryPresenter;

/**
 * 文件打开关闭的监听
 * @author mtage
 * @since 2021/3/19 20:00
 */
public class MyFileEditorManagerListener implements FileEditorManagerListener {
    private LocalRepositoryPresenter localRepositoryPresenter;

    public MyFileEditorManagerListener(LocalRepositoryPresenter localRepositoryPresenter) {
        this.localRepositoryPresenter = localRepositoryPresenter;
    }

    @Override
    public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
        file.setDetectedLineSeparator("\n");
        Project project = source.getProject();
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if (document != null) {
            String path = FileClient.GetPath(file);
            DocumentEditor docEditor = new DocumentEditor(project, document, project.getName() + "/" + path);

            MyDocumentListener documentListener = new MyDocumentListener(docEditor, "/" + project.getName() + "/" + path);
            document.addDocumentListener(documentListener);
            docEditor.myDocumentListener = documentListener;

            MyTypedActionHandler myTypedActionHandler = new MyTypedActionHandler();
            TypedActionHandler oldHandler = TypedAction.getInstance().setupRawHandler(myTypedActionHandler);
            myTypedActionHandler.setOldHandler(oldHandler);

        }
        System.out.println("source: " + source + "; open file: " + file);

    }

    @Override
    public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//                JsEngine.invoke("deleteDoc",FileClient.GetPath(file))
//                ApiClient.send(Gson().toJson(CloseFileAction(FileClient.GetPath(file))))
        Project project = source.getProject();
        Document document = FileDocumentManager.getInstance().getDocument(file);
        String path = FileClient.GetPath(file);
        System.out.println("source: " + source + "; close file: " + file);
        if (CollaborationService.getStatus() == CoServiceStatusEnum.CONNECTED ||
                CollaborationService.getStatus() == CoServiceStatusEnum.INITED) {
            CollaborationService.getInstance().closeFile(EclipseDocEditorFactory.getLocalDocEditor(project, document, project.getName() + "/" + path, file.getName()));
        }
    }
}
