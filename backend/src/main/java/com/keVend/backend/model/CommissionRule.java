package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Configurable per-zone (and optionally per-day-of-week) commission overrides.
 * Lookup logic prefers the most specific match before falling back to the
 * global {@code app.platform-commission-rate} property.
 */
@Entity
@Table(name = "commission_rules", indexes = {
        @Index(name = "idx_commission_zone", columnList = "zone")
})
@Data
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Match by lot zone (e.g. "Blloku"). Null = applies to any zone. */
    private String zone;

    /** 0=Sunday .. 6=Saturday; null = any day. */
    private Integer dayOfWeek;

    /** Hour of day [0,23]; null = any hour. */
    private Integer hourFrom;
    private Integer hourTo;

    /** 0..1 — fraction of total kept by the platform. */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    /**
     * Higher number = checked first. Use 0 for general rules and >0 for
     * tiebreakers when multiple rules could match.
     */
    @Column(nullable = false)
    private int priority = 0;
}
