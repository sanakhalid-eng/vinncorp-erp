package com.vinncorp.erp.platform.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import com.vinncorp.erp.platform.user.entity.User;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_user", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_expiry", columnList = "expiry_date"),
    @Index(name = "idx_refresh_tokens_family", columnList = "token_family")
})
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "replaced_by_token")
    private String replacedByToken;

    @Column(name = "token_family", nullable = false, length = 64)
    private String tokenFamily;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public boolean isExpired() {
        return expiryDate.isBefore(Instant.now());
    }
}
