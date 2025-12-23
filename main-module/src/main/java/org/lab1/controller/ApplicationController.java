package org.lab1.controller;

import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public final class ApplicationController {
  private static final String APPLICATION_CREATE_LOG =
      "Attempting to create new application for developer ID: ";
  private static final String APPLICATION_CREATE_SUCCESS_LOG =
      "Successfully created application with ID: ";
  private static final String APPLICATION_CREATE_ERROR_LOG = "Failed to create application: ";
  private static final String APPLICATION_FETCH_LOG = "Fetching application with ID: ";
  private static final String APPLICATION_FOUND_LOG = "Found application with ID: ";
  private static final String APPLICATION_NOT_FOUND_LOG = "Application not found with ID: ";
  private static final String APPLICATION_UPDATE_LOG = "Updating application with ID: ";
  private static final String APPLICATION_UPDATE_SUCCESS_LOG =
      "Successfully updated application with ID: ";
  private static final String APPLICATION_UPDATE_ERROR_LOG =
      "Failed to update application with ID: ";
  private static final String APPLICATION_DELETE_LOG = "Deleting application with ID: ";
  private static final String APPLICATION_DELETE_SUCCESS_LOG =
      "Successfully deleted application with ID: ";
  private static final String APPLICATION_DELETE_ERROR_LOG =
      "Failed to delete application with ID: ";

  private final ApplicationService applicationService;
  private final Logger logger;

  @Autowired
  public ApplicationController(
      final ApplicationService applicationServiceParam,
      final Logger loggerParam) {
    this.applicationService = applicationServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('application.read')")
  @GetMapping
  public ResponseEntity<List<Application>> getAllApplications() {
    logger.info("Fetching all applications");
    List<Application> applications = applicationService.getAllApplications();
    logger.info("Found " + applications.size() + " applications");
    return ResponseEntity.ok(applications);
  }

  @PreAuthorize("hasAuthority('application.read')")
  @GetMapping("/developer/{developerId}")
  public ResponseEntity<List<Application>> getApplicationsByDeveloperId(
      @PathVariable final int developerId) {
    logger.info("Fetching applications for developer ID: " + developerId);
    List<Application> applications =
        applicationService.getApplicationsByDeveloperId(developerId);
    logger.info("Found " + applications.size()
        + " applications for developer ID: " + developerId);
    return ResponseEntity.ok(applications);
  }

  @PreAuthorize("hasAuthority('application.manage')")
  @PostMapping
  public ResponseEntity<Application> createApplication(
      @RequestBody final ApplicationJson applicationJson) {
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
  public ResponseEntity<Application> getApplication(@PathVariable final int id) {
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
  public ResponseEntity<Application> updateApplication(
      @PathVariable final int id,
      @RequestParam final Developer developer,
      @RequestParam final String name,
      @RequestParam final ApplicationType type,
      @RequestParam final double price,
      @RequestParam final String description,
      @RequestParam final ApplicationStatus status) {
    logger.info(APPLICATION_UPDATE_LOG + id);
    try {
      Application application =
          applicationService.updateApplication(
              id, developer, name, type, price, description, status);
      logger.info(APPLICATION_UPDATE_SUCCESS_LOG + id);
      return ResponseEntity.ok(application);
    } catch (Exception exception) {
      logger.error(APPLICATION_UPDATE_ERROR_LOG + id
          + ". Error: " + exception.getMessage());
      throw exception;
    }
  }

  @PreAuthorize("hasAuthority('application.manage')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteApplication(@PathVariable final int id) {
    logger.info(APPLICATION_DELETE_LOG + id);
    try {
      applicationService.deleteApplication(id);
      logger.info(APPLICATION_DELETE_SUCCESS_LOG + id);
      return ResponseEntity.noContent().build();
    } catch (Exception exception) {
      logger.error(APPLICATION_DELETE_ERROR_LOG + id
          + ". Error: " + exception.getMessage());
      throw exception;
    }
  }
}
