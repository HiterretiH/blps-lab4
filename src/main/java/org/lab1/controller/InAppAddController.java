package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.json.InAppAddJson;
import org.lab1.model.InAppAdd;
import org.lab1.service.InAppAddService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/in-app-ads")
public class InAppAddController {

    private final InAppAddService inAppAddService;
    private final Logger logger;

    @Autowired
    public InAppAddController(InAppAddService inAppAddService, Logger logger) {
        this.inAppAddService = inAppAddService;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('in_app_add.manage')")
    @PostMapping("/create")
    public ResponseEntity<InAppAdd> createInAppAdd(@RequestBody InAppAddJson inAppAddJson) {
        logger.info("Received request to create InAppAdd for MonetizedApplication ID: " + inAppAddJson.getMonetizedApplicationId());
        InAppAdd inAppAdd = inAppAddService.createInAppAdd(inAppAddJson);
        logger.info("InAppAdd created with ID: " + inAppAdd.getId() + " for MonetizedApplication ID: " + inAppAdd.getMonetizedApplication().getId());
        return ResponseEntity.ok(inAppAdd);
    }

    @PreAuthorize("hasAuthority('in_app_add.manage')")
    @PostMapping("/bulk")
    public ResponseEntity<List<InAppAdd>> createMultipleInAppAdds(@RequestBody List<InAppAddJson> inAppAddJsons) {
        logger.info("Received request to create multiple InAppAdds. Count: " + (inAppAddJsons != null ? inAppAddJsons.size() : 0));
        try {
            List<InAppAdd> inAppAdds = inAppAddService.createMultipleInAppAdds(inAppAddJsons);
            logger.info("Successfully created " + inAppAdds.size() + " InAppAdds.");
            return ResponseEntity.status(HttpStatus.CREATED).body(inAppAdds);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create multiple InAppAdds. Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PreAuthorize("hasAuthority('in_app_add.read')")
    @GetMapping("/list")
    public ResponseEntity<List<InAppAdd>> getAllInAppAds() {
        logger.info("Received request to list all InAppAdds.");
        List<InAppAdd> inAppAdds = inAppAddService.getAllInAppAds();
        logger.info("Found " + inAppAdds.size() + " InAppAdds.");
        return ResponseEntity.ok(inAppAdds);
    }

    @PreAuthorize("hasAuthority('in_app_add.read')")
    @GetMapping("get/{id}")
    public ResponseEntity<InAppAdd> getInAppAddById(@PathVariable int id) {
        logger.info("Received request to get InAppAdd by ID: " + id);
        Optional<InAppAdd> inAppAdd = inAppAddService.getInAppAddById(id);
        return inAppAdd.map(ResponseEntity::ok).orElseGet(() -> {
            logger.info("InAppAdd not found with ID: " + id);
            return ResponseEntity.notFound().build();
        });
    }

    @PreAuthorize("hasAuthority('in_app_add.read')")
    @GetMapping("/monetized/{monetizedApplicationId}")
    public ResponseEntity<List<InAppAdd>> getInAppAdsByMonetizedApplication(@PathVariable int monetizedApplicationId) {
        logger.info("Received request to get InAppAdds by MonetizedApplication ID: " + monetizedApplicationId);
        List<InAppAdd> inAppAdds = inAppAddService.getInAppAddByMonetizedApplication(monetizedApplicationId);
        logger.info("Found " + inAppAdds.size() + " InAppAdds for MonetizedApplication ID: " + monetizedApplicationId);
        return ResponseEntity.ok(inAppAdds);
    }
}