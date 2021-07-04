package helpers;

import database.FileDBManager;
import lombok.extern.slf4j.Slf4j;
import models.File;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class Helpers {
    public static List<File> mergeSharedFile(List<File> fileList, String directory, int userId) {
        try {
            FileDBManager fileDBManager = new FileDBManager();
            List<File> sharedList = fileDBManager.getFiles(userId, directory);

            fileList.forEach((item) -> {
                item.setOwnerId(userId);
                File sharedFile = sharedList.stream()
                        .filter((shared) -> shared.getName().equals(item.getName()))
                        .findFirst()
                        .orElse(null);
                if (sharedFile != null) {
                    item.setShared(true);
                    item.setFileId(sharedFile.getFileId());
                }
                item.setShared(sharedList.stream()
                        .filter((shared) -> shared.getName().equals(item.getName())).count() == 1);
            });

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return fileList;
    }
}
