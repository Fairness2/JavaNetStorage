package tasks;

import file_tools.FileDirector;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import models.*;
import network.NetConnector;
import store.ApplicationStore;

import java.util.UUID;

@Slf4j
public class DownloadFile extends Task<File> {
    private final File downloadFile;
    private final FileDirector fileDirector;
    private final String clientPath;
    private final NetConnector localNetwork;


    public DownloadFile(File downloadFile, String clientPath) {
        this.downloadFile = downloadFile;
        this.fileDirector = new FileDirector(clientPath + '/');
        this.clientPath = clientPath;
        localNetwork = NetConnector.getInstance();
        ApplicationStore.fileCallback = (TransmittedSignal message) -> {
            synchronized (this) {
                FilePart currentPart = (FilePart) message;
                FileResponse answer;

                boolean res;
                if (currentPart.getPart() == 1) {
                    fileDirector.deleteFile(currentPart.getName());
                }
                res = fileDirector.setFileDataParts(currentPart.getName(), currentPart.getByteArray());

                answer = FileResponse.builder()
                        .uuid(currentPart.getUuid())
                        .status(res)
                        .build();

                localNetwork.sendSignal(answer);

                if (currentPart.isLast()) {
                    notify();
                }
            }
        };
    }

    @Override
    protected File call() throws Exception {
        this.updateMessage("Загрузка файла " + downloadFile.getName());
        FileRequest fileRequest = FileRequest.builder()
                .file(downloadFile)
                .user(ApplicationStore.user)
                .build();
        localNetwork.sendSignal(fileRequest);

        synchronized (this) {
            wait();
        }
        return downloadFile;
    }
}
