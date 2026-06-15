package com.vinncorp.erp.core.user.controller;

import com.vinncorp.erp.core.user.entity.GlobalRole;
import com.vinncorp.erp.core.user.entity.UserGlobalRole;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.GlobalRoleRepository;
import com.vinncorp.erp.core.user.repository.UserGlobalRoleRepository;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/global-roles")
@RequiredArgsConstructor
@Tag(name = "Global Role Management")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class GlobalRoleController {

    private final GlobalRoleRepository globalRoleRepository;
    private final UserGlobalRoleRepository userGlobalRoleRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "List all global roles")
    public ResponseEntity<ApiResponse<List<GlobalRole>>> listRoles() {
        return ResponseEntity.ok(ApiResponse.success("Global roles fetched", globalRoleRepository.findAll()));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get global roles for a user")
    public ResponseEntity<ApiResponse<List<String>>> getUserGlobalRoles(@PathVariable Long userId) {
        List<String> roles = userGlobalRoleRepository.findGlobalRoleNamesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User global roles fetched", roles));
    }

    @PostMapping("/assign")
    @Operation(summary = "Assign a global role to a user")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @RequestBody AssignGlobalRoleRequest request,
            Authentication auth) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));
        GlobalRole globalRole = globalRoleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Global role not found: " + request.getRoleName()));
        User actor = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));

        if (userGlobalRoleRepository.existsByUserIdAndGlobalRoleName(user.getId(), globalRole.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("User already has this global role", 409));
        }

        UserGlobalRole ugr = new UserGlobalRole();
        ugr.setUser(user);
        ugr.setGlobalRole(globalRole);
        ugr.setAssignedBy(actor);
        userGlobalRoleRepository.save(ugr);

        return ResponseEntity.ok(ApiResponse.success("Global role assigned"));
    }

    @PostMapping("/revoke")
    @Operation(summary = "Revoke a global role from a user")
    public ResponseEntity<ApiResponse<Void>> revokeRole(@RequestBody AssignGlobalRoleRequest request) {
        UserGlobalRole ugr = userGlobalRoleRepository
                .findByUserIdAndGlobalRoleName(request.getUserId(), request.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Global role assignment not found"));

        userGlobalRoleRepository.delete(ugr);
        return ResponseEntity.ok(ApiResponse.success("Global role revoked"));
    }

    @Data
    public static class AssignGlobalRoleRequest {
        private Long userId;
        private String roleName;
    }
}
