package com.vinncorp.erp.core.user.repository;

import com.vinncorp.erp.core.user.entity.GlobalRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalRoleRepository extends JpaRepository<GlobalRole, Long> {

    Optional<GlobalRole> findByName(String name);

    boolean existsByName(String name);
}
