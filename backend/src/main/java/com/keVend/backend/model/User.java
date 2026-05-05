package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String surname;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean emailVerified = false;

    /** NFR-13: lock account after N consecutive failed logins. */
    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    /** Set when the account is locked; null when unlocked. */
    private Instant lockedUntil;

    /** NFR-12: GDPR — true when the user has requested deletion. PII is wiped, history kept. */
    @Column(nullable = false)
    private boolean anonymized = false;

    /** NFR-18: ISO 639-1 code (e.g. "sq", "en"). Drives the language of push/SMS notifications. */
    @Column(length = 8)
    private String preferredLocale;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        DRIVER, OWNER, GUEST
    }
}
