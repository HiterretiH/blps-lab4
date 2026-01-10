package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.exception.NotFoundException;
import org.lab1.json.MonetizedApplicationJson;
import org.lab1.model.MonetizedApplication;
import org.lab1.service.MonetizedApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monetized-applications")
public class MonetizedApplicationController {
  private static final String CREATE_REQUEST_LOG =
      "Received request to create MonetizedApplication for developer ID: ";
  private static final String APPLICATION_ID_LOG = ", application ID: ";
  private static final String CREATE_SUCCESS_LOG = "MonetizedApplication created with ID: ";
  private static final String DEVELOPER_ID_LOG = ", developer ID: ";
  private static final String GET_REQUEST_LOG =
      "Received request to get MonetizedApplication by ID: ";
  private static final String GET_NOT_FOUND_LOG = "MonetizedApplication not found with ID: ";

  private final MonetizedApplicationService monetizedApplicationService;
  private final Logger logger;

  @Autowired
  public MonetizedApplicationController(
      final MonetizedApplicationService monetizedApplicationServiceParam,
      final Logger loggerParam) {
    this.monetizedApplicationService = monetizedApplicationServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('monetized_application.manage')")
  @PostMapping
  public ResponseEntity<MonetizedApplication> createMonetizedApplication(
      @RequestBody final MonetizedApplicationJson monetizedApplicationJson) {
    logger.info(
        CREATE_REQUEST_LOG
            + monetizedApplicationJson.getDeveloperId()
            + APPLICATION_ID_LOG
            + monetizedApplicationJson.getApplicationId());
    MonetizedApplication monetizedApplication =
        monetizedApplicationService.createMonetizedApplication(monetizedApplicationJson);
    logger.info(
        CREATE_SUCCESS_LOG
            + monetizedApplication.getId()
            + DEVELOPER_ID_LOG
            + monetizedApplication.getDeveloper().getId()
            + APPLICATION_ID_LOG
            + monetizedApplication.getApplication().getId());
    return ResponseEntity.ok(monetizedApplication);
  }

  @PreAuthorize("hasAuthority('monetized_application.manage')")
  @GetMapping("/{id}")
  public ResponseEntity<MonetizedApplication> getMonetizedApplicationById(
      @PathVariable final int id) {
    logger.info(GET_REQUEST_LOG + id);
    MonetizedApplication monetizedApplication =
        monetizedApplicationService
            .getMonetizedApplicationById(id)
            .orElseThrow(
                () -> new NotFoundException("MonetizedApplication not found with ID: " + id));

    return ResponseEntity.ok(monetizedApplication);
  }

  @PreAuthorize("hasAuthority('monetized_application.manage')")
  @GetMapping("/developer/{developerId}")
  public ResponseEntity<List<MonetizedApplication>> getMonetizedApplicationsByDeveloper(
      @PathVariable final int developerId) {

    logger.info("Received request to get MonetizedApplications for developer ID: " + developerId);

    List<MonetizedApplication> monetizedApplications =
        monetizedApplicationService.getMonetizedApplicationsByDeveloperId(developerId);

    logger.info("Found " + monetizedApplications.size() + " MonetizedApplications for developer ID: " + developerId);
    return ResponseEntity.ok(monetizedApplications);
  }
}
