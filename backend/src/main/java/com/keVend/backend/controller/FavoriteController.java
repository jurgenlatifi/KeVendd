package com.keVend.backend.controller;

import com.keVend.backend.dto.ParkingResponse;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<List<ParkingResponse>> list(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(favoriteService.list(principal.getUser()));
    }

    @PostMapping("/{parkingId}")
    public ResponseEntity<Void> add(
            @PathVariable Long parkingId,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        favoriteService.add(principal.getUser(), parkingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{parkingId}")
    public ResponseEntity<Void> remove(
            @PathVariable Long parkingId,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        favoriteService.remove(principal.getUser(), parkingId);
        return ResponseEntity.noContent().build();
    }
}
