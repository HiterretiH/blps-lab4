package org.lab3.google.repository;

import org.lab3.google.config.EnvConfig;
import org.lab3.google.model.GoogleOperationResult;
import org.lab.logger.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperationResultRepository {
    private static final String DB_URL_KEY = "DB_URL";
    private static final String DB_USERNAME_KEY = "DB_USERNAME";
    private static final String DB_PASSWORD_KEY = "DB_PASSWORD";
    private static final String DEFAULT_DB_URL = "jdbc:postgresql://localhost:5432/google_module";
    private static final String DEFAULT_DB_USERNAME = "postgres";
    private static final String DEFAULT_DB_PASSWORD = "postgres";
    private static final String TABLE_NAME = "google_operation_results";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String OPERATION_COLUMN = "operation";
    private static final String CREATED_AT_COLUMN = "created_at";

    private static final Logger LOGGER = Logger.getInstance("google-module-db");
    private final Connection connection;

    public OperationResultRepository() throws SQLException {
        String url = EnvConfig.get(DB_URL_KEY, DEFAULT_DB_URL);
        String username = EnvConfig.get(DB_USERNAME_KEY, DEFAULT_DB_USERNAME);
        String password = EnvConfig.get(DB_PASSWORD_KEY, DEFAULT_DB_PASSWORD);

        this.connection = DriverManager.getConnection(url, username, password);
        createTableIfNotExists();
        LOGGER.info("Database connection established");
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = """
        CREATE TABLE IF NOT EXISTS %1$s (
            id BIGSERIAL PRIMARY KEY,
            user_id INTEGER,
            operation VARCHAR(50) NOT NULL,
            target_value TEXT,
            result TEXT,
            error TEXT,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        );
        
        CREATE INDEX IF NOT EXISTS idx_%1$s_user_id ON %1$s(user_id);
        CREATE INDEX IF NOT EXISTS idx_%1$s_operation ON %1$s(operation);
        CREATE INDEX IF NOT EXISTS idx_%1$s_created_at ON %1$s(created_at);
        """.formatted(TABLE_NAME);

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            LOGGER.info("Table '" + TABLE_NAME + "' ensured");
        }
    }

    public void save(GoogleOperationResult operationResult) {
        String sql = "INSERT INTO " + TABLE_NAME + " (user_id, operation, target_value, result, error, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setObject(1, operationResult.getUserId(), Types.INTEGER);
            preparedStatement.setString(2, operationResult.getOperation());
            preparedStatement.setString(3, operationResult.getTargetValue());
            preparedStatement.setString(4, operationResult.getResult());
            preparedStatement.setString(5, operationResult.getError());
            preparedStatement.setTimestamp(6, Timestamp.valueOf(operationResult.getCreatedAt()));

            preparedStatement.executeUpdate();
            LOGGER.info("Saved operation result: " + operationResult.getOperation() + " for user " + operationResult.getUserId());
        } catch (SQLException sqlException) {
            LOGGER.error("Failed to save operation result: " + sqlException.getMessage());
        }
    }

    public List<GoogleOperationResult> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? ORDER BY created_at DESC";
        return executeQueryWithParameter(sql, userId);
    }

    public List<GoogleOperationResult> findByOperation(String operation) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE operation = ? ORDER BY created_at DESC";
        return executeQueryWithParameter(sql, operation);
    }

    private List<GoogleOperationResult> executeQueryWithParameter(String sql, Object parameter) throws SQLException {
        List<GoogleOperationResult> results = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            if (parameter instanceof Integer) {
                preparedStatement.setInt(1, (Integer) parameter);
            } else {
                preparedStatement.setString(1, (String) parameter);
            }

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                results.add(mapResultSetToObject(resultSet));
            }
        }

        return results;
    }

    private GoogleOperationResult mapResultSetToObject(ResultSet resultSet) throws SQLException {
        GoogleOperationResult operationResult = new GoogleOperationResult();
        operationResult.setId(resultSet.getLong("id"));
        operationResult.setUserId(resultSet.getInt(USER_ID_COLUMN));
        operationResult.setOperation(resultSet.getString(OPERATION_COLUMN));
        operationResult.setTargetValue(resultSet.getString("target_value"));
        operationResult.setResult(resultSet.getString("result"));
        operationResult.setError(resultSet.getString("error"));
        operationResult.setCreatedAt(resultSet.getTimestamp(CREATED_AT_COLUMN).toLocalDateTime());
        return operationResult;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed");
            }
        } catch (SQLException sqlException) {
            LOGGER.error("Error closing database connection: " + sqlException.getMessage());
        }
    }
}
