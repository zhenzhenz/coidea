package sse.tongji.coidea.presenter;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import dev.mtage.eyjaot.client.entity.ClientCoFile;
import dev.mtage.eyjaot.client.inter.EditOperationSourceEnum;
import dev.mtage.eyjaot.client.inter.presenter.GeneralLocalFilePresenter;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.operation.SimpleDeleteTextOperation;
import dev.mtage.eyjaot.core.operation.SimpleInsertTextOperation;
import lombok.Setter;
import sse.tongji.coidea.listener.MyDocumentListener;
import sse.tongji.coidea.util.CoIDEAFilePathUtil;


/**
 * @author mtage
 * @since 2021/3/17 19:14
 */
@SuppressWarnings("UnstableApiUsage")
public class LocalFilePresenter extends GeneralLocalFilePresenter {
    private final MyLogger log = MyLogger.getLogger(LocalFilePresenter.class);

    private Project project;
    private VirtualFile virtualFile;

    private MyDocumentListener myDocumentListener;
    @Setter
    private LocalRepositoryPresenter localRepositoryPresenter;

    @Setter
    private ClientCoFile otClientCoFile;

    public LocalFilePresenter(Project project, VirtualFile virtualFile) {
        this.project = project;
        this.virtualFile = virtualFile;
        this.myDocumentListener = new MyDocumentListener(this);
        FileDocumentManager.getInstance().getDocument(virtualFile).addDocumentListener(this.myDocumentListener);
    }

    public void onLocalEdit(DocumentEvent event) {
        if (event.getOldLength() != 0) {
            log.info("local delete {0} {1}", event.getOffset(), event.getOldFragment().toString());
            otClientCoFile.localDelete(event.getOffset(), event.getOffset() + event.getOldLength());
        }
        if (event.getNewLength() != 0) {
            log.info("local insert {0} {1}", event.getOffset(), event.getOldFragment().toString());
            otClientCoFile.localInsert(event.getOffset(), event.getNewFragment().toString());
        }
        log.info("本地文档变化处理完成 尝试释放文档编辑锁");
        releaseSemaphore();
    }

    @Override
    public void close() {
        getDocument().removeDocumentListener(myDocumentListener);
    }

    @Override
    public String getPath() {
        return CoIDEAFilePathUtil.getProjectRelativePath(virtualFile.getPath(), project);
    }

    @Override
    public String getName() {
        return virtualFile.getName();
    }

    @Override
    public void onInsert(SimpleInsertTextOperation operation, EditOperationSourceEnum source, CoUser coUser) {
        this.myDocumentListener.remotePlaying();

        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteCommandAction.runWriteCommandAction(project,
                    () -> getDocument().insertString(operation.getPosition(), operation.getContent()));
            this.myDocumentListener.remotePlayingDone();
        });

    }

    @Override
    public void onDelete(SimpleDeleteTextOperation operation, EditOperationSourceEnum source, CoUser coUser) {
        this.myDocumentListener.remotePlaying();

        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                operation.getDeleteRangeSet().asRanges().forEach(eachRange -> {
                    getDocument().deleteString(eachRange.lowerEndpoint(), eachRange.upperEndpoint());
                });
            });
            this.myDocumentListener.remotePlayingDone();
        });
    }

    @Override
    public void onRewrite(String content) {
        this.myDocumentListener.remotePlaying();

        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                getDocument().setText(content);
            });
            this.myDocumentListener.remotePlayingDone();
        });
    }

    private Document getDocument() {
        return FileDocumentManager.getInstance().getDocument(virtualFile);
    }
}
