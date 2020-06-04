package client;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFileManager;
import listeners.MyDocumentListener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DocumentEditor {
    public static Map<String, DocumentEditor> docMap = new HashMap<String, DocumentEditor>();
    public static volatile AtomicBoolean isRemotePlaying = new AtomicBoolean(false);
    public static int remoteCaretOffset = -1;
    public static String username = "";

    public Project project;
    public Document document;
    public String path;

    public DocumentEditor(Project _project, Document _document, String _path){
        this.project = _project;
        this.document = _document;
        this.path = _path;
        docMap.put(_path, this);
    }

    public MyDocumentListener myDocumentListener  = null;
//    public caret : CaretModel? = null
//    public myCaretListener : MyCaretListener? = null

    public void del() {
        if(myDocumentListener != null) {
            document.removeDocumentListener(myDocumentListener);
        }
//        if(myCaretListener !=null) {
//            caret?.removeCaretListener(myCaretListener!!)
//        }
    }

    public Boolean isWritable = true;

    public void highlightLine(String username){
        HightlightClient.hightlight(document, username);
    }

    public void refreshFile(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                VirtualFileManager.getInstance().syncRefresh();
            }
        };
        WriteCommandAction.runWriteCommandAction(project, runnable);
    }

    public void onInsert(int pos, String text) {
        DocumentEditor.isRemotePlaying.set(true);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                insert(pos,text);
                document.insertString(pos, text);
            }
        };
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                WriteCommandAction.runWriteCommandAction(project, runnable);
                DocumentEditor.isRemotePlaying.set(false);
                CollaborationService.getInstance().getDocumentSemaphore().release();
            }
        });
//        WriteCommandAction.runWriteCommandAction(project, runnable);
//        DocumentEditor.isRemotePlaying.set(false);
//        CollaborationService.getInstance().getDocumentSemaphore().release();
    }

    public void onDelete(int pos, int length) {
        DocumentEditor.isRemotePlaying.set(true);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                delete(pos,length);
                document.deleteString(pos, pos + length);
            }
        };
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                WriteCommandAction.runWriteCommandAction(project, runnable);
                DocumentEditor.isRemotePlaying.set(false);
                CollaborationService.getInstance().getDocumentSemaphore().release();
            }
        });
//        WriteCommandAction.runWriteCommandAction(project, runnable);
//        DocumentEditor.isRemotePlaying.set(false);
//        CollaborationService.getInstance().getDocumentSemaphore().release();
    }

    public void onRewrite(String text) {
        DocumentEditor.isRemotePlaying.set(true);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                rewrite(text);
            }
        };
        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            @Override
            public void run() {
                WriteCommandAction.runWriteCommandAction(project, runnable);
                DocumentEditor.isRemotePlaying.set(false);
                System.out.println("done");
            }
        });
    }

//    public void addHighLight(userId: String) {
//        HightlightClient.hightlight(document,userId)
//    }

    private void insert(int pos, String text) {
//        document.insertString(pos, text);
        changeTextContent(new Runnable() {
            @Override
            public void run() {
                document.insertString(pos, text);
            }
        });
    }

    private void delete(int pos, int length) {
        changeTextContent(new Runnable() {
            @Override
            public void run() {
                document.deleteString(pos, pos + length);
            }
        });
    }

    private void rewrite(String text) {
        changeTextContent(new Runnable() {
            @Override
            public void run() {
                document.deleteString(0, document.getTextLength());
                document.insertString(0, text);
            }
        });
    }


    private synchronized void changeTextContent(@NotNull Runnable runnable) {
        boolean writeState = document.isWritable();

        document.setReadOnly(false);
        isWritable = false;

        runnable.run();

        document.setReadOnly(!writeState);
        isWritable = true;
    }


}
