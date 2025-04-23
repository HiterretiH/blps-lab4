package org.lab3.google;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import org.lab3.google.json.GoogleFormRequest;

import java.io.IOException;
import java.util.Map;

public class JmsMessageListener implements MessageEndpoint, MessageListener {
    private final GoogleConnection googleConnection;
    private final ConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JmsMessageListener(GoogleConnection googleConnection, ConnectionFactory connectionFactory) {
        this.googleConnection = googleConnection;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void beforeDelivery(java.lang.reflect.Method method) throws NoSuchMethodException, ResourceException {
        // Called before message delivery
    }

    @Override
    public void afterDelivery() throws ResourceException {
        // Called after message delivery
    }

    @Override
    public void release() {
        // Release resources
        googleConnection.close();
    }

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                String content = ((TextMessage) message).getText();
                String operation = message.getStringProperty("operation");
                String response;

                switch (operation) {
                    case "upload":
                        String fileId = googleConnection.uploadFile("file.txt", content.getBytes());
                        response = "File uploaded with ID: " + fileId;
                        break;

                    case "createSheet":
                        String sheetId = googleConnection.createGoogleSheet(content);
                        response = "Sheet created with ID: " + sheetId;
                        break;

                    case "createForm":
                        GoogleFormRequest request = objectMapper.readValue(content, GoogleFormRequest.class);

                        // Получаем userId из заголовков
                        int userId = message.getIntProperty("userId");

                        // Создаем форму с полученными данными
                        String formUrl = googleConnection.createGoogleForm(
                                request.getFormTitle(),
                                request.getFields()
                        );

                        System.out.println("Created form: " + formUrl);

                    default:
                        response = "Unknown operation: " + operation;
                }

                if (message.getJMSReplyTo() != null) {
                    sendResponse(message.getJMSReplyTo(), response);
                }
            }
        } catch (JMSException | IOException e) {
            throw new RuntimeException("Failed to process message", e);
        }
    }

    private void sendResponse(jakarta.jms.Destination replyTo, String response) throws JMSException {
        try (Connection connection = connectionFactory.createConnection();
             Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
             MessageProducer producer = session.createProducer(replyTo)) {

            TextMessage replyMessage = session.createTextMessage(response);
            producer.send(replyMessage);
        }
    }
}