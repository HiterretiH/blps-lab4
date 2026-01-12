package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.exception.NotFoundException;
import org.lab1.model.Developer;
import org.lab1.service.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequestMapping("/api/developers")
public class DeveloperController {
  private static final String CREATE_REQUEST_LOG = "Received request to create developer.";
  private static final String CREATE_SUCCESS_LOG = "Developer created with ID: ";
  private static final String GET_REQUEST_LOG = "Received request to get developer with ID: ";
  private static final String NOT_FOUND_LOG = "Developer not found with ID: ";
  private static final String UPDATE_REQUEST_LOG = "Received request to update developer with ID: ";
  private static final String UPDATE_SUCCESS_LOG = "Developer updated with ID: ";
  private static final String DELETE_REQUEST_LOG = "Received request to delete developer with ID: ";
  private static final String DELETE_SUCCESS_LOG = "Developer deleted with ID: ";

  private final DeveloperService developerService;
  private final Logger logger;

  @Autowired
  public DeveloperController(
      final DeveloperService developerServiceParam,
      @Qualifier("correlationLogger") final Logger loggerParam) {
    this.developerService = developerServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('developer.read')")
  @GetMapping("/by-user/{userId}")
  public ResponseEntity<Developer> getDeveloperByUserId(@PathVariable final int userId) {
    logger.info("Received request to get developer by user ID: " + userId);
    Developer developer =
        developerService
            .getDeveloperByUserId(userId)
            .orElseThrow(() -> new NotFoundException("Developer not found for user ID: " + userId));

    return ResponseEntity.ok(developer);
  }

  @PreAuthorize("hasAuthority('developer.manage')")
  @PostMapping
  public ResponseEntity<Developer> createDeveloper(@RequestBody final Developer param) {
    logger.info(CREATE_REQUEST_LOG);
    Developer developer = developerService.createDeveloper(param.getName(), param.getDescription());
    logger.info(CREATE_SUCCESS_LOG + developer.getId());
    return ResponseEntity.ok(developer);
  }

  @PreAuthorize("hasAuthority('developer.read')")
  @GetMapping("/{id}")
  public ResponseEntity<Developer> getDeveloper(@PathVariable final int id) {
    logger.info(GET_REQUEST_LOG + id);
    Developer developer =
        developerService
            .getDeveloperById(id)
            .orElseThrow(() -> new NotFoundException("Developer not found with ID: " + id));

    return ResponseEntity.ok(developer);
  }

  @PreAuthorize("hasAuthority('developer.manage')")
  @PutMapping("/{id}")
  public ResponseEntity<Developer> updateDeveloper(
      @PathVariable final int id,
      @RequestParam final String name,
      @RequestParam final String description) {
    logger.info(UPDATE_REQUEST_LOG + id);
    Developer developer = developerService.updateDeveloper(id, name, description);
    logger.info(UPDATE_SUCCESS_LOG + developer.getId());
    return ResponseEntity.ok(developer);
  }

  @PreAuthorize("hasAuthority('developer.manage')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDeveloper(@PathVariable final int id) {
    logger.info(DELETE_REQUEST_LOG + id);
    developerService.deleteDeveloper(id);
    logger.info(DELETE_SUCCESS_LOG + id);
    return ResponseEntity.noContent().build();
  }
}
