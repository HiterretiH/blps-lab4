package org.lab1.repository;

import org.lab1.model.ApplicationStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationStatsRepository extends JpaRepository<ApplicationStats, Integer> {
    ApplicationStats findByApplicationId(int applicationId);
}
