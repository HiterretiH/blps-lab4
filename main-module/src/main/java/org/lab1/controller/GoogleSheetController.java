package org.lab1.controller;

import java.util.Optional;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sheets")
public final class GoogleSheetController {
  private static final String CREATE_REVENUE_SHEET_LOG =
      "Received request to create revenue sheet.";
  private static final String USER_NOT_FOUND_LOG = "User not found: ";
  private static final String REVENUE_SHEET_CREATION_LOG =
      "Revenue sheet creation initiated for user ID: ";
  private static final String REVENUE_SHEET_ERROR_LOG =
      "Error creating revenue sheet for user ID: ";
  private static final String ADD_APP_SHEETS_LOG =
      "Received request to add app sheets for app ID: ";
  private static final String APP_SHEETS_CREATION_LOG =
      "App sheets creation initiated for app ID: ";
  private static final String USER_ID_LOG = ", user ID: ";
  private static final String APP_SHEETS_ERROR_LOG = "Error adding app sheets for app ID: ";
  private static final String UPDATE_TOP_LOG = "Received request to update apps top.";
  private static final String TOP_UPDATE_TRIGGERED_LOG = "Apps top update triggered.";
  private static final String TOP_UPDATE_MESSAGE = "Apps top update triggered";
  private static final String REASON_LOG = ". Reason: ";

  private final GoogleSheetService googleSheetService;
  private final UserRepository userRepository;
  private final Logger logger;

  @Autowired
  public GoogleSheetController(
      final GoogleSheetService googleSheetServiceParam,
      final UserRepository userRepositoryParam,
      final Logger loggerParam) {
    this.googleSheetService = googleSheetServiceParam;
    this.userRepository = userRepositoryParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('stats.create')")
  @PostMapping("/create-revenue-sheet")
  public ResponseEntity<String> createRevenueSheet() {
    logger.info(CREATE_REVENUE_SHEET_LOG);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Optional<User> userOptional =
        userRepository.findByUsername(authentication.getPrincipal().toString());

    if (userOptional.isEmpty()) {
      logger.error(USER_NOT_FOUND_LOG + authentication.getPrincipal());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User user = userOptional.get();
    int userId = user.getId();

    try {
      String result = googleSheetService.createRevenueSheet(userId);
      logger.info(REVENUE_SHEET_CREATION_LOG + userId
          + ". Result: " + result);
      return ResponseEntity.ok(result);
    } catch (Exception exception) {
      logger.error(REVENUE_SHEET_ERROR_LOG + userId
          + REASON_LOG + exception.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error creating revenue sheet: " + exception.getMessage());
    }
  }

  @PreAuthorize("hasAuthority('stats.add_sheets')")
  @PostMapping("/{appId}/add-sheets")
  public ResponseEntity<String> addAppSheets(@PathVariable final int appId) {
    logger.info(ADD_APP_SHEETS_LOG + appId);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    Optional<User> userOptional =
        userRepository.findByUsername(authentication.getPrincipal().toString());

    if (userOptional.isEmpty()) {
      logger.error(USER_NOT_FOUND_LOG + authentication.getPrincipal());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User user = userOptional.get();
    int userId = user.getId();

    try {
      String result = googleSheetService.addAppSheets(userId, appId);
      logger.info(APP_SHEETS_CREATION_LOG + appId
          + USER_ID_LOG + userId + ". Result: " + result);
      return ResponseEntity.ok(result);
    } catch (Exception exception) {
      logger.error(
          APP_SHEETS_ERROR_LOG
              + appId
              + USER_ID_LOG
              + userId
              + REASON_LOG
              + exception.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Error adding app sheets: " + exception.getMessage());
    }
  }

  @PreAuthorize("hasAuthority('stats.update')")
  @PostMapping("/update-top")
  public ResponseEntity<String> updateAppsTop() {
    logger.info(UPDATE_TOP_LOG);
    googleSheetService.triggerUpdateAppsTop();
    logger.info(TOP_UPDATE_TRIGGERED_LOG);
    return ResponseEntity.ok(TOP_UPDATE_MESSAGE);
  }
}
