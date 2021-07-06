package controllers;

import callbacks.Callback;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import models.LoginRequest;
import models.StandardAnswer;
import models.User;
import network.NetConnector;
import store.ApplicationStore;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
public class LoginController implements Initializable {
    public TextField login;
    public TextField password;
    public Label errors;
    private NetConnector network;
    private Stage currentStage;

    public Callback getCallback() {
        return answer -> {
            StandardAnswer loginAnswer = (StandardAnswer) answer;
            if (loginAnswer.isSuccess()) {
                Platform.runLater(() -> goNext((User) loginAnswer.getParam()));
            }
            else {
                Platform.runLater(() -> setErrorText(loginAnswer.getMessage()));
            }
        };
    }

    private void setErrorText(String text) {
        errors.setVisible(true);
        errors.setText(text);
    }

    private void goNext(User user) {
        ApplicationStore.user = user;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource("/windows/client.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Сетевое хранилище");
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.show();
            currentStage.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            setErrorText("Ой-ёй, что-т опошло не так");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApplicationStore.callback = getCallback();
        network = NetConnector.getInstance();
        Thread td = new Thread(network);
        td.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(td);
        td.start();
    }

    public void actionLogin(ActionEvent event) {
        currentStage = (Stage)((Node) event.getSource()).getScene().getWindow();
        errors.setVisible(false);
        LoginRequest request = LoginRequest.builder()
                .login(login.getText())
                .password(password.getText())
                .build();
        network.sendSignal(request);
        password.clear();
    }
}
