package client;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class FileClient {

    public static Project project = null;

    public static void CreateFile(String filePath, Boolean isFolder) {
        if(project == null)
            return;
        String path = project.getBasePath() + "/" + filePath;
        File file = new File(path);
        File parent = new File(file.getParent());

        if(!parent.exists()) {
            parent.mkdirs();
        }
        if(isFolder) {
            file.mkdir();
        }
        else {
            try{
                file.createNewFile();
            }catch (Exception e){
                System.out.println("error when CreateFile");
            }

        }
    }

    public static void DeleteFile(String filePath, Boolean isFolder) {
        if(project == null)
            return;

        String path = project.getBasePath() + "/" + filePath;
        File file = new File(path);
        if(isFolder) {
            file.delete();
        }
        else {
            file.delete();
        }
    }

    public static String GetPath(VirtualFile virtualFile){
        if(project == null)
            return "";

        return (virtualFile.getPath().replace(project.getBasePath() + "/",""));
    }

    public static String GetPath(String filePath ) {
        if(project == null)
            return "";

        return (filePath.replace(project.getBasePath() + "/", ""));
    }

}
