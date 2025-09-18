package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.model.Developer;
import org.lab1.model.User;
import org.lab1.repository.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeveloperService {

    private final DeveloperRepository developerRepository;
    private final Logger logger;

    @Autowired
    public DeveloperService(DeveloperRepository developerRepository, Logger logger) {
        this.developerRepository = developerRepository;
        this.logger = logger;
    }

    public Developer createDeveloper(String name, String description) {
        logger.info("Creating developer with name: " + name);
        Developer developer = new Developer();
        developer.setName(name);
        developer.setDescription(description);
        Developer savedDeveloper = developerRepository.save(developer);
        logger.info("Developer created with ID: " + savedDeveloper.getId());
        return savedDeveloper;
    }

    public Developer createDeveloper(User user) {
        logger.info("Creating developer from user: " + user.getUsername());
        Developer developer = new Developer();
        developer.setName(user.getUsername());
        developer.setDescription(user.getEmail());
        developer.setUser(user);
        Developer savedDeveloper = developerRepository.save(developer);
        logger.info("Developer created with ID: " + savedDeveloper.getId() + " from user: " + user.getUsername());
        return savedDeveloper;
    }

    public Optional<Developer> getDeveloperById(int id) {
        logger.info("Fetching developer by ID: " + id);
        Optional<Developer> developer = developerRepository.findById(id);
        if (developer.isPresent()) {
            logger.info("Developer found with ID: " + id);
        } else {
            logger.info("Developer not found with ID: " + id);
        }
        return developer;
    }

    public Developer updateDeveloper(int id, String name, String description) {
        logger.info("Updating developer with ID: " + id + ", name: " + name);
        Developer developer = developerRepository.findById(id).orElseThrow(() -> {
            logger.error("Developer not found with ID: " + id + " for update.");
            return new RuntimeException("Developer not found");
        });
        developer.setName(name);
        developer.setDescription(description);
        Developer updatedDeveloper = developerRepository.save(developer);
        logger.info("Developer updated with ID: " + updatedDeveloper.getId());
        return updatedDeveloper;
    }

    public void deleteDeveloper(int id) {
        logger.info("Deleting developer with ID: " + id);
        developerRepository.deleteById(id);
        logger.info("Developer deleted with ID: " + id);
    }
}