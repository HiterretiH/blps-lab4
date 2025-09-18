package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.json.ApplicationJson;
import org.lab1.model.Application;
import org.lab1.model.ApplicationStatus;
import org.lab1.model.ApplicationType;
import org.lab1.model.Developer;
import org.lab1.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final Logger logger;

    @Autowired
    public ApplicationController(ApplicationService applicationService,
                                 Logger logger) {
        this.applicationService = applicationService;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('application.manage')")
    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody ApplicationJson applicationJson) {
        logger.info("Attempting to create new application for developer ID: " + applicationJson.getDeveloperId());
        try {
            Application application = applicationService.createApplication(applicationJson);
            logger.info("Successfully created application with ID: " + application.getId());
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            logger.error("Failed to create application: " + e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('application.read')")
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable int id) {
        logger.info("Fetching application with ID: " + id);
        Optional<Application> application = applicationService.getApplicationById(id);
        if (application.isPresent()) {
            logger.info("Found application with ID: " + id);
            return ResponseEntity.ok(application.get());
        } else {
            logger.error("Application not found with ID: " + id);
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasAuthority('application.manage')")
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable int id,
                                                         @RequestParam Developer developer,
                                                         @RequestParam String name,
                                                         @RequestParam ApplicationType type,
                                                         @RequestParam double price,
                                                         @RequestParam String description,
                                                         @RequestParam ApplicationStatus status) {
        logger.info("Updating application with ID: " + id);
        try {
            Application application = applicationService.updateApplication(id, developer, name, type, price, description, status);
            logger.info("Successfully updated application with ID: " + id);
            return ResponseEntity.ok(application);
        } catch (Exception e) {
            logger.error("Failed to update application with ID: " + id + ". Error: " + e.getMessage());
            throw e;
        }
    }

    @PreAuthorize("hasAuthority('application.manage')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable int id) {
        logger.info("Deleting application with ID: " + id);
        try {
            applicationService.deleteApplication(id);
            logger.info("Successfully deleted application with ID: " + id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Failed to delete application with ID: " + id + ". Error: " + e.getMessage());
            throw e;
        }
    }
}