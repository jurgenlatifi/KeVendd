package com.keVend.backend.sms;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Twilio-backed implementation. Activated only when {@code twilio.account-sid}
 * is set, so dev environments fall through to {@link LoggingSmsGateway}.
 *
 * NFR-09: max 3 attempts within 60s. Spring Retry handles the schedule;
 * the surrounding caller sees either a success or {@link SmsDeliveryException}.
 */
@Component("twilioSmsGateway")
@ConditionalOnProperty(prefix = "twilio", name = "account-sid")
@Slf4j
public class TwilioSmsGateway implements SmsGateway {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS gateway initialised; sender = {}", fromNumber);
    }

    @Override
    @Retryable(
            retryFor = SmsDeliveryException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 5_000, multiplier = 2.0)
    )
    public String send(String toPhoneE164, String body) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(toPhoneE164),
                    new PhoneNumber(fromNumber),
                    body
            ).create();
            return message.getSid();
        } catch (ApiException ex) {
            throw new SmsDeliveryException("Twilio rejected SMS to " + toPhoneE164, ex);
        }
    }
}
