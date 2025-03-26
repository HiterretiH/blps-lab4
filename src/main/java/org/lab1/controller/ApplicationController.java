package org.lab1.controller;

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

    @Autowired
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PreAuthorize("hasAuthority('application.manage')")
    @PostMapping
    public ResponseEntity<Application> createApplication(@RequestBody ApplicationJson applicationJson) {
        Application application = applicationService.createApplication(applicationJson);
        return ResponseEntity.ok(application);
    }

    @PreAuthorize("hasAuthority('application.read')")
    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable int id) {
        Optional<Application> application = applicationService.getApplicationById(id);
        return application.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('application.manage')")
    @PutMapping("/{id}")
    public ResponseEntity<Application> updateApplication(@PathVariable int id, @RequestParam Developer developer, @RequestParam String name, @RequestParam ApplicationType type, @RequestParam double price, @RequestParam String description, @RequestParam ApplicationStatus status) {
        Application application = applicationService.updateApplication(id, developer, name, type, price, description, status);
        return ResponseEntity.ok(application);
    }

    @PreAuthorize("hasAuthority('application.manage')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable int id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}