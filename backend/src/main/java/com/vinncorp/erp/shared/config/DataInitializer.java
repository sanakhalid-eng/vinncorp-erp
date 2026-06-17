package com.vinncorp.erp.shared.config;

import com.vinncorp.erp.core.user.repository.RoleRepository;
import com.vinncorp.erp.core.user.repository.PermissionRepository;
import com.vinncorp.erp.core.user.repository.GlobalRoleRepository;
import com.vinncorp.erp.core.user.repository.UserGlobalRoleRepository;
import com.vinncorp.erp.core.user.constants.PermissionConstants;
import com.vinncorp.erp.core.user.entity.Permission;
import com.vinncorp.erp.core.user.entity.GlobalRole;
import com.vinncorp.erp.core.user.entity.UserGlobalRole;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRoleRepository;
import com.vinncorp.erp.core.user.repository.RolePermissionRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;

import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.enums.RoleScope;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final GlobalRoleRepository globalRoleRepository;
    private final UserGlobalRoleRepository userGlobalRoleRepository;
    private final UserRepository userRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    public void run(String @NonNull ... args) {
        initPermissions();
        initSystemRoles();
        initProjectRolesAndPermissions();
        initGlobalRoles();
        migrateProjectMembers();
    }

    private void initPermissions() {
        if (permissionRepository.count() > 0) return;

        List<PermissionSeed> permissionSeeds = Arrays.asList(
            new PermissionSeed(PermissionConstants.VIEW_DASHBOARD, "ANALYTICS"),
            new PermissionSeed(PermissionConstants.VIEW_PROJECT, "PROJECT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.VIEW_ALL_PROJECTS, "PROJECT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CREATE_PROJECT, "PROJECT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.EDIT_PROJECT, "PROJECT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DELETE_PROJECT, "PROJECT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.VIEW_TASK, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.VIEW_ALL_TASKS, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CREATE_TASK, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.EDIT_TASK, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.EDIT_ANY_TASK, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.EDIT_ASSIGNED_TASK, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DELETE_TASK, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.MOVE_TASK, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.ASSIGN_TASK, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.VIEW_MEMBERS, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.ADD_MEMBER, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.REMOVE_MEMBER, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.UPDATE_MEMBER_ROLE, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.VIEW_USERS, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CREATE_USER, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.UPDATE_USER, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DELETE_USER, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.ASSIGN_SYSTEM_ROLE, "SECURITY"),
            new PermissionSeed(PermissionConstants.VIEW_ROLES, "SECURITY"),
            new PermissionSeed(PermissionConstants.CREATE_ROLE, "SECURITY"),
            new PermissionSeed(PermissionConstants.UPDATE_ROLE, "SECURITY"),
            new PermissionSeed(PermissionConstants.DELETE_ROLE, "SECURITY"),
            new PermissionSeed(PermissionConstants.MANAGE_WORKFLOW, "SPRINT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.VIEW_PROFILE, "USER_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CREATE_LABEL, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DELETE_LABEL, "TASK_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.ASSIGN_LABEL, "TASK_MANAGEMENT"),
            // Employee Management
            new PermissionSeed(PermissionConstants.EMPLOYEE_VIEW, "EMPLOYEE_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.EMPLOYEE_CREATE, "EMPLOYEE_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.EMPLOYEE_UPDATE, "EMPLOYEE_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.EMPLOYEE_DELETE, "EMPLOYEE_MANAGEMENT"),
            // Department Management
            new PermissionSeed(PermissionConstants.DEPARTMENT_VIEW, "DEPARTMENT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DEPARTMENT_CREATE, "DEPARTMENT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DEPARTMENT_UPDATE, "DEPARTMENT_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DEPARTMENT_DELETE, "DEPARTMENT_MANAGEMENT"),
            // Designation Management
            new PermissionSeed(PermissionConstants.DESIGNATION_VIEW, "DESIGNATION_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DESIGNATION_CREATE, "DESIGNATION_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DESIGNATION_UPDATE, "DESIGNATION_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DESIGNATION_DELETE, "DESIGNATION_MANAGEMENT"),
            // Platform / Workspace Management
            new PermissionSeed(PermissionConstants.WORKSPACE_MANAGE, "PLATFORM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.PLATFORM_MANAGE, "PLATFORM_MANAGEMENT"),
            // CRM Module
            new PermissionSeed(PermissionConstants.CRM_VIEW, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CRM_MANAGE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CUSTOMER_VIEW, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CUSTOMER_CREATE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CUSTOMER_UPDATE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CUSTOMER_DELETE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.LEAD_VIEW, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.LEAD_CREATE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.LEAD_UPDATE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.LEAD_DELETE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CONTACT_VIEW, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CONTACT_CREATE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CONTACT_UPDATE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.CONTACT_DELETE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DEAL_VIEW, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DEAL_CREATE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DEAL_UPDATE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.DEAL_DELETE, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.PIPELINE_VIEW, "CRM_MANAGEMENT"),
            new PermissionSeed(PermissionConstants.PIPELINE_MANAGE, "CRM_MANAGEMENT")
        );

        for (PermissionSeed seed : permissionSeeds) {
            Permission permission = new Permission();
            permission.setName(seed.name);
            permission.setDescription(seed.name.replace("_", " "));
            permission.setPermissionGroup(seed.group);
            permissionRepository.save(permission);
        }
    }

    private record PermissionSeed(String name, String group) {} 

    private void initSystemRoles() {
        List<Permission> allPermissions = permissionRepository.findAll();
        Map<String, Permission> permMap = new HashMap<>();
        for (Permission p : allPermissions) {
            permMap.put(p.getName(), p);
        }

        Role adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
            Role r = new Role();
            r.setName("ADMIN");
            r.setDescription("System administrator with full access");
            r.setScope(RoleScope.SYSTEM);
            r.setSystemRole(true);
            r.setEditable(false);
            return roleRepository.save(r);
        });
        adminRole.setSystemRole(true);
        adminRole.setEditable(false);
        adminRole.setPermissions(new HashSet<>(allPermissions));
        roleRepository.save(adminRole);

        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("USER");
            r.setDescription("Standard system user");
            r.setScope(RoleScope.SYSTEM);
            r.setSystemRole(true);
            r.setEditable(false);
            return roleRepository.save(r);
        });
        userRole.setPermissions(new HashSet<>(Arrays.asList(
            permMap.get(PermissionConstants.VIEW_DASHBOARD),
            permMap.get(PermissionConstants.VIEW_PROJECT),
            permMap.get(PermissionConstants.VIEW_TASK),
            permMap.get(PermissionConstants.VIEW_MEMBERS),
            permMap.get(PermissionConstants.VIEW_PROFILE)
        )));
        roleRepository.save(userRole);
    }

    private void initProjectRolesAndPermissions() {
        if (projectRoleRepository.count() > 0) return;

        List<Permission> allPermissions = permissionRepository.findAll();
        Map<String, Permission> permMap = new HashMap<>();
        for (Permission p : allPermissions) {
            permMap.put(p.getName(), p);
        }

        // PROJECT_MANAGER - all project permissions
        ProjectRole pmRole = new ProjectRole();
        pmRole.setName(PermissionConstants.PROJECT_MANAGER);
        pmRole.setDescription("Manages project tasks, members, and workflow");
        pmRole = projectRoleRepository.save(pmRole);
        seedRolePermissions(pmRole, permMap, List.of(
            PermissionConstants.VIEW_TASK, PermissionConstants.VIEW_ALL_TASKS,
            PermissionConstants.CREATE_TASK, PermissionConstants.EDIT_TASK,
            PermissionConstants.EDIT_ANY_TASK, PermissionConstants.DELETE_TASK,
            PermissionConstants.MOVE_TASK, PermissionConstants.ASSIGN_TASK,
            PermissionConstants.VIEW_MEMBERS, PermissionConstants.ADD_MEMBER,
            PermissionConstants.REMOVE_MEMBER, PermissionConstants.UPDATE_MEMBER_ROLE,
            PermissionConstants.VIEW_PROJECT, PermissionConstants.EDIT_PROJECT,
            PermissionConstants.MANAGE_WORKFLOW, PermissionConstants.VIEW_DASHBOARD,
            PermissionConstants.CREATE_LABEL, PermissionConstants.DELETE_LABEL, PermissionConstants.ASSIGN_LABEL
        ));

        // TEAM_MEMBER - limited permissions
        ProjectRole tmRole = new ProjectRole();
        tmRole.setName(PermissionConstants.TEAM_MEMBER);
        tmRole.setDescription("Project team member who can work on tasks");
        tmRole = projectRoleRepository.save(tmRole);
        seedRolePermissions(tmRole, permMap, List.of(
            PermissionConstants.VIEW_TASK, PermissionConstants.CREATE_TASK,
            PermissionConstants.MOVE_TASK, PermissionConstants.EDIT_ASSIGNED_TASK,
            PermissionConstants.VIEW_MEMBERS, PermissionConstants.VIEW_PROJECT,
            PermissionConstants.VIEW_DASHBOARD,
            PermissionConstants.ASSIGN_LABEL
        ));

        // Also seed legacy Role entries for backward compatibility
        initLegacyProjectRoles(permMap);
    }

    private void seedRolePermissions(ProjectRole role, Map<String, Permission> permMap, List<String> permNames) {
        for (String name : permNames) {
            Permission p = permMap.get(name);
            if (p != null) {
                RolePermission rp = new RolePermission();
                rp.setProjectRole(role);
                rp.setPermission(p);
                rolePermissionRepository.save(rp);
            }
        }
    }

    private void initLegacyProjectRoles(Map<String, Permission> permMap) {
        Role pmLegacy = roleRepository.findByName(PermissionConstants.PROJECT_MANAGER).orElseGet(() -> {
            Role r = new Role();
            r.setName(PermissionConstants.PROJECT_MANAGER);
            r.setDescription("Manages project tasks and members");
            r.setScope(RoleScope.PROJECT);
            return roleRepository.save(r);
        });
        Set<Permission> legacyPerms = new HashSet<>();
        for (String name : List.of(
            PermissionConstants.VIEW_TASK, PermissionConstants.CREATE_TASK,
            PermissionConstants.EDIT_TASK, PermissionConstants.DELETE_TASK,
            PermissionConstants.MOVE_TASK, PermissionConstants.ASSIGN_TASK,
            PermissionConstants.ADD_MEMBER, PermissionConstants.REMOVE_MEMBER,
            PermissionConstants.CREATE_LABEL, PermissionConstants.DELETE_LABEL,
            PermissionConstants.ASSIGN_LABEL
        )) {
            Permission p = permMap.get(name);
            if (p != null) legacyPerms.add(p);
        }
        pmLegacy.setPermissions(legacyPerms);
        roleRepository.save(pmLegacy);

        Role tmLegacy = roleRepository.findByName(PermissionConstants.TEAM_MEMBER).orElseGet(() -> {
            Role r = new Role();
            r.setName(PermissionConstants.TEAM_MEMBER);
            r.setDescription("Project team member");
            r.setScope(RoleScope.PROJECT);
            return roleRepository.save(r);
        });
        tmLegacy.setPermissions(new HashSet<>(List.of(
            permMap.get(PermissionConstants.VIEW_TASK),
            permMap.get(PermissionConstants.CREATE_TASK),
            permMap.get(PermissionConstants.MOVE_TASK),
            permMap.get(PermissionConstants.ASSIGN_LABEL)
        )));
        roleRepository.save(tmLegacy);
    }

    private void migrateProjectMembers() {
        List<ProjectMember> unmigrated = projectMemberRepository.findMembersWithoutProjectRole();
        if (unmigrated.isEmpty()) return;

        Map<String, ProjectRole> projectRoleMap = new HashMap<>();
        for (ProjectRole pr : projectRoleRepository.findAll()) {
            projectRoleMap.put(pr.getName(), pr);
        }

        for (ProjectMember member : unmigrated) {
            if (member.getRole() != null) {
                String roleName = member.getRole().getName();
                ProjectRole projectRole = projectRoleMap.get(roleName);
                if (projectRole != null) {
                    member.setProjectRole(projectRole);
                    projectMemberRepository.save(member);
                }
            }
        }
    }

    private void initGlobalRoles() {
        if (globalRoleRepository.count() > 0) return;

        GlobalRole superAdmin = new GlobalRole();
        superAdmin.setName("SUPER_ADMIN");
        superAdmin.setDescription("Platform super administrator with full access to all workspaces and settings");
        globalRoleRepository.save(superAdmin);

        GlobalRole supportAdmin = new GlobalRole();
        supportAdmin.setName("SUPPORT_ADMIN");
        supportAdmin.setDescription("Platform support administrator with read access and limited management capabilities");
        globalRoleRepository.save(supportAdmin);

        // Assign SUPER_ADMIN to the first workspace owner (platform creator)
        userRepository.findByWorkspaceOwnerTrue().ifPresent(owner -> {
            if (!userGlobalRoleRepository.existsByUserIdAndGlobalRoleName(owner.getId(), "SUPER_ADMIN")) {
                UserGlobalRole ugr = new UserGlobalRole();
                ugr.setUser(owner);
                ugr.setGlobalRole(superAdmin);
                ugr.setAssignedBy(owner);
                userGlobalRoleRepository.save(ugr);
            }
        });
    }
}


