package com.keVend.backend.controller;

import com.keVend.backend.dto.ReviewRequest;
import com.keVend.backend.dto.ReviewSummary;
import com.keVend.backend.model.Review;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/reviews")
    public ResponseEntity<Review> create(
            @Valid @RequestBody ReviewRequest req,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(reviewService.createForReservation(principal.getUser(), req));
    }

    /** Public — does not require auth (the SecurityConfig allows GET on parking-lots/**). */
    @GetMapping("/parking-lots/{id}/reviews")
    public ResponseEntity<ReviewSummary> forParking(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.publicSummary(id));
    }
}
