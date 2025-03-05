package org.lab1.controller;

import org.lab1.service.FormGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    @Autowired
    private FormGenerationService formGenerationService;

    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generateForm() {
        Map<String, String> formFields = formGenerationService.generateFormFields();
        return ResponseEntity.ok(formFields);
    }

    @PostMapping("/addField")
    public ResponseEntity<String> addField(@RequestBody Map<String, String> fieldRequest) {
        String fieldName = fieldRequest.get("fieldName");

        if (fieldName == null || fieldName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Field name is required");
        }

        formGenerationService.addField(fieldName);
        return ResponseEntity.ok("Field added successfully");
    }
}
