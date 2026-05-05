package com.keVend.backend.service;

import com.keVend.backend.model.Promotion;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * FR-10 / Marketing: arms the welcome offer once per driver — first N
 * reservations after subscribing get a configurable percent off (default
 * "first 5 with 50% off"). Implemented as a per-driver code so usage is
 * tracked through the standard PromotionRedemption flow.
 */
@Service
@RequiredArgsConstructor
public class WelcomePromoService {

    private final PromotionRepository promotionRepository;

    @Value("${app.promo.welcome.first-n}")
    private int firstN;

    @Value("${app.promo.welcome.percent-off}")
    private int percentOff;

    public String codeForDriver(User driver) {
        return "WELCOME-" + driver.getId();
    }

    @Transactional
    public Promotion armForDriver(User driver) {
        String code = codeForDriver(driver);
        return promotionRepository.findByCode(code).orElseGet(() -> {
            Promotion p = new Promotion();
            p.setCode(code);
            p.setType(Promotion.Type.PERCENT_OFF);
            p.setValue(BigDecimal.valueOf(percentOff));
            p.setValidFrom(Instant.now());
            p.setValidTo(Instant.now().plus(365, ChronoUnit.DAYS));
            p.setMaxRedemptions(firstN);
            p.setPerUserLimit(firstN);
            return promotionRepository.save(p);
        });
    }
}
