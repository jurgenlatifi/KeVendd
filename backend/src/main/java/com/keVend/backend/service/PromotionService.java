package com.keVend.backend.service;

import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.Promotion;
import com.keVend.backend.model.PromotionRedemption;
import com.keVend.backend.model.Reservation;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.PromotionRedemptionRepository;
import com.keVend.backend.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final PromotionRedemptionRepository redemptionRepository;
    private final I18n i18n;

    /**
     * Resolves a code (or null) into a usable promo. Returns empty when the
     * code does not exist, is out of validity, or the global cap is hit.
     * Per-user limits are enforced at redeem time so the discount preview can
     * still surface the promo even when this driver is at their personal cap.
     */
    public Optional<Promotion> resolve(String code) {
        if (code == null || code.isBlank()) return Optional.empty();
        return promotionRepository.findByCode(code).filter(this::isValidNow);
    }

    public Optional<Promotion> resolveById(Long id) {
        return promotionRepository.findById(id).filter(this::isValidNow);
    }

    /**
     * Computes {@code total} after applying {@code promo}, returns null if no
     * promo. Caller is responsible for recording the redemption afterwards via
     * {@link #recordRedemption(Promotion, User, Reservation, BigDecimal)}.
     */
    public BigDecimal apply(BigDecimal total, Promotion promo) {
        if (promo == null) return total;
        return switch (promo.getType()) {
            case FREE_RESERVATION -> BigDecimal.ZERO;
            case PERCENT_OFF -> {
                BigDecimal pct = promo.getValue().min(BigDecimal.valueOf(100)).max(BigDecimal.ZERO);
                BigDecimal discount = total.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                yield total.subtract(discount).max(BigDecimal.ZERO);
            }
        };
    }

    @Transactional
    public PromotionRedemption recordRedemption(Promotion promo, User driver,
                                                Reservation reservation, BigDecimal discountApplied) {
        if (promo == null) return null;

        if (promo.getMaxRedemptions() != null
                && redemptionRepository.countByPromotionId(promo.getId()) >= promo.getMaxRedemptions()) {
            throw i18n.conflict("error.promotion.global_cap_reached");
        }
        if (promo.getPerUserLimit() != null
                && redemptionRepository.countByPromotionIdAndDriverId(promo.getId(), driver.getId())
                        >= promo.getPerUserLimit()) {
            throw i18n.conflict("error.promotion.user_cap_reached");
        }

        PromotionRedemption redemption = new PromotionRedemption();
        redemption.setPromotion(promo);
        redemption.setDriver(driver);
        redemption.setReservation(reservation);
        redemption.setDiscountApplied(discountApplied);
        return redemptionRepository.save(redemption);
    }

    public boolean canDriverRedeem(Promotion promo, User driver) {
        if (promo == null) return false;
        if (!isValidNow(promo)) return false;
        if (promo.getMaxRedemptions() != null
                && redemptionRepository.countByPromotionId(promo.getId()) >= promo.getMaxRedemptions()) {
            return false;
        }
        if (promo.getPerUserLimit() != null
                && redemptionRepository.countByPromotionIdAndDriverId(promo.getId(), driver.getId())
                        >= promo.getPerUserLimit()) {
            return false;
        }
        return true;
    }

    private boolean isValidNow(Promotion p) {
        Instant now = Instant.now();
        if (p.getValidFrom() != null && now.isBefore(p.getValidFrom())) return false;
        if (p.getValidTo() != null && now.isAfter(p.getValidTo())) return false;
        return true;
    }
}
