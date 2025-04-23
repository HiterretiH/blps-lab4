package org.lab3.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import org.lab3.google.json.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class Main {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("admin");
        factory.setPassword("password");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.queueDeclare("google.requests", false, false, false, null);

            GoogleConnection googleConn = new GoogleConnectionImpl(
                    GoogleConfig.createConnection()
            );

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            Scheduler scheduler = schedulerFactory.getScheduler();

            // 4. Создание JobDetail с передачей GoogleConnection
            JobDetail job = JobBuilder.newJob(UpdateAppsTopJob.class)
                    .withIdentity("updateAppsTopJob", "group1")
                    .build();

            JobDataMap jobDataMap = job.getJobDataMap();
            jobDataMap.put("googleConnection", googleConn); // Передаем соединение

                        // 5. Создание Trigger (каждые 2 минуты)
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("updateAppsTopTrigger", "group1")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * * * ?"))
                    .build();

                        // 6. Запуск планировщика
            scheduler.start();
            scheduler.scheduleJob(job, trigger);

            DeliverCallback callback = (tag, delivery) -> {
                try {
                    String operation = delivery.getProperties().getHeaders().get("operation").toString();
                    int userId = (int) delivery.getProperties().getHeaders().get("userId");
                    String messageBody = new String(delivery.getBody(), "UTF-8");

                    System.out.println("Processing operation '" + operation + "' for user ID: " + userId);

                    switch (operation) {
                        case "createForm":
                            GoogleFormRequest formRequest = objectMapper.readValue(messageBody, GoogleFormRequest.class);
                            System.out.println("Processing form creation for: " + formRequest.getGoogleEmail());

                            String formId = googleConn.createGoogleForm(
                                    formRequest.getFormTitle(),
                                    formRequest.getFields()
                            );
                            System.out.println("Created form with ID: " + formId);
                            break;

                        case "createSheetWithData":
                            GoogleSheetRequestWithData sheetRequestWithData = objectMapper.readValue(messageBody, GoogleSheetRequestWithData.class);
                            String sheetUrl = googleConn.createRevenueSpreadsheetWithData(
                                    sheetRequestWithData.getSheetTitle(),
                                    sheetRequestWithData.getHeaders(),
                                    sheetRequestWithData.getData()
                            );
                            System.out.println("Created sheet with data: " + sheetUrl);
                            break;

                        case "addAppSheets":
                            GoogleSheetIdentifier sheetIdentifier = objectMapper.readValue(messageBody, GoogleSheetIdentifier.class);
                            String appName = delivery.getProperties().getHeaders().get("appName").toString();

                            System.out.println("Adding sheets for app '" + appName + "' to spreadsheet: " +
                                    sheetIdentifier.getSpreadsheetTitle());

                            googleConn.addAppSheets(
                                    sheetIdentifier.getGoogleEmail(),
                                    sheetIdentifier.getSpreadsheetTitle(),
                                    appName
                            );
                            System.out.println("Successfully added sheets for app: " + appName);
                            break;

                        case "updateMonetization":
                            MonetizationEvent event = objectMapper.readValue(messageBody, MonetizationEvent.class);
                            googleConn.updateMonetizationSheets(event);
                            System.out.println("Updated monetization sheets for event: " + event.getEventType());
                            break;

                        case "updateAppsTop":
                            googleConn.updateAppsTop();
                            System.out.println("Updated apps top");
                            break;

                        default:
                            System.err.println("Unknown operation: " + operation);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage());
                    e.printStackTrace();
                }
            };

            channel.basicConsume("google.requests", true, callback, tag -> {});

            while (true) {
                Thread.sleep(1000);
            }
        }
    }
}