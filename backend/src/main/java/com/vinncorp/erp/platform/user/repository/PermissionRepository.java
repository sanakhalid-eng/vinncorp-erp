package com.vinncorp.erp.platform.user.repository;

import com.vinncorp.erp.platform.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    @Query("SELECT p.name FROM Permission p WHERE p.id IN :ids")
    List<String> findNamesByIds(List<Long> ids);

    List<Permission> findAllByOrderByPermissionGroupAscNameAsc();
}

