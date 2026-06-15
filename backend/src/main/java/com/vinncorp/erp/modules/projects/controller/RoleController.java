package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.Role;
import com.vinncorp.erp.modules.projects.service.impl.RoleServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles & Permissions")
public class RoleController {

    private final RoleServiceImpl roleServiceImpl;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @Operation(summary = "Create role", description = "Create a new role (admin only)")
    public ResponseEntity<ApiResponse<Role>> create(@RequestBody Role role) {
        Role created = roleServiceImpl.createRole(role);
        return ResponseEntity.ok(new ApiResponse<>(true, "Role created successfully", created));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieve all available roles")
    public ResponseEntity<ApiResponse<List<Role>>> getAll() {
        List<Role> roles = roleServiceImpl.getAllRoles();
        return ResponseEntity.ok(new ApiResponse<>(true, "Roles fetched successfully", roles));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Update an existing role by ID (admin only)")
    public ResponseEntity<ApiResponse<Role>> update(@PathVariable Long id, @RequestBody Role role) {
        Role updated = roleServiceImpl.updateRole(id, role);
        return ResponseEntity.ok(new ApiResponse<>(true, "Role updated successfully", updated));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Delete a role by ID (admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleServiceImpl.deleteRole(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Role deleted successfully", null));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/permissions")
    @Operation(summary = "Update role permissions", description = "Replace all permissions for a role (admin only)")
    public ResponseEntity<ApiResponse<Role>> updatePermissions(
            @PathVariable Long id,
            @RequestBody Map<String, List<Long>> body
    ) {
        List<Long> permissionIds = body.get("permissions");
        if (permissionIds == null) {
            permissionIds = List.of();
        }
        Role updated = roleServiceImpl.updateRolePermissions(id, permissionIds);
        return ResponseEntity.ok(new ApiResponse<>(true, "Permissions updated successfully", updated));
    }
}



