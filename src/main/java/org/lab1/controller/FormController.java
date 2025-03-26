package org.lab1.controller;

import org.lab1.service.FormGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    @Autowired
    private FormGenerationService formGenerationService;

    @PreAuthorize("hasAuthority('form.read')")
    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generateForm() {
        Map<String, String> formFields = formGenerationService.generateFormFields();
        return ResponseEntity.ok(formFields);
    }

    @PreAuthorize("hasAuthority('form.manage')")
    @PostMapping("/addField")
    public ResponseEntity<String> addField(@RequestBody Map<String, String> fieldRequest) {
        String fieldName = fieldRequest.get("fieldName");

        if (fieldName == null || fieldName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Field name is required");
        }

        formGenerationService.addField(fieldName);
        return ResponseEntity.status(HttpStatus.CREATED).body("Field added successfully");
    }

    @PreAuthorize("hasAuthority('form.manage')")
    @PostMapping("/addFields")
    public ResponseEntity<String> addFields(@RequestBody List<Map<String, String>> fieldsRequest) {
        if (fieldsRequest == null || fieldsRequest.isEmpty()) {
            return ResponseEntity.badRequest().body("Field names are required");
        }

        List<String> fieldNames = fieldsRequest.stream()
                .map(fieldMap -> fieldMap.get("fieldName"))
                .filter(fieldName -> fieldName != null && !fieldName.trim().isEmpty())
                .toList();

        if (fieldNames.isEmpty()) {
            return ResponseEntity.badRequest().body("No valid field names provided");
        }

        formGenerationService.addFields(fieldNames);
        return ResponseEntity.status(HttpStatus.CREATED).body("Fields added successfully");
    }
}
