package com.vinncorp.erp.modules.projects.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddProjectMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Role name is required")
    @Size(max = 50)
    private String role;
}



