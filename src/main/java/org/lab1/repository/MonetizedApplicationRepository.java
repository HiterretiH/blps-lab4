package org.lab1.repository;

import org.lab1.model.MonetizedApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonetizedApplicationRepository extends JpaRepository<MonetizedApplication, Integer> {
    MonetizedApplication findByApplicationId(int applicationId);
    List<MonetizedApplication> findByDeveloperUserId(int developerUserId);
}
