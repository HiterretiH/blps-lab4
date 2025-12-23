package org.lab1.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Optional;
import org.lab1.json.Card;
import org.lab1.json.MonetizationEvent;
import org.lab1.model.Application;
import org.lab1.model.User;
import org.lab1.repository.ApplicationRepository;
import org.lab1.repository.InAppAddRepository;
import org.lab1.repository.InAppPurchaseRepository;
import org.lab1.repository.UserRepository;
import org.lab1.service.GoogleTaskSender;
import org.lab1.service.UserMonetizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user")
public final class UserMonetizationController {
  private static final String APPLICATION_NOT_FOUND_MESSAGE = "Application not found";
  private static final String DOWNLOAD_SUCCESS_MESSAGE = "Application downloaded successfully.";
  private static final String DOWNLOAD_FAILED_MESSAGE = "Application not found or download failed.";
  private static final String PURCHASE_NOT_FOUND_MESSAGE = "Purchase not found";
  private static final String PURCHASE_SUCCESS_MESSAGE = "In-app purchase successful.";
  private static final String PURCHASE_FAILED_MESSAGE = "Purchase not found or failed.";
  private static final String AD_NOT_FOUND_MESSAGE = "Ad not found";
  private static final String AD_VIEW_SUCCESS_MESSAGE = "Ad viewed successfully. Revenue: ";
  private static final String AD_VIEW_FAILED_MESSAGE = "Ad not found or view failed.";
  private static final String USER_NOT_AUTHORIZED_MESSAGE = "User not authorized";
  private static final String PURCHASE_NOT_FOUND_EXCEPTION = "Purchase not found";
  private static final String AD_NOT_FOUND_EXCEPTION = "Ad not found";
  private static final String PURCHASE_PROCESSING_TIME_METRIC =
      "user.monetization.purchase.processing.time";
  private static final String AD_VIEW_PROCESSING_TIME_METRIC =
      "user.monetization.ad_view.processing.time";

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
  public UserMonetizationController(
      final UserMonetizationService userMonetizationServiceParam,
      final GoogleTaskSender googleTaskSenderParam,
      final InAppPurchaseRepository purchaseRepositoryParam,
      final InAppAddRepository addRepositoryParam,
      final ApplicationRepository applicationRepositoryParam,
      final UserRepository userRepositoryParam,
      final MeterRegistry meterRegistryParam) {
    this.userMonetizationService = userMonetizationServiceParam;
    this.googleTaskSender = googleTaskSenderParam;
    this.purchaseRepository = purchaseRepositoryParam;
    this.addRepository = addRepositoryParam;
    this.applicationRepository = applicationRepositoryParam;
    this.userRepository = userRepositoryParam;
    this.meterRegistry = meterRegistryParam;
    this.purchaseProcessingTime =
        meterRegistry.timer(PURCHASE_PROCESSING_TIME_METRIC);
    this.adViewProcessingTime =
        meterRegistry.timer(AD_VIEW_PROCESSING_TIME_METRIC);
  }

  @PreAuthorize("hasAuthority('user.download_application')")
  @PostMapping("/download/{applicationId}")
  public ResponseEntity<String> downloadApplication(
      @PathVariable final int applicationId,
      @RequestBody final Card card,
      final Authentication authentication) {
    Optional<User> userOptional =
        userRepository.findByUsername(authentication.getPrincipal().toString());
    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User user = userOptional.get();
    int userId = user.getId();

    Application application =
        applicationRepository
            .findById(applicationId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, APPLICATION_NOT_FOUND_MESSAGE));

    boolean success =
        userMonetizationService.downloadApplication(
            applicationId,
            userId,
            card.getCardNumber(),
            card.getCardHolderName(),
            card.getExpiryDate(),
            card.getCvv());

    if (success) {
      MonetizationEvent event =
          new MonetizationEvent(
              MonetizationEvent.EventType.DOWNLOAD,
              userId,
              applicationId,
              applicationId,
              application.getPrice());
      googleTaskSender.sendMonetizationEvent(userId, event);
      return ResponseEntity.ok(DOWNLOAD_SUCCESS_MESSAGE);
    }

    return ResponseEntity.badRequest().body(DOWNLOAD_FAILED_MESSAGE);
  }

  @PreAuthorize("hasAuthority('user.purchase_in_app_item')")
  @PostMapping("/purchase/{purchaseId}")
  public ResponseEntity<String> purchaseInAppItem(
      @PathVariable final int purchaseId,
      @RequestBody final Card card,
      final Authentication authentication) {
    Timer.Sample sample = Timer.start(meterRegistry);

    Optional<User> userOptional =
        userRepository.findByUsername(authentication.getPrincipal().toString());
    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User user = userOptional.get();
    int userId = user.getId();

    boolean success =
        userMonetizationService.purchaseInAppItem(
            purchaseId,
            userId,
            card.getCardNumber(),
            card.getCardHolderName(),
            card.getExpiryDate(),
            card.getCvv());

    if (success) {
      double price =
          purchaseRepository
              .findById(purchaseId)
              .orElseThrow(
                  () ->
                      new ResponseStatusException(HttpStatus.NOT_FOUND,
                          PURCHASE_NOT_FOUND_MESSAGE))
              .getPrice();

      MonetizationEvent event =
          new MonetizationEvent(
              MonetizationEvent.EventType.PURCHASE,
              userId,
              getApplicationIdForPurchase(purchaseId),
              purchaseId,
              price);
      sample.stop(purchaseProcessingTime);
      googleTaskSender.sendMonetizationEvent(userId, event);
      return ResponseEntity.ok(PURCHASE_SUCCESS_MESSAGE);
    }

    return ResponseEntity.badRequest().body(PURCHASE_FAILED_MESSAGE);
  }

  @PreAuthorize("hasAuthority('user.view_advertisement')")
  @PostMapping("/view-ad/{adId}")
  public ResponseEntity<String> viewAdvertisement(
      @PathVariable final int adId,
      final Authentication authentication) {
    Timer.Sample sample = Timer.start(meterRegistry);

    Optional<User> userOptional =
        userRepository.findByUsername(authentication.getPrincipal().toString());
    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    User user = userOptional.get();
    int userId = user.getId();

    boolean success = userMonetizationService.viewAdvertisement(adId);

    if (success) {
      double revenue =
          addRepository
              .findById(adId)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                      AD_NOT_FOUND_MESSAGE))
              .getPrice();

      MonetizationEvent event =
          new MonetizationEvent(
              MonetizationEvent.EventType.AD_VIEW,
              userId,
              getApplicationIdForAd(adId),
              adId,
              revenue);
      sample.stop(adViewProcessingTime);
      googleTaskSender.sendMonetizationEvent(userId, event);
      return ResponseEntity.ok(AD_VIEW_SUCCESS_MESSAGE + revenue);
    }

    return ResponseEntity.badRequest().body(AD_VIEW_FAILED_MESSAGE);
  }

  private int getApplicationIdForPurchase(final int purchaseId) {
    return purchaseRepository
        .findById(purchaseId)
        .orElseThrow(() -> new IllegalArgumentException(PURCHASE_NOT_FOUND_EXCEPTION))
        .getMonetizedApplication()
        .getApplication()
        .getId();
  }

  private int getApplicationIdForAd(final int adId) {
    return addRepository
        .findById(adId)
        .orElseThrow(() -> new IllegalArgumentException(AD_NOT_FOUND_EXCEPTION))
        .getMonetizedApplication()
        .getApplication()
        .getId();
  }
}
