package com.keVend.backend.repository;

import com.keVend.backend.model.PromotionRedemption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromotionRedemptionRepository extends JpaRepository<PromotionRedemption, Long> {

    long countByPromotionId(Long promotionId);

    long countByPromotionIdAndDriverId(Long promotionId, Long driverId);
}
