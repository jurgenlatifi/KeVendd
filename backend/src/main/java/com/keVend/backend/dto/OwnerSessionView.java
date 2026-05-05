package com.keVend.backend.dto;

import com.keVend.backend.model.Reservation;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * FR-13 / NFR-14: owner-facing view of a reservation. The driver is referenced
 * only by an anonymous opaque ID — no name, email, or phone is exposed.
 */
@Data
@Builder
public class OwnerSessionView {

    private Long reservationId;
    private String driverAnonymousId;
    private Long parkingId;
    private String parkingName;
    private int spotsReserved;
    private Instant startTime;
    private Instant endTime;
    private Reservation.ReservationStatus status;
    private BigDecimal totalCost;
    private BigDecimal ownerRevenue;

    public static OwnerSessionView from(Reservation r) {
        return OwnerSessionView.builder()
                .reservationId(r.getId())
                .driverAnonymousId(anonymize(r.getDriver() != null ? r.getDriver().getId() : null))
                .parkingId(r.getParking().getId())
                .parkingName(r.getParking().getName())
                .spotsReserved(r.getSpotsReserved())
                .startTime(r.getStartTime())
                .endTime(r.getEndTime())
                .status(r.getStatus())
                .totalCost(r.getTotalCost())
                .ownerRevenue(r.getOwnerRevenue())
                .build();
    }

    /** Stable per-driver token so owners can recognise repeat customers without identifying them. */
    private static String anonymize(Long driverId) {
        if (driverId == null) return "anon-deleted";
        // Simple deterministic obfuscation. Replace with HMAC if linkability across owners must also be prevented.
        return "drv-" + Integer.toHexString(driverId.hashCode() ^ 0x5EED);
    }
}
