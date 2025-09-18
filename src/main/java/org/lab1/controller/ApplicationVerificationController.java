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
    private final ApplicationVerificationService applicationVerificationService;
    private final Logger logger;

    @Autowired
    public ApplicationVerificationController(ApplicationVerificationService applicationVerificationService, Logger logger) {
        this.applicationVerificationService = applicationVerificationService;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('application.verify')")
    @PostMapping("/verify")
    public ResponseEntity<String> verifyApplicationForm(@RequestBody ApplicationForm form) {
        logger.info("Received request to verify application form.");
        VerificationLog verificationLog = applicationVerificationService.verifyApplicationForm(form);

        if (!verificationLog.isSecurityCheckPassed()) {
            logger.info("Security check failed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Security check failed.");
        }
        if (!verificationLog.isPolicyCheckPassed()) {
            logger.info("Policy check failed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Policy check failed.");
        }
        if (!verificationLog.isAdsCheckPassed()) {
            logger.info("Ads check failed.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ads check failed.");
        }

        logger.info("Application form verification successful.");
        return ResponseEntity.ok(verificationLog.getLogMessage());
    }
}