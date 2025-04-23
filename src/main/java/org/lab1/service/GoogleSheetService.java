package org.lab1.service;

import com.rabbitmq.client.MessageProperties;
import jakarta.jms.Message;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
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

    @Autowired
    public GoogleSheetService(GoogleTaskSender googleTaskSender,
                              GoogleOAuthService googleOAuthService,
                              UserService userService,
                              MonetizedApplicationRepository monetizedApplicationRepository,
                              ApplicationRepository applicationRepository) {
        this.googleTaskSender = googleTaskSender;
        this.googleOAuthService = googleOAuthService;
        this.userService = userService;
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.applicationRepository = applicationRepository;
    }

    public String createRevenueSheet(int userId) throws OAuthException {
        if (!googleOAuthService.isGoogleConnected(userId)) {
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

        return "Revenue sheet creation with data request sent for user: " + userId;
    }

    @Transactional
    public String addAppSheets(int userId, int appId) {
        try {
            // 1. Проверяем существование пользователя
            User user = userService.getUserById(userId);

            // 2. Проверяем, что пользователь - разработчик
            if (user.getRole() != Role.DEVELOPER) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only developers can add app sheets");
            }

            // 3. Получаем приложение
            Application app = applicationRepository.findById(appId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

            // 4. Проверяем, что приложение принадлежит разработчику
            if (app.getDeveloper().getUser().getId() != userId) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Application doesn't belong to this developer");
            }

            // 5. Проверяем подключение Google
            if (!googleOAuthService.isGoogleConnected(userId)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User has not connected Google account");
            }

            // 6. Отправляем задачу на добавление листов
            googleTaskSender.sendAddAppSheetsRequest(userId, app.getName());

            return "App sheets creation request sent for app: " + appId;
        }
        catch (OAuthException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google connection failed");
        }
    }

    public void triggerUpdateAppsTop() {
        googleTaskSender.sendUpdateAppsTopRequest();
    }
}
