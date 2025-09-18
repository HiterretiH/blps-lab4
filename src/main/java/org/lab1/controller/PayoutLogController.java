package org.lab1.controller;

import org.lab.logger.Logger;
import org.lab1.model.PayoutLog;
import org.lab1.service.PayoutLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payout-logs")
public class PayoutLogController {

    @Autowired
    private PayoutLogService payoutLogService;
    @Autowired
    private Logger logger;

    @PostMapping
    public PayoutLog create(@RequestBody PayoutLog payoutLog) {
        logger.info("Creating PayoutLog");
        return payoutLogService.save(payoutLog);
    }

    @PreAuthorize("hasAuthority('payout_log.manage')")
    @PutMapping("/{id}")
    public PayoutLog update(@PathVariable int id, @RequestBody PayoutLog payoutLog) {
        logger.info("Updating PayoutLog with ID: " + id);
        payoutLog.setId(id);
        return payoutLogService.save(payoutLog);
    }

    @PreAuthorize("hasAuthority('payout_log.read')")
    @GetMapping("/{id}")
    public Optional<PayoutLog> getById(@PathVariable int id) {
        logger.info("Getting PayoutLog with ID: " + id);
        return payoutLogService.findById(id);
    }

    @PreAuthorize("hasAuthority('payout_log.read')")
    @GetMapping
    public List<PayoutLog> getAll() {
        logger.info("Getting all PayoutLogs");
        return payoutLogService.findAll();
    }

    @PreAuthorize("hasAuthority('payout_log.manage')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        logger.info("Deleting PayoutLog with ID: " + id);
        payoutLogService.delete(id);
    }
}