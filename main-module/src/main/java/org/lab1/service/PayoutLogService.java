package org.lab1.service;

import java.util.List;
import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.model.PayoutLog;
import org.lab1.repository.PayoutLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PayoutLogService {
  private static final String SAVE_LOG = "Saving PayoutLog";
  private static final String FIND_BY_ID_LOG = "Finding PayoutLog by ID: ";
  private static final String FIND_ALL_LOG = "Finding all PayoutLogs";
  private static final String DELETE_LOG = "Deleting PayoutLog by ID: ";

  @Autowired private PayoutLogRepository payoutLogRepository;

  @Autowired
  @Qualifier("correlationLogger")
  private Logger logger;

  public final PayoutLog save(final PayoutLog payoutLog) {
    logger.info(SAVE_LOG);
    return payoutLogRepository.save(payoutLog);
  }

  public final Optional<PayoutLog> findById(final int id) {
    logger.info(FIND_BY_ID_LOG + id);
    return payoutLogRepository.findById(id);
  }

  public final List<PayoutLog> findAll() {
    logger.info(FIND_ALL_LOG);
    return payoutLogRepository.findAll();
  }

  public final void delete(final int id) {
    logger.info(DELETE_LOG + id);
    payoutLogRepository.deleteById(id);
  }
}
