package controllers;

import callbacks.Callback;
import file_tools.FileDirector;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import models.*;
import network.NetConnector;
import store.ApplicationStore;
import tasks.DownloadFile;
import tasks.UploadFile;
import view_components.ListItem;

@Slf4j
public class ClientController implements Initializable {
    public ListView<File> clientListView;
    public ListView<File> serverListView;
    private NetConnector network;
    private FileDirector fileDirector;
    public ProgressBar progressBar;
    public Label progressLabel;
    private boolean isLoad = false;
    private String serverPath;
    private String clientPath;
    private String rootClientPath;
    public Button sharedButton;
    private boolean sharedView = false;

    public MenuBar serverMenuBar;
    public Button serverBackButton;

    private WatchKey currentKey;


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
                if (clientSignal.getMessage() != null) {
                    Platform.runLater(() -> showNotify(clientSignal.getMessage(), AlertType.CONFIRMATION));
                }
            }
            else {
                Platform.runLater(() -> showNotify(clientSignal.getMessage(), AlertType.ERROR));
            }
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serverListView.setCellFactory((ListView<File> param) -> new ListItem());
        clientListView.setCellFactory((ListView<File> param) -> new ListItem());

        ApplicationStore.callback = getCallback();
        network = NetConnector.getInstance();
        fileDirector = new FileDirector("");
        fileDirector.setWatchKeyConsumer((WatchEvent<?> event) -> {
            Platform.runLater(() -> refreshFileList(null));
        });

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

    public void synchroniseShared(ActionEvent event) {
        SynchronizeSharedFilesRequest request = SynchronizeSharedFilesRequest.builder()
                .user(ApplicationStore.user)
                .build();
        network.sendSignal(request);
    }

    public void selectDirectory(ActionEvent event) {
        java.io.File selectedDirectory;
        do {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            selectedDirectory = directoryChooser.showDialog(null);
        } while (selectedDirectory == null);
        clientPath = rootClientPath = selectedDirectory.getAbsolutePath();
        fileDirector.setRootPath(clientPath + "/");

        currentKey = fileDirector.registerWatcher("");

        this.refreshFileList(null);
    }



    public void refreshFileList(ActionEvent event) {
        if (fileDirector == null) {
            showNotify("???????????? ?????????????????? ??????????????????", AlertType.WARNING);
            return;
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
            showNotify("?????????????? ???????????????? ?????? ????????", AlertType.INFORMATION);
            return;
        }

        ObservableList<File> oList = clientListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("???????????????? ????????", AlertType.WARNING);
            return;
        }

        File selectedFile = oList.get(0);
        if (fileDirector.isDirectory(selectedFile.getName())) {
            showNotify("???????????? ?????????????????? ??????????", AlertType.WARNING);//TODO
        }
        else {
            if (fileDirector.fileExists(selectedFile.getName())) {
                UploadFile task = new UploadFile(selectedFile, serverPath);
                taskStart(task);
            }
            else {
                showNotify("???????? ???? ????????????????????", AlertType.ERROR);
            }
        }

    }

    public void downloadFile(ActionEvent event) {
        if (isLoad) {
            showNotify("?????????????? ???????????????? ?????? ????????", AlertType.INFORMATION);
            return;
        }

        ObservableList<File> oList = serverListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("???????????????? ????????", AlertType.WARNING);
            return;
        }

        File selectedFile = oList.get(0);
        if (fileDirector.isDirectory(selectedFile.getName())) {
            showNotify("???????????? ?????????????? ??????????", AlertType.WARNING); //TODO
        }
        else {
            DownloadFile task = new DownloadFile(selectedFile, clientPath);
            taskStart(task);
        }

    }

    private void taskStart(Task<File> task) {
        progressBar.setVisible(true);
        //progressBar.setProgress(0);
        progressLabel.setVisible(true);
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());
        progressLabel.textProperty().unbind();
        progressLabel.textProperty().bind(task.messageProperty());

        // When completed tasks
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (WorkerStateEvent t) -> {
            refreshFileList(null);
            if (!this.sharedView) {
                synchronise(null);
            }
            else {
                synchroniseShared(null);
            }
            progressLabel.textProperty().unbind();
            progressLabel.setVisible(false);
            progressBar.setVisible(false);
            isLoad = false;
            showNotify("???????? ????????????????????", AlertType.CONFIRMATION);
        });

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, (WorkerStateEvent t) -> {
            progressLabel.textProperty().unbind();
            progressLabel.setVisible(false);
            progressBar.setVisible(false);
            isLoad = false;
            showNotify("?????????????????? ????????????", AlertType.ERROR);
        });

        isLoad = true;
        new Thread(task).start();
    }

    public void onClickedClientList(MouseEvent click) {
        if (click.getClickCount() == 2) {
            ObservableList<File> oList = clientListView.getSelectionModel().getSelectedItems();
            if (oList.size() == 0) {
                showNotify("???????????????? ????????", AlertType.WARNING);
                return;
            }

            File selectedFile = oList.get(0);
            if (selectedFile.getType().equals(File.DIR_TYPE)) {
                clientPath += "/" + selectedFile.getName();

                fileDirector.setRootPath(clientPath + "/");

                fileDirector.closeKey(currentKey);
                currentKey = fileDirector.registerWatcher("");

                refreshFileList(null);
            }
            else {
                ApplicationStore.hostServices.showDocument(fileDirector.getAbsolutePath(selectedFile.getName()));
            }
       }
    }

    public void onReturnClientList(ActionEvent event) {
        if (rootClientPath.equals(clientPath)) {
            showNotify("???????????? ?????????????? ??????????", AlertType.WARNING);
            return;
        }
        String newPath = getPreviousPath(clientPath);
        if (newPath == null) {
            showNotify("???????????? ?????????????? ??????????", AlertType.WARNING);
        }
        else {
            clientPath = newPath;
            fileDirector.setRootPath(clientPath + "/");

            fileDirector.closeKey(currentKey);
            currentKey = fileDirector.registerWatcher("");

            refreshFileList(null);
        }
    }

    public void onReturnServerList(ActionEvent event) {
        if (serverPath == null) {
            showNotify("???????????? ?????????????? ??????????", AlertType.WARNING);
            return;
        }
        serverPath = getPreviousPath(serverPath);
        synchronise(null);
    }

    private String getPreviousPath(String path) {
        int lastIndex = clientPath.lastIndexOf("/");

        if (lastIndex != -1) {
            return clientPath.substring(0, lastIndex);
        }

        return null;
    }

    public void onClickedServerList(MouseEvent click) {
        if (click.getClickCount() == 2) {
            ObservableList<File> oList = serverListView.getSelectionModel().getSelectedItems();
            if (oList.size() == 0) {
                showNotify("???????????????? ????????", AlertType.WARNING);
                return;
            }

            File selectedFile = oList.get(0);
            if (selectedFile.getType().equals(File.DIR_TYPE)) {
                serverPath =  selectedFile.getPath() + "/" + selectedFile.getName();
                synchronise(null);
            }
            else {
                showNotify("?????????????????? ????????, ?????????? ?????????????? ??????", AlertType.INFORMATION);
            }
        }
    }

    public void createClientDirectory(ActionEvent event) {
        String dirName = getValueFromDialog("?????????????? ????????????????????", "????????????????");
        if (dirName != null && dirName.length() != 0) {
            if (fileDirector.fileExists(dirName)) {
                showNotify("???????????????????? ????????????????????", AlertType.WARNING);
            }
            else {
                if (fileDirector.createDirectory(dirName)) {
                    refreshFileList(null);
                    showNotify("???????????????????? ??????????????", AlertType.CONFIRMATION);
                }
                else {
                    showNotify("?????????????? ???????????????????? ???? ??????????????", AlertType.ERROR);
                }
            }
        }
        else {
            showNotify("?????????????? ???????????????? ????????????????????", AlertType.WARNING);
        }
    }

    public void createClientFile(ActionEvent event) {
        String dirName = getValueFromDialog("?????????????? ????????", "????????????????");
        if (dirName != null && dirName.length() != 0) {
            dirName = dirName + ".txt";
            if (fileDirector.fileExists(dirName)) {
                showNotify("???????? ????????????????????", AlertType.WARNING);
            }
            else {
                if (fileDirector.createFile(dirName)) {
                    refreshFileList(null);
                    showNotify("???????? ????????????", AlertType.CONFIRMATION);
                }
                else {
                    showNotify("?????????????? ???????? ???? ??????????????", AlertType.ERROR);
                }
            }
        }
        else {
            showNotify("?????????????? ???????????????? ??????????", AlertType.WARNING);
        }
    }

    public void updateClientFile(ActionEvent event) {
        ObservableList<File> oList = clientListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("???????????????? ????????", AlertType.WARNING);
            return;
        }
        File selectedFile = oList.get(0);
        String dirName = getValueFromDialog("?????????????????????????? ????????", "?????????? ????????????????");
        if (dirName != null && dirName.length() != 0) {
            if (fileDirector.fileExists(dirName)) {
                showNotify("???????? ?? ?????????? ?????????????????? ?????? ????????????????????", AlertType.WARNING);
            }
            else {
                if (fileDirector.updateFileName(selectedFile.getName(), dirName)) {
                    refreshFileList(null);
                    showNotify("???????? ????????????????????????", AlertType.CONFIRMATION);
                }
                else {
                    showNotify("?????????????????????????? ???????? ???? ??????????????", AlertType.ERROR);
                }
            }
        }
        else {
            showNotify("?????????????? ???????????????? ????????????????????", AlertType.WARNING);
        }
    }

    private String getValueFromDialog(String title, String filedName) {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setTitle(title);
        textInputDialog.getDialogPane().setContentText(filedName);
        Optional<String> result = textInputDialog.showAndWait();
        return textInputDialog.getEditor().getText();
    }

    public void deleteClientFile(ActionEvent event) {
        ObservableList<File> oList = clientListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("???????????????? ????????", AlertType.WARNING);
            return;
        }
        File selectedFile = oList.get(0);
        if (fileDirector.deleteFile(selectedFile.getName())) {
            refreshFileList(null);
            showNotify("???????? ????????????", AlertType.CONFIRMATION);
        }
        else {
            showNotify("?????????????? ???????? ???? ??????????????", AlertType.ERROR);
        }
    }

    public void createServerDirectory(ActionEvent event) {
        String dirName = getValueFromDialog("?????????????? ????????????????????", "????????????????");
        if (dirName != null && dirName.length() != 0) {
            FileCreate fileCreate = FileCreate.builder()
                    .file(File.builder()
                        .path(serverPath)
                        .type(File.DIR_TYPE)
                        .name(dirName)
                        .ownerId(ApplicationStore.user.getId())
                        .build())
                    .user(ApplicationStore.user)
                    .build();
            network.sendSignal(fileCreate);
        }
        else {
            showNotify("?????????????? ???????????????? ????????????????????", AlertType.WARNING);
        }
    }

    public void createServerFile(ActionEvent event) {
        String dirName = getValueFromDialog("?????????????? ????????", "????????????????");
        if (dirName != null && dirName.length() != 0) {
            dirName = dirName + ".txt";
            FileCreate fileCreate = FileCreate.builder()
                    .file(File.builder()
                            .path(serverPath)
                            .type(File.FILE_TYPE)
                            .name(dirName)
                            .ownerId(ApplicationStore.user.getId())
                            .build())
                    .user(ApplicationStore.user)
                    .build();
            network.sendSignal(fileCreate);
        }
        else {
            showNotify("?????????????? ???????????????? ??????????", AlertType.WARNING);
        }
    }

    public void updateServerFile(ActionEvent event) {
        ObservableList<File> oList = serverListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("???????????????? ????????", AlertType.WARNING);
            return;
        }
        File selectedFile = oList.get(0);
        String dirName = getValueFromDialog("?????????????????????????? ????????", "?????????? ????????????????");
        if (dirName != null && dirName.length() != 0) {
            FileUpdate fileUpdate = FileUpdate.builder()
                    .file(selectedFile)
                    .newName(dirName)
                    .user(ApplicationStore.user)
                    .build();
            network.sendSignal(fileUpdate);
        }
        else {
            showNotify("?????????????? ???????????????? ????????????????????", AlertType.WARNING);
        }
    }

    public void deleteServerFile(ActionEvent event) {
        ObservableList<File> oList = serverListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("???????????????? ????????", AlertType.WARNING);
            return;
        }
        File selectedFile = oList.get(0);
        FileDelete fileDelete = FileDelete.builder()
                .file(selectedFile)
                .user(ApplicationStore.user)
                .build();
        network.sendSignal(fileDelete);
    }

    public void switchView(ActionEvent event) {
        this.sharedView = !this.sharedView;
        this.serverMenuBar.setVisible(!this.sharedView);
        this.serverBackButton.setVisible(!this.sharedView);
        this.sharedButton.setText(this.sharedView ? "??????" : "??????????????????");
        if (this.sharedView) {
            this.synchroniseShared(null);
        }
        else {
            this.synchronise(null);
        }
    }

    public void shareFile(ActionEvent event) {
        ObservableList<File> oList = serverListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("???????????????? ????????", AlertType.WARNING);
            return;
        }
        File selectedFile = oList.get(0);
        if (selectedFile.getType().equals(File.DIR_TYPE)) {
            showNotify("???? ???? ???????????? ?????????????????? ??????????????????????", AlertType.WARNING);
            return;
        }

        String dirName = getValueFromDialog("?????????????????? ???????????? ?? ??????????????????????????", "?????????? ????????????????????????");
        if (dirName != null && dirName.length() != 0) {
            ShareFile shareFile = ShareFile.builder()
                    .file(selectedFile)
                    .user(ApplicationStore.user)
                    .userLogin(dirName)
                    .build();
            network.sendSignal(shareFile);
        }
        else {
            showNotify("?????????????? ?????????? ???????????????????????? ?? ???????????? ???????????? ??????????????????", AlertType.WARNING);
        }
    }

    public void deleteSharedFile(ActionEvent event) {
        ObservableList<File> oList = serverListView.getSelectionModel().getSelectedItems();
        if (oList.size() == 0) {
            showNotify("???????????????? ????????", AlertType.WARNING);
            return;
        }
        File selectedFile = oList.get(0);
        if (selectedFile.getType().equals(File.DIR_TYPE)) {
            showNotify("???? ???? ???????????? ?????????????? ??????????????????????", AlertType.WARNING);
            return;
        }

        DeleteSharedFile deleteSharedFile = DeleteSharedFile.builder()
                .file(selectedFile)
                .user(ApplicationStore.user)
                .build();

        network.sendSignal(deleteSharedFile);
    }

    public void logout(ActionEvent event) {
        Stage currentStage = (Stage)((Node) event.getSource()).getScene().getWindow();
        ApplicationStore.user = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/windows/login.fxml"));
            Stage stage = new Stage();
            stage.setTitle("?????????????? ??????????????????");
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
            currentStage.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
