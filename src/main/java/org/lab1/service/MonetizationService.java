package org.lab1.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.lab.logger.Logger;
import org.lab1.model.MonetizedApplication;
import org.lab1.model.PaymentRequest;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MonetizationService {

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
    public MonetizationService(MonetizedApplicationRepository monetizedApplicationRepository,
                               MeterRegistry meterRegistry,
                               Logger logger) {
        this.monetizedApplicationRepository = monetizedApplicationRepository;
        this.logger = logger;
        this.payoutSuccessCounter = Counter.builder("monetization.payout.result")
                .tag("status", "success")
                .register(meterRegistry);
        this.payoutFailCounter = Counter.builder("monetization.payout.result")
                .tag("status", "fail")
                .register(meterRegistry);
        this.payoutExpiredCounter = Counter.builder("monetization.payout.reason")
                .tag("reason", "expired")
                .register(meterRegistry);
        this.invalidCardCounter = Counter.builder("monetization.payout.reason")
                .tag("reason", "invalid_card")
                .register(meterRegistry);
        this.insufficientFundsCounter = Counter.builder("monetization.payout.reason")
                .tag("reason", "insufficient_funds")
                .register(meterRegistry);
        this.appNotFoundCounter = Counter.builder("monetization.payout.reason")
                .tag("reason", "app_not_found")
                .register(meterRegistry);
        this.payoutAmountSummary = DistributionSummary.builder("monetization.payout.amount")
                .baseUnit("USD")
                .register(meterRegistry);
    }

    public MonetizedApplication getMonetizationInfo(int applicationId) {
        logger.info("Fetching monetization info for application ID: " + applicationId);
        MonetizedApplication monetizedApp = monetizedApplicationRepository.findByApplicationId(applicationId);
        if (monetizedApp != null) {
            logger.info("Monetization info found for application ID: " + applicationId + ". Current balance: " + monetizedApp.getCurrentBalance());
        } else {
            logger.info("Monetization info not found for application ID: " + applicationId);
        }
        return monetizedApp;
    }

    public PaymentRequest sendForm(int applicationId, double amount) {
        logger.info("Generating payout form for application ID: " + applicationId + ", amount: " + amount);
        PaymentRequest paymentRequest = new PaymentRequest(applicationId, amount);
        logger.info("Payout form generated for application ID: " + applicationId + ", request ID: " + paymentRequest.getApplicationId() + ", amount: " + paymentRequest.getAmount());
        return paymentRequest;
    }

    public String makePayout(PaymentRequest paymentRequest) {
        logger.info("Processing payout for application ID: " + paymentRequest.getApplicationId() + ", amount: " + paymentRequest.getAmount());
        MonetizedApplication monetizedApp = monetizedApplicationRepository.findByApplicationId(paymentRequest.getApplicationId());
        if (monetizedApp == null) {
            appNotFoundCounter.increment();
            logger.error("Application not found with ID: " + paymentRequest.getApplicationId());
            return "Application not found.";
        }
        if (paymentRequest.getRequestTime() == null) {
            payoutFailCounter.increment();
            logger.error("Request time is missing for payout of application ID: " + paymentRequest.getApplicationId());
            return "Request time is missing.";
        }
        Duration timeElapsed = Duration.between(paymentRequest.getRequestTime(), LocalDateTime.now());
        if (timeElapsed.toMinutes() > 30) {
            payoutExpiredCounter.increment();
            logger.error("Payment request expired for application ID: " + paymentRequest.getApplicationId() + " (more than 30 minutes).");
            return "Payment request expired (more than 30 minutes).";
        }
        if (!paymentRequest.isCardValid()) {
            invalidCardCounter.increment();
            logger.error("Card details are invalid for payout of application ID: " + paymentRequest.getApplicationId());
            return "Card details are invalid.";
        }
        if (monetizedApp.getCurrentBalance() < paymentRequest.getAmount()) {
            insufficientFundsCounter.increment();
            logger.error("Insufficient funds for application ID: " + paymentRequest.getApplicationId() + ". Current balance: " + monetizedApp.getCurrentBalance() + ", requested amount: " + paymentRequest.getAmount());
            return "Insufficient funds.";
        }
        monetizedApp.setCurrentBalance(monetizedApp.getCurrentBalance() - paymentRequest.getAmount());
        monetizedApplicationRepository.save(monetizedApp);
        payoutSuccessCounter.increment();
        payoutAmountSummary.record(paymentRequest.getAmount());
        logger.info("Payout successful for application ID: " + paymentRequest.getApplicationId() + ", amount: " + paymentRequest.getAmount() + ". New balance: " + monetizedApp.getCurrentBalance());
        return "Payment successful. Amount deducted: " + paymentRequest.getAmount();
    }
}