package com.vinncorp.erp.shared.scheduling;

import com.vinncorp.erp.platform.auth.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TokenCleanupScheduler {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    // Run at 2AM daily
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = tokenRepository.deleteByExpiryDateBefore(now);
        if (deletedCount > 0) {
            System.out.println("Cleaned up " + deletedCount + " expired verification tokens");
        }
    }
}
