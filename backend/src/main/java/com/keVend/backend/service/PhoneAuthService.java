package com.keVend.backend.service;

import com.keVend.backend.dto.AuthResponse;
import com.keVend.backend.i18n.I18n;
import com.keVend.backend.model.PhoneOtpToken;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.PhoneOtpTokenRepository;
import com.keVend.backend.repository.UserRepository;
import com.keVend.backend.security.JwtService;
import com.keVend.backend.security.TokenHashUtil;
import com.keVend.backend.security.UserDetailsImpl;
import com.keVend.backend.sms.SmsGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * FR-09 phone path: 6-digit SMS OTP for registration & login.
 * NFR-13 spirit: lock after 5 invalid attempts on the same code.
 */
@Service
@RequiredArgsConstructor
public class PhoneAuthService {

    private static final int OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RNG = new SecureRandom();

    private final PhoneOtpTokenRepository otpRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final SmsGateway smsGateway;
    private final I18n i18n;

    @Transactional
    public void requestOtp(String phone) {
        String code = String.format("%06d", RNG.nextInt(1_000_000));
        PhoneOtpToken token = new PhoneOtpToken();
        token.setPhone(phone);
        token.setOtpHash(TokenHashUtil.hash(code));
        token.setExpiresAt(Instant.now().plus(OTP_TTL_MINUTES, ChronoUnit.MINUTES));
        otpRepository.save(token);

        // Best-effort SMS delivery — keep response identical regardless of phone validity
        // to avoid revealing which numbers are registered.
        try {
            smsGateway.send(phone, "KeVend code: " + code + " (valid " + OTP_TTL_MINUTES + " min)");
        } catch (RuntimeException ignored) {
            // Non-fatal — driver can request a fresh OTP
        }
    }

    @Transactional
    public AuthResponse verifyOtp(String phone, String code) {
        PhoneOtpToken token = otpRepository.findFirstByPhoneAndUsedFalseOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> i18n.badRequest("error.phone.otp_invalid"));

        if (token.getAttempts() >= MAX_ATTEMPTS) {
            throw i18n.badRequest("error.phone.otp_too_many");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw i18n.badRequest("error.phone.otp_invalid");
        }
        if (!token.getOtpHash().equals(TokenHashUtil.hash(code))) {
            token.setAttempts(token.getAttempts() + 1);
            otpRepository.save(token);
            throw i18n.badRequest("error.phone.otp_invalid");
        }

        token.setUsed(true);
        otpRepository.save(token);

        // Find or auto-create the user
        User user = userRepository.findByPhone(phone).orElseGet(() -> {
            User u = new User();
            u.setPhone(phone);
            u.setEmail("phone-" + System.currentTimeMillis() + "-" + Math.abs(phone.hashCode()) + "@phone.kevend.local");
            u.setPasswordHash("");
            u.setRole(User.Role.DRIVER);
            u.setEmailVerified(false);
            return userRepository.save(u);
        });

        UserDetailsImpl details = new UserDetailsImpl(user);
        String accessToken = jwtService.generateAccessToken(details);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        
        // ✅ FIXED - All 7 parameters required by AuthResponse constructor
        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",                          // tokenType
            jwtService.getExpiration(),        // expiresIn
            user.getId().toString(),           // userId
            user.getEmail(),                   // email
            user.getRole().name()              // role
        );
    }
}
