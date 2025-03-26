package org.lab1.controller;

import org.lab1.json.InAppPurchasesJson;
import org.lab1.model.InAppPurchase;
import org.lab1.service.InAppPurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/in-app-purchases")
public class InAppPurchaseController {

    @Autowired
    private InAppPurchaseService inAppPurchaseService;

    @PreAuthorize("hasAuthority('in_app_purchase.manage')")
    @PostMapping("/create")
    public ResponseEntity<List<InAppPurchase>> createInAppPurchases(@RequestBody InAppPurchasesJson inAppPurchases) {
        try {
            List<InAppPurchase> purchases = inAppPurchaseService.createInAppPurchases(
                    inAppPurchases.getTitles(),
                    inAppPurchases.getDescriptions(),
                    inAppPurchases.getPrices());
            return ResponseEntity.ok(purchases);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PreAuthorize("hasAuthority('in_app_purchase.read')")
    @GetMapping("/all")
    public ResponseEntity<List<InAppPurchase>> getAllInAppPurchases() {
        List<InAppPurchase> purchases = inAppPurchaseService.getAllInAppPurchases();
        return ResponseEntity.ok(purchases);
    }

    @PreAuthorize("hasAuthority('in_app_purchase.read')")
    @GetMapping("/{id}")
    public ResponseEntity<InAppPurchase> getInAppPurchaseById(@PathVariable int id) {
        Optional<InAppPurchase> purchase = inAppPurchaseService.getInAppPurchaseById(id);

        return purchase.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority('in_app_purchase.manage')")
    @PostMapping("/link-to-monetized-app/{monetizedApplicationId}")
    public ResponseEntity<List<InAppPurchase>> linkMonetizedAppToPurchases(@PathVariable int monetizedApplicationId) {
        try {
            List<InAppPurchase> linkedPurchases = inAppPurchaseService.linkMonetizedAppToPurchases(monetizedApplicationId);
            return ResponseEntity.ok(linkedPurchases);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
