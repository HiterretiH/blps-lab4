package org.lab1.controller;

import org.lab1.json.ApplicationStatsJson;
import org.lab1.model.ApplicationStats;
import org.lab1.service.ApplicationStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/application-stats")
public class ApplicationStatsController {

    @Autowired
    private ApplicationStatsService applicationStatsService;

    @PreAuthorize("hasAuthority('application_stats.manage')")
    @PostMapping
    public ApplicationStats create(@RequestBody ApplicationStatsJson applicationStatsJson) {
        return applicationStatsService.save(applicationStatsJson);
    }

    @PreAuthorize("hasAuthority('application_stats.manage')")
    @PutMapping("/{id}")
    public ApplicationStats update(@PathVariable int id, @RequestBody ApplicationStatsJson applicationStats) {
        applicationStats.setId(id);
        return applicationStatsService.save(applicationStats);
    }

    @PreAuthorize("hasAuthority('application_stats.read')")
    @GetMapping("/{id}")
    public Optional<ApplicationStats> getById(@PathVariable int id) {
        return applicationStatsService.findById(id);
    }

    @PreAuthorize("hasAuthority('application_stats.read')")
    @GetMapping
    public List<ApplicationStats> getAll() {
        return applicationStatsService.findAll();
    }

    @PreAuthorize("hasAuthority('application_stats.manage')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        applicationStatsService.delete(id);
    }
}