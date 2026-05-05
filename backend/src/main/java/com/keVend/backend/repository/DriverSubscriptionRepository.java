package com.keVend.backend.repository;

import com.keVend.backend.model.DriverSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverSubscriptionRepository extends JpaRepository<DriverSubscription, Long> {

    @Query("""
            SELECT s FROM DriverSubscription s
            WHERE s.driver.id = :driverId
              AND s.status = com.keVend.backend.model.DriverSubscription.Status.ACTIVE
              AND s.periodEnd > :now
            """)
    Optional<DriverSubscription> findActive(@Param("driverId") Long driverId, @Param("now") Instant now);

    @Query("""
            SELECT s FROM DriverSubscription s
            WHERE s.status = com.keVend.backend.model.DriverSubscription.Status.ACTIVE
              AND s.periodEnd <= :now
            """)
    List<DriverSubscription> findExpired(@Param("now") Instant now);
}
