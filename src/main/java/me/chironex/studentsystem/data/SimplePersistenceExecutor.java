package me.chironex.studentsystem.data;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Consumer;

/**
 * Simple implementation of PersistenceExecutor with reconnect logic.
 */
@RequiredArgsConstructor
public class SimplePersistenceExecutor implements PersistenceExecutor {
    private final ReconnectStrategy reconnectStrategy;

    private Connection connection;

    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY_MS = 1000;

    /**
     * Establishes a database connection using the reconnect strategy.
     */
    public void connect() {
        reconnect();
    }

    private void reconnect() {
        int reconnectAttempts = 0;
        boolean reconnectFailed = true;

        while (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            try {
                this.connection = reconnectStrategy.openConnection();
                reconnectFailed = false;
                break;
            } catch (Exception e) {
                reconnectAttempts++;
                System.err.println("Database connection failed (attempt " + reconnectAttempts + "): " + e.getMessage());
                try {
                    Thread.sleep(RECONNECT_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (reconnectFailed) {
            System.err.println("Could not reconnect to the database after " + MAX_RECONNECT_ATTEMPTS + " attempts. Exiting.");
            System.exit(1);
        }
    }

    private void handleStatementException(SQLException e) {
        System.err.println("SQL Exception: " + e.getMessage());
        reconnect();
    }

    /**
     * Performs an operation using a SQL Statement.
     *
     * @param statementAction the action to perform with the Statement
     */
    @Override
    public void performOperation(Consumer<Statement> statementAction) {
        try (Statement statement = connection.createStatement()) {
            statementAction.accept(statement);
        } catch (SQLException e) {
            System.err.println("Error executing statement: " + e.getMessage());
            handleStatementException(e);
        }
    }

    /**
     * Performs an operation using a PreparedStatement.
     *
     * @param sql the SQL query to prepare
     * @param statementAction the action to perform with the PreparedStatement
     */
    @Override
    public void performPreparedOperation(String sql, Consumer<PreparedStatement> statementAction) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statementAction.accept(statement);
        } catch (SQLException e) {
            System.err.println("Error executing prepared statement: " + e.getMessage());
            handleStatementException(e);
        }
    }

    /**
     * Closes the database connection if it is open.
     */
    @SneakyThrows(SQLException.class)
    public void close() {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}