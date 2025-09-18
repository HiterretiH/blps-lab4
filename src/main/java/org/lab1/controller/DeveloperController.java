package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.model.Developer;
import org.lab1.service.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/developers")
public class DeveloperController {
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
        logger.info("Received request to create developer.");
        Developer developer = developerService.createDeveloper(param.getName(), param.getDescription());
        logger.info("Developer created with ID: " + developer.getId());
        return ResponseEntity.ok(developer);
    }

    @PreAuthorize("hasAuthority('developer.read')")
    @GetMapping("/{id}")
    public ResponseEntity<Developer> getDeveloper(@PathVariable int id) {
        logger.info("Received request to get developer with ID: " + id);
        Optional<Developer> developer = developerService.getDeveloperById(id);
        return developer.map(ResponseEntity::ok).orElseGet(() -> {
            logger.info("Developer not found with ID: " + id);
            return ResponseEntity.notFound().build();
        });
    }

    @PreAuthorize("hasAuthority('developer.manage')")
    @PutMapping("/{id}")
    public ResponseEntity<Developer> updateDeveloper(@PathVariable int id, @RequestParam String name, @RequestParam String description) {
        logger.info("Received request to update developer with ID: " + id);
        Developer developer = developerService.updateDeveloper(id, name, description);
        logger.info("Developer updated with ID: " + developer.getId());
        return ResponseEntity.ok(developer);
    }

    @PreAuthorize("hasAuthority('developer.manage')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable int id) {
        logger.info("Received request to delete developer with ID: " + id);
        developerService.deleteDeveloper(id);
        logger.info("Developer deleted with ID: " + id);
        return ResponseEntity.noContent().build();
    }
}