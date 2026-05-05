package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "promotions", indexes = {
        @Index(name = "idx_promo_code", columnList = "code", unique = true)
})
@Data
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Codeless promotions (e.g. auto-attached welcome offer) leave this null. */
    @Column(unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    /** For PERCENT_OFF: 1..100. For FREE_RESERVATION: ignored. */
    private BigDecimal value;

    private Instant validFrom;
    private Instant validTo;

    /** Global cap; null = unlimited. */
    private Integer maxRedemptions;

    /** How many times any single driver can redeem; null = unlimited. */
    private Integer perUserLimit;

    public enum Type {
        PERCENT_OFF,
        FREE_RESERVATION
    }
}
