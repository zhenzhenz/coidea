package sse.tongji.coidea.view;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;

import com.intellij.openapi.vfs.LocalFileSystem;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.ranges.Range;
import sse.tongji.coidea.util.CoIDEAFilePathUtil;
import sse.tongji.dal.locksystem.BasicRegion;
import sse.tongji.dal.locksystem.BasicRegionList;
import sse.tongji.dal.locksystem.Lock;
import sse.tongji.dal.locksystem.LockType;
import sse.tongji.dal.userinfo.DalUser;
import sse.tongji.dal.userinfo.DalUserGroup;

import javax.print.Doc;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author xuwenhua
 */
public class DALAwarenessPrinter {
    final MyLogger log = MyLogger.getLogger(DALAwarenessPrinter.class);
    private final Project project;

    private HighlightManager highlightManager;
    private static Map<Editor, List<RangeHighlighter>> highLightList = new HashMap<>();
    private static Map<String, Color> siteNameToColor = new HashMap<>();
    private final Color colorSharedWriteLock = new Color(208, 32, 144);
    private final Color colorSharedReadLock = new Color(181, 230, 29);
    private final Color colorWhite = new Color(255, 255, 255);

    public DALAwarenessPrinter(Project project) {
        this.project = project;
        this.highlightManager = HighlightManager.getInstance(project);
    }

    public void refreshHighlight(String fileName) {
//        System.out.println("RRRRefreshHighlight");
//        System.out.println("BasicRegionList Size: " + BasicRegionList.getBasicRegionList().size());
        clearHighlight();
        addHighlight();
    }

    private void addEditor(Editor editor) {
        if (!highLightList.containsKey(editor)) {
            highLightList.put(editor, new ArrayList<>());
        }
    }

    private List<RangeHighlighter> getHighLightListByEditor(Editor editor) {
        return highLightList.get(editor);
    }

    private void addHighlight() {
        for (BasicRegion basicRegion : BasicRegionList.getBasicRegionList()) {
            Document document = null;
            if (LocalFileSystem.getInstance()
                    .refreshAndFindFileByPath(CoIDEAFilePathUtil
                            .getStandardAbsolutePath(basicRegion.getRegionFileName(), project.getBasePath())) != null) {
                document = FileDocumentManager.getInstance()
                        .getCachedDocument(LocalFileSystem.getInstance()
                                .refreshAndFindFileByPath(CoIDEAFilePathUtil
                                        .getStandardAbsolutePath(basicRegion.getRegionFileName(), project.getBasePath())));
            }
            if (document == null) {
                continue;
            }
            Editor editor = EditorFactory.getInstance().getEditors(document)[0];
            addEditor(editor);
            log.info("BasicRegionName:  " + basicRegion.getRegionId() + "区域的文件名： " + basicRegion.getRegionFileName() + "锁的数量： " + basicRegion.getLockList().size() + "锁定区域:" + basicRegion.getStartOffset() + "-" + basicRegion.getEndOffset());
            if (basicRegion.getStartOffset() >= document.getText().length() || basicRegion.getEndOffset() >= document.getText().length()) {
                continue;
            }
            Color printerColor = getRegionColor(basicRegion);
            highlightManager.addRangeHighlight(editor, basicRegion.getStartOffset(),basicRegion.getEndOffset(), new TextAttributes(null, printerColor, null, EffectType.BOXED, 0), false, getHighLightListByEditor(editor));
        }
    }

    private void clearHighlight() {
        for (Map.Entry<Editor, List<RangeHighlighter>> entry : highLightList.entrySet()) {
            Editor tempEditor = entry.getKey();
            List<RangeHighlighter> tempList = entry.getValue();
            for (RangeHighlighter h : tempList) {
                highlightManager.removeSegmentHighlighter(tempEditor, h);
            }
        }
    }



    private Color getRegionColor(BasicRegion basicRegion) {
        //如果这个区域只有一个锁
        if (basicRegion.getLockList().size() == 1) {
            String userSiteName = basicRegion.getLockList().get(0).getSitename();
            if (!siteNameToColor.containsKey(userSiteName)) {
                siteNameToColor.put(userSiteName, generateColorFromUserName(userSiteName));
            }
            Color regionColor = siteNameToColor.get(userSiteName);
            if (basicRegion.getLockList().get(0).getLockType() == LockType.WORKINGLOCK) {
                return regionColor;
            } else if (basicRegion.getLockList().get(0).getLockType() == LockType.DEPENDENCYLOCK) {
                return mixColorWithColor(regionColor, new Color(240, 248, 255));
            } else if (basicRegion.getLockList().get(0).getLockType() == LockType.AWARENESSLOCK) {
                return mixColorWithColor(mixColorWithColor(regionColor,new Color(255, 255, 255)), new Color(255, 255, 255));
            }
        } else if (basicRegion.getLockList().size() > 1) {

            for (Lock lk : basicRegion.getLockList()) {
                if (lk.getLockType() == LockType.WORKINGLOCK) {
                    return colorSharedWriteLock;
                }
            }
            return colorSharedReadLock;
        }
        return colorWhite;
    }

    public static Color generateColorFromUserName(String userName) {
        int hashCode = userName.hashCode();
        if (hashCode <= 0) {
            hashCode = -hashCode;
        }
        int red = hashCode % 160;
        int green = hashCode % 180;
        int blue = hashCode % 200;
        Color mix = new Color(255, 255, 255);

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
