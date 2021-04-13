package sse.tongji.coidea.view;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;

import dev.mtage.eyjaot.client.inter.util.MyLogger;
import sse.tongji.dal.locksystem.BasicRegion;
import sse.tongji.dal.locksystem.BasicRegionList;
import sse.tongji.dal.locksystem.Lock;
import sse.tongji.dal.locksystem.LockType;

import java.awt.*;
import java.util.*;

/**
 * @author xuwenhua
 */
public class DALAwarenessPrinter {
    final MyLogger log = MyLogger.getLogger(DALAwarenessPrinter.class);
    private final Project project;
    private final Editor editor;
    private HighlightManager highlightManager;
    private static Collection<RangeHighlighter> highLightList = new ArrayList<>();
    private static Map<String, Color> siteNameToColor = new HashMap<>();
    private final Color colorSharedWriteLock = new Color(208, 32, 144);
    private final Color colorSharedReadLock = new Color(181, 230, 29);
    private final Color colorWhite = new Color(255, 255, 255);

    public DALAwarenessPrinter(Project project) {
        this.project = project;
        this.editor = EditorFactory.getInstance().getAllEditors()[0];
        this.highlightManager = HighlightManager.getInstance(project);
    }

    public void refreshHighlight() {
        clearHighlight();
        addHighlight();
    }

    private void addHighlight() {
        Document document = editor.getDocument();
        if (project != null && editor != null) {
            for (BasicRegion b : BasicRegionList.getBasicRegionList()) {
                if (b.getStartOffset() >= document.getText().length()) {
                    continue;
                }
                if (b.getEndOffset() >= document.getText().length()) {
                    continue;
                }
                int lineStartNumber = document.getLineNumber(b.getStartOffset());
                int lineEndNumber = document.getLineNumber(b.getEndOffset());
                Color printerColor = getRegionColor(b);
                highlightManager.addRangeHighlight(editor, document.getLineStartOffset(lineStartNumber), document.getLineEndOffset(lineEndNumber), new TextAttributes(null, printerColor, null, EffectType.BOXED, 0), false, highLightList);
            }
        }
    }

    private void clearHighlight() {
        for (RangeHighlighter h : highLightList) {
            highlightManager.removeSegmentHighlighter(editor, h);
        }
        highLightList.clear();
    }



    private Color getRegionColor(BasicRegion basicRegion) {
        //如果这个区域只有一个锁
        if (basicRegion.getLockList().size() == 1) {
            String userSiteName = basicRegion.getLockList().get(0).getSitename();
            if (!siteNameToColor.containsKey(userSiteName)) {
                siteNameToColor.put(userSiteName, generateRandomColor());
            }
            Color regionColor = siteNameToColor.get(userSiteName);
            if (basicRegion.getLockList().get(0).getLockType() == LockType.WORKINGLOCK) {
                return regionColor;
            } else if (basicRegion.getLockList().get(0).getLockType() == LockType.DEPENDENCYLOCK) {
                return mixColorWithColor(regionColor, new Color(200, 200, 200));
            } else if (basicRegion.getLockList().get(0).getLockType() == LockType.AWARENESSLOCK) {
                return mixColorWithColor(mixColorWithColor(regionColor,new Color(255, 255, 255)), new Color(255, 255, 255));
            }
        } else if (basicRegion.getLockList().size() > 1) {
            for (Lock lk : basicRegion.getLockList()) {
                //如果包含working的锁,锁颜色为写锁
                if (lk.getLockType() == LockType.WORKINGLOCK) {
                    return colorSharedWriteLock;
                }
            }
            return colorSharedReadLock;
        }
        return colorWhite;
    }

    private Color generateRandomColor() {
        Color mix = new Color(255, 255, 255);
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);

        // mix the color
        if (mix != null) {
            red = (red + mix.getRed()) / 2;
            green = (green + mix.getGreen()) / 2;
            blue = (blue + mix.getBlue()) / 2;
        }
        Color color = new Color(red, green, blue);
        return color;
    }

    private Color mixColorWithColor(Color targetColor, Color mixedcolor) {
        int red = (targetColor.getRed() + mixedcolor.getRed()) / 2;
        int green = (targetColor.getGreen() + mixedcolor.getGreen()) / 2;
        int blue = (targetColor.getBlue() + mixedcolor.getBlue()) / 2;
        return new Color(red, green, blue);
    }


}
