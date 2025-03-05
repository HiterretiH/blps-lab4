package org.lab1.controller;

import org.lab1.json.ApplicationStatsJson;
import org.lab1.model.ApplicationStats;
import org.lab1.service.ApplicationStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/application-stats")
public class ApplicationStatsController {

    @Autowired
    private ApplicationStatsService applicationStatsService;

    @PostMapping
    public ApplicationStats create(@RequestBody ApplicationStatsJson applicationStatsJson) {
        return applicationStatsService.save(applicationStatsJson);
    }

    @PutMapping("/{id}")
    public ApplicationStats update(@PathVariable int id, @RequestBody ApplicationStatsJson applicationStats) {
        applicationStats.setId(id);
        return applicationStatsService.save(applicationStats);
    }

    @GetMapping("/{id}")
    public Optional<ApplicationStats> getById(@PathVariable int id) {
        return applicationStatsService.findById(id);
    }

    @GetMapping
    public List<ApplicationStats> getAll() {
        return applicationStatsService.findAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        applicationStatsService.delete(id);
    }
}
