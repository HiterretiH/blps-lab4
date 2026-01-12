package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.exception.NotFoundException;
import org.lab1.exception.ValidationException;
import org.lab1.model.PaymentRequest;
import org.lab1.service.PaymentRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment-requests")
public class PaymentRequestController {
  private static final String CREATE_REQUEST_LOG =
      "Received request to create PaymentRequest for application ID: ";
  private static final String AMOUNT_LOG = ", amount: ";
  private static final String CREATE_SUCCESS_LOG = "PaymentRequest created with ID: ";
  private static final String GET_REQUEST_LOG =
      "Received request to get PaymentRequest by application ID: ";
  private static final String GET_NOT_FOUND_LOG = "PaymentRequest not found for application ID: ";
  private static final String VALIDATE_REQUEST_LOG =
      "Received request to validate card for application ID: ";
  private static final String CARD_VALID_LOG = "Card is valid for application ID: ";
  private static final String CARD_INVALID_LOG = "Card is invalid for application ID: ";
  private static final String CARD_VALID_MESSAGE = "Card is valid";
  private static final String CARD_INVALID_MESSAGE = "Invalid card";

  private final PaymentRequestService paymentRequestService;
  private final Logger logger;

  @Autowired
  public PaymentRequestController(
      final PaymentRequestService paymentRequestServiceParam,
      @Qualifier("correlationLogger") final Logger loggerParam) {
    this.paymentRequestService = paymentRequestServiceParam;
    this.logger = loggerParam;
  }

  @PreAuthorize("hasAuthority('payment_request.create')")
  @PostMapping
  public ResponseEntity<PaymentRequest> createPaymentRequest(
      @RequestParam final int applicationId, @RequestParam final double amount) {
    logger.info(CREATE_REQUEST_LOG + applicationId + AMOUNT_LOG + amount);
    PaymentRequest paymentRequest =
        paymentRequestService.createPaymentRequest(applicationId, amount);
    logger.info(
        CREATE_SUCCESS_LOG
            + paymentRequest.getApplicationId()
            + AMOUNT_LOG
            + paymentRequest.getAmount());
    return ResponseEntity.ok(paymentRequest);
  }

  @PreAuthorize("hasAuthority('payment_request.read')")
  @GetMapping("/{applicationId}")
  public ResponseEntity<PaymentRequest> getPaymentRequest(@PathVariable final int applicationId) {
    logger.info(GET_REQUEST_LOG + applicationId);
    PaymentRequest paymentRequest =
        paymentRequestService
            .getPaymentRequestById(applicationId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "PaymentRequest not found for application ID: " + applicationId));

    return ResponseEntity.ok(paymentRequest);
  }

  @PreAuthorize("hasAuthority('payment_request.validate_card')")
  @GetMapping("/validate/{applicationId}")
  public ResponseEntity<String> validateCard(@PathVariable final int applicationId) {
    logger.info(VALIDATE_REQUEST_LOG + applicationId);
    PaymentRequest paymentRequest =
        paymentRequestService
            .getPaymentRequestById(applicationId)
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "PaymentRequest not found for application ID: " + applicationId));

    if (paymentRequestService.validateCard(paymentRequest)) {
      logger.info(CARD_VALID_LOG + applicationId);
      return ResponseEntity.ok(CARD_VALID_MESSAGE);
    }

    logger.info(CARD_INVALID_LOG + applicationId);
    throw new ValidationException(CARD_INVALID_MESSAGE);
  }
}
