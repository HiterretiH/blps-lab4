package org.lab1.controller;

import org.lab1.model.PayoutLog;
import org.lab1.service.PayoutLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payout-logs")
public class PayoutLogController {

    @Autowired
    private PayoutLogService payoutLogService;

    @PostMapping
    public PayoutLog create(@RequestBody PayoutLog payoutLog) {
        return payoutLogService.save(payoutLog);
    }

    @PreAuthorize("hasAuthority('payout_log.manage')")
    @PutMapping("/{id}")
    public PayoutLog update(@PathVariable int id, @RequestBody PayoutLog payoutLog) {
        payoutLog.setId(id);
        return payoutLogService.save(payoutLog);
    }

    @PreAuthorize("hasAuthority('payout_log.read')")
    @GetMapping("/{id}")
    public Optional<PayoutLog> getById(@PathVariable int id) {
        return payoutLogService.findById(id);
    }

    @PreAuthorize("hasAuthority('payout_log.read')")
    @GetMapping
    public List<PayoutLog> getAll() {
        return payoutLogService.findAll();
    }

    @PreAuthorize("hasAuthority('payout_log.manage')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        payoutLogService.delete(id);
    }
}