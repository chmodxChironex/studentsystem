package me.chironex.studentsystem.data;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.function.Consumer;

/**
 * Interface for executing persistence operations on a database.
 */
public interface PersistenceExecutor {

    /**
     * Performs an operation using a SQL Statement.
     *
     * @param statementAction the action to perform with the Statement
     */
    void performOperation(Consumer<Statement> statementAction);

    /**
     * Performs an operation using a PreparedStatement.
     *
     * @param sql the SQL query to prepare
     * @param statementAction the action to perform with the PreparedStatement
     */
    void performPreparedOperation(String sql, Consumer<PreparedStatement> statementAction);

    /**
     * Performs a chain of simple SQL operations.
     *
     * @param statements the SQL statements to execute in order
     */
    @SuppressWarnings("all")
    default void performSimpleOperationsChain(String... statements) {
        performOperation(statement -> {
            for (String sql : statements) {
                try {
                    statement.execute(sql);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}