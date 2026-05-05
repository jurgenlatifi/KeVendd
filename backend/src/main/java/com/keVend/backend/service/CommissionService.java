package com.keVend.backend.service;

import com.keVend.backend.model.CommissionRule;
import com.keVend.backend.model.Parking;
import com.keVend.backend.repository.CommissionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class CommissionService {

    private final CommissionRuleRepository ruleRepository;

    @Value("${app.platform-commission-rate:0.15}")
    private BigDecimal defaultRate;

    /**
     * Resolves the commission rate that applies to {@code parking} at {@code at}.
     * Falls back to the global rate if no rule matches.
     */
    public BigDecimal rateFor(Parking parking, Instant at) {
        ZonedDateTime when = at.atZone(ZoneId.systemDefault());
        int dow = when.getDayOfWeek().getValue() % 7; // ISO 1=Mon..7=Sun -> map to 0=Sun..6=Sat
        int hour = when.getHour();
        String zone = parking.getZone();

        for (CommissionRule rule : ruleRepository.findAllByOrderByPriorityDescIdAsc()) {
            if (rule.getZone() != null && (zone == null || !rule.getZone().equalsIgnoreCase(zone))) continue;
            if (rule.getDayOfWeek() != null && rule.getDayOfWeek() != dow) continue;
            if (rule.getHourFrom() != null && hour < rule.getHourFrom()) continue;
            if (rule.getHourTo() != null && hour > rule.getHourTo()) continue;
            return rule.getRate();
        }
        return defaultRate;
    }
}
