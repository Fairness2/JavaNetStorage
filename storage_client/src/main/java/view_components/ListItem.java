package view_components;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.File;
import store.ApplicationStore;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ListItem extends ListCell<File> {
    @Override
    public void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            Path path = Paths.get(ApplicationStore.ROOT_PATH + (item.getType().equals(File.DIR_TYPE)
                    ? item.isShared() ? "icons/folder-account.png" : "icons/folder.png"
                    : item.isShared() ? "icons/file-account.png" : "icons/file.png"));
            path.toAbsolutePath().toUri();
            Image fxImage = new Image(path.toAbsolutePath().toUri().toString());
            ImageView imageView = new ImageView(fxImage);
            setGraphic(imageView);
            setText(item.getName());
        }
    }
}
