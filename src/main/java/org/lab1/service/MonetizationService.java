package org.lab1.service;

import org.lab1.model.MonetizedApplication;
import org.lab1.model.PaymentRequest;
import org.lab1.repository.MonetizedApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class MonetizationService {

    @Autowired
    private MonetizedApplicationRepository monetizedApplicationRepository;

    public MonetizedApplication getMonetizationInfo(int applicationId) {
        return monetizedApplicationRepository.findByApplicationId(applicationId);
    }

    public PaymentRequest sendForm(int applicationId, double amount) {
        return new PaymentRequest(applicationId, amount);
    }

    public String makePayout(PaymentRequest paymentRequest) {
        MonetizedApplication monetizedApp = monetizedApplicationRepository.findByApplicationId(paymentRequest.getApplicationId());

        if (monetizedApp == null) {
            return "Application not found.";
        }

        if (paymentRequest.getRequestTime() == null) {
            return "Request time is missing.";
        }

        Duration timeElapsed = Duration.between(paymentRequest.getRequestTime(), LocalDateTime.now());
        if (timeElapsed.toMinutes() > 30) {
            return "Payment request expired (more than 30 minutes).";
        }

        if (!paymentRequest.isCardValid()) {
            return "Card details are invalid.";
        }

        if (monetizedApp.getCurrentBalance() < paymentRequest.getAmount()) {
            return "Insufficient funds.";
        }

        monetizedApp.setCurrentBalance(monetizedApp.getCurrentBalance() - paymentRequest.getAmount());
        monetizedApplicationRepository.save(monetizedApp);

        return "Payment successful. Amount deducted: " + paymentRequest.getAmount();
    }
}
