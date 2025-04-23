package org.lab1.controller;

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

    @Autowired
    public GoogleSheetController(GoogleSheetService googleSheetService,
                                 UserRepository userRepository) {
        this.googleSheetService = googleSheetService;
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasAuthority('stats.create')")
    @PostMapping("/create-revenue-sheet")
    public ResponseEntity<String> createRevenueSheet() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();

        int userId = user.getId();

        try {
            String result = googleSheetService.createRevenueSheet(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating revenue sheet: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('stats.add_sheets')")
    @PostMapping("/{appId}/add-sheets")
    public ResponseEntity<String> addAppSheets(@PathVariable int appId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();

        int userId = user.getId();

        try {
            String result = googleSheetService.addAppSheets(userId, appId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error adding app sheets: " + e.getMessage());
        }
    }

    @PreAuthorize("hasAuthority('stats.update')")
    @PostMapping("/update-top")
    public ResponseEntity<String> updateAppsTop() {
        googleSheetService.triggerUpdateAppsTop();
        return ResponseEntity.ok("Apps top update triggered");
    }
}
