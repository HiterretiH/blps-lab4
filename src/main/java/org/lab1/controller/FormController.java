package org.lab1.controller;

import io.micrometer.core.instrument.Counter;
import org.lab.logger.Logger;
import org.lab1.model.User;
import org.lab1.repository.UserRepository;
import org.lab1.service.FormGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/forms")
public class FormController {

    private final FormGenerationService formGenerationService;
    private final UserRepository userRepository;
    private final Counter formsGeneratedCounter;
    private final Counter fieldsAddedCounter;
    private final Logger logger;

    @Autowired
    public FormController(FormGenerationService formGenerationService,
                          UserRepository userRepository,
                          MeterRegistry meterRegistry,
                          Logger logger) {
        this.formGenerationService = formGenerationService;
        this.userRepository = userRepository;
        this.formsGeneratedCounter = Counter.builder("forms.generated.total")
                .description("Total number of forms generated")
                .register(meterRegistry);
        this.fieldsAddedCounter = Counter.builder("fields.added.total")
                .description("Total number of fields added")
                .register(meterRegistry);
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('form.create')")
    @PostMapping("/create")
    public ResponseEntity<String> generateGoogleForm() {
        logger.info("Received request to generate Google Form.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            logger.error("User not found: " + authentication.getPrincipal());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();
        int userId = user.getId();

        try {
            String result = formGenerationService.generateAndSendGoogleForm(userId);
            formsGeneratedCounter.increment();
            logger.info("Google Form generation initiated for user ID: " + userId + ". Result: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error generating Google Form for user ID: " + userId + ". Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating form: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('form.read')")
    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generateForm() {
        logger.info("Received request to generate form fields.");
        Map<String, String> formFields = formGenerationService.generateFormFields();
        logger.info("Generated " + formFields.size() + " form fields.");
        return ResponseEntity.ok(formFields);
    }

    @PreAuthorize("hasAuthority('form.manage')")
    @PostMapping("/addField")
    public ResponseEntity<String> addField(@RequestBody Map<String, String> fieldRequest) {
        String fieldName = fieldRequest.get("fieldName");
        logger.info("Received request to add field: " + fieldName);
        if (fieldName == null || fieldName.trim().isEmpty()) {
            logger.error("Field name is required.");
            return ResponseEntity.badRequest().body("Field name is required");
        }
        formGenerationService.addField(fieldName);
        fieldsAddedCounter.increment();
        logger.info("Field '" + fieldName + "' added successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body("Field added successfully");
    }

    @PreAuthorize("hasAuthority('form.manage')")
    @PostMapping("/addFields")
    public ResponseEntity<String> addFields(@RequestBody List<Map<String, String>> fieldsRequest) {
        logger.info("Received request to add multiple fields.");
        if (fieldsRequest == null || fieldsRequest.isEmpty()) {
            logger.error("Field names are required.");
            return ResponseEntity.badRequest().body("Field names are required");
        }
        List<String> fieldNames = fieldsRequest.stream()
                .map(fieldMap -> fieldMap.get("fieldName"))
                .filter(fieldName -> fieldName != null && !fieldName.trim().isEmpty())
                .toList();
        if (fieldNames.isEmpty()) {
            logger.error("No valid field names provided.");
            return ResponseEntity.badRequest().body("No valid field names provided");
        }
        formGenerationService.addFields(fieldNames);
        fieldsAddedCounter.increment(fieldNames.size());
        logger.info("Added " + fieldNames.size() + " fields successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body("Fields added successfully");
    }
}