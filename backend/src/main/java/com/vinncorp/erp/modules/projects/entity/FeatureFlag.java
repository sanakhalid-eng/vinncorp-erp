package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.Data;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "feature_flags", uniqueConstraints = {
    @UniqueConstraint(columnNames = "flag_key")
})
@Data
public class FeatureFlag extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flag_key", nullable = false, length = 100)
    private String flagKey;

    @Column(name = "flag_value", nullable = false)
    private boolean flagValue = true;

    @Column(name = "description", length = 500)
    private String description;
}



