package org.lab1.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.lab1.exception.NotFoundException;
import org.lab1.exception.UnauthorizedException;
import org.lab1.json.Card;
import org.lab1.json.MonetizationEvent;
import org.lab1.model.Application;
import org.lab1.service.ApplicationService;
import org.lab1.service.GoogleTaskSender;
import org.lab1.service.UserMonetizationService;
import org.lab1.service.UserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserMonetizationController {
  private static final String APPLICATION_NOT_FOUND_MESSAGE = "Application not found";
  private static final String DOWNLOAD_SUCCESS_MESSAGE = "Application downloaded successfully.";
  private static final String DOWNLOAD_FAILED_MESSAGE = "Application not found or download failed.";
  private static final String PURCHASE_SUCCESS_MESSAGE = "In-app purchase successful.";
  private static final String PURCHASE_FAILED_MESSAGE = "Purchase not found or failed.";
  private static final String AD_VIEW_SUCCESS_MESSAGE = "Ad viewed successfully. Revenue: ";
  private static final String AD_VIEW_FAILED_MESSAGE = "Ad not found or view failed.";
  private static final String USER_NOT_AUTHORIZED_MESSAGE = "User not authorized";
  private static final String PURCHASE_PROCESSING_TIME_METRIC =
      "user.monetization.purchase.processing.time";
  private static final String AD_VIEW_PROCESSING_TIME_METRIC =
      "user.monetization.ad_view.processing.time";

  private final UserMonetizationService userMonetizationService;
  private final GoogleTaskSender googleTaskSender;
  private final ApplicationService applicationService;
  private final UserQueryService userQueryService;
  private final MeterRegistry meterRegistry;
  private final Timer purchaseProcessingTime;
  private final Timer adViewProcessingTime;

  @Autowired
  public UserMonetizationController(
      final UserMonetizationService userMonetizationServiceParam,
      final GoogleTaskSender googleTaskSenderParam,
      final ApplicationService applicationServiceParam,
      final UserQueryService userQueryServiceParam,
      final MeterRegistry meterRegistryParam) {
    this.userMonetizationService = userMonetizationServiceParam;
    this.googleTaskSender = googleTaskSenderParam;
    this.applicationService = applicationServiceParam;
    this.userQueryService = userQueryServiceParam;
    this.meterRegistry = meterRegistryParam;
    this.purchaseProcessingTime = meterRegistry.timer(PURCHASE_PROCESSING_TIME_METRIC);
    this.adViewProcessingTime = meterRegistry.timer(AD_VIEW_PROCESSING_TIME_METRIC);
  }

  @PreAuthorize("hasAuthority('user.download_application')")
  @PostMapping("/download/{applicationId}")
  public ResponseEntity<String> downloadApplication(
      @PathVariable final int applicationId,
      @RequestBody final Card card,
      final Authentication authentication) {
    try {
      int userId = userQueryService.getCurrentAuthenticatedUser().getId();

      Application application = applicationService.getApplicationByIdOrThrow(applicationId);

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
            userMonetizationService.createDownloadEvent(
                userId, applicationId, application.getPrice());
        googleTaskSender.sendMonetizationEvent(userId, event);
        return ResponseEntity.ok(DOWNLOAD_SUCCESS_MESSAGE);
      }

      throw new NotFoundException(DOWNLOAD_FAILED_MESSAGE);
    } catch (NotFoundException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new UnauthorizedException(USER_NOT_AUTHORIZED_MESSAGE);
    }
  }

  @PreAuthorize("hasAuthority('user.purchase_in_app_item')")
  @PostMapping("/purchase/{purchaseId}")
  public ResponseEntity<String> purchaseInAppItem(
      @PathVariable final int purchaseId,
      @RequestBody final Card card,
      final Authentication authentication) {
    Timer.Sample sample = Timer.start(meterRegistry);

    try {
      int userId = userQueryService.getCurrentAuthenticatedUser().getId();

      boolean success =
          userMonetizationService.purchaseInAppItem(
              purchaseId,
              userId,
              card.getCardNumber(),
              card.getCardHolderName(),
              card.getExpiryDate(),
              card.getCvv());

      if (success) {
        double price = userMonetizationService.getInAppPurchaseById(purchaseId).getPrice();
        MonetizationEvent event =
            userMonetizationService.createPurchaseEvent(userId, purchaseId, price);
        sample.stop(purchaseProcessingTime);
        googleTaskSender.sendMonetizationEvent(userId, event);
        return ResponseEntity.ok(PURCHASE_SUCCESS_MESSAGE);
      }

      throw new NotFoundException(PURCHASE_FAILED_MESSAGE);
    } catch (NotFoundException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new UnauthorizedException(USER_NOT_AUTHORIZED_MESSAGE);
    }
  }

  @PreAuthorize("hasAuthority('user.view_advertisement')")
  @PostMapping("/view-ad/{adId}")
  public ResponseEntity<String> viewAdvertisement(
      @PathVariable final int adId, final Authentication authentication) {
    Timer.Sample sample = Timer.start(meterRegistry);

    try {
      int userId = userQueryService.getCurrentAuthenticatedUser().getId();

      boolean success = userMonetizationService.viewAdvertisement(adId);

      if (success) {
        double revenue = userMonetizationService.getInAppAddById(adId).getPrice();
        MonetizationEvent event = userMonetizationService.createAdViewEvent(userId, adId, revenue);
        sample.stop(adViewProcessingTime);
        googleTaskSender.sendMonetizationEvent(userId, event);
        return ResponseEntity.ok(AD_VIEW_SUCCESS_MESSAGE + revenue);
      }

      throw new NotFoundException(AD_VIEW_FAILED_MESSAGE);
    } catch (NotFoundException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new UnauthorizedException(USER_NOT_AUTHORIZED_MESSAGE);
    }
  }
}
