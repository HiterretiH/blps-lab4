package org.lab1.controller;

import org.lab1.model.MonetizedApplication;
import org.lab1.model.PaymentRequest;
import org.lab1.service.MonetizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monetization")
public class MonetizationController {

    @Autowired
    private MonetizationService monetizationService;

    @PreAuthorize("hasAuthority('monetization.read')")
    @GetMapping("/info/{applicationId}")
    public ResponseEntity<MonetizedApplication> getMonetizationInfo(@PathVariable int applicationId) {
        MonetizedApplication monetizedApp = monetizationService.getMonetizationInfo(applicationId);

        if (monetizedApp == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(monetizedApp);
    }

    @PreAuthorize("hasAuthority('monetization.payout.request')")
    @PostMapping("/sendForm/{applicationId}")
    public ResponseEntity<PaymentRequest> sendForm(@PathVariable int applicationId, @RequestParam double amount) {
        PaymentRequest paymentRequest = monetizationService.sendForm(applicationId, amount);

        return ResponseEntity.ok(paymentRequest);
    }

    @PreAuthorize("hasAuthority('monetization.payout.execute')")
    @PostMapping("/payout")
    public ResponseEntity<String> makePayout(@RequestBody PaymentRequest paymentRequest) {
        String result = monetizationService.makePayout(paymentRequest);

        if (result.contains("successful")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
