package org.lab1.service;

import java.util.List;
import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.exception.NotFoundException;
import org.lab1.json.ApplicationStatsJson;
import org.lab1.mapper.ApplicationStatsMapper;
import org.lab1.model.Application;
import org.lab1.model.ApplicationStats;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.ApplicationStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
  private final ApplicationStatsMapper applicationStatsMapper;
  private final Logger logger;

  @Autowired
  public ApplicationStatsService(
      final ApplicationStatsRepository applicationStatsRepositoryParam,
      final ApplicationRepository applicationRepositoryParam,
      final ApplicationStatsMapper applicationStatsMapperParam,
      @Qualifier("correlationLogger") final Logger loggerParam) {
    this.applicationStatsRepository = applicationStatsRepositoryParam;
    this.applicationRepository = applicationRepositoryParam;
    this.applicationStatsMapper = applicationStatsMapperParam;
    this.logger = loggerParam;
  }

  public final ApplicationStats save(final ApplicationStatsJson applicationStatsJson) {
    logger.info(SAVE_REQUEST_LOG + applicationStatsJson.getApplicationId());

    Application application =
        applicationRepository
            .findById(applicationStatsJson.getApplicationId())
            .orElseThrow(
                () -> {
                  logger.error(APP_NOT_FOUND_LOG + applicationStatsJson.getApplicationId());
                  return new NotFoundException(
                      "Application not found with ID: " + applicationStatsJson.getApplicationId());
                });

    ApplicationStats applicationStats = applicationStatsMapper.toEntity(applicationStatsJson);
    applicationStats.setApplication(application);

    ApplicationStats savedStats = applicationStatsRepository.save(applicationStats);
    logger.info(SAVED_STATS_LOG + savedStats.getId() + FOR_APP_ID_LOG + application.getId());
    return savedStats;
  }

  public final ApplicationStatsJson saveAndReturnJson(
      final ApplicationStatsJson applicationStatsJson) {
    ApplicationStats savedStats = save(applicationStatsJson);
    return applicationStatsMapper.toDto(savedStats);
  }

  public final Optional<ApplicationStats> findById(final int id) {
    logger.info(FIND_BY_ID_LOG + id);
    Optional<ApplicationStats> stats = applicationStatsRepository.findById(id);

    if (stats.isPresent()) {
      logger.info(STATS_FOUND_LOG + id);
    } else {
      logger.info(STATS_NOT_FOUND_LOG + id);
    }

    return stats;
  }

  public final Optional<ApplicationStatsJson> findByIdAsJson(final int id) {
    Optional<ApplicationStats> stats = findById(id);
    return stats.map(applicationStatsMapper::toDto);
  }

  public final List<ApplicationStats> findAll() {
    logger.info(FIND_ALL_LOG);
    List<ApplicationStats> allStats = applicationStatsRepository.findAll();
    logger.info(FOUND_ALL_LOG + allStats.size() + STATS_COUNT_LOG);
    return allStats;
  }

  public final List<ApplicationStatsJson> findAllAsJson() {
    logger.info(FIND_ALL_LOG);
    List<ApplicationStats> allStats = applicationStatsRepository.findAll();
    logger.info(FOUND_ALL_LOG + allStats.size() + STATS_COUNT_LOG);
    return allStats.stream().map(applicationStatsMapper::toDto).toList();
  }

  public final void delete(final int id) {
    logger.info(DELETE_LOG + id);
    applicationStatsRepository.deleteById(id);
    logger.info(DELETED_LOG + id);
  }
}
