package org.lab1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lab1.json.*;
import org.lab1.model.User;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GoogleTaskSender {
    private final RabbitTemplate rabbitTemplate;
    private final UserService userService;
    private final GoogleOAuthService googleOAuthService;
    private final ObjectMapper objectMapper;

    @Autowired
    public GoogleTaskSender(RabbitTemplate rabbitTemplate,
                            UserService userService,
                            GoogleOAuthService googleOAuthService,
                            ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.userService = userService;
        this.googleOAuthService = googleOAuthService;
        this.objectMapper = objectMapper;
    }

    public void sendFormCreationRequest(int userId, Map<String, String> fields, String formTitle, String googleEmail) {
        try {
            GoogleFormRequest request = new GoogleFormRequest(
                    googleEmail,
                    fields,
                    formTitle
            );

            MessageProperties properties = new MessageProperties();
            properties.setHeader("userId", userId);
            properties.setHeader("operation", "createForm");

            Message message = new Message(
                    objectMapper.writeValueAsBytes(request),
                    properties
            );

            rabbitTemplate.send("google.requests", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize form request", e);
        }
    }

    public void sendSheetCreationRequest(int userId, GoogleSheetRequestWithData request) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader("userId", userId);
            properties.setHeader("operation", "createSheetWithData");

            Message message = new Message(
                    objectMapper.writeValueAsBytes(request),
                    properties
            );

            rabbitTemplate.send("google.requests", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize sheet request with data", e);
        }
    }

    public void sendAddAppSheetsRequest(int userId, String appName) {
        try {
            String googleEmail = googleOAuthService.getUserGoogleEmail(userId);
            String spreadsheetTitle = "Revenue Statistics - " + googleEmail + " - 52";

            GoogleSheetIdentifier request = new GoogleSheetIdentifier(googleEmail, spreadsheetTitle);

            MessageProperties properties = new MessageProperties();
            properties.setHeader("userId", userId);
            properties.setHeader("operation", "addAppSheets");
            properties.setHeader("appName", appName); // Добавляем название приложения в заголовки

            Message message = new Message(
                    objectMapper.writeValueAsBytes(request),
                    properties
            );

            rabbitTemplate.send("google.requests", message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send add app sheets request", e);
        }
    }

    public void sendMonetizationEvent(int userId, MonetizationEvent event) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader("operation", "updateMonetization");
            properties.setHeader("userId", userId);
            properties.setHeader("eventType", event.getEventType().name());

            Message message = new Message(
                    objectMapper.writeValueAsBytes(event),
                    properties
            );

            rabbitTemplate.send("google.requests", message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize monetization event", e);
        }
    }

    public void sendUpdateAppsTopRequest() {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setHeader("operation", "updateAppsTop");

            Message message = MessageBuilder
                    .withBody(new byte[0])
                    .andProperties(properties)
                    .build();

            rabbitTemplate.send("google.requests", message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send update apps top request", e);
        }
    }
}