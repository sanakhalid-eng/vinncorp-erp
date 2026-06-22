package com.vinncorp.erp.integration;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.AbstractIntegrationTest;
import com.vinncorp.erp.modules.projects.dto.request.CreateProjectRequest;
import com.vinncorp.erp.modules.projects.dto.request.TimeLogRequest;
import com.vinncorp.erp.modules.projects.dto.response.ProjectResponse;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.workflow.entity.WorkflowStatus;
import com.vinncorp.erp.shared.exception.BadRequestException;
import com.vinncorp.erp.shared.exception.CustomAccessDeniedException;
import com.vinncorp.erp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CrossWorkspaceIsolationIntegrationTest extends AbstractIntegrationTest {

    private Workspace workspaceB;
    private Project projectB;
    private Task taskA;
    private Task taskB;
    private WorkflowStatus statusForB;

    @BeforeEach
    void setUp() {
        timeLogRepository.deleteAll();
        activeTimerRepository.deleteAll();
        taskRepository.deleteAll();
        sprintRepository.deleteAll();
        taskSprintRepository.deleteAll();
        taskDependencyRepository.deleteAll();
        projectMemberRepository.deleteAll();

        // Workspace B: only secondAdmin is a member
        workspaceB = new Workspace();
        workspaceB.setName("Workspace B");
        workspaceB.setSlug("workspace-b");
        workspaceB.setActive(true);
        workspaceB.setCreatedAt(LocalDateTime.now());
        workspaceB = workspaceRepository.save(workspaceB);

        WorkspaceMember wmB = new WorkspaceMember();
        wmB.setWorkspace(workspaceB);
        wmB.setUser(secondAdmin);
        wmB.setWorkspaceRole("WORKSPACE_OWNER");
        wmB.setJoinedAt(LocalDateTime.now());
        wmB.setActive(true);
        workspaceMemberRepository.save(wmB);

        // Project B in workspace B
        projectB = new Project();
        projectB.setName("Project B");
        projectB.setDescription("Project in workspace B");
        projectB.setOwner(secondAdmin);
        projectB.setWorkspace(workspaceB);
        projectB.setActive(true);
        projectB.setCreatedAt(LocalDateTime.now());
        projectB = projectRepository.save(projectB);

        // Statuses for Workspace B project
        statusForB = new WorkflowStatus();
        statusForB.setName("TODO");
        statusForB.setDefault(true);
        statusForB.setColor("#6B7280");
        statusForB.setPosition(0);
        statusForB.setProject(projectB);
        statusForB = workflowStatusRepository.save(statusForB);

        WorkflowStatus doneForB = new WorkflowStatus();
        doneForB.setName("DONE");
        doneForB.setDefault(false);
        doneForB.setColor("#10B981");
        doneForB.setPosition(1);
        doneForB.setProject(projectB);
        workflowStatusRepository.save(doneForB);

        // Project members for project B
        ProjectMember pmB = new ProjectMember();
        pmB.setProject(projectB);
        pmB.setUser(secondAdmin);
        pmB.setProjectRole(projectManagerRole);
        pmB.setIsActive(true);
        projectMemberRepository.save(pmB);

        // Recreate project A members (deleted above)
        ProjectMember pmA1 = new ProjectMember();
        pmA1.setProject(testProject);
        pmA1.setUser(adminUser);
        pmA1.setProjectRole(projectManagerRole);
        pmA1.setIsActive(true);
        projectMemberRepository.save(pmA1);

        ProjectMember pmA2 = new ProjectMember();
        pmA2.setProject(testProject);
        pmA2.setUser(normalUser);
        pmA2.setProjectRole(teamMemberRole);
        pmA2.setIsActive(true);
        projectMemberRepository.save(pmA2);

        // Create tasks using existing statuses from setUpBase
        taskA = new Task();
        taskA.setTitle("Task A in Workspace A");
        taskA.setProject(testProject);
        taskA.setCreatedBy(adminUser.getId());
        taskA.setAssignee(adminUser);
        taskA.setStatusEntity(defaultStatus);
        taskA = taskRepository.save(taskA);

        taskB = new Task();
        taskB.setTitle("Task B in Workspace B");
        taskB.setProject(projectB);
        taskB.setCreatedBy(secondAdmin.getId());
        taskB.setAssignee(secondAdmin);
        taskB.setStatusEntity(statusForB);
        taskB = taskRepository.save(taskB);
    }

    // ========== Helper: set auth + workspace context ==========

    private void authenticateAs(Long userId, String email, Long workspaceId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        email, "pass",
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                )
        );
        currentWorkspaceResolver.setCurrentWorkspace(workspaceId);
    }

    private void clearContext() {
        SecurityContextHolder.clearContext();
        defaultWorkspaceResolver.clear();
    }

    // ========== Project Isolation ==========

    @Test
    void getProjectById_fromWrongWorkspace_shouldFail() {
        authenticateAs(normalUser.getId(), "user@test.com", workspaceB.getId());

        assertThrows(BadRequestException.class, () ->
                projectServiceImpl.getProjectById(testProject.getId()));
    }

    @Test
    void getAllProjects_shouldOnlyReturnCurrentWorkspaceProjects() {
        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        List<ProjectResponse> projects =
                projectServiceImpl.getAllProjects();

        assertEquals(1, projects.size());
        assertEquals("Project B", projects.get(0).getName());
    }

    @Test
    void getProjectsForUser_shouldOnlyReturnCurrentWorkspaceProjects() {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        List<ProjectResponse> projects =
                projectServiceImpl.getProjectsForUser(adminUser.getEmail());

        assertTrue(projects.stream().allMatch(p -> p.getName().equals("Test Project")));
    }

    @Test
    void createProject_shouldAssignToCurrentWorkspace() {
        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project in B");
        request.setDescription("Should be in workspace B");

        var response = projectServiceImpl.createProject(request, secondAdmin.getEmail());

        assertNotNull(response);
        Project saved = projectRepository.findById(response.getId()).orElseThrow();
        assertEquals(workspaceB.getId(), saved.getWorkspace().getId());
    }

    @Test
    void updateProject_fromWrongWorkspace_shouldFail() {
        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Hacked Name");

        assertThrows(BadRequestException.class, () ->
                projectServiceImpl.updateProject(testProject.getId(), request));
    }

    // ========== Task Isolation ==========

    @Test
    void getTaskTimeLogs_fromTaskInWrongWorkspace_shouldFail() {
        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        assertThrows(BadRequestException.class, () ->
                timeTrackingService.getTaskTimeLogs(taskA.getId()));
    }

    @Test
    void logTime_onTaskFromWrongWorkspace_shouldFail() {
        authenticateAs(normalUser.getId(), "user@test.com", testWorkspace.getId());

        TimeLogRequest request = new TimeLogRequest();
        request.setHours(BigDecimal.valueOf(2));
        request.setDescription("Test");

        assertThrows(BadRequestException.class, () ->
                timeTrackingService.logTime(taskB.getId(), normalUser.getId(), request));
    }

    @Test
    void startTimer_onTaskFromWrongWorkspace_shouldFail() {
        authenticateAs(normalUser.getId(), "user@test.com", workspaceB.getId());

        assertThrows(BadRequestException.class, () ->
                timeTrackingService.startTimer(taskA.getId(), normalUser.getId(), "Should fail"));
    }

    // ========== TimeLog Workspace Scoping ==========

    @Test
    void logTime_shouldSetWorkspaceOnTimeLog() {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        TimeLogRequest request = new TimeLogRequest();
        request.setHours(BigDecimal.valueOf(3));
        request.setDescription("Test log");
        request.setLogDate(LocalDate.now());

        TimeLog timeLog = timeTrackingService.logTime(taskA.getId(), adminUser.getId(), request);

        assertNotNull(timeLog.getWorkspace());
        assertEquals(testWorkspace.getId(), timeLog.getWorkspace().getId());
    }

    @Test
    void getUserTimeLogs_shouldOnlyReturnCurrentWorkspaceLogs() {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        TimeLogRequest request = new TimeLogRequest();
        request.setHours(BigDecimal.valueOf(1.5));
        request.setDescription("Log in workspace A");
        request.setLogDate(LocalDate.now());
        timeTrackingService.logTime(taskA.getId(), adminUser.getId(), request);

        // Switch to workspace B
        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        List<TimeLog> logsInB = timeTrackingService.getUserTimeLogs(adminUser.getId());

        assertTrue(logsInB.isEmpty());
    }

    @Test
    void updateTimeLog_fromWrongWorkspace_shouldFail() {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        TimeLogRequest request = new TimeLogRequest();
        request.setHours(BigDecimal.valueOf(2));
        request.setDescription("Original");
        request.setLogDate(LocalDate.now());
        TimeLog timeLog = timeTrackingService.logTime(taskA.getId(), adminUser.getId(), request);

        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        TimeLogRequest updateReq = new TimeLogRequest();
        updateReq.setHours(BigDecimal.valueOf(5));

        assertThrows(BadRequestException.class, () ->
                timeTrackingService.updateTimeLog(timeLog.getId(), adminUser.getId(), updateReq));
    }

    @Test
    void deleteTimeLog_fromWrongWorkspace_shouldFail() {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        TimeLogRequest request = new TimeLogRequest();
        request.setHours(BigDecimal.valueOf(1));
        request.setDescription("To delete");
        request.setLogDate(LocalDate.now());
        TimeLog timeLog = timeTrackingService.logTime(taskA.getId(), adminUser.getId(), request);

        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        assertThrows(BadRequestException.class, () ->
                timeTrackingService.deleteTimeLog(timeLog.getId(), adminUser.getId(), true));
    }

    @Test
    void getUserTimesheet_shouldOnlyReturnCurrentWorkspaceData() {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        TimeLogRequest request = new TimeLogRequest();
        request.setHours(BigDecimal.valueOf(2));
        request.setDescription("Timesheet entry");
        request.setLogDate(LocalDate.now());
        timeTrackingService.logTime(taskA.getId(), adminUser.getId(), request);

        // Switch workspace
        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        List<Object[]> timesheet = timeTrackingService.getUserTimesheet(
                adminUser.getId(), LocalDate.now().minusDays(7), LocalDate.now().plusDays(1));

        assertTrue(timesheet.isEmpty());
    }

    // ========== TenantAccessValidator Isolation ==========

    @Test
    void validateWorkspaceAccess_userNotInWorkspace_shouldThrow() {
        clearContext();
        currentWorkspaceResolver.setCurrentWorkspace(workspaceB.getId());

        assertThrows(CustomAccessDeniedException.class, () ->
                tenantAccessValidator.validateWorkspaceAccess(workspaceB.getId(), normalUser.getId()));
    }

    @Test
    void validateProjectWorkspace_projectFromDifferentWorkspace_shouldThrow() {
        clearContext();
        currentWorkspaceResolver.setCurrentWorkspace(workspaceB.getId());

        assertThrows(CustomAccessDeniedException.class, () ->
                tenantAccessValidator.validateProjectWorkspace(testProject.getId()));
    }

    @Test
    void requireWorkspaceAccess_nonMemberAccessingWorkspace_shouldThrow() {
        clearContext();

        assertThrows(CustomAccessDeniedException.class, () ->
                tenantAccessValidator.requireWorkspaceAccess(workspaceB.getId(), normalUser.getId()));
    }

    @Test
    void requireWorkspaceAccess_nonexistentWorkspace_shouldThrow404() {
        clearContext();

        assertThrows(ResourceNotFoundException.class, () ->
                tenantAccessValidator.requireWorkspaceAccess(99999L, adminUser.getId()));
    }

    // ========== Cross-Workspace TimeLog Ownership ==========

    @Test
    void stopTimer_shouldSetWorkspaceOnTimeLog() throws Exception {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        timeTrackingService.startTimer(taskA.getId(), adminUser.getId(), "Timer test");
        Thread.sleep(1100);
        TimeLog timeLog = timeTrackingService.stopTimer(adminUser.getId(), "Stopped");

        assertNotNull(timeLog.getWorkspace());
        assertEquals(testWorkspace.getId(), timeLog.getWorkspace().getId());
    }

    @Test
    void getTaskTimeLogs_correctWorkspace_shouldSucceed() {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        TimeLogRequest request = new TimeLogRequest();
        request.setHours(BigDecimal.valueOf(1));
        request.setDescription("Test");
        request.setLogDate(LocalDate.now());
        timeTrackingService.logTime(taskA.getId(), adminUser.getId(), request);

        List<TimeLog> logs = timeTrackingService.getTaskTimeLogs(taskA.getId());

        assertEquals(1, logs.size());
    }

    // ========== Workspace Context Switch Works Correctly ==========

    @Test
    void switchingWorkspaces_shouldIsolateQueries() {
        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        TimeLogRequest request = new TimeLogRequest();
        request.setHours(BigDecimal.valueOf(1));
        request.setDescription("In workspace A");
        request.setLogDate(LocalDate.now());
        timeTrackingService.logTime(taskA.getId(), adminUser.getId(), request);

        assertEquals(1, timeTrackingService.getUserTimeLogs(adminUser.getId()).size());

        authenticateAs(secondAdmin.getId(), "admin2@test.com", workspaceB.getId());

        assertTrue(timeTrackingService.getUserTimeLogs(adminUser.getId()).isEmpty());

        TimeLogRequest requestB = new TimeLogRequest();
        requestB.setHours(BigDecimal.valueOf(2));
        requestB.setDescription("In workspace B");
        requestB.setLogDate(LocalDate.now());
        timeTrackingService.logTime(taskB.getId(), secondAdmin.getId(), requestB);

        assertEquals(1, timeTrackingService.getUserTimeLogs(secondAdmin.getId()).size());

        authenticateAs(adminUser.getId(), "admin@test.com", testWorkspace.getId());

        assertEquals(1, timeTrackingService.getUserTimeLogs(adminUser.getId()).size());
        assertEquals(0, timeTrackingService.getUserTimeLogs(secondAdmin.getId()).size());
    }
}

