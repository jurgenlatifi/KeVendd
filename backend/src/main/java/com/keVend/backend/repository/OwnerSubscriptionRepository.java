package com.keVend.backend.repository;

import com.keVend.backend.model.OwnerSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerSubscriptionRepository extends JpaRepository<OwnerSubscription, Long> {

    @Query("""
            SELECT s FROM OwnerSubscription s
            WHERE s.owner.id = :ownerId
              AND s.status IN (
                  com.keVend.backend.model.OwnerSubscription.Status.TRIALING,
                  com.keVend.backend.model.OwnerSubscription.Status.ACTIVE
              )
              AND s.periodEnd > :now
            """)
    Optional<OwnerSubscription> findActive(@Param("ownerId") Long ownerId, @Param("now") Instant now);

    @Query("""
            SELECT s FROM OwnerSubscription s
            WHERE s.status IN (
                  com.keVend.backend.model.OwnerSubscription.Status.TRIALING,
                  com.keVend.backend.model.OwnerSubscription.Status.ACTIVE
              )
              AND s.periodEnd <= :now
            """)
    List<OwnerSubscription> findExpired(@Param("now") Instant now);
}
