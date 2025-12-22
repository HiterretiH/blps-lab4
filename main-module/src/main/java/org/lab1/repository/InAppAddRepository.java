package org.lab1.repository;

import org.lab1.model.InAppAdd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InAppAddRepository extends JpaRepository<InAppAdd, Integer> {
    List<InAppAdd> findByMonetizedApplicationId(int monetizedApplicationId);
}
