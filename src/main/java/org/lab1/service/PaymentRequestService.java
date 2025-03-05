package org.lab1.service;

import org.lab1.model.PaymentRequest;
import org.lab1.repository.PaymentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentRequestService {

    private final PaymentRequestRepository paymentRequestRepository;

    @Autowired
    public PaymentRequestService(PaymentRequestRepository paymentRequestRepository) {
        this.paymentRequestRepository = paymentRequestRepository;
    }

    public PaymentRequest createPaymentRequest(int applicationId, double amount) {
        PaymentRequest paymentRequest = new PaymentRequest(applicationId, amount);
        return paymentRequestRepository.save(paymentRequest);
    }

    public Optional<PaymentRequest> getPaymentRequestById(int applicationId) {
        return paymentRequestRepository.findById(applicationId);
    }

    public boolean validateCard(PaymentRequest paymentRequest) {
        return paymentRequest.isCardValid();
    }
}