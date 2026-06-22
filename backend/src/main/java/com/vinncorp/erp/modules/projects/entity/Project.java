package com.vinncorp.erp.modules.projects.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;

import com.vinncorp.erp.modules.projects.enums.ProjectPriority;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.workspace.entity.Workspace;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "projects")
public class Project extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Workspace workspace;

    private String name;

    @Column(length = 2000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne
    @JoinColumn(name = "project_manager_id")
    private User projectManager;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectPriority priority = ProjectPriority.MEDIUM;

    @Column(length = 500)
    private String tags; // Comma-separated tags

    private String category;

    @Column(length = 1000)
    private String objectives; // Project objectives/goals

    @ManyToOne
    @JoinColumn(name = "status_id")
    private WorkflowStatus status;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private boolean isPublic = false; // Visible to all users

    private Double budget; // Project budget

    private String currency = "USD";

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ProjectMember> members;
}


