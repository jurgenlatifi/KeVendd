package com.keVend.backend.controller;

import com.keVend.backend.dto.PaymentRequest;
import com.keVend.backend.model.Payment;
import com.keVend.backend.model.Reservation;
import com.keVend.backend.repository.UserRepository;
import com.keVend.backend.service.PaymentService;
import com.keVend.backend.service.ReservationService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    /**
     * Stripe sends payment lifecycle events here. We act on
     * {@code payment_intent.succeeded} by recording a Payment row + confirming
     * the reservation. Idempotent: PaymentService rejects duplicates.
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handle(@RequestBody String payload,
                                         @RequestHeader("Stripe-Signature") String signature) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("invalid signature");
        }

        if (!"payment_intent.succeeded".equals(event.getType())) {
            return ResponseEntity.ok("ignored");
        }

        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow(() -> new IllegalStateException("Missing intent"));

        Long reservationId = Long.valueOf(intent.getMetadata().get("reservation_id"));
        Long driverId = Long.valueOf(intent.getMetadata().get("driver_id"));

        Reservation r = reservationService.loadOrThrow(reservationId);
        var driver = userRepository.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Driver " + driverId + " not found"));

        PaymentRequest req = new PaymentRequest();
        req.setReservationId(r.getId());
        req.setMethod(Payment.PaymentMethod.CARD);
        req.setProvider(Payment.PaymentProvider.STRIPE);
        req.setCurrency(Payment.Currency.EUR);
        req.setTransactionReference(intent.getId());

        try {
            paymentService.pay(driver, req);
        } catch (Exception ex) {
            // Idempotent retry — Stripe will resend on 5xx, so 200 swallows duplicate-paid conflicts
            log.info("[stripe-webhook] reservation {} already settled or rejected: {}",
                    reservationId, ex.getMessage());
        }
        return ResponseEntity.ok("ok");
    }
}
