package action;

import client.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.MessageBus;
import config.ApiConfig;
import dal.DALPolicySettingData;
import entity.EclipseDocEditorFactory;
import entity.Repository;
import listeners.*;
import org.jetbrains.annotations.NotNull;
import dev.mtage.eyjaot.client.OtClient;
import util.MyLogger;

import java.io.File;


public class InitCoIdea extends AnAction {
    private MyLogger log = MyLogger.getLogger(InitCoIdea.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // TODO: insert action logic here

        OtClient otClient;

        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project == null){
            return;
        }

        FileClient.project = project;
//        NotificationClient.project = project
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

        log.info("初始化Repository信息: {0}", repository.toString());

        VirtualFileManager.getInstance().addVirtualFileListener(new MyVirtualFileListener());

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