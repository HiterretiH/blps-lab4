package org.lab3.google.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import jakarta.resource.spi.endpoint.MessageEndpoint;
import java.io.IOException;
import org.lab3.google.json.GoogleFormRequest;
import org.lab3.google.service.GoogleConnection;

public class JmsMessageListener implements MessageEndpoint, MessageListener {
  private static final String UPLOAD_OPERATION = "upload";
  private static final String CREATE_SHEET_OPERATION = "createSheet";
  private static final String CREATE_FORM_OPERATION = "createForm";
  private static final String USER_ID_PROPERTY = "userId";
  private static final String OPERATION_PROPERTY = "operation";
  private static final String FILE_NAME = "file.txt";

  private final GoogleConnection googleConnection;
  private final ConnectionFactory connectionFactory;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public JmsMessageListener(
      GoogleConnection googleConnection, ConnectionFactory connectionFactory) {
    this.googleConnection = googleConnection;
    this.connectionFactory = connectionFactory;
  }

  @Override
  public void beforeDelivery(java.lang.reflect.Method method) {}

  @Override
  public void afterDelivery() {}

  @Override
  public void release() {
    googleConnection.close();
  }

  @Override
  public void onMessage(Message message) {
    try {
      if (message instanceof TextMessage) {
        String content = ((TextMessage) message).getText();
        String operation = message.getStringProperty(OPERATION_PROPERTY);
        String response = processMessage(operation, content, message);

        if (message.getJMSReplyTo() != null) {
          sendResponse(message.getJMSReplyTo(), response);
        }
      }
    } catch (JMSException | IOException exception) {
      throw new RuntimeException("Failed to process message", exception);
    }
  }

  private String processMessage(String operation, String content, Message message)
      throws JMSException, IOException {
    return switch (operation) {
      case UPLOAD_OPERATION -> processUploadOperation(content);
      case CREATE_SHEET_OPERATION -> processCreateSheetOperation(content);
      case CREATE_FORM_OPERATION -> processCreateFormOperation(content, message);
      default -> "Unknown operation: " + operation;
    };
  }

  private String processUploadOperation(String content) throws IOException {
    String fileId = googleConnection.uploadFile(FILE_NAME, content.getBytes());
    return "File uploaded with ID: " + fileId;
  }

  private String processCreateSheetOperation(String content) throws IOException {
    String sheetId = googleConnection.createGoogleSheet(content);
    return "Sheet created with ID: " + sheetId;
  }

  private String processCreateFormOperation(String content, Message message)
      throws IOException, JMSException {
    GoogleFormRequest request = objectMapper.readValue(content, GoogleFormRequest.class);
    int userId = message.getIntProperty(USER_ID_PROPERTY);
    String formUrl = googleConnection.createGoogleForm(request.getFormTitle(), request.getFields());
    System.out.println("Created form: " + formUrl);
    return "Form created: " + formUrl;
  }

  private void sendResponse(Destination replyTo, String response) throws JMSException {
    try (Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageProducer producer = session.createProducer(replyTo)) {

      TextMessage replyMessage = session.createTextMessage(response);
      producer.send(replyMessage);
    }
  }
}
