package listeners;

import client.CollaborationService;
import client.DocumentEditor;
import client.FileClient;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class MyCaretListener implements CaretListener {
    DocumentEditor docEditor;
    Document document;
    Project project;
    String path;

    public MyCaretListener(DocumentEditor _docEditor, String _path){
        docEditor = _docEditor;
        document = _docEditor.document;
        project = _docEditor.project;
        path = _path;
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        int offset =  event.getCaret().getOffset();
        System.out.println("caret offset: " + offset);
        CollaborationService.getInstance().localCaretMove(path, offset, DocumentEditor.username);
    }

}
