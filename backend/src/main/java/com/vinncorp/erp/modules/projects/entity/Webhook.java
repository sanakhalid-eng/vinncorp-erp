package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "webhooks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String secret;

    @Column(columnDefinition = "JSON", nullable = false)
    private String events; // JSON array of event types

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "webhook", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WebhookDelivery> deliveries;
}



