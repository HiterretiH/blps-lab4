package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.model.MonetizedApplication;
import org.lab1.model.PaymentRequest;
import org.lab1.service.MonetizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monetization")
public class MonetizationController {

    private final MonetizationService monetizationService;
    private final Logger logger;

    @Autowired
    public MonetizationController(MonetizationService monetizationService, Logger logger) {
        this.monetizationService = monetizationService;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('monetization.read')")
    @GetMapping("/info/{applicationId}")
    public ResponseEntity<MonetizedApplication> getMonetizationInfo(@PathVariable int applicationId) {
        logger.info("Received request to get monetization info for application ID: " + applicationId);
        MonetizedApplication monetizedApp = monetizationService.getMonetizationInfo(applicationId);
        if (monetizedApp == null) {
            logger.info("Monetization info not found for application ID: " + applicationId);
            return ResponseEntity.notFound().build();
        }
        logger.info("Monetization info found for application ID: " + applicationId + ". Current balance: " + monetizedApp.getCurrentBalance());
        return ResponseEntity.ok(monetizedApp);
    }

    @PreAuthorize("hasAuthority('monetization.payout.request')")
    @PostMapping("/sendForm/{applicationId}")
    public ResponseEntity<PaymentRequest> sendForm(@PathVariable int applicationId, @RequestParam double amount) {
        logger.info("Received request to send payout form for application ID: " + applicationId + ", amount: " + amount);
        PaymentRequest paymentRequest = monetizationService.sendForm(applicationId, amount);
        logger.info("Payout form sent for application ID: " + applicationId + ", request ID: " + paymentRequest.getApplicationId() + ", amount: " + paymentRequest.getAmount());
        return ResponseEntity.ok(paymentRequest);
    }

    @PreAuthorize("hasAuthority('monetization.payout.execute')")
    @PostMapping("/payout")
    public ResponseEntity<String> makePayout(@RequestBody PaymentRequest paymentRequest) {
        logger.info("Received payout request for application ID: " + paymentRequest.getApplicationId() + ", amount: " + paymentRequest.getAmount());
        String result = monetizationService.makePayout(paymentRequest);
        if (result.contains("successful")) {
            logger.info("Payout successful for application ID: " + paymentRequest.getApplicationId() + ", amount: " + paymentRequest.getAmount() + ". Result: " + result);
            return ResponseEntity.ok(result);
        } else {
            logger.error("Payout failed for application ID: " + paymentRequest.getApplicationId() + ", amount: " + paymentRequest.getAmount() + ". Reason: " + result);
            return ResponseEntity.badRequest().body(result);
        }
    }
}