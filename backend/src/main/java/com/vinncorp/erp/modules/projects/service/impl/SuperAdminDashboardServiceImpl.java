package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.core.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.core.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.core.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import com.vinncorp.erp.modules.projects.dto.response.SuperAdminDashboardResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.service.SuperAdminDashboardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminDashboardServiceImpl implements SuperAdminDashboardService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    @Transactional
    public SuperAdminDashboardResponse getSuperAdminDashboard() {
        long totalWorkspaces = workspaceRepository.count();
        long totalUsers = userRepository.count();
        long totalEmployees = employeeRepository.count();
        long totalProjects = projectRepository.count();

        // Count active workspaces
        long activeWorkspaces = workspaceRepository.findAll().stream()
                .filter(Workspace::isActive)
                .count();

        // Recent workspaces (last 10) - batch load members to avoid N+1
        List<Workspace> recentWorkspaces = workspaceRepository.findAll(
                Sort.by(Sort.Order.desc("createdAt"))
        ).stream().limit(10).toList();

        List<Long> recentWorkspaceIds = recentWorkspaces.stream().map(Workspace::getId).toList();

        // Batch load all members for recent workspaces in one query
        Map<Long, Long> memberCountByWorkspace = recentWorkspaceIds.isEmpty()
                ? Map.of()
                : workspaceMemberRepository.findByWorkspaceIdIn(recentWorkspaceIds).stream()
                        .collect(Collectors.groupingBy(
                                m -> m.getWorkspace().getId(),
                                Collectors.counting()
                        ));

        List<SuperAdminDashboardResponse.WorkspaceSummaryItem> workspaceItems = recentWorkspaces.stream()
                .map(ws -> SuperAdminDashboardResponse.WorkspaceSummaryItem.builder()
                        .id(ws.getId())
                        .name(ws.getName())
                        .slug(ws.getSlug())
                        .active(ws.isActive())
                        .memberCount(memberCountByWorkspace.getOrDefault(ws.getId(), 0L).intValue())
                        .createdAt(ws.getCreatedAt())
                        .build())
                .toList();

        // Recent users (last 10)
        List<User> recentUsers = userRepository.findAll(
                Sort.by(Sort.Order.desc("createdAt"))
        );
        List<SuperAdminDashboardResponse.UserSummaryItem> userItems = recentUsers.stream()
                .limit(10)
                .map(u -> SuperAdminDashboardResponse.UserSummaryItem.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .avatarUrl(u.getAvatarUrl())
                        .isActive(u.isActive())
                        .createdAt(u.getCreatedAt())
                        .build())
                .toList();

        return SuperAdminDashboardResponse.builder()
                .totalWorkspaces(totalWorkspaces)
                .totalUsers(totalUsers)
                .totalEmployees(totalEmployees)
                .totalProjects(totalProjects)
                .activeWorkspaces(activeWorkspaces)
                .recentWorkspaces(workspaceItems)
                .recentUsers(userItems)
                .build();
    }
}
