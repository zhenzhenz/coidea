package listeners;

import client.CollaborationService;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyVirtualFileListener implements VirtualFileListener {

    public static AtomicBoolean isResourceListening = new AtomicBoolean(true);

    @Override
    public void fileCreated(@NotNull VirtualFileEvent event){

        if (!isResourceListening.get())
        {
            System.out.println("isResourceListening false");
            return;
        }

        if(isResourceListening.get()){
            System.out.println("fileCreated");
            System.out.println(event.getFile().getPath());
            System.out.println(isResourceListening.toString());
            CollaborationService.getInstance().localCreateFile(new File(event.getFile().getPath()));
        }


    }

    @Override
    public void fileDeleted(@NotNull VirtualFileEvent event) {

        if (!isResourceListening.get())
        {
            System.out.println("isResourceListening false");
            return;
        }

        if(isResourceListening.get()){
            System.out.println("fileDeleted");
            System.out.println(event.getFile().getPath());
            System.out.println(isResourceListening.toString());
            CollaborationService.getInstance().localDeleteFile(new File(event.getFile().getPath()));
        }


    }

    @Override
    public void contentsChanged(@NotNull VirtualFileEvent event){

        if (!isResourceListening.get())
        {
            return;
        }

        System.out.println("contentsChanged");

    }
}

