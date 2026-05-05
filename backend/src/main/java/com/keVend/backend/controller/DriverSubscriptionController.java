package com.keVend.backend.controller;

import com.keVend.backend.model.DriverSubscription;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.DriverSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class DriverSubscriptionController {

    private final DriverSubscriptionService subscriptionService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> mine(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        Optional<DriverSubscription> active = subscriptionService.active(principal.getUser());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("active", active.isPresent());
        body.put("monthlyPriceEur", subscriptionService.premiumMonthlyPrice());
        active.ifPresent(s -> {
            body.put("plan", s.getPlan().name());
            body.put("periodStart", s.getPeriodStart());
            body.put("periodEnd", s.getPeriodEnd());
            body.put("autoRenew", s.isAutoRenew());
        });
        return ResponseEntity.ok(body);
    }

    @PostMapping
    public ResponseEntity<DriverSubscription> subscribe(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(subscriptionService.subscribe(principal.getUser()));
    }

    @DeleteMapping("/active")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        subscriptionService.cancel(principal.getUser());
        return ResponseEntity.noContent().build();
    }

    /** Pricing endpoint for the mobile checkout screen. */
    @GetMapping("/pricing")
    public ResponseEntity<Map<String, BigDecimal>> pricing() {
        return ResponseEntity.ok(Map.of("monthlyEur", subscriptionService.premiumMonthlyPrice()));
    }
}
