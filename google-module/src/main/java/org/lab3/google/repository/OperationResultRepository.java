package org.lab3.google.repository;

import org.lab3.google.model.GoogleOperationResult;
import org.lab3.google.config.EnvConfig;
import org.lab.logger.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OperationResultRepository {
    private static final Logger logger = Logger.getInstance("google-module-db");
    private final Connection connection;

    public OperationResultRepository() throws SQLException {
        String url = EnvConfig.get("DB_URL", "jdbc:postgresql://localhost:5432/google_module");
        String username = EnvConfig.get("DB_USERNAME", "postgres");
        String password = EnvConfig.get("DB_PASSWORD", "postgres");

        this.connection = DriverManager.getConnection(url, username, password);
        createTableIfNotExists();
        logger.info("Database connection established");
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS google_operation_results (
                id BIGSERIAL PRIMARY KEY,
                user_id INTEGER,
                operation VARCHAR(50) NOT NULL,
                target_value TEXT,
                result TEXT,
                error TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE INDEX IF NOT EXISTS idx_user_id ON google_operation_results(user_id);
            CREATE INDEX IF NOT EXISTS idx_operation ON google_operation_results(operation);
            CREATE INDEX IF NOT EXISTS idx_created_at ON google_operation_results(created_at);
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Table 'google_operation_results' created or already exists");
        }
    }

    public void save(GoogleOperationResult result) {
        String sql = "INSERT INTO google_operation_results (user_id, operation, target_value, result, error, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, result.getUserId(), Types.INTEGER);
            pstmt.setString(2, result.getOperation());
            pstmt.setString(3, result.getTargetValue());
            pstmt.setString(4, result.getResult());
            pstmt.setString(5, result.getError());
            pstmt.setTimestamp(6, Timestamp.valueOf(result.getCreatedAt()));

            pstmt.executeUpdate();
            logger.info("Saved operation result: " + result.getOperation() + " for user " + result.getUserId());
        } catch (SQLException e) {
            logger.error("Failed to save operation result: " + e.getMessage());
        }
    }

    public List<GoogleOperationResult> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM google_operation_results WHERE user_id = ? ORDER BY created_at DESC";
        List<GoogleOperationResult> results = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToObject(rs));
            }
        }

        return results;
    }

    public List<GoogleOperationResult> findByOperation(String operation) throws SQLException {
        String sql = "SELECT * FROM google_operation_results WHERE operation = ? ORDER BY created_at DESC";
        List<GoogleOperationResult> results = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, operation);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(mapResultSetToObject(rs));
            }
        }

        return results;
    }

    private GoogleOperationResult mapResultSetToObject(ResultSet rs) throws SQLException {
        GoogleOperationResult result = new GoogleOperationResult();
        result.setId(rs.getLong("id"));
        result.setUserId(rs.getInt("user_id"));
        result.setOperation(rs.getString("operation"));
        result.setTargetValue(rs.getString("target_value"));
        result.setResult(rs.getString("result"));
        result.setError(rs.getString("error"));
        result.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return result;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection: " + e.getMessage());
        }
    }
}
