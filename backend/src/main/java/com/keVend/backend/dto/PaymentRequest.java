package com.keVend.backend.dto;

import com.keVend.backend.model.Payment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull
    private Long reservationId;

    @NotNull
    private Payment.PaymentMethod method;

    @NotNull
    private Payment.PaymentProvider provider;

    @NotNull
    private Payment.Currency currency;

    /** Optional reference returned by the upstream gateway/SMS provider. */
    private String transactionReference;
}
