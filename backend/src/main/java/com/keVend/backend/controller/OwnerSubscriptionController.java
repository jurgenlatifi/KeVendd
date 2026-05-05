package com.keVend.backend.controller;

import com.keVend.backend.model.OwnerSubscription;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.OwnerSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/owner/subscription")
@RequiredArgsConstructor
public class OwnerSubscriptionController {

    private final OwnerSubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> mine(@AuthenticationPrincipal UserDetailsImpl principal) {
        Optional<OwnerSubscription> active = subscriptionService.active(principal.getUser());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("active", active.isPresent());
        body.put("monthlyPriceEur", subscriptionService.monthlyPrice());
        active.ifPresent(s -> {
            body.put("status", s.getStatus().name());
            body.put("periodEnd", s.getPeriodEnd());
            body.put("lastChargedAt", s.getLastChargedAt());
        });
        return ResponseEntity.ok(body);
    }

    /** Subscribe or renew for one month. (Real billing wires Stripe in front of this.) */
    @PostMapping
    public ResponseEntity<OwnerSubscription> subscribe(@AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(subscriptionService.startTrialOrRenew(principal.getUser()));
    }

    @DeleteMapping
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserDetailsImpl principal) {
        subscriptionService.cancel(principal.getUser());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pricing")
    public ResponseEntity<Map<String, BigDecimal>> pricing() {
        return ResponseEntity.ok(Map.of("monthlyEur", subscriptionService.monthlyPrice()));
    }
}
