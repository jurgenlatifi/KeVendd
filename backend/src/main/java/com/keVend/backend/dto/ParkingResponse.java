package com.keVend.backend.dto;

import com.keVend.backend.model.Parking;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ParkingResponse {

    private Long id;
    private String name;
    private String zone;
    private Double latitude;
    private Double longitude;
    private Integer totalSpots;
    private Integer availableSpots;
    private BigDecimal pricePerHour;
    private Parking.Status status;
    private LocalDateTime openTime;
    private LocalDateTime closeTime;
    private Long ownerId;
    /** Optional — populated only when the request supplied a user coordinate. */
    private Double distanceKm;

    public static ParkingResponse from(Parking p) {
        return from(p, null);
    }

    public static ParkingResponse from(Parking p, Double distanceKm) {
        return ParkingResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .zone(p.getZone())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .totalSpots(p.getTotalSpots())
                .availableSpots(p.getAvailableSpots())
                .pricePerHour(p.getPricePerHour())
                .status(p.getStatus())
                .openTime(p.getOpenTime())
                .closeTime(p.getCloseTime())
                .ownerId(p.getOwner() != null ? p.getOwner().getId() : null)
                .distanceKm(distanceKm)
                .build();
    }
}
