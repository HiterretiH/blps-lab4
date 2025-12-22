package org.lab1.repository;

import java.util.List;
import org.lab1.model.MonetizedApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonetizedApplicationRepository
    extends JpaRepository<MonetizedApplication, Integer> {
  MonetizedApplication findByApplicationId(int applicationId);

  List<MonetizedApplication> findByDeveloperUserId(int developerUserId);
}
