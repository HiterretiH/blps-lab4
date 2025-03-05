package org.lab1.service;

import org.lab1.model.Developer;
import org.lab1.repository.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeveloperService {

    private final DeveloperRepository developerRepository;

    @Autowired
    public DeveloperService(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    public Developer createDeveloper(String name, String description) {
        Developer developer = new Developer();
        developer.setName(name);
        developer.setDescription(description);
        return developerRepository.save(developer);
    }

    public Optional<Developer> getDeveloperById(int id) {
        return developerRepository.findById(id);
    }

    public Developer updateDeveloper(int id, String name, String description) {
        Developer developer = developerRepository.findById(id).orElseThrow();
        developer.setName(name);
        developer.setDescription(description);
        return developerRepository.save(developer);
    }

    public void deleteDeveloper(int id) {
        developerRepository.deleteById(id);
    }
}