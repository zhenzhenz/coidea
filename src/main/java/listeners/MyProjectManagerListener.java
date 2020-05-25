package listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;

public class MyProjectManagerListener implements ProjectManagerListener {

    @Override
    public void projectClosed(Project event){
//        ApiClient.disconnect()
        System.out.println("projectClosed");

    }

}
