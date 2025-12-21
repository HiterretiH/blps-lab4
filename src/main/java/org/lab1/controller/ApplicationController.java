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
    private static final String APPLICATION_CREATE_LOG = "Attempting to create new application for developer ID: ";
    private static final String APPLICATION_CREATE_SUCCESS_LOG = "Successfully created application with ID: ";
    private static final String APPLICATION_CREATE_ERROR_LOG = "Failed to create application: ";
    private static final String APPLICATION_FETCH_LOG = "Fetching application with ID: ";
    private static final String APPLICATION_FOUND_LOG = "Found application with ID: ";
    private static final String APPLICATION_NOT_FOUND_LOG = "Application not found with ID: ";
    private static final String APPLICATION_UPDATE_LOG = "Updating application with ID: ";
    private static final String APPLICATION_UPDATE_SUCCESS_LOG = "Successfully updated application with ID: ";
    private static final String APPLICATION_UPDATE_ERROR_LOG = "Failed to update application with ID: ";
    private static final String APPLICATION_DELETE_LOG = "Deleting application with ID: ";
    private static final String APPLICATION_DELETE_SUCCESS_LOG = "Successfully deleted application with ID: ";
    private static final String APPLICATION_DELETE_ERROR_LOG = "Failed to delete application with ID: ";

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
        logger.info(APPLICATION_CREATE_LOG + applicationJson.getDeveloperId());
        try {
            Application application = applicationService.createApplication(applicationJson);
            logger.info(APPLICATION_CREATE_SUCCESS_LOG + application.getId());
            return ResponseEntity.ok(application);
        } catch (Exception exception) {
            logger.error(APPLICATION_CREATE_ERROR_LOG + exception.getMessage());
            throw exception;
        }
    }

    @PreAuthorize("hasAuthority('application.read')")
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable int id) {
        logger.info(APPLICATION_FETCH_LOG + id);
        Optional<Application> application = applicationService.getApplicationById(id);

        if (application.isPresent()) {
            logger.info(APPLICATION_FOUND_LOG + id);
            return ResponseEntity.ok(application.get());
        }

        logger.error(APPLICATION_NOT_FOUND_LOG + id);
        return ResponseEntity.notFound().build();
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
        logger.info(APPLICATION_UPDATE_LOG + id);
        try {
            Application application = applicationService.updateApplication(id, developer, name, type, price, description, status);
            logger.info(APPLICATION_UPDATE_SUCCESS_LOG + id);
            return ResponseEntity.ok(application);
        } catch (Exception exception) {
            logger.error(APPLICATION_UPDATE_ERROR_LOG + id + ". Error: " + exception.getMessage());
            throw exception;
        }
    }

    @PreAuthorize("hasAuthority('application.manage')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable int id) {
        logger.info(APPLICATION_DELETE_LOG + id);
        try {
            applicationService.deleteApplication(id);
            logger.info(APPLICATION_DELETE_SUCCESS_LOG + id);
            return ResponseEntity.noContent().build();
        } catch (Exception exception) {
            logger.error(APPLICATION_DELETE_ERROR_LOG + id + ". Error: " + exception.getMessage());
            throw exception;
        }
    }
}
