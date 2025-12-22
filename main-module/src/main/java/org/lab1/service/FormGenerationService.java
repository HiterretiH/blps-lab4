package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.exception.OAuthException;
import org.lab1.model.FormField;
import org.lab1.repository.FormFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormGenerationService {
    private static final String GENERATE_FORM_LOG = "Initiating Google Form generation for user ID: ";
    private static final String GOOGLE_NOT_CONNECTED_LOG = "User ID ";
    private static final String NOT_CONNECTED_GOOGLE_LOG = " has not connected Google account.";
    private static final String NOT_CONNECTED_MSG = "User has not connected Google account";
    private static final String RETRIEVED_EMAIL_LOG = "Retrieved Google email for user ID ";
    private static final String GENERATED_FIELDS_LOG = "Generated form fields (";
    private static final String AND_TITLE_LOG = ") and title: ";
    private static final String FORM_CREATION_SENT_LOG = "Form creation request sent for user ID: ";
    private static final String TO_EMAIL_LOG = " to email: ";
    private static final String GENERATE_FIELDS_LOG = "Generating form fields from database.";
    private static final String GENERATED_FIELDS_COUNT_LOG = "Generated ";
    private static final String FORM_FIELDS_LOG = " form fields.";
    private static final String ADD_MULTIPLE_FIELDS_LOG = "Adding multiple fields: ";
    private static final String ADDED_FIELD_LOG = "Added field: ";
    private static final String TRANSACTION_COMMITTED_LOG = "Transaction committed for adding ";
    private static final String FIELDS_LOG = " fields.";
    private static final String TRANSACTION_ROLLED_BACK_LOG = "Transaction rolled back due to error adding fields: ";
    private static final String ADD_FIELD_LOG = "Adding field: ";
    private static final String FIELD_SAVED_LOG = "Field '";
    private static final String SAVED_TO_DB_LOG = "' saved to database.";
    private static final String COLON_SEPARATOR = ": ";

    private final FormFieldRepository formFieldRepository;
    private final JtaTransactionManager transactionManager;
    private final GoogleTaskSender googleTaskSender;
    private final GoogleOAuthService googleOAuthService;
    private final Logger logger;

    @Autowired
    public FormGenerationService(FormFieldRepository formFieldRepository, JtaTransactionManager transactionManager,
                                 GoogleTaskSender googleTaskSender, GoogleOAuthService googleOAuthService, Logger logger) {
        this.formFieldRepository = formFieldRepository;
        this.transactionManager = transactionManager;
        this.googleTaskSender = googleTaskSender;
        this.googleOAuthService = googleOAuthService;
        this.logger = logger;
    }

    public String generateAndSendGoogleForm(int userId) throws OAuthException {
        logger.info(GENERATE_FORM_LOG + userId);

        if (!googleOAuthService.isGoogleConnected(userId)) {
            logger.error(GOOGLE_NOT_CONNECTED_LOG + userId + NOT_CONNECTED_GOOGLE_LOG);
            throw new IllegalStateException(NOT_CONNECTED_MSG);
        }

        String googleEmail = googleOAuthService.getUserGoogleEmail(userId);
        logger.info(RETRIEVED_EMAIL_LOG + userId + COLON_SEPARATOR + googleEmail);

        Map<String, String> formFields = generateFormFields();
        String formTitle = "Form for " + googleEmail + " - " + Instant.now().toString();
        logger.info(GENERATED_FIELDS_LOG + formFields.size() + AND_TITLE_LOG + formTitle);

        googleTaskSender.sendFormCreationRequest(userId, formFields, formTitle, googleEmail);
        logger.info(FORM_CREATION_SENT_LOG + userId + TO_EMAIL_LOG + googleEmail);

        return "Form creation request sent for user: " + userId;
    }

    public Map<String, String> generateFormFields() {
        logger.info(GENERATE_FIELDS_LOG);
        List<FormField> formFields = formFieldRepository.findAll();
        Map<String, String> fieldsMap = formFields.stream()
                .collect(Collectors.toMap(FormField::getFieldName, field -> ""));
        logger.info(GENERATED_FIELDS_COUNT_LOG + fieldsMap.size() + FORM_FIELDS_LOG);
        return fieldsMap;
    }

    public void addFields(List<String> fieldNames) {
        logger.info(ADD_MULTIPLE_FIELDS_LOG + fieldNames);
        TransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(definition);

        try {
            for (String fieldName : fieldNames) {
                FormField newField = new FormField();
                newField.setFieldName(fieldName);
                formFieldRepository.save(newField);
                logger.info(ADDED_FIELD_LOG + fieldName);
            }
            transactionManager.commit(status);
            logger.info(TRANSACTION_COMMITTED_LOG + fieldNames.size() + FIELDS_LOG);
        } catch (Exception exception) {
            transactionManager.rollback(status);
            logger.error(TRANSACTION_ROLLED_BACK_LOG + exception.getMessage());
            throw exception;
        }
    }

    public void addField(String fieldName) {
        logger.info(ADD_FIELD_LOG + fieldName);
        FormField newField = new FormField();
        newField.setFieldName(fieldName);
        formFieldRepository.save(newField);
        logger.info(FIELD_SAVED_LOG + fieldName + SAVED_TO_DB_LOG);
    }
}
