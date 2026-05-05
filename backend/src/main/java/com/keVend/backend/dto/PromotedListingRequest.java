package com.keVend.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PromotedListingRequest {

    @NotNull
    @Min(1)
    private Integer tier;

    @NotNull
    @Min(1)
    private Integer days;

    @NotNull
    private BigDecimal pricePaidEur;
}
