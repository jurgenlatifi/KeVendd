package com.keVend.backend.controller;

import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserDetailsImpl principal) {
        return ResponseEntity.ok(userService.profile(principal.getUser()));
    }

    /** NFR-12 GDPR: driver-initiated account deletion. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal UserDetailsImpl principal) {
        userService.deleteAccount(principal.getUser());
        return ResponseEntity.noContent().build();
    }

    /** NFR-18: pick the language used for push/SMS notifications (e.g. "sq", "en"). */
    @PutMapping("/me/locale")
    public ResponseEntity<Void> setLocale(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam String locale
    ) {
        userService.setPreferredLocale(principal.getUser(), locale);
        return ResponseEntity.noContent().build();
    }
}
