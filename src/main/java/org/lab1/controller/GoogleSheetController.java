package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.model.User;
import org.lab1.repository.UserRepository;
import org.lab1.service.GoogleSheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/sheets")
public class GoogleSheetController {

    private final GoogleSheetService googleSheetService;
    private final UserRepository userRepository;
    private final Logger logger;

    @Autowired
    public GoogleSheetController(GoogleSheetService googleSheetService,
                                 UserRepository userRepository,
                                 Logger logger) {
        this.googleSheetService = googleSheetService;
        this.userRepository = userRepository;
        this.logger = logger;
    }

    @PreAuthorize("hasAuthority('stats.create')")
    @PostMapping("/create-revenue-sheet")
    public ResponseEntity<String> createRevenueSheet() {
        logger.info("Received request to create revenue sheet.");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            logger.error("User not found: " + authentication.getPrincipal());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();
        int userId = user.getId();
        try {
            String result = googleSheetService.createRevenueSheet(userId);
            logger.info("Revenue sheet creation initiated for user ID: " + userId + ". Result: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error creating revenue sheet for user ID: " + userId + ". Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating revenue sheet: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('stats.add_sheets')")
    @PostMapping("/{appId}/add-sheets")
    public ResponseEntity<String> addAppSheets(@PathVariable int appId) {
        logger.info("Received request to add app sheets for app ID: " + appId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            logger.error("User not found: " + authentication.getPrincipal());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();
        int userId = user.getId();
        try {
            String result = googleSheetService.addAppSheets(userId, appId);
            logger.info("App sheets creation initiated for app ID: " + appId + ", user ID: " + userId + ". Result: " + result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error adding app sheets for app ID: " + appId + ", user ID: " + userId + ". Reason: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error adding app sheets: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('stats.update')")
    @PostMapping("/update-top")
    public ResponseEntity<String> updateAppsTop() {
        logger.info("Received request to update apps top.");
        googleSheetService.triggerUpdateAppsTop();
        logger.info("Apps top update triggered.");
        return ResponseEntity.ok("Apps top update triggered");
    }
}