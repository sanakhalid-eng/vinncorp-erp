package com.vinncorp.erp.platform.workspace.entity;

import jakarta.persistence.*;
import lombok.*;
import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

@Entity
@Table(name = "workspaces")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Workspace extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "settings_json", columnDefinition = "TEXT")
    private String settingsJson;

    @Column(nullable = false)
    private boolean active = true;
}

