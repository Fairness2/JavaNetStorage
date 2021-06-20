package controllers;

import callbacks.Callback;
import file_tools.FileDirector;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.ProgressBar;
import javafx.stage.DirectoryChooser;
import models.*;
import network.NetConnector;
import store.ApplicationStore;
import tasks.UploadFile;
import view_components.ListItem;

public class ClientController implements Initializable {
    public ListView<File> clientListView;
    public ListView<File> serverListView;
    private NetConnector network;
    private FileDirector fileDirector;
    public ProgressBar progressBar;
    public Label progressLabel;
    private boolean isLoad = false;
    private String serverPath;

    private Callback getCallback() {
        return signal -> {
            StandardAnswer clientSignal = (StandardAnswer) signal;
            if (clientSignal.isSuccess()) {
                TransmittedSignal param = clientSignal.getParam();
                if (param instanceof SynchronizeResponse) {
                    this.serverPath = ((SynchronizeResponse) param).getPath();
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
                .path(serverPath)
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
        clientListView.getItems().clear();
        fileDirector.getFilesInDirectory("")
                .forEach(file -> clientListView.getItems().add(file));
    }

    private void showNotify(String message, AlertType type) {
        Alert alert = new Alert(type);
        //alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void sendFile(ActionEvent event) {
        if (isLoad) {
            showNotify("Процесс загрузки уже идёт", AlertType.INFORMATION);
            return;
        }

        ObservableList<File> oList = clientListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("Выберите файл", AlertType.WARNING);
            return;
        }

        File selectedFile = oList.get(0);
        progressBar.setVisible(true);
        progressBar.setProgress(0);
        progressLabel.setVisible(true);
        //progressLabel.setText(String.format("Отправка: %s", selectedFile.getName()));
        if (fileDirector.isDirectory(selectedFile.getName())) {
            //TODO
        }
        else {
            if (fileDirector.fileExists(selectedFile.getName())) {
                UploadFile task = new UploadFile(selectedFile, serverPath);
                progressBar.progressProperty().unbind();
                progressBar.progressProperty().bind(task.progressProperty());
                progressLabel.textProperty().unbind();
                progressLabel.textProperty().bind(task.messageProperty());

                // When completed tasks
                task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (WorkerStateEvent t) -> {
                    synchronise(null);
                    progressLabel.textProperty().unbind();
                    progressLabel.setVisible(false);
                    progressBar.setVisible(false);
                    isLoad = false;
                    showNotify("Файл скопирован", AlertType.CONFIRMATION);
                });

                task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, (WorkerStateEvent t) -> {
                    progressLabel.textProperty().unbind();
                    progressLabel.setVisible(false);
                    progressBar.setVisible(false);
                    isLoad = false;
                    showNotify("Произошла ошибка", AlertType.ERROR);
                });

                isLoad = true;
                new Thread(task).start();
            }
            else {
                showNotify("Файл не существует", AlertType.ERROR);
            }
        }

    }



}
