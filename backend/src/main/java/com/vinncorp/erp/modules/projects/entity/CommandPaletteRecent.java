package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "command_palette_recents", indexes = {
        @Index(name = "idx_cmdpal_ws_user_used", columnList = "workspace_id, user_id, used_at")
})
@Getter
@Setter
public class CommandPaletteRecent extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "action_key", nullable = false, length = 100)
    private String actionKey;

    @Column(name = "action_label", nullable = false)
    private String actionLabel;

    @Column(name = "target_url", length = 500)
    private String targetUrl;

    @CreationTimestamp
    @Column(name = "used_at", nullable = false, updatable = false)
    private LocalDateTime usedAt;
}



