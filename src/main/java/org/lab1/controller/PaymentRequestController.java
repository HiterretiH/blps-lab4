package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.model.PaymentRequest;
import org.lab1.service.PaymentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/payment-requests")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;
    private final Logger logger;

    @Autowired
    public PaymentRequestController(PaymentRequestService paymentRequestService, Logger logger) {
        this.paymentRequestService = paymentRequestService;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('payment_request.create')")
    @PostMapping
    public ResponseEntity<PaymentRequest> createPaymentRequest(@RequestParam int applicationId, @RequestParam double amount) {
        logger.info("Received request to create PaymentRequest for application ID: " + applicationId + ", amount: " + amount);
        PaymentRequest paymentRequest = paymentRequestService.createPaymentRequest(applicationId, amount);
        logger.info("PaymentRequest created with ID: " + paymentRequest.getApplicationId() + ", amount: " + paymentRequest.getAmount());
        return ResponseEntity.ok(paymentRequest);
    }

    @PreAuthorize("hasAuthority('payment_request.read')")
    @GetMapping("/{applicationId}")
    public ResponseEntity<PaymentRequest> getPaymentRequest(@PathVariable int applicationId) {
        logger.info("Received request to get PaymentRequest by application ID: " + applicationId);
        Optional<PaymentRequest> paymentRequest = paymentRequestService.getPaymentRequestById(applicationId);
        return paymentRequest.map(ResponseEntity::ok).orElseGet(() -> {
            logger.info("PaymentRequest not found for application ID: " + applicationId);
            return ResponseEntity.notFound().build();
        });
    }

    @PreAuthorize("hasAuthority('payment_request.validate_card')")
    @GetMapping("/validate/{applicationId}")
    public ResponseEntity<String> validateCard(@PathVariable int applicationId) {
        logger.info("Received request to validate card for application ID: " + applicationId);
        Optional<PaymentRequest> paymentRequest = paymentRequestService.getPaymentRequestById(applicationId);
        if (paymentRequest.isPresent() && paymentRequestService.validateCard(paymentRequest.get())) {
            logger.info("Card is valid for application ID: " + applicationId);
            return ResponseEntity.ok("Card is valid");
        } else {
            logger.info("Card is invalid for application ID: " + applicationId);
            return ResponseEntity.status(400).body("Invalid card");
        }
    }
}