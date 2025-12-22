package org.lab1.repository;

import org.lab1.model.ApplicationStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationStatsRepository extends JpaRepository<ApplicationStats, Integer> {
  ApplicationStats findByApplicationId(int applicationId);
}
