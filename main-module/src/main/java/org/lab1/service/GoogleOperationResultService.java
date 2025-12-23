package org.lab1.service;

import java.util.List;
import org.lab.logger.Logger;
import org.lab1.model.GoogleOperationResult;
import org.lab1.repository.GoogleOperationResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GoogleOperationResultService {

  private static final String GET_RESULTS_BY_USER_LOG =
      "Fetching Google operation results for user ID: ";
  private static final String GET_RESULTS_BY_OPERATION_LOG =
      "Fetching Google operation results for operation: ";
  private static final String GET_LAST_RESULTS_LOG =
      "Fetching last {} Google operation results for user ID: ";
  private static final String GET_ERRORS_LOG = "Fetching Google operation results with errors";
  private static final String GET_SUCCESSFUL_LOG = "Fetching successful Google operation results";
  private static final String NO_RESULTS_FOUND_LOG = "No Google operation results found";
  private static final String RESULTS_FOUND_LOG = "Found {} Google operation results";

  private final GoogleOperationResultRepository repository;
  private final Logger logger;

  @Autowired
  public GoogleOperationResultService(
      final GoogleOperationResultRepository repository, final Logger logger) {
    this.repository = repository;
    this.logger = logger;
  }

  public final List<GoogleOperationResult> getResultsByUserId(Integer userId) {
    logger.info(GET_RESULTS_BY_USER_LOG + userId);
    List<GoogleOperationResult> results = repository.findByUserId(userId);

    if (results.isEmpty()) {
      logger.info(NO_RESULTS_FOUND_LOG);
    } else {
      logger.info(RESULTS_FOUND_LOG.replace("{}", String.valueOf(results.size())));
    }

    return results;
  }

  public final List<GoogleOperationResult> getResultsByOperation(String operation) {
    logger.info(GET_RESULTS_BY_OPERATION_LOG + operation);
    List<GoogleOperationResult> results = repository.findByOperation(operation);

    if (results.isEmpty()) {
      logger.info(NO_RESULTS_FOUND_LOG);
    } else {
      logger.info(RESULTS_FOUND_LOG.replace("{}", String.valueOf(results.size())));
    }

    return results;
  }

  public final List<GoogleOperationResult> getResultsByUserIdAndOperation(
      final Integer userId, final String operation) {
    logger.info(GET_RESULTS_BY_USER_LOG + userId + " and operation: " + operation);
    List<GoogleOperationResult> results = repository.findByUserIdAndOperation(userId, operation);

    if (results.isEmpty()) {
      logger.info(NO_RESULTS_FOUND_LOG);
    } else {
      logger.info(RESULTS_FOUND_LOG.replace("{}", String.valueOf(results.size())));
    }

    return results;
  }

  public final List<GoogleOperationResult> getLatestResultsByUserId(
      final Integer userId, final int limit) {
    logger.info(GET_LAST_RESULTS_LOG.replace("{}", String.valueOf(limit)) + userId);
    List<GoogleOperationResult> results = repository.findLatestByUserId(userId, limit);

    if (results.isEmpty()) {
      logger.info(NO_RESULTS_FOUND_LOG);
    } else {
      logger.info(RESULTS_FOUND_LOG.replace("{}", String.valueOf(results.size())));
    }

    return results;
  }

  public List<GoogleOperationResult> getOperationsWithErrors() {
    logger.info(GET_ERRORS_LOG);
    List<GoogleOperationResult> results = repository.findWithErrors();

    if (results.isEmpty()) {
      logger.info(NO_RESULTS_FOUND_LOG);
    } else {
      logger.info(RESULTS_FOUND_LOG.replace("{}", String.valueOf(results.size())));
    }

    return results;
  }

  public List<GoogleOperationResult> getSuccessfulOperations() {
    logger.info(GET_SUCCESSFUL_LOG);
    List<GoogleOperationResult> results = repository.findSuccessfulOperations();

    if (results.isEmpty()) {
      logger.info(NO_RESULTS_FOUND_LOG);
    } else {
      logger.info(RESULTS_FOUND_LOG.replace("{}", String.valueOf(results.size())));
    }

    return results;
  }

  public final GoogleOperationResult getResultById(final Long id) {
    return repository.findById(id).orElse(null);
  }

  public final List<GoogleOperationResult> getResultsByUserIds(final List<Integer> userIds) {
    logger.info("Fetching Google operation results for user IDs: " + userIds);

    List<GoogleOperationResult> allResults =
        repository.findAll().stream()
            .filter(result -> userIds.contains(result.getUserId()))
            .toList();

    logger.info(RESULTS_FOUND_LOG.replace("{}", String.valueOf(allResults.size())));
    return allResults;
  }
}
