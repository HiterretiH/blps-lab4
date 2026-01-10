package org.lab1.repository;

import org.lab1.model.Developer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeveloperRepository extends JpaRepository<Developer, Integer> {
  Optional<Developer> findByUserId(Integer userId);
}
