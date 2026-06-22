package com.vinncorp.erp.modules.hr.dto.response;

import com.vinncorp.erp.modules.hr.entity.Department;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentResponse {

    private Long id;
    private Long workspaceId;
    private String name;
    private String code;
    private String description;
    private Long headEmployeeId;
    private Long parentDepartmentId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DepartmentResponse from(Department d) {
        if (d == null) return null;
        return DepartmentResponse.builder()
            .id(d.getId())
            .workspaceId(d.getWorkspaceId())
            .name(d.getName())
            .code(d.getCode())
            .description(d.getDescription())
            .headEmployeeId(d.getHeadEmployeeId())
            .parentDepartmentId(d.getParentDepartmentId())
            .active(d.isActive())
            .createdAt(d.getCreatedAt())
            .updatedAt(d.getUpdatedAt())
            .build();
    }
}

