package org.lab1.controller;

import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.json.MonetizedApplicationJson;
import org.lab1.model.MonetizedApplication;
import org.lab1.service.MonetizedApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
      MonetizedApplicationService monetizedApplicationService, Logger logger) {
    this.monetizedApplicationService = monetizedApplicationService;
    this.logger = logger;
  }

  @PreAuthorize("hasAuthority('monetized_application.manage')")
  @PostMapping
  public ResponseEntity<MonetizedApplication> createMonetizedApplication(
      @RequestBody MonetizedApplicationJson monetizedApplicationJson) {
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

  @PreAuthorize("hasAuthority('monetized_application.read')")
  @GetMapping("/{id}")
  public ResponseEntity<MonetizedApplication> getMonetizedApplicationById(@PathVariable int id) {
    logger.info(GET_REQUEST_LOG + id);
    Optional<MonetizedApplication> monetizedApplication =
        monetizedApplicationService.getMonetizedApplicationById(id);

    if (monetizedApplication.isPresent()) {
      return ResponseEntity.ok(monetizedApplication.get());
    }

    logger.info(GET_NOT_FOUND_LOG + id);
    return ResponseEntity.notFound().build();
  }
}
