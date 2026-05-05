package com.keVend.backend.controller;

import com.keVend.backend.dto.AuthResponse;
import com.keVend.backend.dto.PhoneAuthRequest;
import com.keVend.backend.service.PhoneAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/phone")
@RequiredArgsConstructor
public class PhoneAuthController {

    private final PhoneAuthService phoneAuthService;

    @PostMapping("/request-otp")
    public ResponseEntity<Void> requestOtp(@Valid @RequestBody PhoneAuthRequest req) {
        phoneAuthService.requestOtp(req.getPhone());
        // Return 204 regardless of phone-existence to prevent enumeration
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody PhoneAuthRequest req) {
        return ResponseEntity.ok(phoneAuthService.verifyOtp(req.getPhone(), req.getCode()));
    }
}
