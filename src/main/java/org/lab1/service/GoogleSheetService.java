package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.exception.OAuthException;
import org.lab1.json.GoogleSheetRequestWithData;
import org.lab1.model.Application;
import org.lab1.model.MonetizedApplication;
import org.lab1.model.Role;
import org.lab1.model.User;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoogleSheetService {
    private final GoogleTaskSender googleTaskSender;
    private final GoogleOAuthService googleOAuthService;
    private final UserService userService;
    private final MonetizedApplicationRepository monetizedApplicationRepository;
    private final ApplicationRepository applicationRepository;
    private final Logger logger;

    @Autowired
    public GoogleSheetService(GoogleTaskSender googleTaskSender,
                              GoogleOAuthService googleOAuthService,
                              UserService userService,
                              MonetizedApplicationRepository monetizedApplicationRepository,
                              ApplicationRepository applicationRepository,
                              Logger logger) {
        this.googleTaskSender = googleTaskSender;
        this.googleOAuthService = googleOAuthService;
        this.userService = userService;
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.applicationRepository = applicationRepository;
        this.logger = logger;
    }

    public String createRevenueSheet(int userId) throws OAuthException {
        logger.info("Creating revenue sheet for user ID: " + userId);
        if (!googleOAuthService.isGoogleConnected(userId)) {
            logger.error("User ID " + userId + " has not connected Google account.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User has not connected Google account");
        }
        String googleEmail = googleOAuthService.getUserGoogleEmail(userId);
        String sheetTitle = "Revenue Statistics - " + googleEmail + " - " + "52";
        List<MonetizedApplication> apps = monetizedApplicationRepository.findByDeveloperUserId(userId);
        List<List<Object>> appData = apps.stream()
                .map(app -> {
                    List<Object> row = new ArrayList<>();
                    row.add(app.getApplication().getId());
                    row.add(app.getApplication().getName());
                    row.add(app.getAdsRevenue());
                    row.add(app.getDownloadRevenue());
                    row.add(app.getPurchasesRevenue());
                    row.add(app.getRevenue());
                    return row;
                })
                .collect(Collectors.toList());
        GoogleSheetRequestWithData request = new GoogleSheetRequestWithData(
                googleEmail,
                sheetTitle,
                List.of("ID", "Application", "Ads Revenue", "Download Revenue", "Purchases Revenue", "Total Revenue"),
                appData
        );
        googleTaskSender.sendSheetCreationRequest(userId, request);
        logger.info("Revenue sheet creation request sent for user ID: " + userId + " to email: " + googleEmail + ", title: " + sheetTitle);
        return "Revenue sheet creation with data request sent for user: " + userId;
    }

    @Transactional
    public String addAppSheets(int userId, int appId) {
        logger.info("Adding app sheets for user ID: " + userId + ", app ID: " + appId);
        try {
            User user = userService.getUserById(userId);
            if (user.getRole() != Role.DEVELOPER) {
                logger.error("User ID " + userId + " is not a developer.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only developers can add app sheets");
            }
            Application app = applicationRepository.findById(appId)
                    .orElseThrow(() -> {
                        logger.error("Application not found with ID: " + appId);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");
                    });
            if (app.getDeveloper().getUser().getId() != userId) {
                logger.error("Application ID " + appId + " does not belong to developer ID " + userId);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Application doesn't belong to this developer");
            }
            if (!googleOAuthService.isGoogleConnected(userId)) {
                logger.error("User ID " + userId + " has not connected Google account.");
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User has not connected Google account");
            }
            googleTaskSender.sendAddAppSheetsRequest(userId, app.getName());
            logger.info("App sheets creation request sent for app: " + app.getName() + ", user ID: " + userId);
            return "App sheets creation request sent for app: " + appId;
        } catch (OAuthException e) {
            logger.error("Google connection failed for user ID: " + userId + ". Reason: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google connection failed");
        }
    }

    public void triggerUpdateAppsTop() {
        logger.info("Triggering update apps top request.");
        googleTaskSender.sendUpdateAppsTopRequest();
        logger.info("Update apps top request sent.");
    }
}