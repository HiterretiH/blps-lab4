package org.lab1.controller;

import org.lab.logger.Logger;
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

    private final ApplicationStatsService applicationStatsService;
    private final Logger logger;

    @Autowired
    public ApplicationStatsController(ApplicationStatsService applicationStatsService, Logger logger) {
        this.applicationStatsService = applicationStatsService;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('application_stats.manage')")
    @PostMapping
    public ApplicationStats create(@RequestBody ApplicationStatsJson applicationStatsJson) {
        logger.info("Received request to create ApplicationStats");
        ApplicationStats stats = applicationStatsService.save(applicationStatsJson);
        logger.info("ApplicationStats created with ID: " + stats.getId());
        return stats;
    }

    @PreAuthorize("hasAuthority('application_stats.manage')")
    @PutMapping("/{id}")
    public ApplicationStats update(@PathVariable int id, @RequestBody ApplicationStatsJson applicationStats) {
        logger.info("Received request to update ApplicationStats with ID: " + id);
        applicationStats.setId(id);
        ApplicationStats updatedStats = applicationStatsService.save(applicationStats);
        logger.info("ApplicationStats updated with ID: " + updatedStats.getId());
        return updatedStats;
    }

    @PreAuthorize("hasAuthority('application_stats.read')")
    @GetMapping("/{id}")
    public Optional<ApplicationStats> getById(@PathVariable int id) {
        logger.info("Received request to get ApplicationStats with ID: " + id);
        Optional<ApplicationStats> stats = applicationStatsService.findById(id);
        if (stats.isPresent()) {
            logger.info("ApplicationStats found with ID: " + id);
        } else {
            logger.info("ApplicationStats not found with ID: " + id);
        }
        return stats;
    }

    @PreAuthorize("hasAuthority('application_stats.read')")
    @GetMapping
    public List<ApplicationStats> getAll() {
        logger.info("Received request to get all ApplicationStats");
        List<ApplicationStats> allStats = applicationStatsService.findAll();
        logger.info("Found " + allStats.size() + " ApplicationStats");
        return allStats;
    }

    @PreAuthorize("hasAuthority('application_stats.manage')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        logger.info("Received request to delete ApplicationStats with ID: " + id);
        applicationStatsService.delete(id);
        logger.info("ApplicationStats deleted with ID: " + id);
    }
}