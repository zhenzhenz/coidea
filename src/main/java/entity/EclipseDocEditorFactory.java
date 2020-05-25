package entity;

import client.DocumentEditor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import dev.mtage.eyjaot.client.inter.EditOperationSourceEnum;
import dev.mtage.eyjaot.client.inter.ILocalFileEditor;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.operation.SimpleDeleteTextOperation;
import dev.mtage.eyjaot.core.operation.SimpleInsertTextOperation;
import dev.mtage.eyjaot.core.util.RangeSetUtil;
import org.apache.commons.lang3.StringUtils;

import util.FilePathUtil;
import util.MyLogger;
import client.CollaborationService;
import client.DocumentEditor;

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("UnstableApiUsage")
public class EclipseDocEditorFactory {
    public static ILocalFileEditor getLocalDocEditor(Project _project, Document _document, String _path, String _name) {
        return new ILocalFileEditor() {

            public Project project;
            public Document document;
            public String path;

            private MyLogger log = MyLogger.getLogger(ILocalFileEditor.class);

            @Override
            public String getPath() {
                return _path;
            }

            @Override
            public String getName() {

                return _name;
            }

            @Override
            public void acquireSemaphore() {
                log.info("收到远端操作 尝试获取文档编辑锁 thread " + Thread.currentThread().getId());
                try {
                    CollaborationService.getInstance().getDocumentSemaphore().acquire();
                    log.info("远端操作获取锁成功");
                } catch (InterruptedException e) {
                    log.error("InterruptedException ", e);
                }
            }

            @Override
            public void onInsert(SimpleInsertTextOperation operation, EditOperationSourceEnum source, CoUser coUser) {
                Integer pos = operation.getPosition();
                String text = operation.getContent();
                log.info("File {0} onInsert pos {1} text: {2}", _path, pos, text);
                boolean isLocal = EditOperationSourceEnum.LOCAL == source;
                if (!operation.isNoop()) {
                    DocumentEditor.docMap.get(_path).onInsert(pos, text);
                }
            }

            @Override
            public void onDelete(SimpleDeleteTextOperation operation, EditOperationSourceEnum source, CoUser coUser) {
                log.info("File {0} onDelete {1} ", _path, operation.getDeleteRangeSet().toString());
                boolean isLocal = EditOperationSourceEnum.LOCAL == source;
                if (!operation.isNoop()) {
                    operation.getDeleteRangeSet().asRanges().forEach(eachRange -> {
                        DocumentEditor.docMap.get(_path).onDelete(eachRange.lowerEndpoint(), RangeSetUtil.lengthOfRange(eachRange));
                    });
                }
            }

            @Override
            public void onRewrite(String data) {
                log.info("File {0} onRewrite data: {1}", _path, data);
                if (StringUtils.isNotBlank(data) && !data.equals(_document.getText())) {
                    DocumentEditor.docMap.get(_path).onRewrite(data);
                }
            }

        };
    }
}

