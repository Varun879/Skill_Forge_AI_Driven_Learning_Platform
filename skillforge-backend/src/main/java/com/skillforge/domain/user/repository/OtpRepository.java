package com.skillforge.domain.user.repository;

import com.skillforge.domain.user.entity.OtpEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpEntry, Long> {

    /**
     * Returns the most recent non-expired, unused OTP for the given email.
     * Used during OTP verification to find the valid entry.
     */
    Optional<OtpEntry> findTopByEmailAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, LocalDateTime now);

        Optional<OtpEntry> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    /**
     * Checks whether a non-expired, unused OTP already exists for rate-limiting.
     * We use this to prevent OTP spam (one OTP per minute per email).
     */
    boolean existsByEmailAndUsedFalseAndExpiresAtAfterAndCreatedAtAfter(
            String email, LocalDateTime expiryCheck, LocalDateTime rateLimitCutoff);

    /** Invalidates all current OTPs for the email before issuing a fresh one. */
    @Modifying
    @Transactional
    @Query("UPDATE OtpEntry o SET o.used = true WHERE o.email = :email AND o.used = false")
    void invalidateAllByEmail(String email);

    /** Removes expired rows — called by the nightly cleanup scheduler. */
    @Modifying
    @Transactional
    @Query("DELETE FROM OtpEntry o WHERE o.expiresAt < :cutoff")
    void deleteExpiredBefore(LocalDateTime cutoff);
}
