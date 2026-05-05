package com.keVend.backend.controller;

import com.keVend.backend.dto.ParkingResponse;
import com.keVend.backend.dto.ParkingUpsertRequest;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/parking-lots")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    /**
     * F-11 Guest browsing: returns lots, optionally filtered by zone, price range,
     * or nearby a coordinate. All params are optional.
     */
    @GetMapping
    public ResponseEntity<List<ParkingResponse>> list(
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly
    ) {
        return ResponseEntity.ok(
                parkingService.search(zone, minPrice, maxPrice, lat, lng, radiusKm, availableOnly)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParkingResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(parkingService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ParkingResponse> create(
            @Valid @RequestBody ParkingUpsertRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(parkingService.create(request, principal.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParkingResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ParkingUpsertRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(parkingService.update(id, request, principal.getUser()));
    }

    /**
     * FR-11 Owner manually updates available spot count. Accepts only the spot
     * delta so we don't allow writing the rest of the lot config in a side path.
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<ParkingResponse> setAvailability(
            @PathVariable Long id,
            @RequestParam int availableSpots,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(parkingService.updateAvailability(id, availableSpots, principal.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        parkingService.delete(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}
