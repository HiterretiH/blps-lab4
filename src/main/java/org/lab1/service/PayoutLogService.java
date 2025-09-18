package org.lab1.service;

import org.lab.logger.Logger;
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
    @Autowired
    private Logger logger;

    public PayoutLog save(PayoutLog payoutLog) {
        logger.info("Saving PayoutLog");
        return payoutLogRepository.save(payoutLog);
    }

    public Optional<PayoutLog> findById(int id) {
        logger.info("Finding PayoutLog by ID: " + id);
        return payoutLogRepository.findById(id);
    }

    public List<PayoutLog> findAll() {
        logger.info("Finding all PayoutLogs");
        return payoutLogRepository.findAll();
    }

    public void delete(int id) {
        logger.info("Deleting PayoutLog by ID: " + id);
        payoutLogRepository.deleteById(id);
    }
}