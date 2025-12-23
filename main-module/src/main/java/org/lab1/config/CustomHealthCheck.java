package org.lab1.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.sql.DataSource;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public final class CustomHealthCheck implements HealthIndicator {
  private static final long TIMEOUT_MILLISECONDS = 5000;
  private static final String GOOGLE_REQUESTS_QUEUE = "google.requests";
  private static final String POSTGRESQL_CHECK_NAME = "PostgreSQL check";
  private static final String RABBITMQ_CHECK_NAME = "RabbitMQ check";
  private static final String STATUS_KEY = "status";
  private static final String UP_STATUS = "UP";
  private static final String DOWN_STATUS = "DOWN";
  private static final String COMPONENTS_KEY = "components";
  private static final String DURATION_MS_KEY = "duration_ms";
  private static final String VERSION_KEY = "version";
  private static final String POSTGRES_COMPONENT = "postgres";
  private static final String RABBITMQ_COMPONENT = "rabbitmq";
  private static final String DETAILS_KEY = "details";
  private static final String DATABASE_KEY = "database";
  private static final String DATABASE_VERSION_KEY = "version";
  private static final String TEST_QUERY = "SELECT 1";
  private static final String HEALTH_CHECK_MESSAGE = "healthcheck";
  private static final String MESSAGE_KEY = "message";
  private static final String QUEUE_DETAILS_KEY = "queue_details";
  private static final String QUEUE_KEY = "queue";
  private static final String MESSAGES_KEY = "messages";
  private static final String CONSUMERS_KEY = "consumers";
  private static final String TEST_MESSAGE_KEY = "test_message";
  private static final String SUCCESS_VALUE = "success";
  private static final String ERROR_KEY = "error";
  private static final String CONNECTION_SUCCESSFUL_MSG = "Connection successful";
  private static final String DATABASE_CONNECTION_FAILED_MSG = "Database connection failed: ";
  private static final String QUEUE_NOT_FOUND_MSG = "Queue ";
  private static final String NOT_FOUND_SUFFIX = " not found";
  private static final String CONNECTION_ESTABLISHED_MSG = "Connection established";
  private static final String RABBITMQ_CONNECTION_FAILED_MSG = "RabbitMQ connection failed: ";
  private static final String CHECK_TIMEOUT_MSG = "Check timed out after ";
  private static final String TIMEOUT_MS_SUFFIX = "ms";
  private static final String TIMEOUT_MS_KEY = "timeout_ms";
  private static final String TEST_QUERY_FAILED_MSG = "Test query failed";

  private final DataSource dataSource;
  private final RabbitAdmin rabbitAdmin;
  private final JdbcTemplate jdbcTemplate;
  private final RabbitTemplate rabbitTemplate;

  public CustomHealthCheck(
      final DataSource dataSourceParam,
      final RabbitAdmin rabbitAdminParam,
      final JdbcTemplate jdbcTemplateParam,
      final RabbitTemplate rabbitTemplateParam) {
    this.dataSource = dataSourceParam;
    this.rabbitAdmin = rabbitAdminParam;
    this.jdbcTemplate = jdbcTemplateParam;
    this.rabbitTemplate = rabbitTemplateParam;
  }

  @Override
  public Health health() {
    Map<String, Object> systemDetails = new LinkedHashMap<>();
    Map<String, Object> components = new LinkedHashMap<>();
    boolean isHealthy = true;
    StopWatch stopWatch = new StopWatch();

    Health postgresHealth = performDatabaseHealthCheck(stopWatch);
    components.put(
        POSTGRES_COMPONENT,
        buildComponentDetails(postgresHealth, stopWatch.getLastTaskTimeMillis()));
    if (postgresHealth.getStatus().equals(Health.down().build().getStatus())) {
      isHealthy = false;
    }

    Health rabbitHealth = performRabbitMQHealthCheck(stopWatch);
    components.put(
        RABBITMQ_COMPONENT,
        buildComponentDetails(rabbitHealth, stopWatch.getLastTaskTimeMillis()));
    if (rabbitHealth.getStatus().equals(Health.down().build().getStatus())) {
      isHealthy = false;
    }

    systemDetails.put(STATUS_KEY, isHealthy ? UP_STATUS : DOWN_STATUS);
    systemDetails.put(COMPONENTS_KEY, components);
    systemDetails.put(DURATION_MS_KEY, stopWatch.getTotalTimeMillis());
    systemDetails.put(VERSION_KEY, getClass().getPackage().getImplementationVersion());

    return isHealthy
        ? Health.up().withDetails(systemDetails).build()
        : Health.down().withDetails(systemDetails).build();
  }

  private Health performDatabaseHealthCheck(final StopWatch stopWatch) {
    stopWatch.start(POSTGRESQL_CHECK_NAME);
    Health postgresHealth = withTimeout(this::checkPostgresHealth, TIMEOUT_MILLISECONDS);
    stopWatch.stop();
    return postgresHealth;
  }

  private Health performRabbitMQHealthCheck(final StopWatch stopWatch) {
    stopWatch.start(RABBITMQ_CHECK_NAME);
    Health rabbitHealth = withTimeout(this::checkRabbitHealth, TIMEOUT_MILLISECONDS);
    stopWatch.stop();
    return rabbitHealth;
  }

  private Map<String, Object> buildComponentDetails(
      final Health health,
      final long duration) {
    Map<String, Object> details = new LinkedHashMap<>();
    details.put(STATUS_KEY, health.getStatus().getCode());
    details.put(DETAILS_KEY, health.getDetails());
    details.put(DURATION_MS_KEY, duration);
    return details;
  }

  private Health checkPostgresHealth() {
    try (Connection connection = dataSource.getConnection()) {
      Map<String, Object> dbDetails = new LinkedHashMap<>();
      dbDetails.put(DATABASE_KEY, connection.getMetaData().getDatabaseProductName());
      dbDetails.put(DATABASE_VERSION_KEY, connection.getMetaData().getDatabaseProductVersion());

      Integer result = jdbcTemplate.queryForObject(TEST_QUERY, Integer.class);
      if (result == null || result != 1) {
        return Health.down()
            .withDetail(ERROR_KEY, TEST_QUERY_FAILED_MSG)
            .withDetails(dbDetails)
            .build();
      }

      return Health.up()
          .withDetail(MESSAGE_KEY, CONNECTION_SUCCESSFUL_MSG)
          .withDetails(dbDetails)
          .build();
    } catch (SQLException sqlException) {
      return Health.down()
          .withDetail(ERROR_KEY, DATABASE_CONNECTION_FAILED_MSG + sqlException.getMessage())
          .build();
    }
  }

  private Health checkRabbitHealth() {
    try {
      org.springframework.amqp.core.QueueInformation queueInfo =
          rabbitAdmin.getQueueInfo(GOOGLE_REQUESTS_QUEUE);

      if (queueInfo == null) {
        return Health.down()
            .withDetail(ERROR_KEY, QUEUE_NOT_FOUND_MSG + GOOGLE_REQUESTS_QUEUE + NOT_FOUND_SUFFIX)
            .build();
      }

      Map<String, Object> queueDetails = new LinkedHashMap<>();
      queueDetails.put(QUEUE_KEY, GOOGLE_REQUESTS_QUEUE);
      queueDetails.put(MESSAGES_KEY, queueInfo.getMessageCount());
      queueDetails.put(CONSUMERS_KEY, queueInfo.getConsumerCount());

      rabbitTemplate.convertAndSend(GOOGLE_REQUESTS_QUEUE, HEALTH_CHECK_MESSAGE);
      queueDetails.put(TEST_MESSAGE_KEY, SUCCESS_VALUE);

      return Health.up()
          .withDetail(MESSAGE_KEY, CONNECTION_ESTABLISHED_MSG)
          .withDetail(QUEUE_DETAILS_KEY, queueDetails)
          .build();
    } catch (Exception exception) {
      return Health.down()
          .withDetail(ERROR_KEY, RABBITMQ_CONNECTION_FAILED_MSG + exception.getMessage())
          .withDetail(QUEUE_KEY, GOOGLE_REQUESTS_QUEUE)
          .build();
    }
  }

  private Health withTimeout(
      final Callable<Health> healthCheck,
      final long timeoutMs) {
    try {
      return healthCheck.call();
    } catch (Exception exception) {
      return Health.down()
          .withDetail(ERROR_KEY, CHECK_TIMEOUT_MSG + timeoutMs + TIMEOUT_MS_SUFFIX)
          .withDetail(TIMEOUT_MS_KEY, timeoutMs)
          .build();
    }
  }
}
