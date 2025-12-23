package org.lab1.controller;

import java.util.List;
import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.json.ApplicationStatsJson;
import org.lab1.model.ApplicationStats;
import org.lab1.service.ApplicationStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/application-stats")
public final class ApplicationStatsController {
  private static final String CREATE_REQUEST_LOG =
      "Received request to create ApplicationStats";
  private static final String CREATE_SUCCESS_LOG =
      "ApplicationStats created with ID: ";
  private static final String UPDATE_REQUEST_LOG =
      "Received request to update ApplicationStats with ID: ";
  private static final String UPDATE_SUCCESS_LOG =
      "ApplicationStats updated with ID: ";
  private static final String GET_REQUEST_LOG =
      "Received request to get ApplicationStats with ID: ";
  private static final String GET_FOUND_LOG = "ApplicationStats found with ID: ";
  private static final String GET_NOT_FOUND_LOG = "ApplicationStats not found with ID: ";
  private static final String GET_ALL_REQUEST_LOG =
      "Received request to get all ApplicationStats";
  private static final String GET_ALL_FOUND_LOG = "Found ";
  private static final String APPLICATION_STATS_COUNT_LOG = " ApplicationStats";
  private static final String DELETE_REQUEST_LOG =
      "Received request to delete ApplicationStats with ID: ";
  private static final String DELETE_SUCCESS_LOG = "ApplicationStats deleted with ID: ";

  private final ApplicationStatsService applicationStatsService;
  private final Logger logger;

  @Autowired
  public ApplicationStatsController(
      final ApplicationStatsService applicationStatsServiceParam,
      final Logger loggerParam) {
    this.applicationStatsService = applicationStatsServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('application_stats.manage')")
  @PostMapping
  public ApplicationStats create(@RequestBody final ApplicationStatsJson applicationStatsJson) {
    logger.info(CREATE_REQUEST_LOG);
    ApplicationStats stats = applicationStatsService.save(applicationStatsJson);
    logger.info(CREATE_SUCCESS_LOG + stats.getId());
    return stats;
  }

  @PreAuthorize("hasAuthority('application_stats.manage')")
  @PutMapping("/{id}")
  public ApplicationStats update(
      @PathVariable final int id,
      @RequestBody final ApplicationStatsJson applicationStats) {
    logger.info(UPDATE_REQUEST_LOG + id);
    applicationStats.setId(id);
    ApplicationStats updatedStats = applicationStatsService.save(applicationStats);
    logger.info(UPDATE_SUCCESS_LOG + updatedStats.getId());
    return updatedStats;
  }

  @PreAuthorize("hasAuthority('application_stats.read')")
  @GetMapping("/{id}")
  public Optional<ApplicationStats> getById(@PathVariable final int id) {
    logger.info(GET_REQUEST_LOG + id);
    Optional<ApplicationStats> stats = applicationStatsService.findById(id);

    if (stats.isPresent()) {
      logger.info(GET_FOUND_LOG + id);
    } else {
      logger.info(GET_NOT_FOUND_LOG + id);
    }

    return stats;
  }

  @PreAuthorize("hasAuthority('application_stats.read')")
  @GetMapping
  public List<ApplicationStats> getAll() {
    logger.info(GET_ALL_REQUEST_LOG);
    List<ApplicationStats> allStats = applicationStatsService.findAll();
    logger.info(GET_ALL_FOUND_LOG + allStats.size()
        + APPLICATION_STATS_COUNT_LOG);
    return allStats;
  }

  @PreAuthorize("hasAuthority('application_stats.manage')")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable final int id) {
    logger.info(DELETE_REQUEST_LOG + id);
    applicationStatsService.delete(id);
    logger.info(DELETE_SUCCESS_LOG + id);
  }
}
