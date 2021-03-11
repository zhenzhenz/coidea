package client;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HightlightClient {
    public static Project project;
    private static Map<Editor, Map<String, RangeHighlighter>> highLightList = new HashMap<Editor, Map<String, RangeHighlighter>>();

    public static void hightlight(Document _document, String _username){
        if (project == null){
            return;
        }
        for (Editor ed : highLightList.keySet()){
            if (ed.getDocument() == _document){
                Map<String, RangeHighlighter> rangeList = highLightList.get(ed);
                HighlightManager highlightManager = HighlightManager.getInstance(project);
                if (rangeList.containsKey(_username) && rangeList.get(_username) != null){
                    WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                        @Override
                        public void run() {
                            highlightManager.removeSegmentHighlighter(ed, rangeList.get(_username));
                            rangeList.remove(_username);
                        }
                    });
                }
                int offset = -1;
                if (DocumentEditor.remoteCaretOffset != -1){
                    offset = DocumentEditor.remoteCaretOffset;
                }else{
                    return;
                }
                if (offset > _document.getTextLength()){
                    return;
                }
                int lineNumber = _document.getLineNumber(offset);
                int endOffset = _document.getLineEndOffset(lineNumber);
                int begOffset = _document.getLineStartOffset(lineNumber);
                TextAttributes textAttributes = new TextAttributes(null, new Color(75, 75, 75), null, EffectType.BOXED, 0);
                WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<RangeHighlighter> collection =  new ArrayList<RangeHighlighter>();
                        highlightManager.addRangeHighlight(ed, begOffset, endOffset, textAttributes, false, false, collection);
                        rangeList.put(_username, collection.get(0));
                        highLightList.replace(ed, rangeList);
                    }
                });
            }
        }

    }

    public static void addEditor(Editor _editor){
        if (!highLightList.containsKey(_editor)){
            highLightList.put(_editor, new HashMap<String, RangeHighlighter>());
        }
    }

    public static void removeEditor(Editor _editor){
        if (highLightList.containsKey(_editor) && highLightList.get(_editor) != null){
            HighlightManager highlightManager = HighlightManager.getInstance(project);
            Map<String, RangeHighlighter> rangeList = highLightList.get(_editor);
            for (RangeHighlighter rg : rangeList.values()){
                highlightManager.removeSegmentHighlighter(_editor, rg);
            }
            highLightList.remove(_editor);
        }
    }
}
