package org.lab1.service;

import org.lab.logger.Logger;
import org.lab1.model.PaymentRequest;
import org.lab1.repository.PaymentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentRequestService {

    private final PaymentRequestRepository paymentRequestRepository;
    private final Logger logger;

    @Autowired
    public PaymentRequestService(PaymentRequestRepository paymentRequestRepository, Logger logger) {
        this.paymentRequestRepository = paymentRequestRepository;
        this.logger = logger;
    }

    public PaymentRequest createPaymentRequest(int applicationId, double amount) {
        logger.info("Creating PaymentRequest for application ID: " + applicationId + ", amount: " + amount);
        PaymentRequest paymentRequest = new PaymentRequest(applicationId, amount);
        PaymentRequest savedRequest = paymentRequestRepository.save(paymentRequest);
        logger.info("PaymentRequest created with ID: " + savedRequest.getApplicationId() + ", amount: " + savedRequest.getAmount());
        return savedRequest;
    }

    public Optional<PaymentRequest> getPaymentRequestById(int applicationId) {
        logger.info("Fetching PaymentRequest by application ID: " + applicationId);
        Optional<PaymentRequest> paymentRequest = paymentRequestRepository.findById(applicationId);
        if (paymentRequest.isPresent()) {
            logger.info("PaymentRequest found for application ID: " + applicationId + ", amount: " + paymentRequest.get().getAmount());
        } else {
            logger.info("PaymentRequest not found for application ID: " + applicationId);
        }
        return paymentRequest;
    }

    public boolean validateCard(PaymentRequest paymentRequest) {
        logger.info("Validating card for PaymentRequest ID: " + paymentRequest.getApplicationId());
        boolean isValid = paymentRequest.isCardValid();
        logger.info("Card validation result for PaymentRequest ID: " + paymentRequest.getApplicationId() + ": " + isValid);
        return isValid;
    }
}