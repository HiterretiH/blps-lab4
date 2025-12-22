package org.lab1.repository;

import org.lab1.model.GoogleOperationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoogleOperationResultRepository extends JpaRepository<GoogleOperationResult, Long> {

    @Query(value = "SELECT * FROM google_operation_results WHERE user_id = :userId ORDER BY created_at DESC",
            nativeQuery = true)
    List<GoogleOperationResult> findByUserId(@Param("userId") Integer userId);

    @Query(value = "SELECT * FROM google_operation_results WHERE operation = :operation ORDER BY created_at DESC",
            nativeQuery = true)
    List<GoogleOperationResult> findByOperation(@Param("operation") String operation);

    @Query(value = "SELECT * FROM google_operation_results WHERE user_id = :userId AND operation = :operation ORDER BY created_at DESC",
            nativeQuery = true)
    List<GoogleOperationResult> findByUserIdAndOperation(@Param("userId") Integer userId,
                                                            @Param("operation") String operation);

    @Query(value = "SELECT * FROM google_operation_results WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit",
            nativeQuery = true)
    List<GoogleOperationResult> findLatestByUserId(@Param("userId") Integer userId,
                                                      @Param("limit") int limit);

    @Query(value = "SELECT * FROM google_operation_results WHERE error IS NOT NULL ORDER BY created_at DESC",
            nativeQuery = true)
    List<GoogleOperationResult> findWithErrors();

    @Query(value = "SELECT * FROM google_operation_results WHERE error IS NULL ORDER BY created_at DESC",
            nativeQuery = true)
    List<GoogleOperationResult> findSuccessfulOperations();
}
