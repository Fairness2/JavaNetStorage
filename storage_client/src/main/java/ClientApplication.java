import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import store.ApplicationStore;

public class ClientApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("windows/login.fxml"));
        stage.setTitle("Вход");
        stage.setScene(new Scene(parent));
        stage.show();
        ApplicationStore.hostServices = getHostServices();
    }
}
