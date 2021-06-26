package tasks;

import file_tools.FileDirector;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import models.File;
import models.FilePart;
import models.TransmittedSignal;
import network.NetConnector;
import store.ApplicationStore;

import java.util.UUID;

@Slf4j
public class UploadFile extends Task<File> {
    private final File uploadFile;
    private final FileDirector fileDirector;
    private final String serverPath;
    private final NetConnector localNetwork;


    public UploadFile(File uploadFile, String serverPath) {
        this.uploadFile = uploadFile;
        this.fileDirector = new FileDirector(uploadFile.getPath() + '/');
        this.serverPath = serverPath;
        localNetwork = NetConnector.getInstance();
        ApplicationStore.fileCallback = (TransmittedSignal message) -> {
            synchronized (this) {
                try {
                    notify();
                }
                catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }


        };
    }

    @Override
    protected File call() throws Exception {
        this.updateMessage("Отправка файла " + uploadFile.getName());
        fileDirector.getFileDataParts(uploadFile.getName(), (FilePart part) -> {
            synchronized (this) {
                part.setPath(serverPath);
                part.setUser(ApplicationStore.user);
                part.setUuid(UUID.randomUUID());
                localNetwork.sendSignal(part);
                this.updateProgress(part.getPart(), part.getCountParts());
                if (!part.isLast()) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }

        });

        return uploadFile;
    }
}
