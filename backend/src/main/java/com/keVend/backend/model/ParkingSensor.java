package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * FR-12 part 2: physical sensor / camera that auto-reports occupancy. The
 * sensor authenticates via a hashed API key in the request header.
 */
@Entity
@Table(name = "parking_sensors", indexes = {
        @Index(name = "idx_sensor_parking", columnList = "parking_id"),
        @Index(name = "idx_sensor_key_hash", columnList = "apiKeyHash", unique = true)
})
@Data
public class ParkingSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = false, unique = true)
    private String apiKeyHash;

    private Instant lastSeenAt;

    private Instant createdAt = Instant.now();
}
