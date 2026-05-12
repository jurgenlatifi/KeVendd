package com.keVend.backend.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Single sink for security- and money-relevant events. Writes a key=value line
 * to the {@code audit} logger so the logback config can route it independently
 * (e.g. to a separate file or a SIEM).
 */
@Component
@RequiredArgsConstructor
public class AuditLog {

    private static final org.slf4j.Logger AUDIT = org.slf4j.LoggerFactory.getLogger("audit");

    public void event(String type, Object... kv) {
        StringBuilder sb = new StringBuilder("type=").append(type);
        for (int i = 0; i + 1 < kv.length; i += 2) {
            sb.append(' ').append(kv[i]).append('=').append(kv[i + 1]);
        }
        AUDIT.info(sb.toString());
    }

    public void loginSuccess(Long userId, String email) { event("login.success", "userId", userId, "email", email); }
    public void loginFailure(String email) { event("login.failure", "email", email); }
    public void accountLocked(Long userId) { event("account.locked", "userId", userId); }
    public void accountAnonymized(Long userId) { event("account.anonymized", "userId", userId); }
    public void payment(Long reservationId, Long driverId, String provider, String currency, Object amount) {
        event("payment.recorded",
                "reservationId", reservationId,
                "driverId", driverId,
                "provider", provider,
                "currency", currency,
                "amount", amount);
    }

    public void profileUpdated(Long userId) {
        AUDIT.info("AUDIT profile_updated userId={}", userId);
    }

    public void credentialsUpdated(Long userId, boolean emailChanged) {
        AUDIT.info("AUDIT credentials_updated userId={} emailChanged={}", userId, emailChanged);
    }

}
