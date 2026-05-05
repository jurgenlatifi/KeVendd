package com.keVend.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class RevenuePoint {
    /** ISO date for daily, ISO week (yyyy-Www) for weekly, yyyy-MM for monthly. */
    private String bucket;
    private BigDecimal ownerEarnings;
    private long sessionCount;
}
