package com.vinncorp.erp.platform.user.entity;

import com.vinncorp.erp.platform.audit.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "global_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class GlobalRole extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;
}
