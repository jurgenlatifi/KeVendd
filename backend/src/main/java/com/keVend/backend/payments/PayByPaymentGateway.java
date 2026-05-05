package com.keVend.backend.payments;

import com.keVend.backend.model.Reservation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * FR-18 PayBY wallet adapter — stub.
 *
 * Activated only when {@code payby.merchant-id} is set. Real implementation
 * needs PayBY's REST API surface and sandbox credentials; until then this
 * returns a fake intent so the rest of the flow is exercisable end-to-end.
 */
@Component
@ConditionalOnProperty(prefix = "payby", name = "merchant-id")
@Slf4j
public class PayByPaymentGateway implements PaymentGateway {

    @Override
    public String providerId() {
        return "PAYBY";
    }

    @Override
    public IntentResult createIntent(Reservation reservation) {
        String fakeId = "payby-" + UUID.randomUUID();
        log.warn("[payby-stub] minted fake intent {} for reservation {} — wire real provider", fakeId, reservation.getId());
        return new IntentResult(fakeId, "stub-secret-" + fakeId);
    }
}
