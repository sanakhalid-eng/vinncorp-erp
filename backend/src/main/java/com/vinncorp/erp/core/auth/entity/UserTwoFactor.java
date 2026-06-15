package com.vinncorp.erp.core.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.audit.BaseAuditableEntity;

@Entity
@Table(name = "user_two_factor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTwoFactor extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "secret_key", nullable = false)
    private String secretKey;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private boolean enabled = false;

    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes; // Comma-separated hashed backup codes
}
