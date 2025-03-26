package org.lab1.controller;

import org.lab1.json.MonetizedApplicationJson;
import org.lab1.model.MonetizedApplication;
import org.lab1.service.MonetizedApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/monetized-applications")
public class MonetizedApplicationController {

    private final MonetizedApplicationService monetizedApplicationService;

    @Autowired
    public MonetizedApplicationController(MonetizedApplicationService monetizedApplicationService) {
        this.monetizedApplicationService = monetizedApplicationService;
    }

    @PreAuthorize("hasAuthority('monetized_application.manage')")
    @PostMapping
    public ResponseEntity<MonetizedApplication> createMonetizedApplication(@RequestBody MonetizedApplicationJson monetizedApplicationJson) {
        MonetizedApplication monetizedApplication = monetizedApplicationService.createMonetizedApplication(monetizedApplicationJson);
        return ResponseEntity.ok(monetizedApplication);
    }

    @PreAuthorize("hasAuthority('monetized_application.read')")
    @GetMapping("/{id}")
    public ResponseEntity<MonetizedApplication> getMonetizedApplicationById(@PathVariable int id) {
        Optional<MonetizedApplication> monetizedApplication = monetizedApplicationService.getMonetizedApplicationById(id);
        return monetizedApplication.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}