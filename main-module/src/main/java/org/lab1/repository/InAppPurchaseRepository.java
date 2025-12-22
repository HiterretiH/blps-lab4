package org.lab1.repository;

import java.util.List;
import org.lab1.model.InAppPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InAppPurchaseRepository extends JpaRepository<InAppPurchase, Integer> {
  List<InAppPurchase> findByMonetizedApplicationNull();
}
