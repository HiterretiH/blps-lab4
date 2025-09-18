package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.json.ApplicationStatsJson;
import org.lab1.model.Application;
import org.lab1.model.ApplicationStats;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.ApplicationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicationStatsService {

    private final ApplicationStatsRepository applicationStatsRepository;
    private final ApplicationRepository applicationRepository;
    private final Logger logger;

    @Autowired
    public ApplicationStatsService(ApplicationStatsRepository applicationStatsRepository,
                                   ApplicationRepository applicationRepository,
                                   Logger logger) {
        this.applicationStatsRepository = applicationStatsRepository;
        this.applicationRepository = applicationRepository;
        this.logger = logger;
    }

    public ApplicationStats save(ApplicationStatsJson applicationStatsJson) {
        logger.info("Saving ApplicationStats for application ID: " + applicationStatsJson.getApplicationId());
        Application application = applicationRepository.findById(applicationStatsJson.getApplicationId())
                .orElseThrow(() -> {
                    logger.error("Application not found with ID: " + applicationStatsJson.getApplicationId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
                });

        ApplicationStats applicationStats = new ApplicationStats();
        applicationStats.setId(application.getId());
        applicationStats.setApplication(application);
        applicationStats.setDownloads(applicationStatsJson.getDownloads());
        applicationStats.setRating(applicationStatsJson.getRating());

        ApplicationStats savedStats = applicationStatsRepository.save(applicationStats);
        logger.info("ApplicationStats saved with ID: " + savedStats.getId() + " for application ID: " + application.getId());
        return savedStats;
    }

    public Optional<ApplicationStats> findById(int id) {
        logger.info("Finding ApplicationStats by ID: " + id);
        Optional<ApplicationStats> stats = applicationStatsRepository.findById(id);
        if (stats.isPresent()) {
            logger.info("ApplicationStats found with ID: " + id);
        } else {
            logger.info("ApplicationStats not found with ID: " + id);
        }
        return stats;
    }

    public List<ApplicationStats> findAll() {
        logger.info("Finding all ApplicationStats");
        List<ApplicationStats> allStats = applicationStatsRepository.findAll();
        logger.info("Found " + allStats.size() + " ApplicationStats");
        return allStats;
    }

    public void delete(int id) {
        logger.info("Deleting ApplicationStats with ID: " + id);
        applicationStatsRepository.deleteById(id);
        logger.info("ApplicationStats deleted with ID: " + id);
    }
}