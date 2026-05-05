package com.keVend.backend.payments;

import com.keVend.backend.model.Reservation;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * FR-05 card path. Active only when {@code stripe.secret-key} is set.
 *
 * Amount is converted to the smallest currency unit (cents for EUR; ALL is
 * not subdivided so we use whole-currency units there).
 */
@Component
@ConditionalOnProperty(prefix = "stripe", name = "secret-key")
@Slf4j
public class StripePaymentGateway implements PaymentGateway {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe payment gateway initialised");
    }

    @Override
    public String providerId() {
        return "STRIPE";
    }

    @Override
    public IntentResult createIntent(Reservation reservation) {
        try {
            String currency = reservation.getTotalCost() != null && reservation.getParking() != null
                    ? "eur" // currency derived from reservation context; switch to per-payment currency in payment flow
                    : "eur";
            long amountMinor = currency.equals("all")
                    ? reservation.getTotalCost().longValue()
                    : reservation.getTotalCost().multiply(java.math.BigDecimal.valueOf(100)).longValue();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountMinor)
                    .setCurrency(currency)
                    .putMetadata("reservation_id", String.valueOf(reservation.getId()))
                    .putMetadata("driver_id", String.valueOf(reservation.getDriver().getId()))
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            return new IntentResult(intent.getId(), intent.getClientSecret());
        } catch (StripeException ex) {
            throw new RuntimeException("Stripe intent creation failed: " + ex.getMessage(), ex);
        }
    }
}
