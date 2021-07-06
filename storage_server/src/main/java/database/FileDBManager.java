package database;

import database.connections.ProjectDBConnector;
import lombok.extern.slf4j.Slf4j;
import models.File;
import models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер для работы с пользователями в БД
 */
@Slf4j
public class FileDBManager {
    private final ProjectDBConnector connector;

    public FileDBManager() throws SQLException {
        connector = new ProjectDBConnector();
    }

    /**
     * Получение доступных файлов
     */
    public List<File> getSharedFiles(int userId) {
        ArrayList<File> list = new ArrayList<>();
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("SELECT sf.id AS id, sf.name AS name, sf.path AS path, sf.owner_id AS owner_id FROM ref_shared_file_user ref, shared_file sf WHERE ref.user_id = ? AND sf.id = ref.file_id");
            ps.setInt(1, userId);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                list.add(
                        File.builder()
                        .type(File.SHARED_FILE_TYPE)
                        .name(res.getString("name"))
                        .path(res.getString("path"))
                        .ownerId(res.getInt("owner_id"))
                        .shared(true)
                        .fileId(res.getInt("id"))
                        .build()
                );
            }
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return list;
    }

    /**
     * Расшарить файл
     */
    public Integer shareFile(int fileId, int userId) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("INSERT INTO ref_shared_file_user (file_id, user_id) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, fileId);
            ps.setInt(2, userId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next())
            {
                return rs.getInt(1);
            }
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Добавить файл
     */
    public Integer createFile(File file) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("INSERT INTO shared_file (name, path, owner_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, file.getName());
            ps.setString(2, file.getPath().replace("/", "\\"));
            ps.setInt(3, file.getOwnerId());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next())
            {
                return rs.getInt(1);
            }
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);

        }
        return null;
    }

    /**
     * Получение файлов
     */
    public List<File> getFiles(int userId, String path) {
        ArrayList<File> list = new ArrayList<>();
        try {
            String newPath = path.replace("/", "\\");
            PreparedStatement ps = connector.getConnection().prepareStatement("SELECT id, name, path, owner_id FROM shared_file WHERE owner_id = ? AND path = ?");
            ps.setInt(1, userId);
            ps.setString(2, newPath);
            ResultSet res = ps.executeQuery();

            while (res.next()) {
                list.add(
                        File.builder()
                                .type(File.SHARED_FILE_TYPE)
                                .name(res.getString("name"))
                                .path(res.getString("path"))
                                .ownerId(res.getInt("owner_id"))
                                .shared(true)
                                .fileId(res.getInt("id"))
                                .build()
                );
            }
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

        return list;
    }

    /**
     * Удалить файл
     */
    public int deleteFile(File file) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("DELETE FROM shared_file WHERE name = ? AND path = ?");
            ps.setString(1, file.getName());
            ps.setString(2, file.getPath().replace("/", "\\"));

            return ps.executeUpdate();
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Удалить директорию
     */
    public int deleteFile(String path) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("DELETE FROM shared_file WHERE path = ?");
            ps.setString(1, path.replace("/", "\\"));

            return ps.executeUpdate();
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Удалить шаренные файлы
     */
    public int deleteSharedFile(int fileId) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("DELETE FROM ref_shared_file_user WHERE id = ?");
            ps.setInt(1, fileId);

            return ps.executeUpdate();
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Удалить шаренные файлы
     */
    public int updateFile(int fileId, String newName) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("UPDATE shared_file SET name = ? WHERE id = ?");
            ps.setString(1, newName);
            ps.setInt(2, fileId);

            return ps.executeUpdate();
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            return 0;
        }
    }
}
