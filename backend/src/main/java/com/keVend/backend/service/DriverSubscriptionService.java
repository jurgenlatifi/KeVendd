package com.keVend.backend.service;

import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.DriverSubscription;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.DriverSubscriptionRepository;
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
public class DriverSubscriptionService {

    private final DriverSubscriptionRepository repository;
    private final WelcomePromoService welcomePromoService;
    private final I18n i18n;

    @Value("${app.subscription.driver-premium-monthly-eur}")
    private BigDecimal premiumMonthlyEur;

    public BigDecimal premiumMonthlyPrice() {
        return premiumMonthlyEur;
    }

    public Optional<DriverSubscription> active(User driver) {
        return repository.findActive(driver.getId(), Instant.now());
    }

    public boolean isPremium(User driver) {
        return active(driver).isPresent();
    }

    /**
     * Subscribes a driver. Closes any existing active subscription, creates a
     * fresh 30-day premium one, and arms the welcome promo (FR-10) the first
     * time this driver subscribes.
     */
    @Transactional
    public DriverSubscription subscribe(User driver) {
        repository.findActive(driver.getId(), Instant.now()).ifPresent(existing -> {
            existing.setStatus(DriverSubscription.Status.CANCELLED);
            existing.setCancelledAt(Instant.now());
            repository.save(existing);
        });

        DriverSubscription sub = new DriverSubscription();
        sub.setDriver(driver);
        sub.setPeriodStart(Instant.now());
        sub.setPeriodEnd(Instant.now().plus(30, ChronoUnit.DAYS));
        repository.save(sub);

        welcomePromoService.armForDriver(driver);

        log.info("[subscription] driver={} subscribed, periodEnd={}", driver.getId(), sub.getPeriodEnd());
        return sub;
    }

    @Transactional
    public void cancel(User driver) {
        DriverSubscription sub = repository.findActive(driver.getId(), Instant.now())
                .orElseThrow(() -> i18n.notFound("error.subscription.none_active"));
        sub.setStatus(DriverSubscription.Status.CANCELLED);
        sub.setAutoRenew(false);
        sub.setCancelledAt(Instant.now());
        repository.save(sub);
    }

    /** Daily — flip past-period subscriptions to EXPIRED. */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void expirePastSubscriptions() {
        List<DriverSubscription> expired = repository.findExpired(Instant.now());
        for (DriverSubscription sub : expired) {
            sub.setStatus(DriverSubscription.Status.EXPIRED);
            repository.save(sub);
        }
        if (!expired.isEmpty()) {
            log.info("[subscription] expired {} driver subscriptions", expired.size());
        }
    }
}
