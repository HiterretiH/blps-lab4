package org.lab1.controller;

import java.util.List;
import org.lab.logger.Logger;
import org.lab1.exception.NotFoundException;
import org.lab1.exception.UnauthorizedException;
import org.lab1.model.GoogleOperationResult;
import org.lab1.model.User;
import org.lab1.service.GoogleOperationResultService;
import org.lab1.service.UserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/google-results")
public class GoogleOperationResultsController {
  private static final String GET_USER_RESULTS_LOG =
      "Received request to get Google operation results for current user";
  private static final String GET_USER_RESULTS_BY_ID_LOG =
      "Received request to get Google operation results for user ID: ";
  private static final String GET_RESULTS_BY_OPERATION_LOG =
      "Received request to get Google operation results for operation: ";
  private static final String GET_LAST_RESULTS_LOG =
      "Received request to get last {} Google operation results for current user";
  private static final String GET_ERRORS_LOG =
      "Received request to get Google operation results with errors";
  private static final String GET_SUCCESSFUL_LOG =
      "Received request to get successful Google operation results";
  private static final String GET_SPECIFIC_RESULT_LOG =
      "Received request to get Google operation result by ID: ";
  private static final String USER_NOT_FOUND_LOG = "User not found: ";
  private static final String UNAUTHORIZED_ACCESS_LOG =
      "Unauthorized access attempt to user results";

  private final GoogleOperationResultService googleResultService;
  private final UserQueryService userQueryService;
  private final Logger logger;

  @Autowired
  public GoogleOperationResultsController(
      final GoogleOperationResultService googleResultServiceParam,
      final UserQueryService userQueryServiceParam,
      final Logger loggerParam) {
    this.googleResultService = googleResultServiceParam;
    this.userQueryService = userQueryServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('google_results.read.own')")
  @GetMapping("/my-results")
  public ResponseEntity<List<GoogleOperationResult>> getMyResults() {
    logger.info(GET_USER_RESULTS_LOG);

    try {
      User user = userQueryService.getCurrentAuthenticatedUser();
      List<GoogleOperationResult> results = googleResultService.getResultsByUserId(user.getId());
      return ResponseEntity.ok(results);
    } catch (Exception exception) {
      logger.error(USER_NOT_FOUND_LOG + exception.getMessage());
      throw new UnauthorizedException("User not found: " + exception.getMessage());
    }
  }

  @PreAuthorize("hasAuthority('google_results.read.all')")
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<GoogleOperationResult>> getResultsByUserId(
      @PathVariable final Integer userId) {
    logger.info(GET_USER_RESULTS_BY_ID_LOG + userId);

    try {
      userQueryService.getCurrentAuthenticatedUser();
    } catch (Exception exception) {
      logger.error(USER_NOT_FOUND_LOG + exception.getMessage());
      throw new UnauthorizedException("User not found: " + exception.getMessage());
    }

    List<GoogleOperationResult> results = googleResultService.getResultsByUserId(userId);
    return ResponseEntity.ok(results);
  }

  @PreAuthorize("hasAuthority('google_results.read.by_operation')")
  @GetMapping("/operation/{operation}")
  public ResponseEntity<List<GoogleOperationResult>> getResultsByOperation(
      @PathVariable final String operation) {
    logger.info(GET_RESULTS_BY_OPERATION_LOG + operation);
    List<GoogleOperationResult> results = googleResultService.getResultsByOperation(operation);
    return ResponseEntity.ok(results);
  }

  @PreAuthorize("hasAuthority('google_results.read.own')")
  @GetMapping("/my-results/latest/{limit}")
  public ResponseEntity<List<GoogleOperationResult>> getMyLatestResults(
      @PathVariable final int limit) {
    logger.info(GET_LAST_RESULTS_LOG.replace("{}", String.valueOf(limit)));

    try {
      User user = userQueryService.getCurrentAuthenticatedUser();
      List<GoogleOperationResult> results =
          googleResultService.getLatestResultsByUserId(user.getId(), limit);
      return ResponseEntity.ok(results);
    } catch (Exception exception) {
      logger.error(USER_NOT_FOUND_LOG + exception.getMessage());
      throw new UnauthorizedException("User not found: " + exception.getMessage());
    }
  }

  @PreAuthorize("hasAuthority('google_results.read.errors')")
  @GetMapping("/errors")
  public ResponseEntity<List<GoogleOperationResult>> getOperationsWithErrors() {
    logger.info(GET_ERRORS_LOG);
    List<GoogleOperationResult> results = googleResultService.getOperationsWithErrors();
    return ResponseEntity.ok(results);
  }

  @PreAuthorize("hasAuthority('google_results.read.successful')")
  @GetMapping("/successful")
  public ResponseEntity<List<GoogleOperationResult>> getSuccessfulOperations() {
    logger.info(GET_SUCCESSFUL_LOG);
    List<GoogleOperationResult> results = googleResultService.getSuccessfulOperations();
    return ResponseEntity.ok(results);
  }

  @PreAuthorize("hasAuthority('google_results.read.by_id')")
  @GetMapping("/{id}")
  public ResponseEntity<GoogleOperationResult> getResultById(@PathVariable final Long id) {
    logger.info(GET_SPECIFIC_RESULT_LOG + id);
    GoogleOperationResult result = googleResultService.getResultById(id);

    if (result == null) {
      throw new NotFoundException("GoogleOperationResult not found with ID: " + id);
    }

    try {
      userQueryService.getCurrentAuthenticatedUser();
    } catch (Exception exception) {
      logger.error(USER_NOT_FOUND_LOG + exception.getMessage());
      throw new UnauthorizedException("User not found: " + exception.getMessage());
    }

    return ResponseEntity.ok(result);
  }

  @PreAuthorize("hasAuthority('google_results.read.own')")
  @GetMapping("/my-results/operation/{operation}")
  public ResponseEntity<List<GoogleOperationResult>> getMyResultsByOperation(
      @PathVariable final String operation) {
    logger.info(GET_RESULTS_BY_OPERATION_LOG + operation);

    try {
      User user = userQueryService.getCurrentAuthenticatedUser();
      List<GoogleOperationResult> results =
          googleResultService.getResultsByUserIdAndOperation(user.getId(), operation);
      return ResponseEntity.ok(results);
    } catch (Exception exception) {
      logger.error(USER_NOT_FOUND_LOG + exception.getMessage());
      throw new UnauthorizedException("User not found: " + exception.getMessage());
    }
  }
}
