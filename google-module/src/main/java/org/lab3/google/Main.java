package org.lab3.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import io.prometheus.client.Histogram;
import org.lab.logger.Logger;
import org.lab3.google.config.EnvConfig;
import org.lab3.google.config.GoogleConfig;
import org.lab3.google.json.*;
import org.lab3.google.model.GoogleOperationResult;
import org.lab3.google.quartz.UpdateAppsTopJob;
import org.lab3.google.repository.OperationResultRepository;
import org.lab3.google.service.GoogleConnection;
import org.lab3.google.service.GoogleConnectionImpl;
import org.lab3.google.service.MetricsManager;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    private static final String CREATE_FORM_OPERATION = "createForm";
    private static final String CREATE_SHEET_WITH_DATA_OPERATION = "createSheetWithData";
    private static final String ADD_APP_SHEETS_OPERATION = "addAppSheets";
    private static final String UPDATE_MONETIZATION_OPERATION = "updateMonetization";
    private static final String UPDATE_APPS_TOP_OPERATION = "updateAppsTop";
    private static final String HEALTHCHECK_OPERATION = "healthcheck";
    private static final String UNKNOWN_OPERATION = "unknown";
    private static final String OPERATION_HEADER = "operation";
    private static final String USER_ID_HEADER = "userId";
    private static final String APP_NAME_HEADER = "appName";
    private static final String GOOGLE_REQUESTS_QUEUE = "google.requests";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final MetricsManager METRICS = MetricsManager.getInstance();
    private static final Logger LOGGER = Logger.getInstance("google-module");
    private static OperationResultRepository repository;

    public static void main(String[] args) throws Exception {
        LOGGER.info("Google Module starting...");

        try {
            repository = new OperationResultRepository();
            LOGGER.info("Database repository initialized");
        } catch (Exception e) {
            LOGGER.error("Failed to initialize database repository: " + e.getMessage());
        }

        METRICS.start();
        LOGGER.info("Metrics started.");

        String rabbitHost = EnvConfig.get("RABBITMQ_HOST", "rabbit");
        int rabbitPort = Integer.parseInt(EnvConfig.get("RABBITMQ_PORT", "5672"));
        String rabbitUsername = EnvConfig.get("RABBITMQ_USERNAME", "admin");
        String rabbitPassword = EnvConfig.get("RABBITMQ_PASSWORD", "password");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setPort(rabbitPort);
        factory.setUsername(rabbitUsername);
        factory.setPassword(rabbitPassword);
        LOGGER.info("RabbitMQ connection factory configured for host: " + rabbitHost + ", port: " + rabbitPort);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            LOGGER.info("Successfully established connection to RabbitMQ.");

            channel.queueDeclare(GOOGLE_REQUESTS_QUEUE, false, false, false, null);
            LOGGER.info("Declared queue: " + GOOGLE_REQUESTS_QUEUE + ".");

            GoogleConnection googleConnection = new GoogleConnectionImpl(GoogleConfig.createConnection());
            LOGGER.info("Google API connection established.");

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            LOGGER.info("Quartz Scheduler initialized.");

            JobDetail job = JobBuilder.newJob(UpdateAppsTopJob.class)
                    .withIdentity("updateAppsTopJob", "group1")
                    .build();
            JobDataMap jobDataMap = job.getJobDataMap();
            jobDataMap.put("googleConnection", googleConnection);
            jobDataMap.put("repository", repository);
            LOGGER.info("UpdateAppsTopJob created and dependencies injected.");

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("updateAppsTopTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
                    .build();
            LOGGER.info("UpdateAppsTopTrigger created with cron schedule: 0 0/1 * * * ?");

            scheduler.start();
            scheduler.scheduleJob(job, trigger);
            LOGGER.info("Scheduled job: updateAppsTopJob with trigger: updateAppsTopTrigger.");

            DeliverCallback callback = (tag, delivery) -> processMessage(delivery, googleConnection);

            channel.basicConsume(GOOGLE_REQUESTS_QUEUE, true, callback, tag -> {});
            LOGGER.info("Started consuming from queue: " + GOOGLE_REQUESTS_QUEUE + ".");

            Thread.sleep(Long.MAX_VALUE);
        } finally {
            LOGGER.info("Google Module shutting down...");
            METRICS.stop();
            if (repository != null) {
                repository.close();
            }
            LOGGER.info("Metrics stopped.");
        }
    }

    private static void processMessage(Delivery delivery, GoogleConnection googleConnection) {
        String operation = UNKNOWN_OPERATION;
        boolean success = false;
        int userId = 0;
        String targetValue = null;
        String result = null;
        String error = null;

        Histogram.Timer timer = METRICS.startRequestTimer();
        try {
            operation = delivery.getProperties().getHeaders().get(OPERATION_HEADER).toString();
            userId = (int) delivery.getProperties().getHeaders().get(USER_ID_HEADER);
            String messageBody = new String(delivery.getBody(), StandardCharsets.UTF_8);

            LOGGER.info("Processing operation '" + operation + "' for user ID: " + userId);

            switch (operation) {
                case CREATE_FORM_OPERATION:
                    processFormCreation(messageBody, googleConnection, userId);
                    success = true;
                    break;

                case CREATE_SHEET_WITH_DATA_OPERATION:
                    processSheetCreationWithData(messageBody, googleConnection, userId);
                    success = true;
                    break;

                case ADD_APP_SHEETS_OPERATION:
                    processAppSheetsAddition(delivery, messageBody, googleConnection, userId);
                    success = true;
                    break;

                case UPDATE_MONETIZATION_OPERATION:
                    processMonetizationUpdate(messageBody, googleConnection, userId);
                    success = true;
                    break;

                case UPDATE_APPS_TOP_OPERATION:
                    processAppsTopUpdate(googleConnection, userId);
                    success = true;
                    break;

                case HEALTHCHECK_OPERATION:
                    processHealthcheck(userId);
                    success = true;
                    break;

                default:
                    error = "Unknown operation: " + operation;
                    LOGGER.error(error);
                    success = false;
            }
        } catch (Exception e) {
            error = "Error processing message: " + e.getMessage();
            LOGGER.error(error);
            LOGGER.error("Stack trace: " + e);
            success = false;
        } finally {
            METRICS.recordRequest(operation, success);
            timer.observeDuration();

            if (repository != null) {
                try {
                    GoogleOperationResult dbResult = new GoogleOperationResult(
                            userId,
                            operation,
                            targetValue,
                            success ? result : null,
                            success ? null : error
                    );
                    repository.save(dbResult);
                    LOGGER.info("Operation result saved to database");
                } catch (Exception e) {
                    LOGGER.error("Failed to save operation result to database: " + e.getMessage());
                }
            }
        }
    }

    private static void processFormCreation(String messageBody, GoogleConnection googleConnection, int userId)
            throws IOException {
        GoogleFormRequest formRequest = OBJECT_MAPPER.readValue(messageBody, GoogleFormRequest.class);
        LOGGER.info("Processing form creation for: " + formRequest.getGoogleEmail());

        String formId = googleConnection.createGoogleForm(
                formRequest.getFormTitle(),
                formRequest.getFields()
        );
        String result = "Form created with ID: " + formId;
        LOGGER.info(result);
    }

    private static void processSheetCreationWithData(String messageBody, GoogleConnection googleConnection, int userId)
            throws IOException {
        GoogleSheetRequestWithData sheetRequestWithData = OBJECT_MAPPER.readValue(messageBody, GoogleSheetRequestWithData.class);

        String sheetUrl = googleConnection.createRevenueSpreadsheetWithData(
                sheetRequestWithData.getSheetTitle(),
                sheetRequestWithData.getHeaders(),
                sheetRequestWithData.getData()
        );
        String result = "Sheet created: " + sheetUrl;
        LOGGER.info(result);
    }

    private static void processAppSheetsAddition(Delivery delivery, String messageBody,
                                                 GoogleConnection googleConnection, int userId)
            throws IOException {
        GoogleSheetIdentifier sheetIdentifier = OBJECT_MAPPER.readValue(messageBody, GoogleSheetIdentifier.class);
        String appName = delivery.getProperties().getHeaders().get(APP_NAME_HEADER).toString();

        LOGGER.info("Adding sheets for app '" + appName + "' to spreadsheet: " +
                sheetIdentifier.getSpreadsheetTitle());

        googleConnection.addAppSheets(
                sheetIdentifier.getGoogleEmail(),
                sheetIdentifier.getSpreadsheetTitle(),
                appName
        );
        String result = "Sheets added for app: " + appName;
        LOGGER.info(result);
    }

    private static void processMonetizationUpdate(String messageBody, GoogleConnection googleConnection, int userId)
            throws IOException {
        MonetizationEvent event = OBJECT_MAPPER.readValue(messageBody, MonetizationEvent.class);

        googleConnection.updateMonetizationSheets(event);
        String result = "Monetization updated for event: " + event.getEventType();
        LOGGER.info(result);
    }

    private static void processAppsTopUpdate(GoogleConnection googleConnection, int userId)
            throws IOException {
        googleConnection.updateAppsTop();
        String result = "Apps top updated";
        LOGGER.info(result);
    }

    private static void processHealthcheck(int userId) {
        String result = "Health check performed";
        LOGGER.info(result);
    }
}
