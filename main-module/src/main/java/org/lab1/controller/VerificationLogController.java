package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.exception.NotFoundException;
import org.lab1.model.VerificationLog;
import org.lab1.service.VerificationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification-logs")
public class VerificationLogController {
  private static final String CREATE_LOG = "Creating VerificationLog";
  private static final String GET_LOG = "Getting VerificationLog by ID: ";

  private final VerificationLogService verificationLogService;
  private final Logger logger;

  @Autowired
  public VerificationLogController(
      final VerificationLogService verificationLogServiceParam, final Logger loggerParam) {
    this.verificationLogService = verificationLogServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('verification_log.manage')")
  @PostMapping
  public ResponseEntity<VerificationLog> createVerificationLog(
      @RequestParam final boolean securityCheckPassed,
      @RequestParam final boolean policyCheckPassed,
      @RequestParam final boolean adsCheckPassed,
      @RequestParam final String logMessage) {
    logger.info(CREATE_LOG);
    VerificationLog verificationLog =
        verificationLogService.createVerificationLog(
            securityCheckPassed, policyCheckPassed, adsCheckPassed, logMessage);
    return ResponseEntity.ok(verificationLog);
  }

  @PreAuthorize("hasAuthority('verification_log.read')")
  @GetMapping("/{id}")
  public ResponseEntity<VerificationLog> getVerificationLog(@PathVariable final int id) {
    logger.info(GET_LOG + id);
    VerificationLog verificationLog =
        verificationLogService
            .getVerificationLogById(id)
            .orElseThrow(() -> new NotFoundException("VerificationLog not found with ID: " + id));

    return ResponseEntity.ok(verificationLog);
  }
}
