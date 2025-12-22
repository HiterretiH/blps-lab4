package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.model.ApplicationForm;
import org.lab1.model.VerificationLog;
import org.lab1.service.ApplicationVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationVerificationController {
  private static final String VERIFICATION_REQUEST_LOG =
      "Received request to verify application form.";
  private static final String SECURITY_CHECK_FAILED_LOG = "Security check failed.";
  private static final String POLICY_CHECK_FAILED_LOG = "Policy check failed.";
  private static final String ADS_CHECK_FAILED_LOG = "Ads check failed.";
  private static final String VERIFICATION_SUCCESS_LOG =
      "Application form verification successful.";
  private static final String SECURITY_CHECK_FAILED_MESSAGE = "Security check failed.";
  private static final String POLICY_CHECK_FAILED_MESSAGE = "Policy check failed.";
  private static final String ADS_CHECK_FAILED_MESSAGE = "Ads check failed.";

  private final ApplicationVerificationService applicationVerificationService;
  private final Logger logger;

  @Autowired
  public ApplicationVerificationController(
      ApplicationVerificationService applicationVerificationService, Logger logger) {
    this.applicationVerificationService = applicationVerificationService;
    this.logger = logger;
  }

  @PreAuthorize("hasAuthority('application.verify')")
  @PostMapping("/verify")
  public ResponseEntity<String> verifyApplicationForm(@RequestBody ApplicationForm form) {
    logger.info(VERIFICATION_REQUEST_LOG);
    VerificationLog verificationLog = applicationVerificationService.verifyApplicationForm(form);

    if (!verificationLog.isSecurityCheckPassed()) {
      logger.info(SECURITY_CHECK_FAILED_LOG);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(SECURITY_CHECK_FAILED_MESSAGE);
    }

    if (!verificationLog.isPolicyCheckPassed()) {
      logger.info(POLICY_CHECK_FAILED_LOG);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(POLICY_CHECK_FAILED_MESSAGE);
    }

    if (!verificationLog.isAdsCheckPassed()) {
      logger.info(ADS_CHECK_FAILED_LOG);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ADS_CHECK_FAILED_MESSAGE);
    }

    logger.info(VERIFICATION_SUCCESS_LOG);
    return ResponseEntity.ok(verificationLog.getLogMessage());
  }
}
