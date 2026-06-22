package com.vinncorp.erp.modules.projects.service;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.modules.projects.entity.SearchResult;
import com.vinncorp.erp.modules.projects.entity.SearchResult.SearchHit;
import com.vinncorp.erp.modules.projects.entity.Comment;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.hr.entity.Department;
import com.vinncorp.erp.modules.hr.entity.Employee;
import com.vinncorp.erp.modules.hr.entity.Designation;
import com.vinncorp.erp.shared.security.MembershipResolver;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final int MAX_PER_TYPE = 5;

    private final EntityManager entityManager;
    private final CurrentWorkspaceResolver currentWorkspaceResolver;
    private final MembershipResolver membershipResolver;

    public SearchResult search(String query, Long workspaceId) {
        if (query == null || query.isBlank()) {
            return emptyResult();
        }

        String rawQuery = query.trim().toLowerCase();
        String exactPattern = rawQuery;
        String likePattern = "%" + rawQuery + "%";

        List<SearchHit> projects = searchProjects(exactPattern, likePattern, workspaceId);
        List<SearchHit> tasks = searchTasks(exactPattern, likePattern, workspaceId);
        List<SearchHit> members = searchMembers(exactPattern, likePattern, workspaceId);
        List<SearchHit> comments = searchComments(likePattern, workspaceId);
        List<SearchHit> employees = searchEmployees(exactPattern, likePattern, workspaceId);
        List<SearchHit> departments = searchDepartments(exactPattern, likePattern, workspaceId);

        // SUPER_ADMIN can search across all workspaces
        List<SearchHit> workspaces = searchWorkspacesForSuperAdmin(exactPattern, likePattern);

        return SearchResult.builder()
                .tasks(tasks)
                .projects(projects)
                .members(members)
                .comments(comments)
                .employees(employees)
                .departments(departments)
                .workspaces(workspaces)
                .build();
    }

    private List<SearchHit> searchProjects(String exactPattern, String likePattern, Long workspaceId) {
        List<Project> exact = entityManager
                .createQuery(
                        "SELECT p FROM Project p WHERE p.workspace.id = :wsId AND p.deletedAt IS NULL " +
                        "AND LOWER(p.name) = :exact ORDER BY p.updatedAt DESC", Project.class)
                .setParameter("wsId", workspaceId)
                .setParameter("exact", exactPattern)
                .setMaxResults(1)
                .getResultList();

        long exactCount = exact.size();
        List<Project> fuzzy = entityManager
                .createQuery(
                        "SELECT p FROM Project p WHERE p.workspace.id = :wsId AND p.deletedAt IS NULL " +
                        "AND LOWER(p.name) LIKE :q AND LOWER(p.name) != :exact " +
                        "ORDER BY p.updatedAt DESC", Project.class)
                .setParameter("wsId", workspaceId)
                .setParameter("q", likePattern)
                .setParameter("exact", exactPattern)
                .setMaxResults(MAX_PER_TYPE - (int) exactCount)
                .getResultList();

        return Stream.concat(exact.stream(), fuzzy.stream())
                .map(p -> SearchHit.builder()
                        .id(p.getId())
                        .type("project")
                        .title(p.getName())
                        .subtitle(p.getDescription() != null ? truncate(p.getDescription(), 80) : "")
                        .url("/w/" + workspaceId + "/projects/" + p.getId())
                        .badge(p.getPriority() != null ? p.getPriority().name() : "")
                        .build())
                .collect(Collectors.toList());
    }

    private List<SearchHit> searchTasks(String exactPattern, String likePattern, Long workspaceId) {
        List<Task> exact = entityManager
                .createQuery(
                        "SELECT t FROM Task t WHERE t.project.workspace.id = :wsId AND t.deletedAt IS NULL " +
                        "AND LOWER(t.title) = :exact ORDER BY t.updatedAt DESC", Task.class)
                .setParameter("wsId", workspaceId)
                .setParameter("exact", exactPattern)
                .setMaxResults(1)
                .getResultList();

        long exactCount = exact.size();
        List<Task> fuzzy = entityManager
                .createQuery(
                        "SELECT t FROM Task t WHERE t.project.workspace.id = :wsId AND t.deletedAt IS NULL " +
                        "AND LOWER(t.title) LIKE :q AND LOWER(t.title) != :exact " +
                        "ORDER BY t.updatedAt DESC", Task.class)
                .setParameter("wsId", workspaceId)
                .setParameter("q", likePattern)
                .setParameter("exact", exactPattern)
                .setMaxResults(MAX_PER_TYPE - (int) exactCount)
                .getResultList();

        return Stream.concat(exact.stream(), fuzzy.stream())
                .map(t -> SearchHit.builder()
                        .id(t.getId())
                        .type("task")
                        .title(t.getTitle())
                        .subtitle(t.getProject() != null ? t.getProject().getName() : "")
                        .url("/w/" + workspaceId + "/tasks")
                        .badge(t.getPriority() != null ? t.getPriority().name() : "")
                        .build())
                .collect(Collectors.toList());
    }

    private List<SearchHit> searchMembers(String exactPattern, String likePattern, Long workspaceId) {
        List<WorkspaceMember> exact = entityManager
                .createQuery(
                        "SELECT wm FROM WorkspaceMember wm JOIN FETCH wm.user u " +
                        "WHERE wm.workspace.id = :wsId AND wm.active = true " +
                        "AND (LOWER(u.name) = :exact OR LOWER(u.email) = :exact)", WorkspaceMember.class)
                .setParameter("wsId", workspaceId)
                .setParameter("exact", exactPattern)
                .setMaxResults(1)
                .getResultList();

        long exactCount = exact.size();
        List<WorkspaceMember> fuzzy = entityManager
                .createQuery(
                        "SELECT wm FROM WorkspaceMember wm JOIN FETCH wm.user u " +
                        "WHERE wm.workspace.id = :wsId AND wm.active = true " +
                        "AND (LOWER(u.name) LIKE :q OR LOWER(u.email) LIKE :q) " +
                        "AND LOWER(u.name) != :exact AND LOWER(u.email) != :exact " +
                        "ORDER BY u.name", WorkspaceMember.class)
                .setParameter("wsId", workspaceId)
                .setParameter("q", likePattern)
                .setParameter("exact", exactPattern)
                .setMaxResults(MAX_PER_TYPE - (int) exactCount)
                .getResultList();

        return Stream.concat(exact.stream(), fuzzy.stream())
                .map(m -> SearchHit.builder()
                        .id(m.getUser().getId())
                        .type("member")
                        .title(m.getUser().getName())
                        .subtitle(m.getUser().getEmail())
                        .url("/w/" + workspaceId + "/members")
                        .avatar(m.getUser().getAvatarUrl())
                        .badge(m.getWorkspaceRole())
                        .build())
                .collect(Collectors.toList());
    }

    private List<SearchHit> searchComments(String likePattern, Long workspaceId) {
        List<Comment> results = entityManager
                .createQuery(
                        "SELECT c FROM Comment c WHERE c.task.project.workspace.id = :wsId " +
                        "AND LOWER(c.content) LIKE :q ORDER BY c.createdAt DESC", Comment.class)
                .setParameter("wsId", workspaceId)
                .setParameter("q", likePattern)
                .setMaxResults(MAX_PER_TYPE)
                .getResultList();

        return results.stream()
                .map(c -> SearchHit.builder()
                        .id(c.getId())
                        .type("comment")
                        .title(truncate(c.getContent(), 100))
                        .subtitle(c.getUser() != null ? c.getUser().getName() : "")
                        .url("/w/" + workspaceId + "/tasks")
                        .avatar(c.getUser() != null ? c.getUser().getAvatarUrl() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private SearchResult emptyResult() {
        return SearchResult.builder()
                .tasks(List.of())
                .projects(List.of())
                .members(List.of())
                .comments(List.of())
                .employees(List.of())
                .departments(List.of())
                .workspaces(List.of())
                .build();
    }

    private List<SearchHit> searchEmployees(String exactPattern, String likePattern, Long workspaceId) {
        List<Employee> exact = entityManager
                .createQuery(
                        "SELECT e FROM Employee e WHERE e.workspace.id = :wsId " +
                        "AND (LOWER(e.user.name) = :exact OR LOWER(e.employeeCode) = :exact " +
                        "OR LOWER(e.workEmail) = :exact) ORDER BY e.user.name", Employee.class)
                .setParameter("wsId", workspaceId)
                .setParameter("exact", exactPattern)
                .setMaxResults(1)
                .getResultList();

        long exactCount = exact.size();
        List<Employee> fuzzy = entityManager
                .createQuery(
                        "SELECT e FROM Employee e WHERE e.workspace.id = :wsId " +
                        "AND (LOWER(e.user.name) LIKE :q OR LOWER(e.employeeCode) LIKE :q " +
                        "OR LOWER(e.workEmail) LIKE :q) " +
                        "AND LOWER(e.user.name) != :exact ORDER BY e.user.name", Employee.class)
                .setParameter("wsId", workspaceId)
                .setParameter("q", likePattern)
                .setParameter("exact", exactPattern)
                .setMaxResults(MAX_PER_TYPE - (int) exactCount)
                .getResultList();

        return Stream.concat(exact.stream(), fuzzy.stream())
                .map(e -> SearchHit.builder()
                        .id(e.getId())
                        .type("employee")
                        .title(e.getUser() != null ? e.getUser().getName() : e.getEmployeeCode())
                        .subtitle(e.getJobTitle() != null ? e.getJobTitle() : e.getWorkEmail())
                        .url("/w/" + workspaceId + "/hr/employees/" + e.getId())
                        .avatar(e.getUser() != null ? e.getUser().getAvatarUrl() : null)
                        .badge(e.getStatus() != null ? e.getStatus().name() : "")
                        .build())
                .collect(Collectors.toList());
    }

    private List<SearchHit> searchDepartments(String exactPattern, String likePattern, Long workspaceId) {
        List<Department> exact = entityManager
                .createQuery(
                        "SELECT d FROM Department d WHERE d.workspace.id = :wsId " +
                        "AND LOWER(d.name) = :exact ORDER BY d.name", Department.class)
                .setParameter("wsId", workspaceId)
                .setParameter("exact", exactPattern)
                .setMaxResults(1)
                .getResultList();

        long exactCount = exact.size();
        List<Department> fuzzy = entityManager
                .createQuery(
                        "SELECT d FROM Department d WHERE d.workspace.id = :wsId " +
                        "AND LOWER(d.name) LIKE :q AND LOWER(d.name) != :exact ORDER BY d.name", Department.class)
                .setParameter("wsId", workspaceId)
                .setParameter("q", likePattern)
                .setParameter("exact", exactPattern)
                .setMaxResults(MAX_PER_TYPE - (int) exactCount)
                .getResultList();

        return Stream.concat(exact.stream(), fuzzy.stream())
                .map(d -> SearchHit.builder()
                        .id(d.getId())
                        .type("department")
                        .title(d.getName())
                        .subtitle(d.getDescription() != null ? truncate(d.getDescription(), 80) : "")
                        .url("/w/" + workspaceId + "/hr/departments")
                        .build())
                .collect(Collectors.toList());
    }

    private List<SearchHit> searchWorkspacesForSuperAdmin(String exactPattern, String likePattern) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return List.of();

        String email = auth.getName();
        if (!membershipResolver.isSuperAdmin(email)) return List.of();

        List<Workspace> exact = entityManager
                .createQuery(
                        "SELECT w FROM Workspace w WHERE LOWER(w.name) = :exact ORDER BY w.createdAt DESC", Workspace.class)
                .setParameter("exact", exactPattern)
                .setMaxResults(1)
                .getResultList();

        long exactCount = exact.size();
        List<Workspace> fuzzy = entityManager
                .createQuery(
                        "SELECT w FROM Workspace w WHERE LOWER(w.name) LIKE :q AND LOWER(w.name) != :exact ORDER BY w.createdAt DESC", Workspace.class)
                .setParameter("q", likePattern)
                .setParameter("exact", exactPattern)
                .setMaxResults(MAX_PER_TYPE - (int) exactCount)
                .getResultList();

        return Stream.concat(exact.stream(), fuzzy.stream())
                .map(w -> SearchHit.builder()
                        .id(w.getId())
                        .type("workspace")
                        .title(w.getName())
                        .subtitle(w.getSlug())
                        .url("/w/" + w.getSlug() + "/dashboard")
                        .badge(w.isActive() ? "ACTIVE" : "INACTIVE")
                        .build())
                .collect(Collectors.toList());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}



