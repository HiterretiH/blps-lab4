package org.lab1.controller;

import java.util.List;
import java.util.Optional;
import org.lab.logger.Logger;
import org.lab1.model.PayoutLog;
import org.lab1.service.PayoutLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payout-logs")
public final class PayoutLogController {
  private static final String CREATE_LOG = "Creating PayoutLog";
  private static final String UPDATE_LOG = "Updating PayoutLog with ID: ";
  private static final String GET_LOG = "Getting PayoutLog with ID: ";
  private static final String GET_ALL_LOG = "Getting all PayoutLogs";
  private static final String DELETE_LOG = "Deleting PayoutLog with ID: ";

  private final PayoutLogService payoutLogService;
  private final Logger logger;

  @Autowired
  public PayoutLogController(
      final PayoutLogService payoutLogServiceParam, final Logger loggerParam) {
    this.payoutLogService = payoutLogServiceParam;
    this.logger = loggerParam;
  }

  @PostMapping
  public PayoutLog create(@RequestBody final PayoutLog payoutLog) {
    logger.info(CREATE_LOG);
    return payoutLogService.save(payoutLog);
  }

  @PreAuthorize("hasAuthority('payout_log.manage')")
  @PutMapping("/{id}")
  public PayoutLog update(@PathVariable final int id, @RequestBody final PayoutLog payoutLog) {
    logger.info(UPDATE_LOG + id);
    payoutLog.setId(id);
    return payoutLogService.save(payoutLog);
  }

  @PreAuthorize("hasAuthority('payout_log.read')")
  @GetMapping("/{id}")
  public Optional<PayoutLog> getById(@PathVariable final int id) {
    logger.info(GET_LOG + id);
    return payoutLogService.findById(id);
  }

  @PreAuthorize("hasAuthority('payout_log.read')")
  @GetMapping
  public List<PayoutLog> getAll() {
    logger.info(GET_ALL_LOG);
    return payoutLogService.findAll();
  }

  @PreAuthorize("hasAuthority('payout_log.manage')")
  @DeleteMapping("/{id}")
  public void delete(@PathVariable final int id) {
    logger.info(DELETE_LOG + id);
    payoutLogService.delete(id);
  }
}
