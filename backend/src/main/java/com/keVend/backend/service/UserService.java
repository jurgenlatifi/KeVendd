package com.keVend.backend.service;

import com.keVend.backend.audit.AuditLog;import com.keVend.backend.dto.UpdateCredentialsRequest;
import com.keVend.backend.dto.UpdateProfileRequest;
import com.keVend.backend.dto.UpdateUserRequest;
import com.keVend.backend.dto.UserProfileResponse;
import com.keVend.backend.exception.EmailAlreadyInUseException;
import com.keVend.backend.model.User;
import com.keVend.backend.repository.EmailVerificationTokenRepository;
import com.keVend.backend.repository.RefreshTokenRepository;
import com.keVend.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DELETED_EMAIL_DOMAIN = "@anon.kevend.local";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuditLog auditLog;
    private final PasswordEncoder passwordEncoder;

    // Replace the existing profile() method and add the two new ones.

    public UserProfileResponse profile(User user) {
        return UserProfileResponse.from(user);
    }

    /**
     * Updates non-sensitive profile fields (name, surname, phone, preferredLocale).
     * Fields that are null in the request are left unchanged.
     */
    @Transactional
    public UserProfileResponse updateProfile(User user, UpdateUserRequest request) {
        if (request.getName() != null)            user.setName(request.getName());
        if (request.getSurname() != null)         user.setSurname(request.getSurname());
        if (request.getPhone() != null)           user.setPhone(request.getPhone());
        if (request.getPreferredLocale() != null) user.setPreferredLocale(request.getPreferredLocale());

        userRepository.save(user);
        auditLog.profileUpdated(user.getId());
        return UserProfileResponse.from(user);
    }

    /**
     * Updates email and/or password after verifying the current password.
     * Email changes reset the emailVerified flag and revoke all refresh tokens
     * so the user must re-authenticate (and re-verify the new address).
     */
    @Transactional
    public void updateCredentials(User user, UpdateCredentialsRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        boolean emailChanged = false;

        if (StringUtils.hasText(request.getNewEmail())
                && !request.getNewEmail().equalsIgnoreCase(user.getEmail())) {

            if (userRepository.existsByEmail(request.getNewEmail())) {
                throw new EmailAlreadyInUseException(request.getNewEmail());
            }
            user.setEmail(request.getNewEmail());
            user.setEmailVerified(false);
            emailChanged = true;
        }

        if (StringUtils.hasText(request.getNewPassword())) {
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        }

        userRepository.save(user);

        if (emailChanged) {
            // Force re-login on all devices; new address still needs verification
            refreshTokenRepository.revokeAllByUser(user);
            emailVerificationTokenRepository.deleteByUser(user);
            // TODO: dispatch a verification email for the new address
        }

        auditLog.credentialsUpdated(user.getId(), emailChanged);
    }

    @org.springframework.transaction.annotation.Transactional
    public void setPreferredLocale(User user, String localeTag) {
        user.setPreferredLocale(localeTag);
        userRepository.save(user);
    }

    /**
     * NFR-12 GDPR: anonymize the account instead of deleting. PII is cleared
     * and the email is rewritten to a deterministic placeholder so the unique
     * constraint still holds; reservations / payments / reviews keep their
     * historical FK references intact for owner accounting (NFR-22).
     *
     * The user can no longer log in: passwordHash is wiped and the original
     * email is gone, so {@code findByEmail} on it returns empty.
     */
    @Transactional
    public void deleteAccount(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        emailVerificationTokenRepository.deleteByUser(user);

        user.setName(null);
        user.setSurname(null);
        user.setPhone(null);
        user.setEmail("deleted-" + user.getId() + DELETED_EMAIL_DOMAIN);
        user.setPasswordHash("");
        user.setEmailVerified(false);
        user.setAnonymized(true);
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        auditLog.accountAnonymized(user.getId());
    }

    @Transactional
    public void updateProfile(User user, UpdateProfileRequest request) {
        if (request.getName() != null)    user.setName(request.getName());
        if (request.getSurname() != null) user.setSurname(request.getSurname());
        if (request.getPhone() != null)   user.setPhone(request.getPhone());
        userRepository.save(user);
    }
}
