package com.keVend.backend.controller;

import com.keVend.backend.dto.OwnerSessionView;
import com.keVend.backend.dto.ParkingResponse;
import com.keVend.backend.dto.PromotedListingRequest;
import com.keVend.backend.dto.RevenuePoint;
import com.keVend.backend.model.PromotionPurchase;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.AnalyticsService;
import com.keVend.backend.service.OwnerService;
import com.keVend.backend.service.PromotedListingService;
import com.keVend.backend.service.SessionPdfExporter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final PromotedListingService promotedListingService;
    private final SessionPdfExporter sessionPdfExporter;
    private final AnalyticsService analyticsService;

    @GetMapping("/parkings")
    public ResponseEntity<List<ParkingResponse>> myParkings(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(ownerService.myParkings(principal.getUser()));
    }

    /** FR-13: anonymized session list. */
    @GetMapping("/sessions")
    public ResponseEntity<List<OwnerSessionView>> sessions(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ownerService.sessions(principal.getUser(), from, to));
    }

    /** FR-17: revenue chart, bucketed by day | week | month. */
    @GetMapping("/revenue")
    public ResponseEntity<List<RevenuePoint>> revenue(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false, defaultValue = "day") String bucket
    ) {
        return ResponseEntity.ok(ownerService.revenue(principal.getUser(), from, to, bucket));
    }

    /** FR-12: pay to boost a lot to the top of search results. */
    @PostMapping("/parkings/{parkingId}/promotion")
    public ResponseEntity<PromotionPurchase> promote(
            @PathVariable Long parkingId,
            @Valid @RequestBody PromotedListingRequest req,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(promotedListingService.purchase(
                principal.getUser(), parkingId, req.getTier(), req.getDays(), req.getPricePaidEur()));
    }

    @GetMapping("/promotions")
    public ResponseEntity<List<PromotionPurchase>> myPromotions(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(promotedListingService.mine(principal.getUser()));
    }

    /**
     * Owner analytics — directional price suggestions per lot.
     * Premium feature; gated behind an active owner subscription (HTTP 402 otherwise).
     */
    @GetMapping("/analytics/price-suggestions")
    public ResponseEntity<List<AnalyticsService.PriceSuggestion>> priceSuggestions(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(analyticsService.priceSuggestionsFor(principal.getUser()));
    }

    /** FR-13: PDF export of the anonymized session list. */
    @GetMapping(value = "/sessions.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> sessionsPdf(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        var sessions = ownerService.sessions(principal.getUser(), from, to);
        var owner = principal.getUser();
        String label = "Owner #" + owner.getId() +
                (owner.getName() != null ? " — " + owner.getName() + " " + (owner.getSurname() == null ? "" : owner.getSurname()) : "");
        byte[] pdf = sessionPdfExporter.render(label, from, to, sessions);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=kevend-sessions.pdf")
                .body(pdf);
    }
}
