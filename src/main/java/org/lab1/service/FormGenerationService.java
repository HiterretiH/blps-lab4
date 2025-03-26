package org.lab1.service;

import jakarta.transaction.Transactional;
import org.lab1.model.FormField;
import org.lab1.repository.FormFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormGenerationService {
    @Autowired
    private FormFieldRepository formFieldRepository;

    public Map<String, String> generateFormFields() {
        List<FormField> formFields = formFieldRepository.findAll();

        return formFields.stream()
                .collect(Collectors.toMap(FormField::getFieldName, field -> ""));
    }

    @Transactional
    public void addFields(List<String> fieldNames) {
        for (String fieldName : fieldNames) {
            FormField newField = new FormField();
            newField.setFieldName(fieldName);
            formFieldRepository.save(newField);
        }
    }

    public void addField(String fieldName) {
        FormField newField = new FormField();
        newField.setFieldName(fieldName);
        formFieldRepository.save(newField);
    }
}
