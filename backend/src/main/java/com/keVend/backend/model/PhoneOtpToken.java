package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "phone_otp_tokens", indexes = {
        @Index(name = "idx_phone_otp_phone", columnList = "phone")
})
@Data
public class PhoneOtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String phone;

    /** Hashed OTP — we never store the raw 6-digit code. */
    @Column(nullable = false)
    private String otpHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean used = false;

    private Instant createdAt = Instant.now();
}
