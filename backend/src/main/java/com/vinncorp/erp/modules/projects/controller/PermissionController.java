package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.platform.user.entity.Permission;
import com.vinncorp.erp.platform.user.repository.PermissionRepository;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.PermissionGroupedResponse;
import com.vinncorp.erp.modules.projects.dto.response.PermissionGroupedResponse.PermissionInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Tag(name = "Roles & Permissions")
public class PermissionController {

    private final PermissionRepository permissionRepository;

    @GetMapping("/grouped")
    @Operation(summary = "Get permissions grouped", description = "Retrieve all permissions grouped by category")
    public ResponseEntity<ApiResponse<List<PermissionGroupedResponse>>> getPermissionsGrouped() {
        List<Permission> allPermissions = permissionRepository.findAllByOrderByPermissionGroupAscNameAsc();

        Map<String, List<PermissionInfo>> grouped = allPermissions.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getPermissionGroup() != null ? p.getPermissionGroup() : "UNGROUPED",
                        LinkedHashMap::new,
                        Collectors.mapping(
                                p -> PermissionInfo.builder()
                                        .id(p.getId())
                                        .name(p.getName())
                                        .description(p.getDescription())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        List<PermissionGroupedResponse> result = grouped.entrySet().stream()
                .map(entry -> PermissionGroupedResponse.builder()
                        .group(entry.getKey())
                        .permissions(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "Permissions grouped successfully", result));
    }
}



