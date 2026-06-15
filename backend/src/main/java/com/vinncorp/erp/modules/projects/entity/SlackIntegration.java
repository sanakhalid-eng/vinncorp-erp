package com.vinncorp.erp.modules.projects.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.vinncorp.erp.core.audit.BaseAuditableEntity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "slack_integrations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackIntegration extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "workspace_id", nullable = false)
    private String workspaceId;

    @Column(name = "workspace_name")
    private String workspaceName;

    @JsonIgnore
    @Column(name = "access_token", nullable = false)
    private String accessToken;

    @JsonIgnore
    @Column(name = "refresh_token")
    private String refreshToken;

    @JsonIgnore
    @Column(name = "signing_secret", nullable = false)
    private String signingSecret;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @Column(name = "channel_id")
    private String channelId;

    @Column(name = "channel_name")
    private String channelName;

    @Column(name = "bot_user_id")
    private String botUserId;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "slackIntegration", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<SlackUserMapping> userMappings;
}



