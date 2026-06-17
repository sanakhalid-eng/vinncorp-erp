package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.constants.PermissionConstants;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.modules.projects.dto.request.AddMultipleMembersRequest;
import com.vinncorp.erp.modules.projects.dto.request.AddProjectMemberRequest;
import com.vinncorp.erp.modules.projects.dto.request.ProjectMemberFilterRequest;
import com.vinncorp.erp.modules.projects.dto.request.UpdateProjectMemberRoleRequest;
import com.vinncorp.erp.modules.projects.dto.response.ProjectMemberResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.ProjectMember;
import com.vinncorp.erp.modules.projects.entity.ProjectRole;
import com.vinncorp.erp.modules.projects.enums.ActionType;
import com.vinncorp.erp.modules.projects.enums.EntityType;
import com.vinncorp.erp.modules.projects.mapper.ProjectMemberMapper;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRoleRepository;
import com.vinncorp.erp.modules.projects.service.ActivityLogService;
import com.vinncorp.erp.modules.projects.service.EmailService;
import com.vinncorp.erp.modules.projects.service.PermissionService;
import com.vinncorp.erp.modules.projects.service.ProjectMemberService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ConflictException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final PermissionService permissionService;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final EmailService emailService;
    private final ActivityLogService activityLogService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @Override
    public ProjectMemberResponse addMemberToProject(Long projectId, AddProjectMemberRequest request) {

        Long workspaceId = resolveWorkspaceId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the current workspace");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, user.getId())) {
            throw new BadRequestException("User is not a member of this workspace");
        }

        if (projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, user.getId())
                .isPresent()) {
            throw new ConflictException("User already a member of this project");
        }

        String requestedRole = request.getRole() == null || request.getRole().isBlank()
                ? PermissionConstants.TEAM_MEMBER
                : request.getRole().trim().toUpperCase();

        ProjectRole projectRole = projectRoleRepository.findByName(requestedRole)
                .orElseThrow(() -> new ResourceNotFoundException("Project role not found: " + requestedRole));

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(user);
        member.setProjectRole(projectRole);

        ProjectMember savedMember = projectMemberRepository.save(member);

        return ProjectMemberMapper.toResponse(savedMember);
    }

    @Override
    public ProjectMemberResponse assignRole(Long projectId, Long userId, Long roleId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProjectRole projectRole = projectRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Project role not found"));

        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, userId)
                .orElseGet(() -> {
                    ProjectMember m = new ProjectMember();
                    m.setProject(project);
                    m.setUser(user);
                    return m;
                });

        member.setProject(project);
        member.setUser(user);
        member.setProjectRole(projectRole);

        ProjectMember saved = projectMemberRepository.save(member);

        permissionService.evictUserProjectPermissions(userId, projectId);

        return ProjectMemberMapper.toResponse(saved);
    }

    @Override
    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {

        return projectMemberRepository
                .findByProject_Id(projectId)
                .stream()
                .map(ProjectMemberMapper::toResponse)
                .toList();
    }

    @Override
    public void removeMemberFromProject(Long projectId, Long userId) {

        validateOwnerRemoval(projectId, userId);
        validateLastManagerRemoval(projectId, userId);

        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in project"));

        projectMemberRepository.delete(member);

        permissionService.evictUserProjectPermissions(userId, projectId);
    }

    @Override
    public ProjectMemberResponse updateMemberRole(Long projectId, Long userId, UpdateProjectMemberRoleRequest request) {

        Project project = projectRepository
                .findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (project.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Cannot change role of project owner");
        }

        ProjectRole newRole = projectRoleRepository.findByName(request.getRole().trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Project role not found"));

        ProjectRole currentRole = member.getProjectRole();

        if (currentRole != null && currentRole.getId().equals(newRole.getId())) {
            return ProjectMemberMapper.toResponse(member);
        }

        if (currentRole != null && PermissionConstants.PROJECT_MANAGER.equals(currentRole.getName())
                && !PermissionConstants.PROJECT_MANAGER.equals(newRole.getName())) {

            long managerCount = projectMemberRepository
                    .countByProject_IdAndProjectRole_Name(projectId, PermissionConstants.PROJECT_MANAGER);

            if (managerCount <= 1) {
                throw new BadRequestException("Cannot downgrade the last PROJECT_MANAGER");
            }
        }

        member.setProjectRole(newRole);

        ProjectMember updated = projectMemberRepository.save(member);

        permissionService.evictUserProjectPermissions(userId, projectId);

        return ProjectMemberMapper.toResponse(updated);
    }

    @Override
    public ProjectMemberResponse getMember(Long projectId, Long userId) {

        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        return ProjectMemberMapper.toResponse(member);
    }

    @Override
    public void leaveProject(Long projectId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateOwnerRemoval(projectId, user.getId());
        validateLastManagerRemoval(projectId, user.getId());

        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, user.getId())
                .orElseThrow(() -> new BadRequestException("You are not a member"));

        projectMemberRepository.delete(member);

        permissionService.evictUserProjectPermissions(user.getId(), projectId);
    }

    @Override
    public List<ProjectMemberResponse> addMultipleMembers(Long projectId,
                                                  AddMultipleMembersRequest request) {
        return request.getMembers().stream()
                .map(memberRequest -> addMemberToProject(projectId, memberRequest))
                .toList();
    }

    @Override
    public List<ProjectMemberResponse> filterMembers(Long projectId, ProjectMemberFilterRequest filter) {
        return projectMemberRepository.findByProject_Id(projectId).stream()
                .filter(member -> {
                    boolean matchesRole = true;
                    boolean matchesSearch = true;

                    if (filter.getRole() != null && !filter.getRole().isBlank()) {
                        String roleName = filter.getRole().trim().toUpperCase();
                        ProjectRole memberRole = member.getProjectRole();
                        matchesRole = memberRole != null && roleName.equals(memberRole.getName());
                    }

                    if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                        String keyword = filter.getSearch().toLowerCase();
                        matchesSearch = member.getUser().getName().toLowerCase().contains(keyword) ||
                                member.getUser().getEmail().toLowerCase().contains(keyword);
                    }

                    return matchesRole && matchesSearch;
                })
                .map(ProjectMemberMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProjectMemberResponse inviteAssigneeByEmail(Long projectId, String email, Long inviterUserId) {

        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();

        Long workspaceId = resolveWorkspaceId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (project.getWorkspace() == null || !project.getWorkspace().getId().equals(workspaceId)) {
            throw new BadRequestException("Project does not belong to the current workspace");
        }

        User inviter = userRepository.findById(inviterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Inviter user not found"));

        boolean userAlreadyExisted = userRepository.existsByEmail(normalizedEmail);

        User assignee = userRepository.findByEmail(normalizedEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setName(deriveDisplayNameFromEmail(normalizedEmail));
            newUser.setEmail(normalizedEmail);
            newUser.setUsername(normalizedEmail);
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setActive(false);
            newUser.setEmailVerified(false);
            newUser.setWorkspaceOwner(false);
            return userRepository.save(newUser);
        });

        if (!workspaceMemberRepository.existsByWorkspaceIdAndUserIdAndActiveTrue(workspaceId, assignee.getId())) {
            WorkspaceMember workspaceMember = new WorkspaceMember();
            workspaceMember.setWorkspace(project.getWorkspace());
            workspaceMember.setUser(assignee);
            workspaceMember.setWorkspaceRole("WORKSPACE_MEMBER");
            workspaceMember.setInvitedBy(inviter);
            workspaceMember.setActive(true);
            workspaceMemberRepository.save(workspaceMember);
        }

        if (projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, assignee.getId())
                .isPresent()) {
            throw new ConflictException("User is already a member of this project");
        }

        ProjectRole teamMemberRole = projectRoleRepository.findByName(PermissionConstants.TEAM_MEMBER)
                .orElseThrow(() -> new ResourceNotFoundException("Project role not found: TEAM_MEMBER"));

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setUser(assignee);
        member.setProjectRole(teamMemberRole);

        ProjectMember savedMember = projectMemberRepository.save(member);

        sendAssigneeInvitationEmail(assignee, inviter, project, userAlreadyExisted);

        try {
            Map<String, Object> newValue = new HashMap<>();
            newValue.put("userId", assignee.getId());
            newValue.put("email", assignee.getEmail());
            newValue.put("projectRole", teamMemberRole.getName());
            newValue.put("invitedBy", inviter.getId());
            newValue.put("accountCreated", !userAlreadyExisted);

            activityLogService.logActivity(
                    inviter.getId(),
                    EntityType.MEMBER,
                    savedMember.getId(),
                    ActionType.MEMBER_ADDED,
                    null,
                    newValue,
                    inviter.getName() + " invited " + assignee.getEmail() + " to " + project.getName(),
                    projectId
            );
        } catch (Exception ignored) {
        }

        return ProjectMemberMapper.toResponse(savedMember);
    }

    private String deriveDisplayNameFromEmail(String email) {
        int at = email.indexOf('@');
        String localPart = at > 0 ? email.substring(0, at) : email;
        String[] parts = localPart.split("[._\\-]+");
        StringBuilder name = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            name.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.length() > 1 ? part.substring(1) : "")
                    .append(' ');
        }
        String result = name.toString().trim();
        return result.isEmpty() ? email : result;
    }

    private void sendAssigneeInvitationEmail(User assignee, User inviter, Project project, boolean userAlreadyExisted) {
        try {
            String subject = userAlreadyExisted
                    ? "You've been added to " + project.getName()
                    : "You're invited to " + project.getName() + " on PMT-SK";

            String inviterName = inviter.getName() == null || inviter.getName().isBlank()
                    ? inviter.getEmail()
                    : inviter.getName();

            String projectName = project.getName() == null ? "the project" : project.getName();

            String body;
            if (userAlreadyExisted) {
                body = "<h2 style='color:#1f2937; margin:0 0 16px 0;'>You've been added to a project</h2>"
                        + "<p style='color:#4b5563; line-height:1.6; margin:0 0 12px 0;'>"
                        + "<strong>" + inviterName + "</strong> has added you as an assignee on the project "
                        + "<strong>" + projectName + "</strong>.</p>"
                        + "<p style='color:#4b5563; line-height:1.6; margin:0 0 12px 0;'>"
                        + "Open PMT-SK to view your new task and start collaborating.</p>";
            } else {
                String registerUrl = baseUrl + "/register?email=" + assignee.getEmail();
                body = "<h2 style='color:#1f2937; margin:0 0 16px 0;'>You've been invited to a project</h2>"
                        + "<p style='color:#4b5563; line-height:1.6; margin:0 0 12px 0;'>"
                        + "<strong>" + inviterName + "</strong> has invited you to collaborate on the project "
                        + "<strong>" + projectName + "</strong> on PMT-SK.</p>"
                        + "<p style='color:#4b5563; line-height:1.6; margin:0 0 12px 0;'>"
                        + "An account has been created for you. Click the button below to set your password and activate it. "
                        + "Once you sign in, you'll see the project waiting for you.</p>"
                        + "<div style='text-align:center; margin:24px 0;'>"
                        + "<a href='" + registerUrl + "' "
                        + "style='display:inline-block; padding:12px 24px; background:linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); "
                        + "color:white; text-decoration:none; border-radius:8px; font-size:15px; font-weight:bold;'>"
                        + "Activate my account</a></div>"
                        + "<p style='color:#6b7280; font-size:13px; line-height:1.6; margin:0 0 4px 0;'>"
                        + "If the button doesn't work, copy and paste this link:</p>"
                        + "<p style='color:#4f46e5; font-size:13px; word-break:break-all; margin:0;'>"
                        + registerUrl + "</p>";
            }

            emailService.sendSimpleEmail(assignee.getEmail(), subject, body);
        } catch (Exception e) {
            // Don't fail the whole flow if email send fails — log and move on.
        }
    }

    private void validateLastManagerRemoval(Long projectId, Long userId) {

        ProjectMember member = projectMemberRepository
                .findByProject_IdAndUser_Id(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        ProjectRole role = member.getProjectRole();
        if (role != null && PermissionConstants.PROJECT_MANAGER.equals(role.getName())) {

            long managerCount = projectMemberRepository
                    .countByProject_IdAndProjectRole_Name(projectId, PermissionConstants.PROJECT_MANAGER);

            if (managerCount <= 1) {
                throw new BadRequestException("Project must have at least one PROJECT_MANAGER");
            }
        }
    }

    private void validateOwnerRemoval(Long projectId, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (project.getOwner().getId().equals(userId)) {
            throw new BadRequestException("Project owner cannot be removed");
        }
    }

    private Long resolveWorkspaceId() {
        Long workspaceId = currentWorkspaceResolver.getCurrentWorkspaceId();
        if (workspaceId == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                workspaceId = currentWorkspaceResolver.resolveDefaultWorkspace(userDetails.getUserId())
                        .map(Workspace::getId)
                        .orElseThrow(() -> new BadRequestException("No workspace context available"));
            } else {
                throw new BadRequestException("No workspace context available");
            }
        }
        return workspaceId;
    }
}



