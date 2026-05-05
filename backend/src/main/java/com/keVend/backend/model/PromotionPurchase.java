package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Records an owner buying a "boost" for a specific lot — sets
 * {@link Parking#getPromotionRank()} to {@code tier} until {@code periodEnd},
 * then a scheduled job restores it to 0.
 */
@Entity
@Table(name = "promotion_purchases", indexes = {
        @Index(name = "idx_promo_purchase_parking", columnList = "parking_id"),
        @Index(name = "idx_promo_purchase_status", columnList = "status")
})
@Data
public class PromotionPurchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;

    @Column(nullable = false)
    private int tier;

    private BigDecimal pricePaidEur;

    private Instant periodStart = Instant.now();
    private Instant periodEnd;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public enum Status { ACTIVE, EXPIRED, REFUNDED }
}
