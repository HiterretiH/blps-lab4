package org.lab1.service;

import org.lab1.model.PayoutLog;
import org.lab1.repository.PayoutLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PayoutLogService {
    @Autowired
    private PayoutLogRepository payoutLogRepository;

    public PayoutLog save(PayoutLog payoutLog) {
        return payoutLogRepository.save(payoutLog);
    }

    public Optional<PayoutLog> findById(int id) {
        return payoutLogRepository.findById(id);
    }

    public List<PayoutLog> findAll() {
        return payoutLogRepository.findAll();
    }

    public void delete(int id) {
        payoutLogRepository.deleteById(id);
    }
}