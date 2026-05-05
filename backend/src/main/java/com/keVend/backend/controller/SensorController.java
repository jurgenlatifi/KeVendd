package com.keVend.backend.controller;

import com.keVend.backend.model.ParkingSensor;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    /** Owner — register a new sensor for one of their lots. Returns the raw key once. */
    @PostMapping("/api/v1/owner/parkings/{parkingId}/sensors")
    public ResponseEntity<SensorService.RegistrationResult> register(
            @PathVariable Long parkingId,
            @RequestParam String vendor,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(sensorService.register(principal.getUser(), parkingId, vendor));
    }

    @GetMapping("/api/v1/owner/parkings/{parkingId}/sensors")
    public ResponseEntity<List<ParkingSensor>> list(
            @PathVariable Long parkingId,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(sensorService.listForOwner(principal.getUser(), parkingId));
    }

    /**
     * Sensor → ingest endpoint. Authenticated by the {@code X-Sensor-Key}
     * header rather than JWT, so it's whitelisted in SecurityConfig.
     */
    @PostMapping("/api/v1/sensors/availability")
    public ResponseEntity<Void> ingest(
            @RequestHeader("X-Sensor-Key") String apiKey,
            @RequestParam int availableSpots
    ) {
        sensorService.reportAvailability(apiKey, availableSpots);
        return ResponseEntity.noContent().build();
    }
}
