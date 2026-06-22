package com.vinncorp.erp.modules.hr.dto.response;

import com.vinncorp.erp.modules.hr.entity.Designation;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DesignationResponse {

    private Long id;
    private Long workspaceId;
    private String title;
    private String code;
    private String description;
    private Integer level;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DesignationResponse from(Designation d) {
        if (d == null) return null;
        return DesignationResponse.builder()
            .id(d.getId())
            .workspaceId(d.getWorkspaceId())
            .title(d.getTitle())
            .code(d.getCode())
            .description(d.getDescription())
            .level(d.getLevel())
            .active(d.isActive())
            .createdAt(d.getCreatedAt())
            .updatedAt(d.getUpdatedAt())
            .build();
    }
}

