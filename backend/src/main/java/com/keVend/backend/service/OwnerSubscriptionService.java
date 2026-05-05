package com.keVend.backend.service;

import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.OwnerSubscription;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.OwnerSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OwnerSubscriptionService {

    private final OwnerSubscriptionRepository repository;
    private final I18n i18n;

    @Value("${app.subscription.owner-monthly-eur}")
    private BigDecimal monthlyEur;

    @Value("${app.subscription.enforce-owner-listing-gate:false}")
    private boolean enforceListingGate;

    public BigDecimal monthlyPrice() {
        return monthlyEur;
    }

    public boolean enforceListingGate() {
        return enforceListingGate;
    }

    public Optional<OwnerSubscription> active(User owner) {
        return repository.findActive(owner.getId(), Instant.now());
    }

    public boolean hasActiveSubscription(Long ownerId) {
        return repository.findActive(ownerId, Instant.now()).isPresent();
    }

    @Transactional
    public OwnerSubscription startTrialOrRenew(User owner) {
        Optional<OwnerSubscription> existing = repository.findActive(owner.getId(), Instant.now());
        if (existing.isPresent()) {
            // Extend by 30 days from current end
            OwnerSubscription sub = existing.get();
            sub.setPeriodEnd(sub.getPeriodEnd().plus(30, ChronoUnit.DAYS));
            sub.setStatus(OwnerSubscription.Status.ACTIVE);
            sub.setLastChargedAt(Instant.now());
            return repository.save(sub);
        }

        OwnerSubscription sub = new OwnerSubscription();
        sub.setOwner(owner);
        sub.setStatus(OwnerSubscription.Status.ACTIVE);
        sub.setPeriodStart(Instant.now());
        sub.setPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
        sub.setLastChargedAt(Instant.now());
        OwnerSubscription saved = repository.save(sub);
        log.info("[owner-subscription] owner={} started, periodEnd={}", owner.getId(), saved.getPeriodEnd());
        return saved;
    }

    @Transactional
    public void cancel(User owner) {
        OwnerSubscription sub = repository.findActive(owner.getId(), Instant.now())
                .orElseThrow(() -> i18n.notFound("error.subscription.none_active"));
        sub.setStatus(OwnerSubscription.Status.CANCELLED);
        repository.save(sub);
    }

    /** Daily — flip overdue subscriptions to PAST_DUE. */
    @Scheduled(cron = "0 10 0 * * *")
    @Transactional
    public void markPastDue() {
        List<OwnerSubscription> expired = repository.findExpired(Instant.now());
        for (OwnerSubscription sub : expired) {
            sub.setStatus(OwnerSubscription.Status.PAST_DUE);
            repository.save(sub);
        }
        if (!expired.isEmpty()) {
            log.info("[owner-subscription] marked {} subscriptions PAST_DUE", expired.size());
        }
    }
}
