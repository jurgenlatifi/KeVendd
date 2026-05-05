package com.keVend.backend.controller;

import com.keVend.backend.dto.AuthResponse;
import com.keVend.backend.dto.LoginRequest;
import com.keVend.backend.dto.RefreshTokenRequest;
import com.keVend.backend.dto.RegisterRequest;
import com.keVend.backend.i18n.I18n;
import com.keVend.backend.service.AuthService;
import com.keVend.backend.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final I18n i18n;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    /**
     * Called when user clicks the link in their verification email.
     * The token is a URL-safe base64 string passed as a query param.
     */
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(i18n.t("info.auth.email_verified"));
    }

    /**
     * Allows a user to request a new verification email if the old one expired.
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@RequestParam String email) {
        authService.resendVerificationEmail(email);
        // Always return 200 to avoid email enumeration
        return ResponseEntity.ok(i18n.t("info.auth.verification_resent"));
    }
}