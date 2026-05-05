package com.keVend.backend.service;

import com.keVend.backend.dto.OwnerSessionView;
import com.keVend.backend.dto.ParkingResponse;
import com.keVend.backend.dto.RevenuePoint;
import com.keVend.backend.model.Payment;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.PaymentRepository;
import com.keVend.backend.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final ParkingService parkingService;

    public List<ParkingResponse> myParkings(User owner) {
        return parkingService.listOwnedBy(owner.getId());
    }

    /** FR-13: anonymized session list for a date range. Defaults to "today". */
    public List<OwnerSessionView> sessions(User owner, LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now();
        LocalDate effectiveTo = to != null ? to : effectiveFrom.plusDays(1);
        return reservationRepository.findOwnerSessionsBetween(
                        owner.getId(),
                        effectiveFrom.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                        effectiveTo.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .stream().map(OwnerSessionView::from).toList();
    }

    /**
     * FR-17: revenue breakdown over a date range, bucketed by day | week | month.
     * Defaults to last 30 days, daily.
     */
    public List<RevenuePoint> revenue(User owner, LocalDate from, LocalDate to, String bucket) {
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(30);
        LocalDateTime fromDt = effectiveFrom.atStartOfDay();
        LocalDateTime toDt = effectiveTo.plusDays(1).atStartOfDay();

        List<Payment> payments = paymentRepository
                .findCompletedForOwnerBetween(owner.getId(), fromDt, toDt);

        Function<Payment, String> keyFn = switch (bucket == null ? "day" : bucket.toLowerCase()) {
            case "week"  -> p -> p.getPaidAt().get(IsoFields.WEEK_BASED_YEAR)
                    + "-W" + String.format("%02d", p.getPaidAt().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case "month" -> p -> p.getPaidAt().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            default      -> p -> p.getPaidAt().toLocalDate().toString();
        };

        Map<String, RevenuePoint> agg = new LinkedHashMap<>();
        for (Payment p : payments) {
            String k = keyFn.apply(p);
            RevenuePoint existing = agg.get(k);
            BigDecimal earnings = p.getOwnerEarnings() != null ? p.getOwnerEarnings() : BigDecimal.ZERO;
            if (existing == null) {
                agg.put(k, new RevenuePoint(k, earnings, 1));
            } else {
                existing.setOwnerEarnings(existing.getOwnerEarnings().add(earnings));
                existing.setSessionCount(existing.getSessionCount() + 1);
            }
        }
        return List.copyOf(agg.values());
    }
}
