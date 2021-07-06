package database.connections;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Интерфейс класса для пдключения к бд
 */
public interface DBConnector extends Closeable {
    /**
     * Создание подключения к БД
     */
    Connection getConnection();

    /**
     * Закгрытие подключения к БД
     */
    void close();
}
