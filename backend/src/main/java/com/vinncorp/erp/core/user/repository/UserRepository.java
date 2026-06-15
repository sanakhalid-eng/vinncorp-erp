package com.vinncorp.erp.core.user.repository;

import com.vinncorp.erp.core.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailOrUsername(String email, String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByWorkspaceOwnerTrue();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role r LEFT JOIN FETCH r.permissions WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role r LEFT JOIN FETCH r.permissions WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    List<User> findByIsActiveTrue();

    long countByIsActiveTrue();

    Optional<User> findByWorkspaceOwnerTrue();

    @Query("SELECT u FROM User u JOIN ProjectMember pm ON pm.user.id = u.id WHERE pm.project.id = :projectId AND pm.role = :role")
    Optional<User> findFirstByProjectRole(@Param("projectId") Long projectId, @Param("role") String role);
}

