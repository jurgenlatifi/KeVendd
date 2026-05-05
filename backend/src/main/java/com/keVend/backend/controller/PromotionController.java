package com.keVend.backend.controller;

import com.keVend.backend.model.Promotion;
import com.keVend.backend.repository.PromotionRepository;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.PromotionService;
import com.keVend.backend.service.WelcomePromoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionRepository promotionRepository;
    private final PromotionService promotionService;
    private final WelcomePromoService welcomePromoService;

    /** Driver self-service: check whether a code is valid for them right now. */
    @GetMapping("/api/v1/promotions/check")
    public ResponseEntity<Map<String, Object>> check(
            @RequestParam String code,
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        var resolved = promotionService.resolve(code);
        body.put("valid", resolved.isPresent()
                && promotionService.canDriverRedeem(resolved.get(), principal.getUser()));
        resolved.ifPresent(p -> {
            body.put("type", p.getType().name());
            body.put("value", p.getValue());
        });
        return ResponseEntity.ok(body);
    }

    /** Convenience: returns the auto-armed welcome code for the authenticated driver. */
    @GetMapping("/api/v1/promotions/welcome-code")
    public ResponseEntity<Map<String, String>> welcomeCode(
            @AuthenticationPrincipal UserDetailsImpl principal
    ) {
        return ResponseEntity.ok(Map.of("code", welcomePromoService.codeForDriver(principal.getUser())));
    }

    // Admin CRUD —————————————————————————————

    @GetMapping("/api/v1/admin/promotions")
    public ResponseEntity<Iterable<Promotion>> listAll() {
        return ResponseEntity.ok(promotionRepository.findAll());
    }

    @PostMapping("/api/v1/admin/promotions")
    public ResponseEntity<Promotion> create(@RequestBody Promotion promo) {
        promo.setId(null);
        return ResponseEntity.ok(promotionRepository.save(promo));
    }

    @PutMapping("/api/v1/admin/promotions/{id}")
    public ResponseEntity<Promotion> update(@PathVariable Long id, @RequestBody Promotion promo) {
        promo.setId(id);
        return ResponseEntity.ok(promotionRepository.save(promo));
    }

    @DeleteMapping("/api/v1/admin/promotions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
