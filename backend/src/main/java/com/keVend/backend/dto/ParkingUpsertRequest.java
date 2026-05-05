package com.keVend.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ParkingUpsertRequest {

    @NotBlank
    private String name;

    private String zone;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    @Min(0)
    private Integer totalSpots;

    @NotNull
    @Min(0)
    private Integer availableSpots;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal pricePerHour;

    private LocalDateTime openTime;

    private LocalDateTime closeTime;

    /** FR-12 promoted listings — owners pay extra for higher rank. */
    @Min(0)
    private Integer promotionRank;
}
