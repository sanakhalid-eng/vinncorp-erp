package com.vinncorp.erp.core.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login credentials")
public class LoginRequest {

    @NotBlank(message = "Email or username is required")
    @Schema(example = "user@example.com or johndoe", description = "User email address or username")
    private String identifier;

    @NotBlank(message = "Password is required")
    @Schema(example = "password123", description = "User password")
    private String password;
}

