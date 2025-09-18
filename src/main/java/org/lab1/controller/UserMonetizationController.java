package org.lab1.controller;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.lab1.json.Card;
import org.lab1.model.Application;
import org.lab1.model.User;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.InAppAddRepository;
import org.lab1.repository.InAppPurchaseRepository;
import org.lab1.repository.UserRepository;
import org.lab1.service.UserMonetizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.lab1.service.GoogleTaskSender;
import org.lab1.json.MonetizationEvent;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserMonetizationController {

    private final UserMonetizationService userMonetizationService;
    private final GoogleTaskSender googleTaskSender;
    private final InAppPurchaseRepository purchaseRepository;
    private final InAppAddRepository addRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;

    private final Timer purchaseProcessingTime;
    private final Timer adViewProcessingTime;


    @Autowired
    public UserMonetizationController(UserMonetizationService userMonetizationService,
                                      GoogleTaskSender googleTaskSender,
                                      InAppPurchaseRepository purchaseRepository,
                                      InAppAddRepository addRepository,
                                      ApplicationRepository applicationRepository,
                                      UserRepository userRepository,
                                      MeterRegistry meterRegistry) {
        this.userMonetizationService = userMonetizationService;
        this.googleTaskSender = googleTaskSender;
        this.purchaseRepository = purchaseRepository;
        this.addRepository = addRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.meterRegistry = meterRegistry;
        this.purchaseProcessingTime = meterRegistry.timer("user.monetization.purchase.processing.time");
        this.adViewProcessingTime = meterRegistry.timer("user.monetization.ad_view.processing.time");
    }

    @PreAuthorize("hasAuthority('user.download_application')")
    @PostMapping("/download/{applicationId}")
    public ResponseEntity<String> downloadApplication(
            @PathVariable int applicationId,
            @RequestBody Card card,
            Authentication authentication) {

        // Извлекаем userId из аутентификации
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();
        int userId = user.getId();

        // Получаем приложение для получения цены
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        boolean success = userMonetizationService.downloadApplication(
                applicationId,
                userId,
                card.getCardNumber(),
                card.getCardHolderName(),
                card.getExpiryDate(),
                card.getCvv()
        );

        if (success) {
            // Отправляем событие в очередь только после успешной загрузки
            MonetizationEvent event = new MonetizationEvent(
                    MonetizationEvent.EventType.DOWNLOAD,
                    userId,
                    applicationId,
                    applicationId,
                    application.getPrice() // Используем цену приложения
            );
            googleTaskSender.sendMonetizationEvent(userId, event);
            return ResponseEntity.ok("Application downloaded successfully.");
        }
        return ResponseEntity.badRequest().body("Application not found or download failed.");
    }

    @PreAuthorize("hasAuthority('user.purchase_in_app_item')")
    @PostMapping("/purchase/{purchaseId}")
    public ResponseEntity<String> purchaseInAppItem(
            @PathVariable int purchaseId,
            @RequestBody Card card,
            Authentication authentication) {
        Timer.Sample sample = Timer.start(meterRegistry);

        // Извлекаем userId из аутентификации
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();
        int userId = user.getId();
        boolean success = userMonetizationService.purchaseInAppItem(
                purchaseId,
                userId,
                card.getCardNumber(),
                card.getCardHolderName(),
                card.getExpiryDate(),
                card.getCvv()
        );

        if (success) {
            // Получаем цену покупки
            double price = purchaseRepository.findById(purchaseId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase not found"))
                    .getPrice();

            // Отправляем событие в очередь только после успешной покупки
            MonetizationEvent event = new MonetizationEvent(
                    MonetizationEvent.EventType.PURCHASE,
                    userId,
                    getApplicationIdForPurchase(purchaseId), // метод для получения appId по purchaseId
                    purchaseId,
                    price
            );
            sample.stop(purchaseProcessingTime);
            googleTaskSender.sendMonetizationEvent(userId, event);
            return ResponseEntity.ok("In-app purchase successful.");
        }
        return ResponseEntity.badRequest().body("Purchase not found or failed.");
    }

    @PreAuthorize("hasAuthority('user.view_advertisement')")
    @PostMapping("/view-ad/{adId}")
    public ResponseEntity<String> viewAdvertisement(
            @PathVariable int adId,
            Authentication authentication) {
        Timer.Sample sample = Timer.start(meterRegistry);

        // Извлекаем userId из аутентификации
        Optional<User> userOptional = userRepository.findByUsername(authentication.getPrincipal().toString());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = userOptional.get();
        int userId = user.getId();
        boolean success = userMonetizationService.viewAdvertisement(adId);

        if (success) {
            // Получаем доход от просмотра рекламы
            double revenue = addRepository.findById(adId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ad not found"))
                    .getPrice(); // Предполагаем, что цена InAppAdd и есть доход от просмотра

            // Отправляем событие в очередь только после успешного просмотра
            MonetizationEvent event = new MonetizationEvent(
                    MonetizationEvent.EventType.AD_VIEW,
                    userId,
                    getApplicationIdForAd(adId), // метод для получения appId по adId
                    adId,
                    revenue
            );
            sample.stop(adViewProcessingTime);
            googleTaskSender.sendMonetizationEvent(userId, event);
            return ResponseEntity.ok("Ad viewed successfully. Revenue: " + revenue);
        }
        return ResponseEntity.badRequest().body("Ad not found or view failed.");
    }

    private int getApplicationIdForPurchase(int purchaseId) {
        // Реализация получения applicationId по purchaseId
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"))
                .getMonetizedApplication()
                .getApplication()
                .getId();
    }

    private int getApplicationIdForAd(int adId) {
        // Реализация получения applicationId по adId
        return addRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found"))
                .getMonetizedApplication()
                .getApplication()
                .getId();
    }
}