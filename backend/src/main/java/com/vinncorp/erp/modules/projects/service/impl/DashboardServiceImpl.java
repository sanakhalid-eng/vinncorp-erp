package com.vinncorp.erp.modules.projects.service.impl;

import com.vinncorp.erp.core.user.constants.PermissionConstants;
import com.vinncorp.erp.core.user.entity.User;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.modules.projects.dto.response.DashboardSummaryResponse;
import com.vinncorp.erp.modules.projects.dto.response.EmployeeDashboardResponse;
import com.vinncorp.erp.modules.projects.entity.CustomUserDetails;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.ProjectMember;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.repository.ProjectMemberRepository;
import com.vinncorp.erp.modules.projects.repository.ProjectRepository;
import com.vinncorp.erp.modules.projects.repository.TaskRepository;
import com.vinncorp.erp.modules.projects.service.DashboardService;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import com.vinncorp.erp.core.user.repository.UserRepository;
import com.vinncorp.erp.core.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.hr.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public DashboardSummaryResponse getDashboardSummary(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long workspaceId = resolveWorkspaceId();

        List<Project> ownedProjects = projectRepository.findByWorkspaceIdAndOwner_Email(workspaceId, email);
        List<Project> memberProjects = projectRepository.findDistinctByWorkspaceIdAndMembers_User_Email(workspaceId, email);

        Map<Long, Project> projectMap = new LinkedHashMap<>();
        ownedProjects.forEach(project -> projectMap.put(project.getId(), project));
        memberProjects.forEach(project -> projectMap.put(project.getId(), project));

        List<Project> projects = new ArrayList<>(projectMap.values());
        List<Long> projectIds = projects.stream().map(Project::getId).toList();

        List<Task> projectTasks = projectIds.isEmpty()
                ? List.of()
                : taskRepository.findByProjectIdIn(projectIds);

        List<Task> myTasks = taskRepository.findByAssignee_Id(user.getId());
        List<Task> recentTasks = taskRepository.findTop5ByAssignee_Id(
                user.getId(),
                Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("createdAt"))
        );

        List<ProjectMember> allMembers = projectIds.isEmpty()
                ? List.of()
                : projectMemberRepository.findByProject_IdIn(projectIds);

        Map<Long, Long> memberCountByProject = allMembers.stream()
                .filter(member -> member.getProject() != null && member.getUser() != null)
                .collect(Collectors.groupingBy(
                        member -> member.getProject().getId(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                member -> member.getUser().getId(),
                                Collectors.collectingAndThen(Collectors.toSet(), ids -> (long) ids.size())
                        )
                ));

        Map<String, Long> tasksByPriority = myTasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getPriority() == null ? "UNSPECIFIED" : task.getPriority().name(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        Map<String, Long> tasksByStatus = myTasks.stream()
                .collect(Collectors.groupingBy(
                        task -> task.getStatusEntity() == null || task.getStatusEntity().getName() == null
                                ? "UNSPECIFIED"
                                : task.getStatusEntity().getName(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        Map<String, Long> roleCounts = allMembers.stream()
                .filter(member -> member.getRole() != null && member.getUser() != null)
                .collect(Collectors.groupingBy(
                        member -> member.getRole().getName(),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                member -> member.getUser().getId(),
                                Collectors.collectingAndThen(Collectors.toSet(), ids -> (long) ids.size())
                        )
                ));

        Map<Long, DashboardSummaryResponse.DashboardMemberItem> uniqueMembers = allMembers.stream()
                .filter(member -> member.getUser() != null)
                .collect(Collectors.toMap(
                        member -> member.getUser().getId(),
                        member -> DashboardSummaryResponse.DashboardMemberItem.builder()
                                .id(member.getUser().getId())
                                .name(member.getUser().getName())
                                .email(member.getUser().getEmail())
                                .avatarUrl(member.getUser().getAvatarUrl())
                                .role(member.getProjectRole() != null ? member.getProjectRole().getName()
                                      : member.getRole() != null ? member.getRole().getName()
                                      : PermissionConstants.TEAM_MEMBER)
                                .build(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        long completedTasks = myTasks.stream()
                .filter(task -> isCompletedStatus(task.getStatusEntity() == null ? null : task.getStatusEntity().getName()))
                .count();

        long overdueTasks = myTasks.stream()
                .filter(task -> !isCompletedStatus(task.getStatusEntity() == null ? null : task.getStatusEntity().getName()))
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(LocalDateTime.now()))
                .count();

        LocalDateTime dueSoonThreshold = LocalDateTime.now().plusDays(7);
        long dueSoonTasks = myTasks.stream()
                .filter(task -> !isCompletedStatus(task.getStatusEntity() == null ? null : task.getStatusEntity().getName()))
                .filter(task -> task.getDueDate() != null
                        && !task.getDueDate().isBefore(LocalDateTime.now())
                        && !task.getDueDate().isAfter(dueSoonThreshold))
                .count();

        List<DashboardSummaryResponse.DashboardProjectItem> recentProjects = projects.stream()
                .sorted(Comparator.comparing(Project::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .map(project -> DashboardSummaryResponse.DashboardProjectItem.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .status(project.getStatus() == null ? "UNKNOWN" : project.getStatus().getName())
                        .createdAt(project.getCreatedAt())
                        .endDate(project.getEndDate())
                        .memberCount(memberCountByProject.getOrDefault(project.getId(), 0L).intValue())
                        .build())
                .toList();

        List<DashboardSummaryResponse.DashboardTaskItem> recentTaskItems = recentTasks.stream()
                .map(task -> DashboardSummaryResponse.DashboardTaskItem.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .status(task.getStatusEntity() == null ? "UNSPECIFIED" : task.getStatusEntity().getName())
                        .priority(task.getPriority() == null ? null : task.getPriority().name())
                        .projectId(task.getProject() == null ? null : task.getProject().getId())
                        .projectName(task.getProject() == null ? null : task.getProject().getName())
                        .updatedAt(task.getUpdatedAt() == null ? task.getCreatedAt() : task.getUpdatedAt())
                        .dueDate(task.getDueDate())
                        .build())
                .toList();

        return DashboardSummaryResponse.builder()
                .totalProjects(projects.size())
                .totalTasks(projectTasks.size())
                .myTasks(myTasks.size())
                .completedTasks(completedTasks)
                .overdueTasks(overdueTasks)
                .dueSoonTasks(dueSoonTasks)
                .tasksByPriority(defaultTaskBuckets(tasksByPriority, List.of("LOW", "MEDIUM", "HIGH", "CRITICAL", "UNSPECIFIED")))
                .tasksByStatus(tasksByStatus)
                .roleCounts(roleCounts)
                .recentTasks(recentTaskItems)
                .recentProjects(recentProjects)
                .membersOverview(uniqueMembers.values().stream().limit(8).toList())
                .build();
    }

    @Override
    @Transactional
    public EmployeeDashboardResponse getEmployeeDashboard(String email) {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Long workspaceId = resolveWorkspaceId();

        // Get employee profile
        com.vinncorp.erp.modules.hr.entity.Employee employee = employeeRepository
                .findByUserIdAndWorkspaceId(user.getId(), workspaceId)
                .orElse(null);

        // Get projects
        List<Project> ownedProjects = projectRepository.findByWorkspaceIdAndOwner_Email(workspaceId, email);
        List<Project> memberProjects = projectRepository.findDistinctByWorkspaceIdAndMembers_User_Email(workspaceId, email);

        java.util.Map<Long, Project> projectMap = new java.util.LinkedHashMap<>();
        ownedProjects.forEach(p -> projectMap.put(p.getId(), p));
        memberProjects.forEach(p -> projectMap.put(p.getId(), p));
        List<Project> projects = new java.util.ArrayList<>(projectMap.values());

        // Get tasks
        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        List<Task> myTasks = taskRepository.findByAssignee_Id(user.getId());

        long completedTasks = myTasks.stream()
                .filter(t -> isCompletedStatus(t.getStatusEntity() == null ? null : t.getStatusEntity().getName()))
                .count();
        long overdueTasks = myTasks.stream()
                .filter(t -> !isCompletedStatus(t.getStatusEntity() == null ? null : t.getStatusEntity().getName()))
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDateTime.now()))
                .count();
        long pendingTasks = myTasks.size() - completedTasks;

        Map<String, Long> tasksByStatus = myTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatusEntity() == null || t.getStatusEntity().getName() == null
                                ? "UNSPECIFIED" : t.getStatusEntity().getName(),
                        LinkedHashMap::new, Collectors.counting()));

        Map<String, Long> tasksByPriority = myTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getPriority() == null ? "UNSPECIFIED" : t.getPriority().name(),
                        LinkedHashMap::new, Collectors.counting()));

        List<Task> recentTasks = taskRepository.findTop5ByAssignee_Id(
                user.getId(),
                Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("createdAt")));

        List<EmployeeDashboardResponse.MyProjectItem> projectItems = projects.stream()
                .limit(5)
                .map(p -> EmployeeDashboardResponse.MyProjectItem.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .status(p.getStatus() == null ? "UNKNOWN" : p.getStatus().getName())
                        .updatedAt(p.getUpdatedAt())
                        .build())
                .toList();

        List<EmployeeDashboardResponse.MyTaskItem> taskItems = recentTasks.stream()
                .map(t -> EmployeeDashboardResponse.MyTaskItem.builder()
                        .id(t.getId())
                        .title(t.getTitle())
                        .status(t.getStatusEntity() == null ? "UNSPECIFIED" : t.getStatusEntity().getName())
                        .priority(t.getPriority() == null ? null : t.getPriority().name())
                        .projectName(t.getProject() == null ? null : t.getProject().getName())
                        .dueDate(t.getDueDate())
                        .updatedAt(t.getUpdatedAt() == null ? t.getCreatedAt() : t.getUpdatedAt())
                        .build())
                .toList();

        return EmployeeDashboardResponse.builder()
                .employeeId(employee != null ? employee.getId() : null)
                .employeeCode(employee != null ? employee.getEmployeeCode() : null)
                .fullName(user.getName())
                .workEmail(employee != null ? employee.getWorkEmail() : user.getEmail())
                .jobTitle(employee != null ? employee.getJobTitle() : null)
                .departmentName(null)
                .designationName(null)
                .hireDate(employee != null ? employee.getHireDate() : null)
                .employmentType(employee != null ? employee.getEmploymentType().name() : null)
                .status(employee != null ? employee.getStatus().name() : null)
                .myProjectsCount(projects.size())
                .myTasksCount(myTasks.size())
                .completedTasks(completedTasks)
                .pendingTasks(pendingTasks)
                .overdueTasks(overdueTasks)
                .tasksByStatus(tasksByStatus)
                .tasksByPriority(tasksByPriority)
                .recentProjects(projectItems)
                .recentTasks(taskItems)
                .build();
    }

    private boolean isCompletedStatus(String status) {
        if (status == null) {
            return false;
        }

        String normalized = status.trim().toUpperCase();
        return normalized.contains("DONE")
                || normalized.contains("COMPLETE")
                || normalized.contains("CLOSED")
                || normalized.contains("RESOLVED");
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

    private Map<String, Long> defaultTaskBuckets(Map<String, Long> source, List<String> order) {
        Map<String, Long> result = new LinkedHashMap<>();
        order.forEach(key -> result.put(key, source.getOrDefault(key, 0L)));
        source.forEach(result::putIfAbsent);
        return result;
    }
}



