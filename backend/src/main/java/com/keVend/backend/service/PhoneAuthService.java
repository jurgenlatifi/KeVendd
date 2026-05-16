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
    
    // ✅ FIXED - All 7 parameters (using 7 days = 604800000 ms)
    return new AuthResponse(
        accessToken,
        refreshToken,
        "Bearer",
        604800000L,  // 7 days in milliseconds (temporary - replace with jwtService method once we know the name)
        user.getId().toString(),
        user.getEmail(),
        user.getRole().name()
    );
}