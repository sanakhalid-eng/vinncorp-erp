package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.core.user.entity.UserRole;
import com.vinncorp.erp.core.user.service.UserRoleService;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Roles & Permissions")
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PreAuthorize("hasAuthority('ASSIGN_SYSTEM_ROLE') or @securityService.isCurrentUser(#userId)")
    @PostMapping("/{userId}/system-role")
    @Operation(summary = "Assign system role", description = "Assign a system role to a user")
    public ResponseEntity<ApiResponse<Void>> assignSystemRole(
            @PathVariable Long userId,
            @RequestParam Long roleId) {
        userRoleService.assignSystemRole(userId, roleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "System role assigned successfully", null));
    }

    @PreAuthorize("hasAuthority('ASSIGN_SYSTEM_ROLE')")
    @DeleteMapping("/{userId}/system-role")
    @Operation(summary = "Remove system role", description = "Remove a system role from a user")
    public ResponseEntity<ApiResponse<Void>> removeSystemRole(
            @PathVariable Long userId,
            @RequestParam Long roleId) {
        userRoleService.removeSystemRole(userId, roleId);
        return ResponseEntity.ok(new ApiResponse<>(true, "System role removed successfully", null));
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping("/{userId}/system-roles")
    @Operation(summary = "Get user system roles", description = "Retrieve all system roles assigned to a user")
    public ResponseEntity<ApiResponse<List<UserRole>>> getUserSystemRoles(@PathVariable Long userId) {
        List<UserRole> roles = userRoleService.getUserSystemRoles(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "User system roles fetched successfully", roles));
    }
}



