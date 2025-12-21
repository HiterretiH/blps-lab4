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
    private static final String SAVE_REQUEST_LOG = "Saving ApplicationStats for application ID: ";
    private static final String APP_NOT_FOUND_LOG = "Application not found with ID: ";
    private static final String APP_NOT_FOUND_MSG = "Application not found";
    private static final String SAVED_STATS_LOG = "ApplicationStats saved with ID: ";
    private static final String FOR_APP_ID_LOG = " for application ID: ";
    private static final String FIND_BY_ID_LOG = "Finding ApplicationStats by ID: ";
    private static final String STATS_FOUND_LOG = "ApplicationStats found with ID: ";
    private static final String STATS_NOT_FOUND_LOG = "ApplicationStats not found with ID: ";
    private static final String FIND_ALL_LOG = "Finding all ApplicationStats";
    private static final String FOUND_ALL_LOG = "Found ";
    private static final String STATS_COUNT_LOG = " ApplicationStats";
    private static final String DELETE_LOG = "Deleting ApplicationStats with ID: ";
    private static final String DELETED_LOG = "ApplicationStats deleted with ID: ";

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
        logger.info(SAVE_REQUEST_LOG + applicationStatsJson.getApplicationId());
        Application application = applicationRepository.findById(applicationStatsJson.getApplicationId())
                .orElseThrow(() -> {
                    logger.error(APP_NOT_FOUND_LOG + applicationStatsJson.getApplicationId());
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, APP_NOT_FOUND_MSG);
                });

        ApplicationStats applicationStats = new ApplicationStats();
        applicationStats.setId(application.getId());
        applicationStats.setApplication(application);
        applicationStats.setDownloads(applicationStatsJson.getDownloads());
        applicationStats.setRating(applicationStatsJson.getRating());

        ApplicationStats savedStats = applicationStatsRepository.save(applicationStats);
        logger.info(SAVED_STATS_LOG + savedStats.getId() + FOR_APP_ID_LOG + application.getId());
        return savedStats;
    }

    public Optional<ApplicationStats> findById(int id) {
        logger.info(FIND_BY_ID_LOG + id);
        Optional<ApplicationStats> stats = applicationStatsRepository.findById(id);

        if (stats.isPresent()) {
            logger.info(STATS_FOUND_LOG + id);
        } else {
            logger.info(STATS_NOT_FOUND_LOG + id);
        }

        return stats;
    }

    public List<ApplicationStats> findAll() {
        logger.info(FIND_ALL_LOG);
        List<ApplicationStats> allStats = applicationStatsRepository.findAll();
        logger.info(FOUND_ALL_LOG + allStats.size() + STATS_COUNT_LOG);
        return allStats;
    }

    public void delete(int id) {
        logger.info(DELETE_LOG + id);
        applicationStatsRepository.deleteById(id);
        logger.info(DELETED_LOG + id);
    }
}
