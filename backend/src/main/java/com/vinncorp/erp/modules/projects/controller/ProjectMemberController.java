package com.vinncorp.erp.modules.projects.controller;

import com.vinncorp.erp.modules.projects.dto.request.*;
import com.vinncorp.erp.modules.projects.dto.response.ApiResponse;
import com.vinncorp.erp.modules.projects.dto.response.ProjectMemberResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.entity.ProjectRole;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRoleRepository;
import com.vinncorp.erp.modules.projects.service.PermissionService;
import com.vinncorp.erp.modules.projects.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/members")
@Tag(name = "Projects")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final PermissionService permissionService;

    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project roles", description = "Retrieve all available project roles")
    public ResponseEntity<ApiResponse<List<ProjectRoleDto>>> getProjectRoles() {
        List<ProjectRole> roles = projectRoleRepository.findAll();
        List<ProjectRoleDto> dtos = roles.stream()
                .map(r -> new ProjectRoleDto(r.getId(), r.getName(), r.getDescription()))
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project roles fetched", dtos));
    }

    public record ProjectRoleDto(Long id, String name, String description) {}

    @PreAuthorize(
            "hasAuthority('ADD_MEMBER') and " +
            "(@projectSecurity.isProjectManager(#projectId, authentication.name)) "
    )
    @PostMapping
    @Operation(summary = "Add member", description = "Add a member to a project")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> addMember(
            @PathVariable Long projectId,
            @RequestBody @Valid AddProjectMemberRequest request
    ) {
        ProjectMemberResponse member = projectMemberService.addMemberToProject(projectId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Member added successfully", member));
    }

    @PreAuthorize(
            "hasAuthority('ADD_MEMBER') and " +
            "(@projectSecurity.isProjectManager(#projectId, authentication.name)) "
    )
    @PostMapping("/bulk")
    @Operation(summary = "Add multiple members", description = "Add multiple members to a project at once")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> addMultipleMembers(
            @PathVariable Long projectId,
            @RequestBody AddMultipleMembersRequest request) {

        List<ProjectMemberResponse> members = projectMemberService.addMultipleMembers(projectId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Members added successfully", members));
    }

    @PreAuthorize(
            "hasAuthority('VIEW_MEMBERS') and " +
            "@projectSecurity.isProjectMember(#projectId, authentication.name)"
    )
    @GetMapping
    @Operation(summary = "Get project members", description = "Retrieve all members of a project")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMemberResponse> members = projectMemberService.getProjectMembers(projectId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Project members fetched", members));
    }

    @PreAuthorize("hasAuthority('VIEW_MEMBERS') and @projectSecurity.isProjectMember(#projectId, authentication.name)")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> getMember(
            @PathVariable Long projectId,
            @PathVariable Long userId
    ) {
        ProjectMemberResponse member = projectMemberService.getMember(projectId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Member fetched", member));
    }

    @PreAuthorize(
            "hasAuthority('VIEW_MEMBERS') and " +
            "@projectSecurity.isProjectMember(#projectId, authentication.name)"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>> searchMembers(
            @PathVariable Long projectId,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String search) {

        ProjectMemberFilterRequest filter = new ProjectMemberFilterRequest();
        filter.setRole(role);
        filter.setSearch(search);

        List<ProjectMemberResponse> members = projectMemberService.filterMembers(projectId, filter);
        return ResponseEntity.ok(new ApiResponse<>(true, "Members fetched", members));
    }

    @PreAuthorize(
            "hasAuthority('UPDATE_MEMBER_ROLE') and " +
            "(@projectSecurity.isProjectManager(#projectId, authentication.name))"
    )
    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> updateRole(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody UpdateProjectMemberRoleRequest request
    ) {
        ProjectMemberResponse member = projectMemberService.updateMemberRole(projectId, userId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Role updated successfully", member));
    }

    @PreAuthorize(
            "hasAuthority('REMOVE_MEMBER') and " +
            "(@projectSecurity.isProjectManager(#projectId, authentication.name))"
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable Long projectId,
                               @PathVariable Long userId){
        projectMemberService.removeMemberFromProject(projectId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Member removed successfully", null));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> leaveProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        projectMemberService.leaveProject(projectId, user.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(true, "You left the project", null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/permissions")
    public ResponseEntity<ApiResponse<List<String>>> getMyPermissions(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        Long userId = user.getUserId();
        List<String> permissions = projectMemberRepository.getUserPermissions(projectId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Permissions fetched", permissions));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/invite-by-email")
    @Operation(
            summary = "Invite assignee by email",
            description = "Invite a new user (or add an existing user) as a project member / assignee by email. " +
                    "If the user does not exist, an inactive account is created and an activation email is sent."
    )
    public ResponseEntity<ApiResponse<ProjectMemberResponse>> inviteAssigneeByEmail(
            @PathVariable Long projectId,
            @RequestBody @Valid InviteAssigneeRequest request,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        ProjectMemberResponse member = projectMemberService.inviteAssigneeByEmail(
                projectId, request.getEmail(), user.getUserId()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Assignee invited successfully", member));
    }

}



