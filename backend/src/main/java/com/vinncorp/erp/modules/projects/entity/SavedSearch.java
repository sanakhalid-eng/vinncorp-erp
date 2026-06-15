package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "saved_searches", indexes = {
        @Index(name = "idx_savedsearch_ws_user_created", columnList = "workspace_id, user_id, created_at")
})
@Getter
@Setter
public class SavedSearch extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "query_text", nullable = false, length = 500)
    private String queryText;

    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}



