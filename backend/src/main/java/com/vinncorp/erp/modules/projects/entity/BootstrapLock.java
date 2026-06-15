package com.vinncorp.erp.modules.projects.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bootstrap_lock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BootstrapLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lock_name", nullable = false, unique = true)
    private String lockName;

    @Version
    private Integer version;
}



