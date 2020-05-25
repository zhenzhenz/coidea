package listeners;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;

public class MyEditorFactoryListener implements EditorFactoryListener {

    @Override
    public void editorCreated(EditorFactoryEvent event){
//        HightlightClient.addEditor(event.editor)
        System.out.println("editorCreated");
    }

    @Override
    public void editorReleased(EditorFactoryEvent event){
//        HightlightClient.removeEditor(event.editor)
        System.out.println("editorReleased");
    }

}
