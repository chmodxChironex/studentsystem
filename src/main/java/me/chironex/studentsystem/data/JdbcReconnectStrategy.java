package me.chironex.studentsystem.data;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * JDBC implementation of the ReconnectStrategy interface.
 * Opens a new database connection using the provided JDBC URL.
 */
@RequiredArgsConstructor
public class JdbcReconnectStrategy implements ReconnectStrategy {
    private final String jdbcUrl;

    /**
     * Opens a new JDBC connection.
     *
     * @return a new Connection instance
     * @throws Exception if a database access error occurs
     */
    @Override
    public Connection openConnection() throws Exception {
        return DriverManager.getConnection(jdbcUrl);
    }
}