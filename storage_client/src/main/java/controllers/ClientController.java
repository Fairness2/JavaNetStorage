package controllers;

import callbacks.Callback;
import file_tools.FileDirector;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javafx.stage.DirectoryChooser;
import models.*;
import network.NetConnector;
import store.ApplicationStore;
import view_components.ListItem;

public class ClientController implements Initializable {
    public ListView<File> clientListView;
    public ListView<File> serverListView;
    private NetConnector network;
    private FileDirector fileDirector;

    private Callback getCallback() {
        return signal -> {
            StandardAnswer clientSignal = (StandardAnswer) signal;
            if (clientSignal.isSuccess()) {
                TransmittedSignal param = clientSignal.getParam();
                if (param instanceof SynchronizeResponse) {
                    Platform.runLater(() -> {
                        serverListView.getItems().clear();
                        ((SynchronizeResponse) param).getFileList()
                                .forEach(file -> serverListView.getItems().add(file));
                    });
                }
            }
            else {
                //TODO
            }
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        serverListView.setCellFactory((ListView<File> param) -> new ListItem());
        clientListView.setCellFactory((ListView<File> param) -> new ListItem());

        ApplicationStore.callback = getCallback();
        network = NetConnector.getInstance();
        this.synchronise(null);
        this.selectDirectory(null);
    }

    public void synchronise(ActionEvent event) {
        SynchronizeRequest request = SynchronizeRequest.builder()
                .user(ApplicationStore.user)
                .path(null)
                .build();
        network.sendSignal(request);
    }

    public void selectDirectory(ActionEvent event) {
        java.io.File selectedDirectory;
        do {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selectedDirectory = directoryChooser.showDialog(null);
        } while (selectedDirectory == null);
        if (fileDirector == null) {
            fileDirector = new FileDirector(selectedDirectory.getAbsolutePath() + "/");
        }
        else {
            fileDirector.setRootPath(selectedDirectory.getAbsolutePath() + "/");
        }
        this.refreshFileList(null);
    }

    public void refreshFileList(ActionEvent event) {
        if (fileDirector == null) {
            return; //TODO
        }
        fileDirector.getFilesInDirectory("")
                .forEach(file -> clientListView.getItems().add(file));
    }
}
