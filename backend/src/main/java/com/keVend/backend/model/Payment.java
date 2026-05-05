package com.keVend.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private BigDecimal amount;

    private BigDecimal platformCommission;

    private BigDecimal ownerEarnings;

    private String transactionReference;

    @Column(columnDefinition = "TEXT")
    private String providerMetadata;

    private LocalDateTime paidAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum PaymentMethod {
        CARD,
        DIGITAL_WALLET,
        SMS
    }

    public enum PaymentProvider {
        STRIPE,
        PAYPAL,
        PAYBY,
        POK,
        TWILIO
    }

    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }

    public enum Currency {
        ALL, EUR
    }
}