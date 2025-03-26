package org.lab1.controller;

import org.lab1.model.Application;
import org.lab1.model.ApplicationForm;
import org.lab1.model.VerificationLog;
import org.lab1.service.ApplicationVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationVerificationController {
    @Autowired
    private ApplicationVerificationService applicationVerificationService;

    @PreAuthorize("hasAuthority('application.verify')")
    @PostMapping("/verify")
    public ResponseEntity<String> verifyApplicationForm(@RequestBody ApplicationForm form) {
        VerificationLog verificationLog = applicationVerificationService.verifyApplicationForm(form);

        if (!verificationLog.isSecurityCheckPassed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Security check failed.");
        }
        if (!verificationLog.isPolicyCheckPassed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Policy check failed.");
        }
        if (!verificationLog.isAdsCheckPassed()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ads check failed.");
        }

        return ResponseEntity.ok(verificationLog.getLogMessage());
    }
}
