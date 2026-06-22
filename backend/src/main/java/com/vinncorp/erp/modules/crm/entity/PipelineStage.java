package com.vinncorp.erp.modules.crm.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "crm_pipeline_stages", indexes = {
    @Index(name = "idx_crm_stages_pipeline", columnList = "pipeline_id"),
    @Index(name = "idx_crm_stages_workspace", columnList = "workspace_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class PipelineStage extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @Column(name = "workspace_id", nullable = false, updatable = false)
    private Long workspaceId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @Column(name = "probability_pct", nullable = false)
    private int probabilityPct = 0;

    @Column(name = "is_won", nullable = false)
    private boolean isWon = false;

    @Column(name = "is_lost", nullable = false)
    private boolean isLost = false;
}
