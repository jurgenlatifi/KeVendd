package com.keVend.backend.service;

import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.Parking;
import com.keVend.backend.model.ParkingSensor;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.ParkingRepository;
import com.keVend.backend.repository.ParkingSensorRepository;
import com.keVend.backend.security.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorService {

    private static final SecureRandom RNG = new SecureRandom();

    private final ParkingSensorRepository sensorRepository;
    private final ParkingRepository parkingRepository;
    private final ParkingService parkingService;
    private final I18n i18n;

    /** Owner-side: registers a new sensor and returns the raw key once (never re-shown). */
    @Transactional
    public RegistrationResult register(User owner, Long parkingId, String vendor) {
        Parking parking = parkingService.loadOrThrow(parkingId);
        if (parking.getOwner() == null || !parking.getOwner().getId().equals(owner.getId())) {
            throw i18n.forbidden("error.parking.not_owner");
        }

        String rawKey = generateKey();
        ParkingSensor sensor = new ParkingSensor();
        sensor.setParking(parking);
        sensor.setVendor(vendor);
        sensor.setApiKeyHash(TokenHashUtil.hash(rawKey));
        ParkingSensor saved = sensorRepository.save(sensor);
        return new RegistrationResult(saved.getId(), rawKey);
    }

    public List<ParkingSensor> listForOwner(User owner, Long parkingId) {
        Parking parking = parkingService.loadOrThrow(parkingId);
        if (parking.getOwner() == null || !parking.getOwner().getId().equals(owner.getId())) {
            throw i18n.forbidden("error.parking.not_owner");
        }
        return sensorRepository.findByParkingId(parkingId);
    }

    /** Minimum gap between two reports from the same sensor — FR-12 hardening. */
    private static final java.time.Duration MIN_REPORT_GAP = java.time.Duration.ofSeconds(5);

    /** Sensor-side: authenticated by API key in the X-Sensor-Key header. */
    @Transactional
    public void reportAvailability(String rawApiKey, int availableSpots) {
        ParkingSensor sensor = sensorRepository.findByApiKeyHash(TokenHashUtil.hash(rawApiKey))
                .orElseThrow(() -> i18n.status(org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "error.sensor.invalid_key"));

        // Throttle to one report per MIN_REPORT_GAP per sensor
        if (sensor.getLastSeenAt() != null
                && java.time.Duration.between(sensor.getLastSeenAt(), Instant.now())
                        .compareTo(MIN_REPORT_GAP) < 0) {
            throw i18n.status(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS,
                    "error.sensor.rate_limited");
        }

        Parking parking = sensor.getParking();
        int clamped = Math.max(0, Math.min(availableSpots, parking.getTotalSpots()));
        parking.setAvailableSpots(clamped);
        if (clamped == 0 && parking.getStatus() == Parking.Status.OPEN) {
            parking.setStatus(Parking.Status.FULL);
        } else if (clamped > 0 && parking.getStatus() == Parking.Status.FULL) {
            parking.setStatus(Parking.Status.OPEN);
        }
        parkingRepository.save(parking);

        sensor.setLastSeenAt(Instant.now());
        sensorRepository.save(sensor);
    }

    private String generateKey() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return "sk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record RegistrationResult(Long sensorId, String rawApiKey) {}
}
