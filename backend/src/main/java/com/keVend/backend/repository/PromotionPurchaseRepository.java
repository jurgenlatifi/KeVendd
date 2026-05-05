package com.keVend.backend.repository;

import com.keVend.backend.model.PromotionPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PromotionPurchaseRepository extends JpaRepository<PromotionPurchase, Long> {

    List<PromotionPurchase> findByOwnerId(Long ownerId);

    @Query("""
            SELECT pp FROM PromotionPurchase pp
            WHERE pp.status = com.keVend.backend.model.PromotionPurchase.Status.ACTIVE
              AND pp.periodEnd <= :now
            """)
    List<PromotionPurchase> findExpired(@Param("now") Instant now);
}
