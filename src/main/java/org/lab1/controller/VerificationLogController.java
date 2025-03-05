package org.lab1.controller;

import org.lab1.model.VerificationLog;
import org.lab1.service.VerificationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/verification-logs")
public class VerificationLogController {

    private final VerificationLogService verificationLogService;

    @Autowired
    public VerificationLogController(VerificationLogService verificationLogService) {
        this.verificationLogService = verificationLogService;
    }

    @PostMapping
    public ResponseEntity<VerificationLog> createVerificationLog(@RequestParam boolean securityCheckPassed, @RequestParam boolean policyCheckPassed, @RequestParam boolean adsCheckPassed, @RequestParam String logMessage) {
        VerificationLog verificationLog = verificationLogService.createVerificationLog(securityCheckPassed, policyCheckPassed, adsCheckPassed, logMessage);
        return ResponseEntity.ok(verificationLog);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VerificationLog> getVerificationLog(@PathVariable int id) {
        Optional<VerificationLog> verificationLog = verificationLogService.getVerificationLogById(id);
        return verificationLog.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}