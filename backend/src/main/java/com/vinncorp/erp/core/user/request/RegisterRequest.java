package com.vinncorp.erp.core.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

@Data
@Schema(description = "User registration request")
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Schema(example = "John Doe", description = "Full name of the user")
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, hyphens, and underscores")
    @Schema(example = "johndoe", description = "Unique username for login")
    private String username;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Schema(example = "user@example.com", description = "Email address")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    @NotBlank(message = "Password is required")
    @Schema(example = "SecurePass123!", description = "Password (min 8 characters)")
    private String password;
}

