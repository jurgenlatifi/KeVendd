package com.keVend.backend.payments;

import com.keVend.backend.model.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * FR-18 POK wallet adapter — stub. Same shape as {@link PayByPaymentGateway}:
 * activated by {@code pok.merchant-id}, hands back a fake intent until the
 * real API is integrated.
 */
@Component
@ConditionalOnProperty(prefix = "pok", name = "merchant-id")
@Slf4j
public class PokPaymentGateway implements PaymentGateway {

    @Override
    public String providerId() {
        return "POK";
    }

    @Override
    public IntentResult createIntent(Reservation reservation) {
        String fakeId = "pok-" + UUID.randomUUID();
        log.warn("[pok-stub] minted fake intent {} for reservation {} — wire real provider", fakeId, reservation.getId());
        return new IntentResult(fakeId, "stub-secret-" + fakeId);
    }
}
