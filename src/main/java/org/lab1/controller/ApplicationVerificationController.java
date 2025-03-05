package org.lab1.controller;

import org.lab1.model.Application;
import org.lab1.model.ApplicationForm;
import org.lab1.service.ApplicationVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationVerificationController {

    @Autowired
    private ApplicationVerificationService applicationVerificationService;

    @PostMapping("/verify")
    public ResponseEntity<String> verifyApplicationForm(@RequestBody ApplicationForm form) {
        var verificationLog = applicationVerificationService.verifyApplicationForm(form);
        return ResponseEntity.ok(verificationLog.getLogMessage());
    }
}
