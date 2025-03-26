package org.lab1.controller;

import org.lab1.json.Card;
import org.lab1.service.UserMonetizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserMonetizationController {

    @Autowired
    private UserMonetizationService userMonetizationService;

    @PreAuthorize("hasAuthority('user.download_application')")
    @PostMapping("/download/{applicationId}")
    public ResponseEntity<String> downloadApplication(@PathVariable int applicationId, @RequestBody Card card) {
        boolean success = userMonetizationService.downloadApplication(
                applicationId,
                1,
                card.getCardNumber(),
                card.getCardHolderName(),
                card.getExpiryDate(),
                card.getCvv()
        );
        if (success) {
            return ResponseEntity.ok("Application downloaded successfully.");
        }
        return ResponseEntity.badRequest().body("Application not found.");
    }

    @PreAuthorize("hasAuthority('user.purchase_in_app_item')")
    @PostMapping("/purchase/{purchaseId}")
    public ResponseEntity<String> purchaseInAppItem(@PathVariable int purchaseId, @RequestBody Card card) {
        boolean success = userMonetizationService.purchaseInAppItem(purchaseId,
                1,
                card.getCardNumber(),
                card.getCardHolderName(),
                card.getExpiryDate(),
                card.getCvv()
        );
        if (success) {
            return ResponseEntity.ok("In-app purchase successful.");
        }
        return ResponseEntity.badRequest().body("Purchase not found.");
    }

    @PreAuthorize("hasAuthority('user.view_advertisement')")
    @PostMapping("/view-ad/{adId}")
    public ResponseEntity<String> viewAdvertisement(@PathVariable int adId) {
        boolean success = userMonetizationService.viewAdvertisement(adId);
        if (success) {
            return ResponseEntity.ok("Ad viewed successfully.");
        }
        return ResponseEntity.badRequest().body("Ad not found.");
    }
}
