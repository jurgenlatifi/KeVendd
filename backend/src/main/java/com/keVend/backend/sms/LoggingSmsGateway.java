package com.keVend.backend.sms;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Default SMS gateway used in dev / when no real provider is configured.
 * Logs the message and returns a fake message ID — never raises.
 */
@Component
@ConditionalOnMissingBean(name = "twilioSmsGateway")
@Slf4j
public class LoggingSmsGateway implements SmsGateway {

    @Override
    public String send(String toPhoneE164, String body) {
        String fakeId = "log-" + UUID.randomUUID();
        log.info("[sms-stub] to={} body={} id={}", toPhoneE164, body, fakeId);
        return fakeId;
    }
}
