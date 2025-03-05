package org.lab1.repository;

import org.lab1.model.PayoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayoutLogRepository extends JpaRepository<PayoutLog, Integer> {
}