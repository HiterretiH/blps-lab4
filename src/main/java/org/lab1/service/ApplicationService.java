package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.json.ApplicationJson;
import org.lab1.model.Application;
import org.lab1.model.ApplicationStatus;
import org.lab1.model.ApplicationType;
import org.lab1.model.Developer;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final DeveloperRepository developerRepository;
    private final Logger logger;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository,
                              DeveloperRepository developerRepository,
                              Logger logger) {
        this.applicationRepository = applicationRepository;
        this.developerRepository = developerRepository;
        this.logger = logger;
    }

    @GetMapping("/api/applications")
    public List<Application> getAllApplications() {
        logger.info("Fetching all applications");
        List<Application> applications = applicationRepository.findAll();
        logger.info("Found " + applications.size() + " applications");
        return applications;
    }

    @PostMapping("/api/applications")
    public ResponseEntity<Application> submitApplicationForCheck(@RequestBody Application application) {
        logger.info("Submitting application for check: " + application.getName());
        try {
            Application savedApplication = applicationRepository.save(application);
            logger.info("Application submitted successfully with ID: " + savedApplication.getId());
            return ResponseEntity.ok(savedApplication);
        } catch (Exception e) {
            logger.error("Failed to submit application: " + e.getMessage());
            throw e;
        }
    }

    @GetMapping("/api/applications/status/{applicationId}")
    public ResponseEntity<ApplicationStatus> getApplicationCheckStatus(@PathVariable int applicationId) {
        logger.info("Checking status for application ID: " + applicationId);
        Optional<Application> application = applicationRepository.findById(applicationId);
        if (application.isPresent()) {
            logger.info("Found status for application ID: " + applicationId + " - " + application.get().getStatus());
            return ResponseEntity.ok(application.get().getStatus());
        }
        logger.error("Application not found for status check with ID: " + applicationId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/api/applications/{applicationId}")
    public ResponseEntity<Application> getApplication(@PathVariable int applicationId) {
        logger.info("Fetching application with ID: " + applicationId);
        Optional<Application> application = applicationRepository.findById(applicationId);
        if (application.isPresent()) {
            logger.info("Found application with ID: " + applicationId);
            return ResponseEntity.ok(application.get());
        }
        logger.error("Application not found with ID: " + applicationId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/api/applications/developer/{developerId}")
    public ResponseEntity<List<Application>> getApplicationsByDeveloperId(@PathVariable int developerId) {
        logger.info("Fetching applications for developer ID: " + developerId);
        List<Application> applications = applicationRepository.findByDeveloperId(developerId);
        logger.info("Found " + applications.size() + " applications for developer ID: " + developerId);
        return ResponseEntity.ok(applications);
    }

    public Application createApplication(ApplicationJson applicationJson) {
        logger.info("Creating new application for developer ID: " + applicationJson.getDeveloperId());
        Developer developer = developerRepository.findById(applicationJson.getDeveloperId())
                .orElseThrow(() -> {
                    logger.error("Developer not found with ID: " + applicationJson.getDeveloperId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer not found");
                });

        Application application = new Application();
        application.setDeveloper(developer);
        application.setName(applicationJson.getName());
        application.setType(applicationJson.getType());
        application.setPrice(applicationJson.getPrice());
        application.setDescription(applicationJson.getDescription());
        application.setStatus(applicationJson.getStatus());

        try {
            Application savedApplication = applicationRepository.save(application);
            logger.info("Created application with ID: " + savedApplication.getId());
            return savedApplication;
        } catch (Exception e) {
            logger.error("Failed to create application: " + e.getMessage());
            throw e;
        }
    }

    public Optional<Application> getApplicationById(int id) {
        logger.info("Looking up application with ID: " + id);
        Optional<Application> application = applicationRepository.findById(id);
        if (application.isPresent()) {
            logger.info("Found application with ID: " + id);
        } else {
            logger.error("Application not found with ID: " + id);
        }
        return application;
    }

    public Application updateApplication(int id, Developer developer, String name,
                                         ApplicationType type, double price,
                                         String description, ApplicationStatus status) {
        logger.info("Updating application with ID: " + id);
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Application not found for update with ID: " + id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
                });

        application.setDeveloper(developer);
        application.setName(name);
        application.setType(type);
        application.setPrice(price);
        application.setDescription(description);
        application.setStatus(status);

        try {
            Application updatedApplication = applicationRepository.save(application);
            logger.info("Successfully updated application with ID: " + id);
            return updatedApplication;
        } catch (Exception e) {
            logger.error("Failed to update application with ID: " + id + ". Error: " + e.getMessage());
            throw e;
        }
    }

    public void deleteApplication(int id) {
        logger.info("Attempting to delete application with ID: " + id);
        try {
            if (applicationRepository.existsById(id)) {
                applicationRepository.deleteById(id);
                logger.info("Successfully deleted application with ID: " + id);
            } else {
                logger.error("Application not found for deletion with ID: " + id);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
            }
        } catch (Exception e) {
            logger.error("Failed to delete application with ID: " + id + ". Error: " + e.getMessage());
            throw e;
        }
    }
}
