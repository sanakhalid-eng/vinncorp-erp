package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.modules.projects.enums.WebhookDeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "webhook_deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookDelivery extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "webhook_id", nullable = false)
    private Webhook webhook;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(columnDefinition = "JSON", nullable = false)
    private String payload;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private WebhookDeliveryStatus status = WebhookDeliveryStatus.PENDING;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
}



