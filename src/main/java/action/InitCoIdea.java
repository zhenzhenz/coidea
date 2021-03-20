package action;

import client.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.MessageBus;
import config.ApiConfig;
import dal.DALPolicySettingData;
import dev.mtage.eyjaot.client.OtClient;
import entity.EclipseDocEditorFactory;
import entity.Repository;
import listeners.*;
import org.jetbrains.annotations.NotNull;
import sse.tongji.coidea.util.CoIDEAFilePathUtil;
import util.MyLogger;


public class InitCoIdea extends AnAction {
    private MyLogger log = MyLogger.getLogger(InitCoIdea.class);
    private Logger logger = Logger.getInstance(InitCoIdea.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // TODO: insert action logic here

        OtClient otClient;

        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null){
            return;
        }
        log.info("当前Project basePath={0} name={1} projectFilePath={2}",
                project.getBasePath(), project.getName(), project.getProjectFilePath());

        FileClient.project = project;
        NotificationClient.project = project;
//        ZipService.project = project
        HightlightClient.project = project;


        String userID;
        userID = Messages.showInputDialog(
                project,
                "What is your name?",
                "Input your name",
                Messages.getQuestionIcon()
        );
        System.out.println(userID);
        DocumentEditor.username = userID;

        Pair<String, Boolean> pair =  Messages.showInputDialogWithCheckBox(
                "What the repo-Id?",
                "Input the repo-Id",
                "Is New repo",
                false,
                true,
                Messages.getQuestionIcon(),
                "",
                null
        );
//        FileChooserDescriptor descriptor = new FileChooserDescriptor(true,false,false,false,false,false);
//        VirtualFile file = FileChooser.chooseFile(descriptor,project,null);
//        boolean isIgnoreFile = file.getName().endsWith(".coideaignore");
//        if(isIgnoreFile) {
//            System.out.println("isIgnoreFile");
////            ZipUtil.initIgnoreList(file)
//        }


        Repository repository = new Repository(userID, pair.first, pair.second, ApiConfig.DEFAULT_SERVER_ADDR, false, new DALPolicySettingData());

        logger.info("Logger Test!!");

        log.info("初始化Repository信息: {0}", repository.toString());

//        VirtualFileManager.getInstance().addVirtualFileListener(new MyVirtualFileListener());

        CollaborationService.createInstance(project, repository);

        ProjectManager.getInstance().addProjectManagerListener(project, new MyProjectManagerListener());
        EditorFactory.getInstance().addEditorFactoryListener(new MyEditorFactoryListener(),project);

        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                file.setDetectedLineSeparator("\n");
                Project project = source.getProject();
                Document document = FileDocumentManager.getInstance().getDocument(file);
                log.info("file opened path={0} getPath={1} modifiedPath={2}", file.getPath(),
                        CoIDEAFilePathUtil.getProjectRelativePath(file.getPath(), project));
                if (document != null){
                    String path = FileClient.GetPath(file);
                    DocumentEditor docEditor = new DocumentEditor(project, document, project.getName()+ "/" + path);

//                    DocumentEditor.remoteCaretOffset = 6;
//                    docEditor.highlightLine("user");

                    MyDocumentListener documentListener = new MyDocumentListener(docEditor, "/" + project.getName()+ "/" + path);
                    document.addDocumentListener(documentListener);
                    docEditor.myDocumentListener = documentListener;

                    MyCaretListener caretListener = new MyCaretListener(docEditor, "/" + project.getName()+ "/" + path);
                    CaretModel caret = source.getSelectedTextEditor().getCaretModel();
                    caret.addCaretListener(caretListener);

                    MyTypedActionHandler myTypedActionHandler = new MyTypedActionHandler();
                    TypedActionHandler oldHandler = TypedAction.getInstance().setupRawHandler(myTypedActionHandler);
                    myTypedActionHandler.setOldHandler(oldHandler);

                    CollaborationService.getInstance().openFile(EclipseDocEditorFactory.getLocalDocEditor(project, document,project.getName()+ "/" + path, file.getName()));

                }
                System.out.println("source: " + source + "; open file: " + file);

            }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
//                JsEngine.invoke("deleteDoc",FileClient.GetPath(file))
//                ApiClient.send(Gson().toJson(CloseFileAction(FileClient.GetPath(file))))
                Project project = source.getProject();
                Document document = FileDocumentManager.getInstance().getDocument(file);
                String path = FileClient.GetPath(file);
                System.out.println("source: " + source + "; close file: " + file);
                if (CollaborationService.getStatus() == CoServiceStatusEnum.CONNECTED ||
                        CollaborationService.getStatus() == CoServiceStatusEnum.INITED) {
                    CollaborationService.getInstance().closeFile(EclipseDocEditorFactory.getLocalDocEditor(project, document,project.getName()+ "/" + path, file.getName() ));
                }
            }
        });


    }
}