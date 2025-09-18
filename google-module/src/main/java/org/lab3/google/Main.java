package org.lab3.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import io.prometheus.client.Histogram;
import org.lab.logger.Logger;
import org.lab3.google.json.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class Main {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MetricsManager metrics = MetricsManager.getInstance();
    private static final Logger logger = Logger.getInstance("google-module");

    public static void main(String[] args) throws Exception {
        logger.info("Google Module starting...");
        metrics.start();
        logger.info("Metrics started.");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("rabbit");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("password");
        logger.info("RabbitMQ connection factory configured for host: rabbit, port: 5672.");

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
            jobDataMap.put("googleConnection", googleConn); // Передаем соединение
            logger.info("UpdateAppsTopJob created and GoogleConnection injected.");

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
                Histogram.Timer timer = metrics.startRequestTimer();
                try {
                    operation = delivery.getProperties().getHeaders().get("operation").toString();
                    int userId = (int) delivery.getProperties().getHeaders().get("userId");
                    String messageBody = new String(delivery.getBody(), "UTF-8");

                    logger.info("Processing operation '" + operation + "' for user ID: " + userId);

                    switch (operation) {
                        case "createForm":
                            GoogleFormRequest formRequest = objectMapper.readValue(messageBody, GoogleFormRequest.class);
                            logger.info("Processing form creation for: " + formRequest.getGoogleEmail());

                            String formId = googleConn.createGoogleForm(
                                    formRequest.getFormTitle(),
                                    formRequest.getFields()
                            );
                            logger.info("Created form with ID: " + formId);
                            break;

                        case "createSheetWithData":
                            GoogleSheetRequestWithData sheetRequestWithData = objectMapper.readValue(messageBody, GoogleSheetRequestWithData.class);
                            String sheetUrl = googleConn.createRevenueSpreadsheetWithData(
                                    sheetRequestWithData.getSheetTitle(),
                                    sheetRequestWithData.getHeaders(),
                                    sheetRequestWithData.getData()
                            );
                            logger.info("Created sheet with data: " + sheetUrl);
                            break;

                        case "addAppSheets":
                            GoogleSheetIdentifier sheetIdentifier = objectMapper.readValue(messageBody, GoogleSheetIdentifier.class);
                            String appName = delivery.getProperties().getHeaders().get("appName").toString();

                            logger.info("Adding sheets for app '" + appName + "' to spreadsheet: " +
                                    sheetIdentifier.getSpreadsheetTitle());

                            googleConn.addAppSheets(
                                    sheetIdentifier.getGoogleEmail(),
                                    sheetIdentifier.getSpreadsheetTitle(),
                                    appName
                            );
                            logger.info("Successfully added sheets for app: " + appName);
                            break;

                        case "updateMonetization":
                            MonetizationEvent event = objectMapper.readValue(messageBody, MonetizationEvent.class);
                            googleConn.updateMonetizationSheets(event);
                            logger.info("Updated monetization sheets for event: " + event.getEventType());
                            break;

                        case "updateAppsTop":
                            googleConn.updateAppsTop();
                            logger.info("Updated apps top");
                            break;

                        case "healthcheck":
                            break;

                        default:
                            logger.error("Unknown operation: " + operation);
                    }
                    success = true;
                } catch (Exception e) {
                    logger.error("Error processing message: " + e.getMessage());
                    logger.error("Stack trace: " + e);
                    success = false;
                } finally {
                    metrics.recordRequest(operation, success);
                    timer.observeDuration();
                }
            };

            channel.basicConsume("google.requests", true, callback, tag -> {});
            logger.info("Started consuming from queue: google.requests.");

            while (true) {
                Thread.sleep(1000);
            }
        } finally {
            logger.info("Google Module shutting down...");
            metrics.stop();
            logger.info("Metrics stopped.");
        }
    }
}