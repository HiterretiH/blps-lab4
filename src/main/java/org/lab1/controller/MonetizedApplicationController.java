package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.json.MonetizedApplicationJson;
import org.lab1.model.MonetizedApplication;
import org.lab1.service.MonetizedApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/monetized-applications")
public class MonetizedApplicationController {

    private final MonetizedApplicationService monetizedApplicationService;
    private final Logger logger;

    @Autowired
    public MonetizedApplicationController(MonetizedApplicationService monetizedApplicationService, Logger logger) {
        this.monetizedApplicationService = monetizedApplicationService;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('monetized_application.manage')")
    @PostMapping
    public ResponseEntity<MonetizedApplication> createMonetizedApplication(@RequestBody MonetizedApplicationJson monetizedApplicationJson) {
        logger.info("Received request to create MonetizedApplication for developer ID: " + monetizedApplicationJson.getDeveloperId() +
                ", application ID: " + monetizedApplicationJson.getApplicationId());
        MonetizedApplication monetizedApplication = monetizedApplicationService.createMonetizedApplication(monetizedApplicationJson);
        logger.info("MonetizedApplication created with ID: " + monetizedApplication.getId() +
                ", developer ID: " + monetizedApplication.getDeveloper().getId() +
                ", application ID: " + monetizedApplication.getApplication().getId());
        return ResponseEntity.ok(monetizedApplication);
    }

    @PreAuthorize("hasAuthority('monetized_application.read')")
    @GetMapping("/{id}")
    public ResponseEntity<MonetizedApplication> getMonetizedApplicationById(@PathVariable int id) {
        logger.info("Received request to get MonetizedApplication by ID: " + id);
        Optional<MonetizedApplication> monetizedApplication = monetizedApplicationService.getMonetizedApplicationById(id);
        return monetizedApplication.map(ResponseEntity::ok).orElseGet(() -> {
            logger.info("MonetizedApplication not found with ID: " + id);
            return ResponseEntity.notFound().build();
        });
    }
}