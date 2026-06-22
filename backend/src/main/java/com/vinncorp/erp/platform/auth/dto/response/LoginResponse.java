package com.vinncorp.erp.platform.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.vinncorp.erp.platform.workspace.dto.response.WorkspaceSummary;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Login response with tokens")
public class LoginResponse {

    @Schema(example = "eyJhbGciOiJIUzI1NiIs...", description = "JWT access token")
    private String accessToken;

    @Schema(example = "dGhpcyBpcyBhIHJlZnJl...", description = "JWT refresh token")
    private String refreshToken;

    @Schema(example = "1", description = "User ID")
    private Long userId;

    @Schema(description = "Current active workspace information")
    private WorkspaceSummary currentWorkspace;

    @Schema(description = "All workspaces the user belongs to (populated on login)")
    private List<WorkspaceSummary> workspaces;

    @Schema(description = "User's roles in the current workspace")
    private List<String> workspaceRoles;

    @Schema(description = "User's permissions in the current workspace")
    private List<String> workspacePermissions;
}
