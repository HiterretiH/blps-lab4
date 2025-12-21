package org.lab3.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import io.prometheus.client.Histogram;
import org.lab.logger.Logger;
import org.lab3.google.config.EnvConfig;
import org.lab3.google.json.*;
import org.lab3.google.model.GoogleOperationResult;
import org.lab3.google.repository.OperationResultRepository;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class Main {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MetricsManager metrics = MetricsManager.getInstance();
    private static final Logger logger = Logger.getInstance("google-module");
    private static OperationResultRepository repository;

    public static void main(String[] args) throws Exception {
        logger.info("Google Module starting...");

        try {
            repository = new OperationResultRepository();
            logger.info("Database repository initialized");
        } catch (Exception e) {
            logger.error("Failed to initialize database repository: " + e.getMessage());
        }

        metrics.start();
        logger.info("Metrics started.");

        String rabbitHost = EnvConfig.get("RABBITMQ_HOST", "rabbit");
        int rabbitPort = Integer.parseInt(EnvConfig.get("RABBITMQ_PORT", "5672"));
        String rabbitUsername = EnvConfig.get("RABBITMQ_USERNAME", "admin");
        String rabbitPassword = EnvConfig.get("RABBITMQ_PASSWORD", "password");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setPort(rabbitPort);
        factory.setUsername(rabbitUsername);
        factory.setPassword(rabbitPassword);
        logger.info("RabbitMQ connection factory configured for host: " + rabbitHost + ", port: " + rabbitPort);

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            logger.info("Successfully established connection to RabbitMQ.");

            channel.queueDeclare("google.requests", false, false, false, null);
            logger.info("Declared queue: google.requests.");

            GoogleConnection googleConn = new GoogleConnectionImpl(
                    GoogleConfig.createConnection()
            );
            logger.info("Google API connection established.");

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();
            logger.info("Quartz Scheduler initialized.");

            JobDetail job = JobBuilder.newJob(UpdateAppsTopJob.class)
                    .withIdentity("updateAppsTopJob", "group1")
                    .build();
            JobDataMap jobDataMap = job.getJobDataMap();
            jobDataMap.put("googleConnection", googleConn);
            jobDataMap.put("repository", repository); // Передаем репозиторий в job
            logger.info("UpdateAppsTopJob created and dependencies injected.");

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("updateAppsTopTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
                    .build();
            logger.info("UpdateAppsTopTrigger created with cron schedule: 0 0/1 * * * ?");

            scheduler.start();
            scheduler.scheduleJob(job, trigger);
            logger.info("Scheduled job: updateAppsTopJob with trigger: updateAppsTopTrigger.");

            DeliverCallback callback = (tag, delivery) -> {
                String operation = "unknown";
                boolean success = false;
                int userId = 0;
                String targetValue = null;
                String result = null;
                String error = null;

                Histogram.Timer timer = metrics.startRequestTimer();
                try {
                    operation = delivery.getProperties().getHeaders().get("operation").toString();
                    userId = (int) delivery.getProperties().getHeaders().get("userId");
                    String messageBody = new String(delivery.getBody(), "UTF-8");

                    logger.info("Processing operation '" + operation + "' for user ID: " + userId);

                    switch (operation) {
                        case "createForm":
                            GoogleFormRequest formRequest = objectMapper.readValue(messageBody, GoogleFormRequest.class);
                            logger.info("Processing form creation for: " + formRequest.getGoogleEmail());
                            targetValue = formRequest.getFormTitle();

                            String formId = googleConn.createGoogleForm(
                                    formRequest.getFormTitle(),
                                    formRequest.getFields()
                            );
                            result = "Form created with ID: " + formId;
                            logger.info(result);
                            success = true;
                            break;

                        case "createSheetWithData":
                            GoogleSheetRequestWithData sheetRequestWithData = objectMapper.readValue(messageBody, GoogleSheetRequestWithData.class);
                            targetValue = sheetRequestWithData.getSheetTitle();

                            String sheetUrl = googleConn.createRevenueSpreadsheetWithData(
                                    sheetRequestWithData.getSheetTitle(),
                                    sheetRequestWithData.getHeaders(),
                                    sheetRequestWithData.getData()
                            );
                            result = "Sheet created: " + sheetUrl;
                            logger.info(result);
                            success = true;
                            break;

                        case "addAppSheets":
                            GoogleSheetIdentifier sheetIdentifier = objectMapper.readValue(messageBody, GoogleSheetIdentifier.class);
                            String appName = delivery.getProperties().getHeaders().get("appName").toString();
                            targetValue = sheetIdentifier.getSpreadsheetTitle() + " | " + appName;

                            logger.info("Adding sheets for app '" + appName + "' to spreadsheet: " +
                                    sheetIdentifier.getSpreadsheetTitle());

                            googleConn.addAppSheets(
                                    sheetIdentifier.getGoogleEmail(),
                                    sheetIdentifier.getSpreadsheetTitle(),
                                    appName
                            );
                            result = "Sheets added for app: " + appName;
                            logger.info(result);
                            success = true;
                            break;

                        case "updateMonetization":
                            MonetizationEvent event = objectMapper.readValue(messageBody, MonetizationEvent.class);
                            targetValue = "App ID: " + event.getApplicationId();

                            googleConn.updateMonetizationSheets(event);
                            result = "Monetization updated for event: " + event.getEventType();
                            logger.info(result);
                            success = true;
                            break;

                        case "updateAppsTop":
                            targetValue = "All spreadsheets";
                            googleConn.updateAppsTop();
                            result = "Apps top updated";
                            logger.info(result);
                            success = true;
                            break;

                        case "healthcheck":
                            targetValue = "Health check";
                            result = "Health check performed";
                            success = true;
                            break;

                        default:
                            error = "Unknown operation: " + operation;
                            logger.error(error);
                            success = false;
                    }
                } catch (Exception e) {
                    error = "Error processing message: " + e.getMessage();
                    logger.error(error);
                    logger.error("Stack trace: " + e);
                    success = false;
                } finally {
                    metrics.recordRequest(operation, success);
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
                            logger.info("Operation result saved to database");
                        } catch (Exception e) {
                            logger.error("Failed to save operation result to database: " + e.getMessage());
                        }
                    }
                }
            };

            channel.basicConsume("google.requests", true, callback, tag -> {});
            logger.info("Started consuming from queue: google.requests.");

            Thread.sleep(Long.MAX_VALUE);
        } finally {
            logger.info("Google Module shutting down...");
            metrics.stop();
            if (repository != null) {
                repository.close();
            }
            logger.info("Metrics stopped.");
        }
    }
}
