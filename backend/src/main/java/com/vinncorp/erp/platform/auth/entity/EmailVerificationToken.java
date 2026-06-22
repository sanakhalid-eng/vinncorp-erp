package com.vinncorp.erp.platform.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

@Entity
@Table(name = "email_verification_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmailVerificationToken extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(name = "is_used")
    private Boolean isUsed = false;

    // New fields to store registration data temporarily
    @Column(name = "temp_name")
    private String tempName;

    @Column(name = "temp_username")
    private String tempUsername;

    @Column(name = "temp_password")
    private String tempPassword;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}

