package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.json.InAppPurchasesJson;
import org.lab1.model.InAppPurchase;
import org.lab1.service.InAppPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/in-app-purchases")
public class InAppPurchaseController {

    private final InAppPurchaseService inAppPurchaseService;
    private final Logger logger;

    @Autowired
    public InAppPurchaseController(InAppPurchaseService inAppPurchaseService, Logger logger) {
        this.inAppPurchaseService = inAppPurchaseService;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('in_app_purchase.manage')")
    @PostMapping("/create")
    public ResponseEntity<List<InAppPurchase>> createInAppPurchases(@RequestBody InAppPurchasesJson inAppPurchases) {
        logger.info("Received request to create InAppPurchases. Titles count: " + (inAppPurchases.getTitles() != null ? inAppPurchases.getTitles().size() : 0) +
                ", Descriptions count: " + (inAppPurchases.getDescriptions() != null ? inAppPurchases.getDescriptions().size() : 0) +
                ", Prices count: " + (inAppPurchases.getPrices() != null ? inAppPurchases.getPrices().size() : 0));
        try {
            List<InAppPurchase> purchases = inAppPurchaseService.createInAppPurchases(
                    inAppPurchases.getTitles(),
                    inAppPurchases.getDescriptions(),
                    inAppPurchases.getPrices());
            logger.info("Successfully created " + purchases.size() + " InAppPurchases.");
            return ResponseEntity.ok(purchases);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create InAppPurchases. Reason: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("hasAuthority('in_app_purchase.read')")
    @GetMapping("/all")
    public ResponseEntity<List<InAppPurchase>> getAllInAppPurchases() {
        logger.info("Received request to list all InAppPurchases.");
        List<InAppPurchase> purchases = inAppPurchaseService.getAllInAppPurchases();
        logger.info("Found " + purchases.size() + " InAppPurchases.");
        return ResponseEntity.ok(purchases);
    }

    @PreAuthorize("hasAuthority('in_app_purchase.read')")
    @GetMapping("/{id}")
    public ResponseEntity<InAppPurchase> getInAppPurchaseById(@PathVariable int id) {
        logger.info("Received request to get InAppPurchase by ID: " + id);
        Optional<InAppPurchase> purchase = inAppPurchaseService.getInAppPurchaseById(id);
        return purchase.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.info("InAppPurchase not found with ID: " + id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PreAuthorize("hasAuthority('in_app_purchase.manage')")
    @PostMapping("/link-to-monetized-app/{monetizedApplicationId}")
    public ResponseEntity<List<InAppPurchase>> linkMonetizedAppToPurchases(@PathVariable int monetizedApplicationId) {
        logger.info("Received request to link InAppPurchases to MonetizedApplication ID: " + monetizedApplicationId);
        try {
            List<InAppPurchase> linkedPurchases = inAppPurchaseService.linkMonetizedAppToPurchases(monetizedApplicationId);
            logger.info("Successfully linked " + linkedPurchases.size() + " InAppPurchases to MonetizedApplication ID: " + monetizedApplicationId);
            return ResponseEntity.ok(linkedPurchases);
        } catch (RuntimeException e) {
            logger.error("Failed to link InAppPurchases to MonetizedApplication ID: " + monetizedApplicationId + ". Reason: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}