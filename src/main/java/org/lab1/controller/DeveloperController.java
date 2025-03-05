package org.lab1.controller;

import org.lab1.model.Developer;
import org.lab1.service.DeveloperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/developers")
public class DeveloperController {

    private final DeveloperService developerService;

    @Autowired
    public DeveloperController(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @PostMapping
    public ResponseEntity<Developer> createDeveloper(@RequestBody Developer param) {
        Developer developer = developerService.createDeveloper(param.getName(), param.getDescription());
        return ResponseEntity.ok(developer);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Developer> getDeveloper(@PathVariable int id) {
        Optional<Developer> developer = developerService.getDeveloperById(id);
        return developer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Developer> updateDeveloper(@PathVariable int id, @RequestParam String name, @RequestParam String description) {
        Developer developer = developerService.updateDeveloper(id, name, description);
        return ResponseEntity.ok(developer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable int id) {
        developerService.deleteDeveloper(id);
        return ResponseEntity.noContent().build();
    }
}