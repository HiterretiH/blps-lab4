package org.lab1.service;

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

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository, DeveloperRepository developerRepository) {
        this.applicationRepository = applicationRepository;
        this.developerRepository = developerRepository;
    }

    @GetMapping("/api/applications")
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @PostMapping("/api/applications")
    public ResponseEntity<Application> submitApplicationForCheck(@RequestBody Application application) {
        Application savedApplication = applicationRepository.save(application);
        return ResponseEntity.ok(savedApplication);
    }

    @GetMapping("/api/applications/status/{applicationId}")
    public ResponseEntity<ApplicationStatus> getApplicationCheckStatus(@PathVariable int applicationId) {
        Optional<Application> application = applicationRepository.findById(applicationId);
        if (application.isPresent()) {
            return ResponseEntity.ok(application.get().getStatus());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/api/applications/{applicationId}")
    public ResponseEntity<Application> getApplication(@PathVariable int applicationId) {
        Optional<Application> application = applicationRepository.findById(applicationId);
        return application.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/api/applications/developer/{developerId}")
    public ResponseEntity<List<Application>> getApplicationsByDeveloperId(@PathVariable int developerId) {
        List<Application> applications = applicationRepository.findByDeveloperId(developerId);
        return ResponseEntity.ok(applications);
    }

    public Application createApplication(ApplicationJson applicationJson) {
        Developer developer = developerRepository.findById(applicationJson.getDeveloperId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Developer not found"));

        Application application = new Application();
        application.setDeveloper(developer);
        application.setName(applicationJson.getName());
        application.setType(applicationJson.getType());
        application.setPrice(applicationJson.getPrice());
        application.setDescription(applicationJson.getDescription());
        application.setStatus(applicationJson.getStatus());

        return applicationRepository.save(application);
    }

    public Optional<Application> getApplicationById(int id) {
        return applicationRepository.findById(id);
    }

    public Application updateApplication(int id, Developer developer, String name, ApplicationType type, double price, String description, ApplicationStatus status) {
        Application application = applicationRepository.findById(id).orElseThrow();
        application.setDeveloper(developer);
        application.setName(name);
        application.setType(type);
        application.setPrice(price);
        application.setDescription(description);
        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public void deleteApplication(int id) {
        applicationRepository.deleteById(id);
    }
}
