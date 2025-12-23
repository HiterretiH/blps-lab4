package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.model.MonetizedApplication;
import org.lab1.model.PaymentRequest;
import org.lab1.service.MonetizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monetization")
public final class MonetizationController {
  private static final String GET_INFO_REQUEST_LOG =
      "Received request to get monetization info for application ID: ";
  private static final String INFO_NOT_FOUND_LOG =
      "Monetization info not found for application ID: ";
  private static final String INFO_FOUND_LOG = "Monetization info found for application ID: ";
  private static final String CURRENT_BALANCE_LOG = ". Current balance: ";
  private static final String SEND_FORM_REQUEST_LOG =
      "Received request to send payout form for application ID: ";
  private static final String AMOUNT_LOG = ", amount: ";
  private static final String FORM_SENT_LOG = "Payout form sent for application ID: ";
  private static final String REQUEST_ID_LOG = ", request ID: ";
  private static final String PAYOUT_REQUEST_LOG = "Received payout request for application ID: ";
  private static final String PAYOUT_SUCCESS_LOG = "Payout successful for application ID: ";
  private static final String RESULT_LOG = ". Result: ";
  private static final String PAYOUT_FAILED_LOG = "Payout failed for application ID: ";
  private static final String REASON_LOG = ". Reason: ";

  private final MonetizationService monetizationService;
  private final Logger logger;

  @Autowired
  public MonetizationController(
      final MonetizationService monetizationServiceParam,
      final Logger loggerParam) {
    this.monetizationService = monetizationServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('monetization.read')")
  @GetMapping("/info/{applicationId}")
  public ResponseEntity<MonetizedApplication> getMonetizationInfo(
      @PathVariable final int applicationId) {
    logger.info(GET_INFO_REQUEST_LOG + applicationId);
    MonetizedApplication monetizedApp =
        monetizationService.getMonetizationInfo(applicationId);

    if (monetizedApp == null) {
      logger.info(INFO_NOT_FOUND_LOG + applicationId);
      return ResponseEntity.notFound().build();
    }

    logger.info(
        INFO_FOUND_LOG + applicationId
            + CURRENT_BALANCE_LOG + monetizedApp.getCurrentBalance());
    return ResponseEntity.ok(monetizedApp);
  }

  @PreAuthorize("hasAuthority('monetization.payout.request')")
  @PostMapping("/sendForm/{applicationId}")
  public ResponseEntity<PaymentRequest> sendForm(
      @PathVariable final int applicationId,
      @RequestParam final double amount) {
    logger.info(SEND_FORM_REQUEST_LOG + applicationId
        + AMOUNT_LOG + amount);
    PaymentRequest paymentRequest =
        monetizationService.sendForm(applicationId, amount);
    logger.info(
        FORM_SENT_LOG
            + applicationId
            + REQUEST_ID_LOG
            + paymentRequest.getApplicationId()
            + AMOUNT_LOG
            + paymentRequest.getAmount());
    return ResponseEntity.ok(paymentRequest);
  }

  @PreAuthorize("hasAuthority('monetization.payout.execute')")
  @PostMapping("/payout")
  public ResponseEntity<String> makePayout(@RequestBody final PaymentRequest paymentRequest) {
    logger.info(
        PAYOUT_REQUEST_LOG
            + paymentRequest.getApplicationId()
            + AMOUNT_LOG
            + paymentRequest.getAmount());
    String result = monetizationService.makePayout(paymentRequest);

    if (result.contains("successful")) {
      logger.info(
          PAYOUT_SUCCESS_LOG
              + paymentRequest.getApplicationId()
              + AMOUNT_LOG
              + paymentRequest.getAmount()
              + RESULT_LOG
              + result);
      return ResponseEntity.ok(result);
    }

    logger.error(
        PAYOUT_FAILED_LOG
            + paymentRequest.getApplicationId()
            + AMOUNT_LOG
            + paymentRequest.getAmount()
            + REASON_LOG
            + result);
    return ResponseEntity.badRequest().body(result);
  }
}
