package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;

@Entity
@Table(name = "hr_shifts", indexes = {
    @Index(name = "idx_hr_shifts_workspace", columnList = "workspace_id"),
    @Index(name = "idx_hr_shifts_active", columnList = "workspace_id, is_active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLRestriction("deleted_at IS NULL")
public class HrShift extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "break_minutes")
    private Integer breakMinutes = 0;

    @Column(name = "grace_period_minutes")
    private Integer gracePeriodMinutes = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;
}
