package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.core.auth.request.ChangePasswordRequest;
import com.vinncorp.erp.core.user.request.RegisterRequest;
import com.vinncorp.erp.core.user.request.UpdateUserRequest;
import com.vinncorp.erp.core.user.response.UserResponse;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.entity.ProjectRole;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.mapper.UserMapper;
import com.vinncorp.erp.core.user.service.UserService;

import com.vinncorp.erp.modules.projects.repository.ProjectRoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vinncorp.erp.core.user.mapper.UserMapper.toResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "System")
public class UserController {

    private final UserService userService;
    private final ProjectRoleRepository projectRoleRepository;

    // CREATE USER (ADMIN)
    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user (admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody RegisterRequest request
    ) {
        User user = userService.createUserFromRequest(request);

        return ResponseEntity.ok(
                ApiResponse.success("User created successfully", toResponse(user))
        );
    }

    // GET ALL USERS (ADMIN)
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users (admin only)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {

        List<UserResponse> users = userService.getAllUsers()
                .stream()
                .map(UserMapper::toResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.success("Users fetched successfully", users)
        );
    }

    // GET CURRENT USER
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve the currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        return ResponseEntity.ok(
                ApiResponse.success("Current user fetched successfully", toResponse(user))
        );
    }

    // UPDATE OWN PROFILE
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    @Operation(summary = "Update my profile", description = "Update the current user's profile information")
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateUserRequest request
    ) {

        String email = authentication.getName();
        UserResponse updatedUser = userService.updateMyProfile(email, request);

        return ResponseEntity.ok(
                ApiResponse.success("Profile updated successfully", updatedUser)
        );
    }

    // CHANGE PASSWORD
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    @Operation(summary = "Change password", description = "Change the current user's password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {

        String email = authentication.getName();
        userService.changePassword(email, request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully")
        );
    }

    // DEACTIVATE OWN ACCOUNT
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/deactivate")
    @Operation(summary = "Deactivate account", description = "Deactivate the current user's account")
    public ResponseEntity<ApiResponse<String>> deactivateAccount(Authentication authentication) {

        String email = authentication.getName();
        userService.deactivateMyAccount(email);

        return ResponseEntity.ok(
                ApiResponse.success("Account deactivated successfully")
        );
    }

    // UPLOAD AVATAR
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/avatar")
    @Operation(summary = "Upload avatar", description = "Upload an avatar image for the current user")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        String email = authentication.getName();
        String avatarUrl = userService.uploadAvatar(email, file);

        return ResponseEntity.ok(
                ApiResponse.success("Avatar uploaded successfully", avatarUrl)
        );
    }

    // ADMIN UPDATE USER
    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update a user's details by ID (admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request
    ) {

        UserResponse updatedUser = userService.updateUserByAdmin(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully", updatedUser)
        );
    }

    // ADMIN DELETE USER
    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user by ID (admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully")
        );
    }

    // GET CURRENT USER SYSTEM PERMISSIONS
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/permissions")
    @Operation(summary = "Get my permissions", description = "Retrieve the current user's system permissions")
    public ResponseEntity<ApiResponse<Set<String>>> getCurrentUserPermissions(Authentication authentication) {

        String email = authentication.getName();
        User user = userService.getUserByEmail(email);

        Set<String> permissions = user.getUserRoles().stream()
                .flatMap(ur -> ur.getRole().getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                ApiResponse.success("Permissions fetched successfully", permissions)
        );
    }

    // GET PROJECT ROLES (for dropdowns)
    @GetMapping("/project-roles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project roles", description = "Retrieve all project roles for dropdowns")
    public ResponseEntity<ApiResponse<List<ProjectRoleDto>>> getProjectRoles() {
        List<ProjectRole> roles = projectRoleRepository.findAll();
        List<ProjectRoleDto> dtos = roles.stream()
                .map(r -> new ProjectRoleDto(r.getId(), r.getName(), r.getDescription()))
                .toList();
        return ResponseEntity.ok(
                ApiResponse.success("Project roles fetched successfully", dtos)
        );
    }

    public record ProjectRoleDto(Long id, String name, String description) {}
}


