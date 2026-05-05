package com.keVend.backend.repository;

import com.keVend.backend.model.PhoneOtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneOtpTokenRepository extends JpaRepository<PhoneOtpToken, Long> {

    Optional<PhoneOtpToken> findFirstByPhoneAndUsedFalseOrderByCreatedAtDesc(String phone);
}
