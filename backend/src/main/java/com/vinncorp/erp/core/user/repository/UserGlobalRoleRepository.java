package com.vinncorp.erp.core.user.repository;

import com.vinncorp.erp.core.user.entity.UserGlobalRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGlobalRoleRepository extends JpaRepository<UserGlobalRole, Long> {

    List<UserGlobalRole> findByUserId(Long userId);

    List<UserGlobalRole> findByGlobalRoleName(String roleName);

    Optional<UserGlobalRole> findByUserIdAndGlobalRoleName(Long userId, String roleName);

    boolean existsByUserIdAndGlobalRoleName(Long userId, String roleName);

    @Query("SELECT ugr.globalRole.name FROM UserGlobalRole ugr WHERE ugr.user.id = :userId")
    List<String> findGlobalRoleNamesByUserId(@Param("userId") Long userId);

    void deleteByUserIdAndGlobalRoleId(Long userId, Long globalRoleId);
}
