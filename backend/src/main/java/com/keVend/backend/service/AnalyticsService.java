package com.keVend.backend.service;

import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.Parking;
import com.keVend.backend.model.Reservation;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.ParkingRepository;
import com.keVend.backend.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Owner analytics — derives a coarse occupancy figure per lot over a rolling
 * 30-day window and suggests a directional price change.
 *
 * Heuristic only — meant to be a starting point for owners, not a forecast.
 * The real recommender belongs in a separate ML/analytics pipeline once we
 * have more data.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final int WINDOW_DAYS = 30;
    private static final double UPPER_OCCUPANCY = 0.85;
    private static final double LOWER_OCCUPANCY = 0.40;
    private static final BigDecimal PRICE_STEP_PCT = BigDecimal.valueOf(10);

    private final ParkingRepository parkingRepository;
    private final ReservationRepository reservationRepository;
    private final OwnerSubscriptionService ownerSubscriptionService;
    private final I18n i18n;

    /**
     * Premium feature: requires the owner to hold an active subscription
     * (Stakeholders → Owner: "paid extra services like price-optimization").
     */
    public List<PriceSuggestion> priceSuggestionsFor(User owner) {
        if (!ownerSubscriptionService.hasActiveSubscription(owner.getId())) {
            throw i18n.status(org.springframework.http.HttpStatus.PAYMENT_REQUIRED,
                    "error.subscription.required");
        }
        return computeFor(owner.getId());
    }

    private List<PriceSuggestion> computeFor(Long ownerId) {
        Instant since = Instant.now().minus(WINDOW_DAYS, ChronoUnit.DAYS);
        List<PriceSuggestion> out = new ArrayList<>();
        for (Parking p : parkingRepository.findByOwnerId(ownerId)) {
            double occupancy = occupancyRatio(p, since);
            BigDecimal currentPrice = p.getPricePerHour();
            BigDecimal suggested = currentPrice;
            String reason;

            if (occupancy >= UPPER_OCCUPANCY) {
                suggested = adjust(currentPrice, PRICE_STEP_PCT);
                reason = "High occupancy (" + pct(occupancy) + ") — try +"
                        + PRICE_STEP_PCT + "% to monetise demand.";
            } else if (occupancy <= LOWER_OCCUPANCY) {
                suggested = adjust(currentPrice, PRICE_STEP_PCT.negate());
                reason = "Low occupancy (" + pct(occupancy) + ") — try -"
                        + PRICE_STEP_PCT + "% to attract more drivers.";
            } else {
                reason = "Occupancy in healthy range (" + pct(occupancy) + "). Hold current price.";
            }

            out.add(PriceSuggestion.builder()
                    .parkingId(p.getId())
                    .parkingName(p.getName())
                    .occupancy30d(BigDecimal.valueOf(occupancy).setScale(2, RoundingMode.HALF_UP))
                    .currentPrice(currentPrice)
                    .suggestedPrice(suggested)
                    .reason(reason)
                    .build());
        }
        return out;
    }

    private double occupancyRatio(Parking parking, Instant since) {
        if (parking.getTotalSpots() == null || parking.getTotalSpots() == 0) return 0;
        long parkingHoursAvailable = (long) parking.getTotalSpots() * 24 * WINDOW_DAYS;
        long occupiedHours = 0;
        for (Reservation r : reservationRepository.findByParkingId(parking.getId())) {
            if (r.getStartTime() == null || r.getEndTime() == null) continue;
            if (r.getStartTime().isBefore(since)) continue;
            long hours = Math.max(1, Duration.between(r.getStartTime(), r.getEndTime()).toHours());
            occupiedHours += hours * r.getSpotsReserved();
        }
        return Math.min(1.0, (double) occupiedHours / Math.max(parkingHoursAvailable, 1));
    }

    private BigDecimal adjust(BigDecimal price, BigDecimal pct) {
        if (price == null) return null;
        return price.multiply(BigDecimal.ONE.add(pct.movePointLeft(2))).setScale(2, RoundingMode.HALF_UP);
    }

    private String pct(double v) {
        return Math.round(v * 1000.0) / 10.0 + "%";
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class PriceSuggestion {
        private Long parkingId;
        private String parkingName;
        private BigDecimal occupancy30d;
        private BigDecimal currentPrice;
        private BigDecimal suggestedPrice;
        private String reason;
    }
}
