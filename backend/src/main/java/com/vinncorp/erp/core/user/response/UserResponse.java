package com.vinncorp.erp.core.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User response payload")
public class UserResponse {

    @Schema(example = "1", description = "User ID")
    private Long id;

    @Schema(example = "John Doe", description = "Full name")
    private String name;

    @Schema(example = "user@example.com", description = "Email address")
    private String email;

    @Schema(example = "[\"ROLE_USER\"]", description = "User roles")
    private Set<String> roles;

    @Schema(example = "2025-01-01T10:00:00", description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @Schema(example = "https://example.com/avatar.png", description = "Avatar URL")
    private String avatarUrl;

    @Schema(example = "false", description = "Whether the user is a workspace owner")
    private boolean workspaceOwner;
}
