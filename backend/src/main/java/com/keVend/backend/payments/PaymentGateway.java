package com.keVend.backend.payments;

import com.keVend.backend.model.Reservation;

/**
 * Adapter contract for an external payment provider that issues a
 * "client secret" / redirect URL the mobile app uses to collect funds.
 *
 * The actual payment confirmation arrives asynchronously through a webhook,
 * which is the moment {@link com.keVend.backend.service.PaymentService}
 * records the {@link com.keVend.backend.model.Payment} row.
 */
public interface PaymentGateway {

    /** Provider id matching {@link com.keVend.backend.model.Payment.PaymentProvider}. */
    String providerId();

    /** Creates a payment intent for the reservation, returning a client secret. */
    IntentResult createIntent(Reservation reservation);

    record IntentResult(String intentId, String clientSecret) {}
}
