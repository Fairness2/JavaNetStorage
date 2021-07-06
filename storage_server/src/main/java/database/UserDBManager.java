package database;

import database.connections.ProjectDBConnector;
import lombok.extern.slf4j.Slf4j;
import models.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Менеджер для работы с пользователями в БД
 */
@Slf4j
public class UserDBManager {
    private final ProjectDBConnector connector;

    public UserDBManager() throws SQLException {
        connector = new ProjectDBConnector();
    }

    /**
     * Получение пользователя по кредам
     */
    public User findByCredentials(String login, String password) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("SELECT * FROM user WHERE login = ? AND password = ?");
            ps.setString(1, login);
            ps.setString(2, password);
            ResultSet res = ps.executeQuery();
            User user = null;
            if (res.next()) {
                user = new User(res.getString("login"), res.getString("name"), res.getInt("id"));
            }
            return user;
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получение пользователя по id
     */
    public User findById(int id) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("SELECT * FROM user WHERE id = ?");
            ps.setInt(1, id);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                return new User(res.getString("login"), res.getString("name"), res.getInt("id"));
            }
            return null;
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * Получение пользователя по логину
     */
    public User findByLogin(String login) {
        try {
            PreparedStatement ps = connector.getConnection().prepareStatement("SELECT * FROM user WHERE login = ?");
            ps.setString(1, login);
            ResultSet res = ps.executeQuery();
            if (res.next()) {
                return new User(res.getString("login"), res.getString("name"), res.getInt("id"));
            }
            return null;
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
