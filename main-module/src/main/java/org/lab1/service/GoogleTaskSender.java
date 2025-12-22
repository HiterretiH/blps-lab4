package org.lab1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lab.logger.Logger;
import org.lab1.json.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GoogleTaskSender {
    private static final String GOOGLE_REQUESTS_QUEUE = "google.requests";
    private static final String USER_ID_HEADER = "userId";
    private static final String OPERATION_HEADER = "operation";
    private static final String APP_NAME_HEADER = "appName";
    private static final String EVENT_TYPE_HEADER = "eventType";
    private static final String CREATE_FORM_OPERATION = "createForm";
    private static final String CREATE_SHEET_OPERATION = "createSheetWithData";
    private static final String ADD_APP_SHEETS_OPERATION = "addAppSheets";
    private static final String UPDATE_MONETIZATION_OPERATION = "updateMonetization";
    private static final String UPDATE_APPS_TOP_OPERATION = "updateAppsTop";
    private static final String SEND_FORM_LOG = "Sent form creation request for user ID: ";
    private static final String FAILED_FORM_LOG = "Failed to serialize form request for user ID: ";
    private static final String REASON_LOG = ". Reason: ";
    private static final String SERIALIZATION_ERROR = "Failed to serialize form request";
    private static final String SEND_SHEET_LOG = "Sent sheet creation request with data for user ID: ";
    private static final String FAILED_SHEET_LOG = "Failed to serialize sheet request with data for user ID: ";
    private static final String SHEET_SERIALIZATION_ERROR = "Failed to serialize sheet request with data";
    private static final String SEND_APP_SHEETS_LOG = "Sent add app sheets request for user ID: ";
    private static final String APP_NAME_LOG = ", app name: ";
    private static final String FAILED_APP_SHEETS_LOG = "Failed to send add app sheets request for user ID: ";
    private static final String APP_SHEETS_ERROR = "Failed to send add app sheets request";
    private static final String SEND_MONETIZATION_LOG = "Sent monetization event for user ID: ";
    private static final String EVENT_TYPE_LOG = ", event type: ";
    private static final String FAILED_MONETIZATION_LOG = "Failed to serialize monetization event for user ID: ";
    private static final String MONETIZATION_SERIALIZATION_ERROR = "Failed to serialize monetization event";
    private static final String SEND_UPDATE_TOP_LOG = "Sent update apps top request";
    private static final String FAILED_UPDATE_TOP_LOG = "Failed to send update apps top request. Reason: ";
    private static final String UPDATE_TOP_ERROR = "Failed to send update apps top request";
    private static final String REVENUE_STATS_TITLE = "Revenue Statistics - ";
    private static final String REVENUE_STATS_SUFFIX = " - 52";

    private final RabbitTemplate rabbitTemplate;
    private final GoogleOAuthService googleOAuthService;
    private final ObjectMapper objectMapper;
    private final Logger logger;

    @Autowired
    public GoogleTaskSender(RabbitTemplate rabbitTemplate,
                            GoogleOAuthService googleOAuthService,
                            ObjectMapper objectMapper,
                            Logger logger) {
        this.rabbitTemplate = rabbitTemplate;
        this.googleOAuthService = googleOAuthService;
        this.objectMapper = objectMapper;
        this.logger = logger;
    }

    public void sendFormCreationRequest(int userId, Map<String, String> fields, String formTitle, String googleEmail) {
        try {
            GoogleFormRequest request = new GoogleFormRequest(googleEmail, fields, formTitle);

            MessageProperties properties = new MessageProperties();
            properties.setHeader(USER_ID_HEADER, userId);
            properties.setHeader(OPERATION_HEADER, CREATE_FORM_OPERATION);

            Message message = new Message(
                    objectMapper.writeValueAsBytes(request),
                    properties
            );

            rabbitTemplate.send(GOOGLE_REQUESTS_QUEUE, message);
            logger.info(SEND_FORM_LOG + userId);
        } catch (JsonProcessingException jsonProcessingException) {
            logger.error(FAILED_FORM_LOG + userId + REASON_LOG + jsonProcessingException.getMessage());
            throw new RuntimeException(SERIALIZATION_ERROR, jsonProcessingException);
        }
    }

    public void sendSheetCreationRequest(int userId, GoogleSheetRequestWithData request) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(USER_ID_HEADER, userId);
            properties.setHeader(OPERATION_HEADER, CREATE_SHEET_OPERATION);

            Message message = new Message(
                    objectMapper.writeValueAsBytes(request),
                    properties
            );

            rabbitTemplate.send(GOOGLE_REQUESTS_QUEUE, message);
            logger.info(SEND_SHEET_LOG + userId);
        } catch (JsonProcessingException jsonProcessingException) {
            logger.error(FAILED_SHEET_LOG + userId + REASON_LOG + jsonProcessingException.getMessage());
            throw new RuntimeException(SHEET_SERIALIZATION_ERROR, jsonProcessingException);
        }
    }

    public void sendAddAppSheetsRequest(int userId, String appName) {
        try {
            String googleEmail = googleOAuthService.getUserGoogleEmail(userId);
            String spreadsheetTitle = REVENUE_STATS_TITLE + googleEmail + REVENUE_STATS_SUFFIX;

            GoogleSheetIdentifier request = new GoogleSheetIdentifier(googleEmail, spreadsheetTitle);

            MessageProperties properties = new MessageProperties();
            properties.setHeader(USER_ID_HEADER, userId);
            properties.setHeader(OPERATION_HEADER, ADD_APP_SHEETS_OPERATION);
            properties.setHeader(APP_NAME_HEADER, appName);

            Message message = new Message(
                    objectMapper.writeValueAsBytes(request),
                    properties
            );

            rabbitTemplate.send(GOOGLE_REQUESTS_QUEUE, message);
            logger.info(SEND_APP_SHEETS_LOG + userId + APP_NAME_LOG + appName);
        } catch (Exception exception) {
            logger.error(FAILED_APP_SHEETS_LOG + userId + APP_NAME_LOG + appName + REASON_LOG + exception.getMessage());
            throw new RuntimeException(APP_SHEETS_ERROR, exception);
        }
    }

    public void sendMonetizationEvent(int userId, MonetizationEvent event) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(OPERATION_HEADER, UPDATE_MONETIZATION_OPERATION);
            properties.setHeader(USER_ID_HEADER, userId);
            properties.setHeader(EVENT_TYPE_HEADER, event.getEventType().name());

            Message message = new Message(
                    objectMapper.writeValueAsBytes(event),
                    properties
            );

            rabbitTemplate.send(GOOGLE_REQUESTS_QUEUE, message);
            logger.info(SEND_MONETIZATION_LOG + userId + EVENT_TYPE_LOG + event.getEventType().name());
        } catch (JsonProcessingException jsonProcessingException) {
            logger.error(FAILED_MONETIZATION_LOG + userId + EVENT_TYPE_LOG + event.getEventType().name() +
                    REASON_LOG + jsonProcessingException.getMessage());
            throw new RuntimeException(MONETIZATION_SERIALIZATION_ERROR, jsonProcessingException);
        }
    }

    public void sendUpdateAppsTopRequest() {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader(OPERATION_HEADER, UPDATE_APPS_TOP_OPERATION);

            Message message = MessageBuilder
                    .withBody(new byte[0])
                    .andProperties(properties)
                    .build();

            rabbitTemplate.send(GOOGLE_REQUESTS_QUEUE, message);
            logger.info(SEND_UPDATE_TOP_LOG);
        } catch (Exception exception) {
            logger.error(FAILED_UPDATE_TOP_LOG + exception.getMessage());
            throw new RuntimeException(UPDATE_TOP_ERROR, exception);
        }
    }
}
