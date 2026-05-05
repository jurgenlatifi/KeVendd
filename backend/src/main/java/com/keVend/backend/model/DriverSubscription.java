package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "driver_subscriptions", indexes = {
        @Index(name = "idx_driver_sub_user", columnList = "driver_id"),
        @Index(name = "idx_driver_sub_status", columnList = "status")
})
@Data
public class DriverSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Enumerated(EnumType.STRING)
    private Plan plan = Plan.PREMIUM;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    private Instant periodStart = Instant.now();
    private Instant periodEnd;
    private boolean autoRenew = true;
    private Instant cancelledAt;

    public enum Plan { PREMIUM }

    public enum Status { ACTIVE, EXPIRED, CANCELLED }
}
