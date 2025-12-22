package org.lab1.repository;

import java.util.List;
import org.lab1.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Integer> {
  List<Application> findByDeveloperId(int id);
}
