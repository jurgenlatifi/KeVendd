package com.keVend.backend.sms;

/**
 * Single point of contact for outbound SMS. Implementations should be
 * idempotent on the application's view of the message (one logical send per
 * call) and should throw {@link SmsDeliveryException} on transport failure so
 * Spring Retry can apply NFR-09's retry policy.
 */
public interface SmsGateway {

    /**
     * Sends {@code body} to {@code toPhoneE164}. The phone is expected to be in
     * E.164 format (e.g. "+355691234567").
     *
     * @return provider-specific message ID for downstream reconciliation
     */
    String send(String toPhoneE164, String body);
}
