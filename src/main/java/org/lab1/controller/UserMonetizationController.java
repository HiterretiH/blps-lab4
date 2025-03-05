package org.lab1.controller;

import org.lab1.service.UserMonetizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserMonetizationController {

    @Autowired
    private UserMonetizationService userMonetizationService;

    @PostMapping("/download/{applicationId}")
    public ResponseEntity<String> downloadApplication(@PathVariable int applicationId) {
        boolean success = userMonetizationService.downloadApplication(applicationId);
        if (success) {
            return ResponseEntity.ok("Application downloaded successfully.");
        }
        return ResponseEntity.badRequest().body("Application not found.");
    }

    @PostMapping("/purchase/{purchaseId}")
    public ResponseEntity<String> purchaseInAppItem(@PathVariable int purchaseId) {
        boolean success = userMonetizationService.purchaseInAppItem(purchaseId);
        if (success) {
            return ResponseEntity.ok("In-app purchase successful.");
        }
        return ResponseEntity.badRequest().body("Purchase not found.");
    }

    @PostMapping("/view-ad/{adId}")
    public ResponseEntity<String> viewAdvertisement(@PathVariable int adId) {
        boolean success = userMonetizationService.viewAdvertisement(adId);
        if (success) {
            return ResponseEntity.ok("Ad viewed successfully.");
        }
        return ResponseEntity.badRequest().body("Ad not found.");
    }
}
