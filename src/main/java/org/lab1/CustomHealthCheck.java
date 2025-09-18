package org.lab1;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Component
public class CustomHealthCheck implements HealthIndicator {
    private static final long TIMEOUT_MS = 5000;
    private static final String GOOGLE_REQUESTS_QUEUE = "google.requests";

    private final DataSource dataSource;
    private final RabbitAdmin rabbitAdmin;
    private final JdbcTemplate jdbcTemplate;
    private final RabbitTemplate rabbitTemplate;

    public CustomHealthCheck(DataSource dataSource, RabbitAdmin rabbitAdmin,
                             JdbcTemplate jdbcTemplate, RabbitTemplate rabbitTemplate) {
        this.dataSource = dataSource;
        this.rabbitAdmin = rabbitAdmin;
        this.jdbcTemplate = jdbcTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public Health health() {
        Map<String, Object> systemDetails = new LinkedHashMap<>();
        Map<String, Object> components = new LinkedHashMap<>();
        boolean isHealthy = true;
        StopWatch stopWatch = new StopWatch();

        // PostgreSQL check
        stopWatch.start("PostgreSQL check");
        Health postgresHealth = withTimeout(this::checkPostgresHealth, TIMEOUT_MS);
        stopWatch.stop();
        components.put("postgres", buildComponentDetails(postgresHealth, stopWatch.getLastTaskTimeMillis()));
        if (postgresHealth.getStatus().equals(Health.down().build().getStatus())) {
            isHealthy = false;
        }

        // RabbitMQ check
        stopWatch.start("RabbitMQ check");
        Health rabbitHealth = withTimeout(this::checkRabbitHealth, TIMEOUT_MS);
        stopWatch.stop();
        components.put("rabbitmq", buildComponentDetails(rabbitHealth, stopWatch.getLastTaskTimeMillis()));
        if (rabbitHealth.getStatus().equals(Health.down().build().getStatus())) {
            isHealthy = false;
        }

        // System info
        systemDetails.put("status", isHealthy ? "UP" : "DOWN");
        systemDetails.put("components", components);
        systemDetails.put("duration_ms", stopWatch.getTotalTimeMillis());
        systemDetails.put("version", getClass().getPackage().getImplementationVersion());

        return isHealthy
                ? Health.up().withDetails(systemDetails).build()
                : Health.down().withDetails(systemDetails).build();
    }

    private Map<String, Object> buildComponentDetails(Health health, long duration) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", health.getStatus().getCode());
        details.put("details", health.getDetails());
        details.put("duration_ms", duration);
        return details;
    }

    private Health checkPostgresHealth() {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, Object> dbDetails = new LinkedHashMap<>();
            dbDetails.put("database", connection.getMetaData().getDatabaseProductName());
            dbDetails.put("version", connection.getMetaData().getDatabaseProductVersion());

            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            if (result == null || result != 1) {
                return Health.down()
                        .withDetail("error", "Test query failed")
                        .withDetails(dbDetails)
                        .build();
            }

            return Health.up()
                    .withDetail("message", "Connection successful")
                    .withDetails(dbDetails)
                    .build();
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("error", "Database connection failed: " + e.getMessage())
                    .build();
        }
    }

    private Health checkRabbitHealth() {
        try {
            org.springframework.amqp.core.QueueInformation queueInfo =
                    rabbitAdmin.getQueueInfo(GOOGLE_REQUESTS_QUEUE);

            if (queueInfo == null) {
                return Health.down()
                        .withDetail("error", "Queue " + GOOGLE_REQUESTS_QUEUE + " not found")
                        .build();
            }

            Map<String, Object> queueDetails = new LinkedHashMap<>();
            queueDetails.put("queue", GOOGLE_REQUESTS_QUEUE);
            queueDetails.put("messages", queueInfo.getMessageCount());
            queueDetails.put("consumers", queueInfo.getConsumerCount());

            rabbitTemplate.convertAndSend(GOOGLE_REQUESTS_QUEUE, "healthcheck");
            queueDetails.put("test_message", "success");

            return Health.up()
                    .withDetail("message", "Connection established")
                    .withDetail("queue_details", queueDetails)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "RabbitMQ connection failed: " + e.getMessage())
                    .withDetail("queue", GOOGLE_REQUESTS_QUEUE)
                    .build();
        }
    }

    private Health withTimeout(Callable<Health> healthCheck, long timeoutMs) {
        try {
            return healthCheck.call();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Check timed out after " + timeoutMs + "ms")
                    .withDetail("timeout_ms", timeoutMs)
                    .build();
        }
    }
}
