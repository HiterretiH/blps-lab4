package org.lab1.repository;

import java.util.List;
import org.lab1.model.InAppAdd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InAppAddRepository extends JpaRepository<InAppAdd, Integer> {
  List<InAppAdd> findByMonetizedApplicationId(int monetizedApplicationId);
}
