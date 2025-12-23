package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.LocalDateTime;
import org.lab.logger.Logger;
import org.lab1.model.MonetizedApplication;
import org.lab1.model.PaymentRequest;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonetizationService {
  private static final String MONETIZATION_RESULT_METRIC = "monetization.payout.result";
  private static final String STATUS_TAG = "status";
  private static final String SUCCESS_TAG = "success";
  private static final String FAIL_TAG = "fail";
  private static final String MONETIZATION_REASON_METRIC = "monetization.payout.reason";
  private static final String REASON_TAG = "reason";
  private static final String EXPIRED_REASON = "expired";
  private static final String INVALID_CARD_REASON = "invalid_card";
  private static final String INSUFFICIENT_FUNDS_REASON = "insufficient_funds";
  private static final String APP_NOT_FOUND_REASON = "app_not_found";
  private static final String MONETIZATION_AMOUNT_METRIC = "monetization.payout.amount";
  private static final String USD_UNIT = "USD";
  private static final String FETCH_INFO_LOG = "Fetching monetization info for application ID: ";
  private static final String INFO_FOUND_LOG = "Monetization info found for application ID: ";
  private static final String CURRENT_BALANCE_LOG = ". Current balance: ";
  private static final String INFO_NOT_FOUND_LOG =
      "Monetization info not found for application ID: ";
  private static final String GENERATE_FORM_LOG = "Generating payout form for application ID: ";
  private static final String AMOUNT_LOG = ", amount: ";
  private static final String FORM_GENERATED_LOG = "Payout form generated for application ID: ";
  private static final String REQUEST_ID_LOG = ", request ID: ";
  private static final String PROCESS_PAYOUT_LOG = "Processing payout for application ID: ";
  private static final String APP_NOT_FOUND_LOG = "Application not found with ID: ";
  private static final String APP_NOT_FOUND_MSG = "Application not found.";
  private static final String REQUEST_TIME_MISSING_LOG =
      "Request time is missing for payout of application ID: ";
  private static final String REQUEST_TIME_MISSING_MSG = "Request time is missing.";
  private static final int REQUEST_EXPIRY_MINUTES = 30;
  private static final String PAYMENT_EXPIRED_LOG = "Payment request expired for application ID: ";
  private static final String PAYMENT_EXPIRED_MSG =
      "Payment request expired (more than 30 minutes).";
  private static final String CARD_INVALID_LOG =
      "Card details are invalid for payout of application ID: ";
  private static final String CARD_INVALID_MSG = "Card details are invalid.";
  private static final String INSUFFICIENT_FUNDS_LOG = "Insufficient funds for application ID: ";
  private static final String INSUFFICIENT_FUNDS_MSG = "Insufficient funds.";
  private static final String PAYOUT_SUCCESSFUL_LOG = "Payout successful for application ID: ";
  private static final String NEW_BALANCE_LOG = ". New balance: ";
  private static final String PAYMENT_SUCCESSFUL_MSG = "Payment successful. Amount deducted: ";

  private final Counter payoutSuccessCounter;
  private final Counter payoutFailCounter;
  private final Counter payoutExpiredCounter;
  private final Counter invalidCardCounter;
  private final Counter insufficientFundsCounter;
  private final Counter appNotFoundCounter;
  private final DistributionSummary payoutAmountSummary;
  private final MonetizedApplicationRepository monetizedApplicationRepository;
  private final Logger logger;

  @Autowired
  public MonetizationService(
      final MonetizedApplicationRepository monetizedApplicationRepository,
      final MeterRegistry meterRegistry,
      final Logger logger) {
    this.monetizedApplicationRepository = monetizedApplicationRepository;
    this.logger = logger;
    this.payoutSuccessCounter =
        Counter.builder(MONETIZATION_RESULT_METRIC)
            .tag(STATUS_TAG, SUCCESS_TAG)
            .register(meterRegistry);
    this.payoutFailCounter =
        Counter.builder(MONETIZATION_RESULT_METRIC)
            .tag(STATUS_TAG, FAIL_TAG)
            .register(meterRegistry);
    this.payoutExpiredCounter =
        Counter.builder(MONETIZATION_REASON_METRIC)
            .tag(REASON_TAG, EXPIRED_REASON)
            .register(meterRegistry);
    this.invalidCardCounter =
        Counter.builder(MONETIZATION_REASON_METRIC)
            .tag(REASON_TAG, INVALID_CARD_REASON)
            .register(meterRegistry);
    this.insufficientFundsCounter =
        Counter.builder(MONETIZATION_REASON_METRIC)
            .tag(REASON_TAG, INSUFFICIENT_FUNDS_REASON)
            .register(meterRegistry);
    this.appNotFoundCounter =
        Counter.builder(MONETIZATION_REASON_METRIC)
            .tag(REASON_TAG, APP_NOT_FOUND_REASON)
            .register(meterRegistry);
    this.payoutAmountSummary =
        DistributionSummary.builder(MONETIZATION_AMOUNT_METRIC)
            .baseUnit(USD_UNIT)
            .register(meterRegistry);
  }

  public final MonetizedApplication getMonetizationInfo(final int applicationId) {
    logger.info(FETCH_INFO_LOG + applicationId);
    MonetizedApplication monetizedApp =
        monetizedApplicationRepository.findByApplicationId(applicationId);

    if (monetizedApp != null) {
      logger.info(
          INFO_FOUND_LOG + applicationId + CURRENT_BALANCE_LOG + monetizedApp.getCurrentBalance());
    } else {
      logger.info(INFO_NOT_FOUND_LOG + applicationId);
    }

    return monetizedApp;
  }

  public final PaymentRequest sendForm(final int applicationId, final double amount) {
    logger.info(GENERATE_FORM_LOG + applicationId + AMOUNT_LOG + amount);
    PaymentRequest paymentRequest = new PaymentRequest(applicationId, amount);
    logger.info(
        FORM_GENERATED_LOG
            + applicationId
            + REQUEST_ID_LOG
            + paymentRequest.getApplicationId()
            + AMOUNT_LOG
            + paymentRequest.getAmount());
    return paymentRequest;
  }

  public final String makePayout(final PaymentRequest paymentRequest) {
    logger.info(
        PROCESS_PAYOUT_LOG
            + paymentRequest.getApplicationId()
            + AMOUNT_LOG
            + paymentRequest.getAmount());

    MonetizedApplication monetizedApp =
        monetizedApplicationRepository.findByApplicationId(paymentRequest.getApplicationId());
    if (monetizedApp == null) {
      appNotFoundCounter.increment();
      logger.error(APP_NOT_FOUND_LOG + paymentRequest.getApplicationId());
      return APP_NOT_FOUND_MSG;
    }

    if (paymentRequest.getRequestTime() == null) {
      payoutFailCounter.increment();
      logger.error(REQUEST_TIME_MISSING_LOG + paymentRequest.getApplicationId());
      return REQUEST_TIME_MISSING_MSG;
    }

    Duration timeElapsed = Duration.between(paymentRequest.getRequestTime(), LocalDateTime.now());
    if (timeElapsed.toMinutes() > REQUEST_EXPIRY_MINUTES) {
      payoutExpiredCounter.increment();
      logger.error(
          PAYMENT_EXPIRED_LOG + paymentRequest.getApplicationId() + " (more than 30 minutes).");
      return PAYMENT_EXPIRED_MSG;
    }

    if (!paymentRequest.isCardValid()) {
      invalidCardCounter.increment();
      logger.error(CARD_INVALID_LOG + paymentRequest.getApplicationId());
      return CARD_INVALID_MSG;
    }

    if (monetizedApp.getCurrentBalance() < paymentRequest.getAmount()) {
      insufficientFundsCounter.increment();
      logger.error(
          INSUFFICIENT_FUNDS_LOG
              + paymentRequest.getApplicationId()
              + CURRENT_BALANCE_LOG
              + monetizedApp.getCurrentBalance()
              + AMOUNT_LOG
              + paymentRequest.getAmount());
      return INSUFFICIENT_FUNDS_MSG;
    }

    monetizedApp.setCurrentBalance(monetizedApp.getCurrentBalance() - paymentRequest.getAmount());
    monetizedApplicationRepository.save(monetizedApp);

    payoutSuccessCounter.increment();
    payoutAmountSummary.record(paymentRequest.getAmount());
    logger.info(
        PAYOUT_SUCCESSFUL_LOG
            + paymentRequest.getApplicationId()
            + AMOUNT_LOG
            + paymentRequest.getAmount()
            + NEW_BALANCE_LOG
            + monetizedApp.getCurrentBalance());

    return PAYMENT_SUCCESSFUL_MSG + paymentRequest.getAmount();
  }
}
