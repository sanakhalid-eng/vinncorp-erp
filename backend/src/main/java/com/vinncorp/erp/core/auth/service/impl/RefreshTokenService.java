package com.vinncorp.erp.core.auth.service.impl;

import com.vinncorp.erp.core.auth.entity.RefreshToken;
import com.vinncorp.erp.core.auth.repository.RefreshTokenRepository;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String BLACKLIST_PREFIX = "refresh:blacklist:";
    private static final long BLACKLIST_BUFFER_SECONDS = 60;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    @Autowired
    private RefreshTokenRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    public RefreshToken createRefreshToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setTokenFamily(UUID.randomUUID().toString().substring(0, 8));
        token.setExpiryDate(Instant.now().plusMillis(refreshExpiration));

        return repository.save(token);
    }

    /**
     * Rotate refresh token: issue new token, revoke old one, detect reuse via token family.
     */
    @Transactional
    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        if (oldToken.isExpired()) {
            repository.delete(oldToken);
            throw new InvalidTokenException("Refresh token expired");
        }

        if (oldToken.isRevoked()) {
            // Potential token reuse attack - revoke entire family
            revokeTokenFamily(oldToken.getTokenFamily());
            blacklistAccessToken(oldToken.getUser().getId());
            throw new InvalidTokenException("Refresh token reuse detected. All sessions revoked.");
        }

        // Create new token in same family
        RefreshToken newToken = new RefreshToken();
        newToken.setUser(oldToken.getUser());
        newToken.setToken(UUID.randomUUID().toString());
        newToken.setTokenFamily(oldToken.getTokenFamily());
        newToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        newToken.setRevoked(false);

        RefreshToken saved = repository.save(newToken);

        // Mark old token as replaced
        repository.markReplaced(oldToken.getToken(), saved.getToken());

        return saved;
    }

    /**
     * Revoke all refresh tokens for a user (logout everywhere).
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        repository.revokeAllByUserId(userId);
    }

    /**
     * Revoke all tokens in a family (reuse detected).
     */
    @Transactional
    public void revokeTokenFamily(String tokenFamily) {
        repository.findValidByTokenFamily(tokenFamily)
                .forEach(token -> {
                    token.setRevoked(true);
                    repository.save(token);
                });
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            repository.delete(token);
            throw new InvalidTokenException("Refresh token expired");
        }
        if (token.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        return token;
    }

    public RefreshToken findByToken(String token) {
        return repository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
    }

    /**
     * Blacklist access token via Redis (for immediate revocation before expiry).
     */
    private void blacklistAccessToken(Long userId) {
        if (redisTemplate != null) {
            String key = BLACKLIST_PREFIX + userId;
            redisTemplate.opsForValue().set(key, "revoked",
                    Duration.ofSeconds(refreshExpiration / 1000 + BLACKLIST_BUFFER_SECONDS));
        }
    }

    /**
     * Check if access token is blacklisted.
     */
    public boolean isAccessTokenBlacklisted(Long userId) {
        if (redisTemplate == null) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + userId));
    }
}
