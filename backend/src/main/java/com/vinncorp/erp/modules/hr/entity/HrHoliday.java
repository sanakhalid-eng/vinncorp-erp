package com.vinncorp.erp.modules.hr.entity;

import com.vinncorp.erp.core.audit.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@Table(name = "hr_holidays", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"workspace_id", "holiday_date", "name"})
}, indexes = {
    @Index(name = "idx_hr_holidays_workspace", columnList = "workspace_id"),
    @Index(name = "idx_hr_holidays_date", columnList = "workspace_id, holiday_date"),
    @Index(name = "idx_hr_holidays_type", columnList = "workspace_id, holiday_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@SQLRestriction("deleted_at IS NULL")
public class HrHoliday extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "holiday_type", nullable = false, length = 50)
    private String holidayType = "PUBLIC";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_recurring")
    private Boolean isRecurring = false;
}
