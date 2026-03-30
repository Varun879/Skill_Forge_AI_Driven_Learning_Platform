package com.skillforge.domain.user.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillforge.domain.user.repository.OtpRepository;
import com.skillforge.domain.user.repository.RefreshTokenRepository;

/**
 * Nightly scheduled job that purges expired OTPs and revoked/expired
 * refresh tokens to prevent unbounded table growth.
 */
@Service
public class TokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    private final OtpRepository          otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenCleanupScheduler(OtpRepository otpRepository,
                                 RefreshTokenRepository refreshTokenRepository) {
        this.otpRepository          = otpRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        otpRepository.deleteExpiredBefore(now);
        log.info("OTP cleanup complete — cutoff: {}", now);
        refreshTokenRepository.deleteExpiredAndRevoked(now);
        log.info("Refresh-token cleanup complete — cutoff: {}", now);
    }
}
