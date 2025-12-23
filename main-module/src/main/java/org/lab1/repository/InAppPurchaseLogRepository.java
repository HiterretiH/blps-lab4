package org.lab1.repository;

import org.lab1.model.InAppPurchaseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InAppPurchaseLogRepository extends JpaRepository<InAppPurchaseLog, Integer> {}
