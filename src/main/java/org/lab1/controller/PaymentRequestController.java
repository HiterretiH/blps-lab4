package org.lab1.controller;

import org.lab1.model.PaymentRequest;
import org.lab1.service.PaymentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('payment_request.create')")
    @PostMapping
    public ResponseEntity<PaymentRequest> createPaymentRequest(@RequestParam int applicationId, @RequestParam double amount) {
        PaymentRequest paymentRequest = paymentRequestService.createPaymentRequest(applicationId, amount);
        return ResponseEntity.ok(paymentRequest);
    }

    @PreAuthorize("hasAuthority('payment_request.read')")
    @GetMapping("/{applicationId}")
    public ResponseEntity<PaymentRequest> getPaymentRequest(@PathVariable int applicationId) {
        Optional<PaymentRequest> paymentRequest = paymentRequestService.getPaymentRequestById(applicationId);
        return paymentRequest.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('payment_request.validate_card')")
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