package org.lab1.controller;

import org.lab1.model.PaymentRequest;
import org.lab1.service.PaymentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/payment-requests")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;

    @Autowired
    public PaymentRequestController(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    @PostMapping
    public ResponseEntity<PaymentRequest> createPaymentRequest(@RequestParam int applicationId, @RequestParam double amount) {
        PaymentRequest paymentRequest = paymentRequestService.createPaymentRequest(applicationId, amount);
        return ResponseEntity.ok(paymentRequest);
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<PaymentRequest> getPaymentRequest(@PathVariable int applicationId) {
        Optional<PaymentRequest> paymentRequest = paymentRequestService.getPaymentRequestById(applicationId);
        return paymentRequest.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/validate/{applicationId}")
    public ResponseEntity<String> validateCard(@PathVariable int applicationId) {
        Optional<PaymentRequest> paymentRequest = paymentRequestService.getPaymentRequestById(applicationId);
        if (paymentRequest.isPresent() && paymentRequestService.validateCard(paymentRequest.get())) {
            return ResponseEntity.ok("Card is valid");
        } else {
            return ResponseEntity.status(400).body("Invalid card");
        }
    }
}