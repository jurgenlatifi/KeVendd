package com.keVend.backend.service;

import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.Parking;
import com.keVend.backend.model.PromotionPurchase;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.ParkingRepository;
import com.keVend.backend.repository.PromotionPurchaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * FR-12: owner pays to boost a lot's ranking. Tier becomes the lot's
 * promotionRank for the period; on expiry a scheduled job clears it back to 0.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotedListingService {

    private final PromotionPurchaseRepository purchaseRepository;
    private final ParkingRepository parkingRepository;
    private final ParkingService parkingService;
    private final I18n i18n;

    @Transactional
    public PromotionPurchase purchase(User owner, Long parkingId, int tier, int days, BigDecimal pricePaid) {
        if (tier < 1) {
            throw i18n.badRequest("error.parking.invalid_available_spots");
        }
        Parking parking = parkingService.loadOrThrow(parkingId);
        if (parking.getOwner() == null || !parking.getOwner().getId().equals(owner.getId())) {
            throw i18n.forbidden("error.parking.not_owner");
        }

        PromotionPurchase pp = new PromotionPurchase();
        pp.setOwner(owner);
        pp.setParking(parking);
        pp.setTier(tier);
        pp.setPricePaidEur(pricePaid);
        pp.setPeriodStart(Instant.now());
        pp.setPeriodEnd(Instant.now().plus(days, ChronoUnit.DAYS));
        purchaseRepository.save(pp);

        // Apply immediately — keep the highest active tier for the lot
        if (parking.getPromotionRank() < tier) {
            parking.setPromotionRank(tier);
            parkingRepository.save(parking);
        }
        log.info("[promoted-listing] owner={} parking={} tier={} until={}",
                owner.getId(), parking.getId(), tier, pp.getPeriodEnd());
        return pp;
    }

    public List<PromotionPurchase> mine(User owner) {
        return purchaseRepository.findByOwnerId(owner.getId());
    }

    /** Daily — flip expired purchases and reset their lot's rank to 0. */
    @Scheduled(cron = "0 15 0 * * *")
    @Transactional
    public void expirePurchases() {
        for (PromotionPurchase pp : purchaseRepository.findExpired(Instant.now())) {
            pp.setStatus(PromotionPurchase.Status.EXPIRED);
            purchaseRepository.save(pp);

            Parking parking = pp.getParking();
            // Only reset to 0 if no other active boost is keeping the rank up
            boolean stillBoosted = purchaseRepository.findByOwnerId(parking.getOwner().getId()).stream()
                    .anyMatch(o -> o.getStatus() == PromotionPurchase.Status.ACTIVE
                            && o.getParking().getId().equals(parking.getId()));
            if (!stillBoosted && parking.getPromotionRank() > 0) {
                parking.setPromotionRank(0);
                parkingRepository.save(parking);
            }
        }
    }
}
