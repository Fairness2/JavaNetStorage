package database.connections;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для пдключения к бд
 */
@Slf4j
public class ProjectDBConnector implements DBConnector {
    private static final String url = "jdbc:mysql://localhost:3306/chat";
    private static final String dbUser = "root";
    private static final String dbPassword = "1234567890";

    private Connection connection;

    public ProjectDBConnector() throws SQLException {
        connection = DriverManager.getConnection(url, dbUser, dbPassword);
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            connection.close();
        }
        catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

    }


}
