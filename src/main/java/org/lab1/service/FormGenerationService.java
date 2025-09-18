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
    private final FormFieldRepository formFieldRepository;
    private final JtaTransactionManager transactionManager;
    private final GoogleTaskSender googleTaskSender;
    private final UserService userService;
    private final GoogleOAuthService googleOAuthService;
    private final Logger logger;

    @Autowired
    public FormGenerationService(FormFieldRepository formFieldRepository, JtaTransactionManager transactionManager, GoogleTaskSender googleTaskSender, UserService userService, GoogleOAuthService googleOAuthService, Logger logger) {
        this.formFieldRepository = formFieldRepository;
        this.transactionManager = transactionManager;
        this.googleTaskSender = googleTaskSender;
        this.userService = userService;
        this.googleOAuthService = googleOAuthService;
        this.logger = logger;
    }

    public String generateAndSendGoogleForm(int userId) throws OAuthException {
        logger.info("Initiating Google Form generation for user ID: " + userId);
        if (!googleOAuthService.isGoogleConnected(userId)) {
            logger.error("User ID " + userId + " has not connected Google account.");
            throw new IllegalStateException("User has not connected Google account");
        }
        String googleEmail = googleOAuthService.getUserGoogleEmail(userId);
        logger.info("Retrieved Google email for user ID " + userId + ": " + googleEmail);
        Map<String, String> formFields = generateFormFields();
        String formTitle = "Form for " + googleEmail + " - " + Instant.now().toString();
        logger.info("Generated form fields (" + formFields.size() + ") and title: " + formTitle);
        googleTaskSender.sendFormCreationRequest(userId, formFields, formTitle, googleEmail);
        logger.info("Form creation request sent for user ID: " + userId + " to email: " + googleEmail);
        return "Form creation request sent for user: " + userId;
    }

    public Map<String, String> generateFormFields() {
        logger.info("Generating form fields from database.");
        List<FormField> formFields = formFieldRepository.findAll();
        Map<String, String> fieldsMap = formFields.stream()
                .collect(Collectors.toMap(FormField::getFieldName, field -> ""));
        logger.info("Generated " + fieldsMap.size() + " form fields.");
        return fieldsMap;
    }

    public void addFields(List<String> fieldNames) {
        logger.info("Adding multiple fields: " + fieldNames);
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            for (String fieldName : fieldNames) {
                FormField newField = new FormField();
                newField.setFieldName(fieldName);
                formFieldRepository.save(newField);
                logger.info("Added field: " + fieldName);
            }
            transactionManager.commit(status);
            logger.info("Transaction committed for adding " + fieldNames.size() + " fields.");
        } catch (Exception ex) {
            transactionManager.rollback(status);
            logger.error("Transaction rolled back due to error adding fields: " + ex.getMessage());
            throw ex;
        }
    }

    public void addField(String fieldName) {
        logger.info("Adding field: " + fieldName);
        FormField newField = new FormField();
        newField.setFieldName(fieldName);
        formFieldRepository.save(newField);
        logger.info("Field '" + fieldName + "' saved to database.");
    }
}