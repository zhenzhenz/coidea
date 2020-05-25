package listeners;

import client.CollaborationService;
import client.DocumentEditor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MyDocumentListener implements DocumentListener {
    DocumentEditor docEditor;
    Document document;
    Project project;
    String path;
//    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)!!

    public MyDocumentListener(DocumentEditor _docEditor, String _path){
        docEditor = _docEditor;
        document = _docEditor.document;
        project = _docEditor.project;
        path = _path;
    }
    @Override
    public void beforeDocumentChange(@NotNull DocumentEvent event) {
        System.out.println("beforeDocumentChange");
        if(DocumentEditor.isRemotePlaying.get()){
            System.out.println("isRemotePlaying");
            return;
        }
        System.out.println(event.toString());
        project = docEditor.project;

        System.out.println("file path3: " + path);

    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        if(DocumentEditor.isRemotePlaying.get()){
            System.out.println("isRemotePlaying");
            return;
        }
        if (event.getOldLength() != 0){
            //delete;
            System.out.println("delete");
            System.out.println(event.getOldFragment().toString());
            CollaborationService.getInstance().localDelete(path, event.getOffset(), event.getOffset() + event.getOldLength());
        }
        if (event.getNewLength() != 0){
            //insert
            System.out.println("insert");
            System.out.println(event.getNewFragment().toString());
            CollaborationService.getInstance().localInsert(path, event.getOffset(), event.getNewFragment().toString());
        }
        System.out.println("documentChanged");
        System.out.println(event.toString());
    }
}
