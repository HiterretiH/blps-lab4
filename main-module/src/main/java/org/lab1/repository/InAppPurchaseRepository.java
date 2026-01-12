package org.lab1.repository;

import java.util.List;
import org.lab1.model.InAppPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InAppPurchaseRepository extends JpaRepository<InAppPurchase, Integer> {
  List<InAppPurchase> findByMonetizedApplicationNull();

  @Query("SELECT p FROM InAppPurchase p WHERE p.monetizedApplication.id = :monetizedAppId")
  List<InAppPurchase> findByMonetizedApplicationId(@Param("monetizedAppId") int monetizedAppId);
}
