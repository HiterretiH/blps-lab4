package org.lab1.repository;

import org.lab1.model.GoogleAuthData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoogleAuthDataRepository extends JpaRepository<GoogleAuthData, Integer> {
    Optional<GoogleAuthData> findByUserId(Integer userId);
    boolean existsByUserEmail(String email);
    void deleteByUserId(Integer userId);
}
