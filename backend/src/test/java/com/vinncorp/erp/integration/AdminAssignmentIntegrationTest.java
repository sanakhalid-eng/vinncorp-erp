package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;
import com.vinncorp.erp.core.user.entity.UserRole;
import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.modules.projects.enums.RoleScope;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.CustomAccessDeniedException;
import com.vinncorp.erp.core.user.repository.RoleRepository;
import com.vinncorp.erp.core.user.repository.UserRoleRepository;
import com.vinncorp.erp.shared.security.MembershipResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminAssignmentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MembershipResolver membershipResolver;

    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        List<UserRole> roles = userRoleRepository.findByUserId(secondAdmin.getId());
        for (UserRole ur : roles) {
            userRoleRepository.delete(ur);
        }
    }

    private void authenticateAsWorkspaceOwner() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        adminUser.getEmail(), "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
    }

    @Test
    void assignAdminRole_byWorkspaceOwner_shouldSucceed() {
        authenticateAsWorkspaceOwner();
        userRoleService.assignSystemRole(normalUser.getId(), adminRole.getId());

        boolean isAdmin = userRoleRepository.findByUserId(normalUser.getId()).stream()
                .anyMatch(ur -> ur.getRole().getId().equals(adminRole.getId()));
        assertTrue(isAdmin);
    }

    @Test
    void assignAdminRole_byNonOwner_shouldFail() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        normalUser.getEmail(), "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        assertThrows(CustomAccessDeniedException.class, () ->
                userRoleService.assignSystemRole(secondAdmin.getId(), adminRole.getId()));
    }

    @Test
    void removeAdminRole_shouldSucceed() {
        authenticateAsWorkspaceOwner();
        userRoleService.assignSystemRole(normalUser.getId(), adminRole.getId());
        assertTrue(isUserAdmin(normalUser.getId()));

        userRoleService.removeSystemRole(normalUser.getId(), adminRole.getId());
        assertFalse(isUserAdmin(normalUser.getId()));
    }

    @Test
    void removeAdminRole_fromWorkspaceOwner_shouldFail() {
        authenticateAsWorkspaceOwner();
        assertThrows(BadRequestException.class, () ->
                userRoleService.removeSystemRole(adminUser.getId(), adminRole.getId()));
    }

    @Test
    void removeLastAdminRole_shouldFail() {
        authenticateAsWorkspaceOwner();
        assertThrows(BadRequestException.class, () ->
                userRoleService.removeSystemRole(adminUser.getId(), adminRole.getId()));
    }

    @Test
    void assignSystemRole_toNonExistentUser_shouldFail() {
        authenticateAsWorkspaceOwner();
        assertThrows(Exception.class, () ->
                userRoleService.assignSystemRole(99999L, adminRole.getId()));
    }

    @Test
    void assignProjectRole_viaSystemRoleService_shouldFail() {
        authenticateAsWorkspaceOwner();
        Role projectRole = new Role();
        projectRole.setName("TEST_PROJECT_ROLE");
        projectRole.setScope(RoleScope.PROJECT);
        Role savedProjectRole = roleRepository.save(projectRole);

        assertThrows(BadRequestException.class, () ->
                userRoleService.assignSystemRole(normalUser.getId(), savedProjectRole.getId()));
    }

    private boolean isUserAdmin(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .anyMatch(ur -> ur.getRole().getId().equals(adminRole.getId()));
    }
}

