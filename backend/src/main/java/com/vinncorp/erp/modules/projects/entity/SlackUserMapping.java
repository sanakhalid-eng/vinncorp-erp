package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "slack_user_mappings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackUserMapping extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "slack_integration_id", nullable = false)
    private SlackIntegration slackIntegration;

    @Column(name = "slack_user_id", nullable = false)
    private String slackUserId;

    @Column(name = "slack_username")
    private String slackUsername;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}



