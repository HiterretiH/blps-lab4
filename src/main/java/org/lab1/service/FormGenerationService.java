package org.lab1.service;

import jakarta.transaction.Transactional;
import org.lab1.model.FormField;
import org.lab1.repository.FormFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormGenerationService {
    private final FormFieldRepository formFieldRepository;
    private final JtaTransactionManager transactionManager;

    public FormGenerationService(FormFieldRepository formFieldRepository, JtaTransactionManager transactionManager) {
        this.formFieldRepository = formFieldRepository;
        this.transactionManager = transactionManager;
    }

    public Map<String, String> generateFormFields() {
        List<FormField> formFields = formFieldRepository.findAll();

        return formFields.stream()
                .collect(Collectors.toMap(FormField::getFieldName, field -> ""));
    }

    public void addFields(List<String> fieldNames) {
        TransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = transactionManager.getTransaction(def);
        try {
            for (String fieldName : fieldNames) {
                FormField newField = new FormField();
                newField.setFieldName(fieldName);
                formFieldRepository.save(newField);
            }
            transactionManager.commit(status);
        } catch (Exception ex) {
            transactionManager.rollback(status);
            throw ex;
        }
    }

    public void addField(String fieldName) {
        FormField newField = new FormField();
        newField.setFieldName(fieldName);
        formFieldRepository.save(newField);
    }
}
