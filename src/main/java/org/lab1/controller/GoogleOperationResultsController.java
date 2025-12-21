package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.model.GoogleOperationResult;
import org.lab1.model.User;
import org.lab1.repository.UserRepository;
import org.lab1.service.GoogleOperationResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/google-results")
public class GoogleOperationResultsController {

    private static final String GET_USER_RESULTS_LOG = "Received request to get Google operation results for current user";
    private static final String GET_USER_RESULTS_BY_ID_LOG = "Received request to get Google operation results for user ID: ";
    private static final String GET_RESULTS_BY_OPERATION_LOG = "Received request to get Google operation results for operation: ";
    private static final String GET_LAST_RESULTS_LOG = "Received request to get last {} Google operation results for current user";
    private static final String GET_ERRORS_LOG = "Received request to get Google operation results with errors";
    private static final String GET_SUCCESSFUL_LOG = "Received request to get successful Google operation results";
    private static final String GET_SPECIFIC_RESULT_LOG = "Received request to get Google operation result by ID: ";
    private static final String USER_NOT_FOUND_LOG = "User not found: ";
    private static final String UNAUTHORIZED_ACCESS_LOG = "Unauthorized access attempt to user results";

    private final GoogleOperationResultService googleResultService;
    private final UserRepository userRepository;
    private final Logger logger;

    @Autowired
    public GoogleOperationResultsController(GoogleOperationResultService googleResultService,
                                            UserRepository userRepository,
                                            Logger logger) {
        this.googleResultService = googleResultService;
        this.userRepository = userRepository;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('google_results.read.own')")
    @GetMapping("/my-results")
    public ResponseEntity<List<GoogleOperationResult>> getMyResults() {
        logger.info(GET_USER_RESULTS_LOG);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());

        if (userOptional.isEmpty()) {
            logger.error(USER_NOT_FOUND_LOG + authentication.getPrincipal());
            return ResponseEntity.status(401).build();
        }

        User user = userOptional.get();
        List<GoogleOperationResult> results = googleResultService.getResultsByUserId(user.getId());
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("hasAuthority('google_results.read.all')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GoogleOperationResult>> getResultsByUserId(@PathVariable Integer userId) {
        logger.info(GET_USER_RESULTS_BY_ID_LOG + userId);Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            logger.error(USER_NOT_FOUND_LOG + authentication.getPrincipal());
            return ResponseEntity.status(401).build();
        }

        List<GoogleOperationResult> results = googleResultService.getResultsByUserId(userId);
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("hasAuthority('google_results.read.by_operation')")
    @GetMapping("/operation/{operation}")
    public ResponseEntity<List<GoogleOperationResult>> getResultsByOperation(@PathVariable String operation) {
        logger.info(GET_RESULTS_BY_OPERATION_LOG + operation);
        List<GoogleOperationResult> results = googleResultService.getResultsByOperation(operation);
        return ResponseEntity.ok(results);
    }

    @PreAuthorize("hasAuthority('google_results.read.own')")
    @GetMapping("/my-results/latest/{limit}")
    public ResponseEntity<List<GoogleOperationResult>> getMyLatestResults(@PathVariable int limit) {
        logger.info(GET_LAST_RESULTS_LOG.replace("{}", String.valueOf(limit)));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());

        if (userOptional.isEmpty()) {
            logger.error(USER_NOT_FOUND_LOG + authentication.getPrincipal());
            return ResponseEntity.status(401).build();
        }

        User user = userOptional.get();
        List<GoogleOperationResult> results = googleResultService.getLatestResultsByUserId(user.getId(), limit);
        return ResponseEntity.ok(results);
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
    public ResponseEntity<GoogleOperationResult> getResultById(@PathVariable Long id) {
        logger.info(GET_SPECIFIC_RESULT_LOG + id);
        GoogleOperationResult result = googleResultService.getResultById(id);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());

        if (userOptional.isEmpty()) {
            logger.error(USER_NOT_FOUND_LOG + authentication.getPrincipal());
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasAuthority('google_results.read.own')")
    @GetMapping("/my-results/operation/{operation}")
    public ResponseEntity<List<GoogleOperationResult>> getMyResultsByOperation(@PathVariable String operation) {
        logger.info(GET_RESULTS_BY_OPERATION_LOG + operation);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());

        if (userOptional.isEmpty()) {
            logger.error(USER_NOT_FOUND_LOG + authentication.getPrincipal());
            return ResponseEntity.status(401).build();
        }

        User user = userOptional.get();
        List<GoogleOperationResult> results = googleResultService.getResultsByUserIdAndOperation(user.getId(), operation);
        return ResponseEntity.ok(results);
    }
}
