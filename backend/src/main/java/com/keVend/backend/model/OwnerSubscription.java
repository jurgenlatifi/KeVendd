package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "owner_subscriptions", indexes = {
        @Index(name = "idx_owner_sub_owner", columnList = "owner_id"),
        @Index(name = "idx_owner_sub_status", columnList = "status")
})
@Data
public class OwnerSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    private Status status = Status.TRIALING;

    private Instant periodStart = Instant.now();
    private Instant periodEnd;

    /** Last successful billing event. */
    private Instant lastChargedAt;

    public enum Status {
        TRIALING, ACTIVE, PAST_DUE, CANCELLED
    }
}
