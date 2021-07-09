package sse.tongji.coidea.presenter;

import com.google.common.collect.Range;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import dev.mtage.eyjaot.client.OtClient;
import dev.mtage.eyjaot.client.entity.ClientCoFile;
import dev.mtage.eyjaot.client.inter.presenter.GeneralLocalFilePresenter;
import dev.mtage.eyjaot.client.inter.util.MyLogger;
import dev.mtage.eyjaot.core.CoUser;
import dev.mtage.eyjaot.core.action.file.FileContentDeleteAction;
import dev.mtage.eyjaot.core.action.file.FileContentInsertAction;
import dev.mtage.eyjaot.core.dal.DalPolicySettings;
import dev.mtage.eyjaot.core.util.EditOperationSourceEnum;
import dev.mtage.eyjaot.core.util.RangeSetUtil;
import lombok.Setter;
import sse.tongji.coidea.dal.ASTCoreImpl;
import sse.tongji.coidea.listener.MyAllKeyListener;
import sse.tongji.coidea.listener.MyCaretListener;
import sse.tongji.coidea.listener.MyDocumentListener;
import sse.tongji.coidea.util.CoIDEAFilePathUtil;
import sse.tongji.coidea.view.DALAwarenessPrinter;
import sse.tongji.dal.astoperation.ASTCoreObject;
import sse.tongji.dal.core.DALCore;
import sse.tongji.dal.editingoperation.EditingOperation;
import sse.tongji.dal.locksystem.BasicRegionList;
import sse.tongji.dal.locksystem.CheckPermission;
import sse.tongji.dal.userinfo.DalUserGroup;
import sse.tongji.dal.editingoperation.OperationType;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


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
    private MyAllKeyListener myAllKeyListener;
    private MyCaretListener myCaretListener;
    @Setter
    private LocalRepositoryPresenter localRepositoryPresenter;

    @Setter
    private ClientCoFile otClientCoFile;
    private DALAwarenessPrinter dalAwarenessPrinter;

    private OtClient otClient;

    public LocalFilePresenter(Project project, VirtualFile virtualFile, OtClient otClient) {
        this.project = project;
        this.virtualFile = virtualFile;
        this.otClient = otClient;
        this.myDocumentListener = new MyDocumentListener(this);
        FileDocumentManager.getInstance().getDocument(virtualFile).addDocumentListener(this.myDocumentListener);
        this.myAllKeyListener = new MyAllKeyListener(this);
        IdeEventQueue.getInstance().addDispatcher(myAllKeyListener, null);
        this.myCaretListener = new MyCaretListener(this);
        FileEditorManager.getInstance(project).getSelectedTextEditor().getCaretModel().addCaretListener(myCaretListener);
        dalAwarenessPrinter = new DALAwarenessPrinter(project);
        ASTCoreObject.astCore = new ASTCoreImpl(project);

    }

    public boolean tryAcquireSemaphore(long timeOut, TimeUnit timeUnit) throws InterruptedException {
        return this.documentSemaphore.tryAcquire(timeOut, timeUnit);
    }

    public void onLocalEdit(DocumentEvent event) {
        if (event.getOldLength() != 0) {
            log.info("local delete {0} {1}", event.getOffset(), event.getOldFragment().toString());
            otClient.deleteFileContent(otClientCoFile, event.getOffset(), event.getOffset() + event.getOldLength());
        } else if (event.getNewLength() != 0) {
            log.info("local insert {0} {1}", event.getOffset(), event.getOldFragment().toString());
            otClient.insertFileContent(otClientCoFile, event.getOffset(), event.getNewFragment().toString());
        } else {
            log.error("本地文档变化但未识别处理 {0}", event);
        }

        // DALPart
        if (event.getOldLength() != 0) {
            DALCore.doDALUpdateByEditingOperation(buildDALEditingOperation(getLocalUser().getUserName(), OperationType.DELETE, event.getOffset(), event.getOldLength()));
        } else if (event.getNewLength() != 0) {
            DALCore.doDALUpdateByEditingOperation(buildDALEditingOperation(getLocalUser().getUserName(), OperationType.INSERT, event.getOffset(), event.getNewLength()));
        }
        dalAwarenessPrinter.refreshHighlight(getPath());
        log.info("本地文档变化处理完成 尝试释放文档编辑锁");
        releaseSemaphore();
    }

    public void localCaretMove(CaretEvent event) {
        log.purpled("鼠标位置变化 : getCaretModel() : " + event.getEditor().getCaretModel().getOffset());
        //判断当前区域是否可以插入光标
        if (CheckPermission.doCheckPermission(buildDALEditingOperation(getLocalUser().getUserName(), OperationType.SELECT, event.getEditor().getCaretModel().getOffset(), 0))) {
            DALCore.doDALUpdateByEditingOperation(buildDALEditingOperation(getLocalUser().getUserName(),  OperationType.SELECT, event.getEditor().getCaretModel().getOffset(), 0));
            //只有本地permit，才给远端发送光标移动的信息
            otClientCoFile.localCaretMove(event.getEditor().getCaretModel().getOffset());
        } else {
            //TODO 提醒这里不能修改文字 并且拒绝任何的修改操作
            Messages.showInfoMessage("You can't work in this Region", "INFO");
        }
        dalAwarenessPrinter.refreshHighlight(getPath());
    }

    @Override
    public void close() {
        getDocument().removeDocumentListener(myDocumentListener);
        IdeEventQueue.getInstance().removeDispatcher(myAllKeyListener);
//        FileEditorManager.getInstance(project).getSelectedTextEditor().getCaretModel().removeCaretListener(myCaretListener);
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
    public void onCaretMove(int offset, CoUser coUser) {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            //监听到远端光标移动， 说明远端允许编辑此位置（同意后才会发送光标移动消息）
            if (getDalPolicySettings().isDalOpen()) {
                log.info("CoUser用户移动光标到" + offset);
                DALCore.doDALUpdateByEditingOperation(buildDALEditingOperation(coUser.getUserName(), OperationType.SELECT, offset, 0));
                log.info("CoUser PolicySetting : field Depth = {0}, method Depth = {1}", coUser.getPersonalSettings().getDalPolicySettings().getFieldDepth(), coUser.getPersonalSettings().getDalPolicySettings().getMethodDepth());
                dalAwarenessPrinter.refreshHighlight(getPath());
            }
        });
    }

    @Override
    public void onInsert(FileContentInsertAction fileContentInsertAction, EditOperationSourceEnum source, CoUser coUser) {
        this.myDocumentListener.remotePlaying();
        log.info("editing userName:{0} dal:{1}", coUser.getUserName(),
                coUser.getPersonalSettings().getDalPolicySettings());

        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteCommandAction.runWriteCommandAction(project,
                    () -> getDocument().insertString(fileContentInsertAction.getPosition(), fileContentInsertAction.getContent()));
            DALCore.doDALUpdateByEditingOperation(buildDALEditingOperation(coUser.getUserName(), OperationType.INSERT,
                    fileContentInsertAction.getPosition(), fileContentInsertAction.getContent().length()));
            dalAwarenessPrinter.refreshHighlight(getPath());
            this.myDocumentListener.remotePlayingDone();
        });
    }

    @Override
    public void onDelete(FileContentDeleteAction fileContentDeleteAction, EditOperationSourceEnum source, CoUser coUser) {
        this.myDocumentListener.remotePlaying();

        ApplicationManager.getApplication().invokeAndWait(() -> {
            WriteCommandAction.runWriteCommandAction(project, () -> {
                fileContentDeleteAction.getDeleteRangeSet().asRanges().forEach(eachRange -> {
                    getDocument().deleteString(eachRange.lowerEndpoint(), eachRange.upperEndpoint());
                });
                for (Range<Integer> range : fileContentDeleteAction.getDeleteRangeSet().asRanges()) {
                    int start = range.lowerEndpoint();
                    DALCore.doDALUpdateByEditingOperation(buildDALEditingOperation(coUser.getUserName(),OperationType.DELETE, start, RangeSetUtil.lengthOfRange(range)));
                }
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


    private CoUser getLocalUser() {
        return this.otClientCoFile.getLocalUser();
    }

    private DalPolicySettings getDalPolicySettings() {
        return this.otClientCoFile.getLocalUser().getPersonalSettings().getDalPolicySettings();
    }

    private EditingOperation buildDALEditingOperation(String siteName, OperationType operationType, int editingPosition, int editingLength) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String editingFileName = getPath();
        EditingOperation editingOperation = new EditingOperation(siteName, operationType, localDateTime, editingFileName, editingPosition, editingLength);
        return editingOperation;
    }
}
