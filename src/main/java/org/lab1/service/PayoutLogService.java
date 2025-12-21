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
    private static final String SAVE_LOG = "Saving PayoutLog";
    private static final String FIND_BY_ID_LOG = "Finding PayoutLog by ID: ";
    private static final String FIND_ALL_LOG = "Finding all PayoutLogs";
    private static final String DELETE_LOG = "Deleting PayoutLog by ID: ";

    @Autowired
    private PayoutLogRepository payoutLogRepository;
    @Autowired
    private Logger logger;

    public PayoutLog save(PayoutLog payoutLog) {
        logger.info(SAVE_LOG);
        return payoutLogRepository.save(payoutLog);
    }

    public Optional<PayoutLog> findById(int id) {
        logger.info(FIND_BY_ID_LOG + id);
        return payoutLogRepository.findById(id);
    }

    public List<PayoutLog> findAll() {
        logger.info(FIND_ALL_LOG);
        return payoutLogRepository.findAll();
    }

    public void delete(int id) {
        logger.info(DELETE_LOG + id);
        payoutLogRepository.deleteById(id);
    }
}
