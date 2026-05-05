package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "favorites", uniqueConstraints =
        @UniqueConstraint(name = "uk_favorite_driver_parking", columnNames = {"driver_id", "parking_id"})
)
@Data
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne(optional = false)
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;

    private Instant createdAt = Instant.now();
}
