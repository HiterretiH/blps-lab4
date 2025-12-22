package org.lab1.controller;

import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.model.Developer;
import org.lab1.service.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
  public DeveloperController(DeveloperService developerService, Logger logger) {
    this.developerService = developerService;
    this.logger = logger;
  }

  @PreAuthorize("hasAuthority('developer.manage')")
  @PostMapping
  public ResponseEntity<Developer> createDeveloper(@RequestBody Developer param) {
    logger.info(CREATE_REQUEST_LOG);
    Developer developer = developerService.createDeveloper(param.getName(), param.getDescription());
    logger.info(CREATE_SUCCESS_LOG + developer.getId());
    return ResponseEntity.ok(developer);
  }

  @PreAuthorize("hasAuthority('developer.read')")
  @GetMapping("/{id}")
  public ResponseEntity<Developer> getDeveloper(@PathVariable int id) {
    logger.info(GET_REQUEST_LOG + id);
    Optional<Developer> developer = developerService.getDeveloperById(id);

    if (developer.isPresent()) {
      return ResponseEntity.ok(developer.get());
    }

    logger.info(NOT_FOUND_LOG + id);
    return ResponseEntity.notFound().build();
  }

  @PreAuthorize("hasAuthority('developer.manage')")
  @PutMapping("/{id}")
  public ResponseEntity<Developer> updateDeveloper(
      @PathVariable int id, @RequestParam String name, @RequestParam String description) {
    logger.info(UPDATE_REQUEST_LOG + id);
    Developer developer = developerService.updateDeveloper(id, name, description);
    logger.info(UPDATE_SUCCESS_LOG + developer.getId());
    return ResponseEntity.ok(developer);
  }

  @PreAuthorize("hasAuthority('developer.manage')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDeveloper(@PathVariable int id) {
    logger.info(DELETE_REQUEST_LOG + id);
    developerService.deleteDeveloper(id);
    logger.info(DELETE_SUCCESS_LOG + id);
    return ResponseEntity.noContent().build();
  }
}
