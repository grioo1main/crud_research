package com.lum1nar.tcp_project.repository;

import com.lum1nar.tcp_project.model.RequestMetric;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MetricRepository extends JpaRepository<RequestMetric, Long> {

    List<RequestMetric> findByLoadTestId(String loadTestId);

    List<RequestMetric> findByTimestampAfter(LocalDateTime after);

    List<RequestMetric> findByHttpMethodAndTimestampAfter(String method, LocalDateTime after);

    @Query("SELECT m FROM RequestMetric m WHERE m.loadTest = false ORDER BY m.timestamp DESC")
    List<RequestMetric> findRecentNonLoadTest();

    @Query("SELECT m FROM RequestMetric m WHERE m.loadTest = false AND m.timestamp > :after ORDER BY m.timestamp ASC")
    List<RequestMetric> findNonLoadTestAfter(@Param("after") LocalDateTime after);

    long countByTimestampAfter(LocalDateTime after);

    long countByStatusCodeGreaterThanEqualAndTimestampAfter(int statusCode, LocalDateTime after);

    @Query("SELECT m FROM RequestMetric m WHERE m.loadTestId = :testId ORDER BY m.timestamp ASC")
    List<RequestMetric> findByLoadTestIdOrdered(@Param("testId") String testId);

    @Modifying
    @Transactional
    void deleteByLoadTestTrue();
}