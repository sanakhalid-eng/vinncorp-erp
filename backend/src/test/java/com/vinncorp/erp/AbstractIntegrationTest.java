package com.vinncorp.erp;

import com.vinncorp.erp.platform.auth.service.AuthService;
import com.vinncorp.erp.platform.user.entity.User;
import com.vinncorp.erp.platform.user.entity.UserRole;
import com.vinncorp.erp.platform.user.repository.RoleRepository;
import com.vinncorp.erp.platform.user.repository.UserRepository;
import com.vinncorp.erp.platform.user.repository.UserRoleRepository;
import com.vinncorp.erp.platform.user.service.RoleService;
import com.vinncorp.erp.platform.user.service.UserRoleService;
import com.vinncorp.erp.platform.user.service.UserService;

import com.vinncorp.erp.platform.workspace.entity.Workspace;
import com.vinncorp.erp.platform.workspace.entity.WorkspaceMember;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceMemberRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceRepository;
import com.vinncorp.erp.platform.workspace.repository.WorkspaceUsageRepository;
import com.vinncorp.erp.platform.workspace.service.CurrentWorkspaceResolver;
import com.vinncorp.erp.platform.workspace.service.impl.DefaultWorkspaceResolver;
import com.vinncorp.erp.modules.projects.controller.SystemController;
import com.vinncorp.erp.modules.projects.entity.*;
import com.vinncorp.erp.modules.projects.entity.WorkflowStatus;
import com.vinncorp.erp.modules.projects.repository.WorkflowStatusRepository;
import com.vinncorp.erp.modules.projects.repository.NotificationRepository;
import com.vinncorp.erp.modules.projects.enums.RoleScope;
import com.vinncorp.erp.modules.projects.enums.SprintStatus;
import com.vinncorp.erp.modules.projects.repository.*;
import com.vinncorp.erp.modules.projects.service.*;
import com.vinncorp.erp.modules.projects.service.impl.ProjectServiceImpl;
import com.vinncorp.erp.shared.tenant.TenantAccessValidator;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import com.vinncorp.erp.config.TestSecurityConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RoleRepository roleRepository;
    @Autowired
    protected UserRoleRepository userRoleRepository;
    @Autowired
    protected ProjectRepository projectRepository;
    @Autowired
    protected ProjectMemberRepository projectMemberRepository;
    @Autowired
    protected ProjectRoleRepository projectRoleRepository;
    @Autowired
    protected TaskRepository taskRepository;
    @Autowired
    protected SprintRepository sprintRepository;
    @Autowired
    protected TaskSprintRepository taskSprintRepository;
    @Autowired
    protected ActiveTimerRepository activeTimerRepository;
    @Autowired
    protected TimeLogRepository timeLogRepository;
    @Autowired
    protected WorkflowStatusRepository workflowStatusRepository;
    @Autowired
    protected TaskDependencyRepository taskDependencyRepository;
    @Autowired
    protected WebhookRepository webhookRepository;
    @Autowired
    protected WebhookDeliveryRepository webhookDeliveryRepository;
    @Autowired
    protected ProjectInvitationRepository projectInvitationRepository;
    @Autowired
    protected ActivityLogRepository activityLogRepository;
    @Autowired
    protected NotificationRepository notificationRepository;
    @Autowired
    protected RetryQueueRepository retryQueueRepository;
    @Autowired
    protected WorkspaceRepository workspaceRepository;
    @Autowired
    protected WorkspaceMemberRepository workspaceMemberRepository;
    @Autowired
    protected WorkspaceUsageRepository workspaceUsageRepository;
    @Autowired
    protected EntityManager entityManager;

    // Services under test
    @Autowired
    protected CurrentWorkspaceResolver currentWorkspaceResolver;
    @Autowired
    protected DefaultWorkspaceResolver defaultWorkspaceResolver;
    @Autowired
    protected TenantAccessValidator tenantAccessValidator;
    @Autowired
    protected ProjectService projectService;
    @Autowired
    protected ProjectServiceImpl projectServiceImpl;
    @Autowired
    protected DashboardService dashboardService;
    @Autowired
    protected ActivityLogService activityLogService;
    @Autowired
    protected InvitationService invitationService;
    @Autowired
    protected SystemController systemController;
    @Autowired
    protected UserRoleService userRoleService;
    @Autowired
    protected RoleService roleService;
    @Autowired
    protected TimeTrackingService timeTrackingService;
    @Autowired
    protected SprintService sprintService;
    @Autowired
    protected TaskDependencyService taskDependencyService;
    @Autowired
    protected TaskService taskService;
    @Autowired
    protected UserService userService;
    @Autowired
    protected AuthService authService;
    @Autowired
    protected SlackService slackService;
    @Autowired
    protected WebhookService webhookService;
    @Autowired
    protected RetryService retryService;

    // Test data
    protected User adminUser;
    protected User normalUser;
    protected User secondAdmin;
    protected Workspace testWorkspace;
    protected Project testProject;
    protected ProjectRole projectManagerRole;
    protected ProjectRole teamMemberRole;
    protected WorkflowStatus defaultStatus;
    protected WorkflowStatus doneStatus;

    @BeforeEach
    void setUpBase() {
        deleteAllData();
        seedRolesAndPermissions();
        seedUsers();
        seedWorkspace();
        seedProjectRoles();
        seedWorkflowStatuses();
        seedProject();
        currentWorkspaceResolver.setCurrentWorkspace(testWorkspace.getId());
    }

    private void deleteAllData() {
        notificationRepository.deleteAll();
        activityLogRepository.deleteAll();
        webhookDeliveryRepository.deleteAll();
        retryQueueRepository.deleteAll();
        webhookRepository.deleteAll();
        taskDependencyRepository.deleteAll();
        taskSprintRepository.deleteAll();
        activeTimerRepository.deleteAll();
        timeLogRepository.deleteAll();
        sprintRepository.deleteAll();
        taskRepository.deleteAll();
        projectMemberRepository.deleteAll();
        projectInvitationRepository.deleteAll();
        // Clear project.status references before deleting workflow_status (bidirectional FK)
        projectRepository.findAll().forEach(p -> {
            p.setStatus(null);
            projectRepository.save(p);
        });
        workflowStatusRepository.deleteAll();
        projectRepository.deleteAll();
        workspaceMemberRepository.deleteAll();
        workspaceUsageRepository.deleteAll();
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        workspaceRepository.deleteAll();
    }

    private void seedRolesAndPermissions() {
        if (roleRepository.findByName("ADMIN").isPresent()) return;

        Role admin = new Role();
        admin.setName("ADMIN");
        admin.setDescription("Admin");
        admin.setScope(RoleScope.SYSTEM);
        admin.setSystemRole(true);
        admin.setEditable(false);
        roleRepository.save(admin);

        Role user = new Role();
        user.setName("USER");
        user.setDescription("User");
        user.setScope(RoleScope.SYSTEM);
        user.setSystemRole(true);
        user.setEditable(false);
        roleRepository.save(user);
    }

    private void seedUsers() {
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("encoded-pass");
        adminUser.setWorkspaceOwner(true);
        adminUser.setActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser = userRepository.save(adminUser);

        Role adminRole = roleRepository.findByName("ADMIN").orElseThrow();
        UserRole ur1 = new UserRole();
        ur1.setUser(adminUser);
        ur1.setRole(adminRole);
        userRoleRepository.save(ur1);

        normalUser = new User();
        normalUser.setName("Normal User");
        normalUser.setEmail("user@test.com");
        normalUser.setPassword("encoded-pass");
        normalUser.setWorkspaceOwner(false);
        normalUser.setActive(true);
        normalUser.setCreatedAt(LocalDateTime.now());
        normalUser = userRepository.save(normalUser);

        Role userRole = roleRepository.findByName("USER").orElseThrow();
        UserRole ur2 = new UserRole();
        ur2.setUser(normalUser);
        ur2.setRole(userRole);
        userRoleRepository.save(ur2);

        secondAdmin = new User();
        secondAdmin.setName("Second Admin");
        secondAdmin.setEmail("admin2@test.com");
        secondAdmin.setPassword("encoded-pass");
        secondAdmin.setWorkspaceOwner(false);
        secondAdmin.setActive(true);
        secondAdmin.setCreatedAt(LocalDateTime.now());
        secondAdmin = userRepository.save(secondAdmin);

        UserRole ur3 = new UserRole();
        ur3.setUser(secondAdmin);
        ur3.setRole(adminRole);
        userRoleRepository.save(ur3);
    }

    private void seedProjectRoles() {
        if (projectRoleRepository.count() == 0) {
            projectManagerRole = new ProjectRole();
            projectManagerRole.setName("PROJECT_MANAGER");
            projectManagerRole.setDescription("Manages project");
            projectManagerRole = projectRoleRepository.save(projectManagerRole);

            teamMemberRole = new ProjectRole();
            teamMemberRole.setName("TEAM_MEMBER");
            teamMemberRole.setDescription("Team member");
            teamMemberRole = projectRoleRepository.save(teamMemberRole);
        } else {
            projectManagerRole = projectRoleRepository.findByName("PROJECT_MANAGER").orElseThrow();
            teamMemberRole = projectRoleRepository.findByName("TEAM_MEMBER").orElseThrow();
        }
    }

    private void seedWorkspace() {
        testWorkspace = new Workspace();
        testWorkspace.setName("Test Workspace");
        testWorkspace.setSlug("test-workspace");
        testWorkspace.setActive(true);
        testWorkspace.setCreatedAt(LocalDateTime.now());
        testWorkspace = workspaceRepository.save(testWorkspace);

        WorkspaceMember wm1 = new WorkspaceMember();
        wm1.setWorkspace(testWorkspace);
        wm1.setUser(adminUser);
        wm1.setWorkspaceRole("WORKSPACE_OWNER");
        wm1.setJoinedAt(LocalDateTime.now());
        wm1.setActive(true);
        workspaceMemberRepository.save(wm1);

        WorkspaceMember wm2 = new WorkspaceMember();
        wm2.setWorkspace(testWorkspace);
        wm2.setUser(normalUser);
        wm2.setWorkspaceRole("WORKSPACE_MEMBER");
        wm2.setJoinedAt(LocalDateTime.now());
        wm2.setActive(true);
        workspaceMemberRepository.save(wm2);
    }

    private void seedWorkflowStatuses() {
        defaultStatus = new WorkflowStatus();
        defaultStatus.setName("TODO");
        defaultStatus.setDefault(true);
        defaultStatus.setColor("#6B7280");
        defaultStatus.setPosition(0);
        defaultStatus = workflowStatusRepository.save(defaultStatus);

        doneStatus = new WorkflowStatus();
        doneStatus.setName("DONE");
        doneStatus.setDefault(false);
        doneStatus.setColor("#10B981");
        doneStatus.setPosition(1);
        doneStatus = workflowStatusRepository.save(doneStatus);
    }

    private void seedProject() {
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setDescription("Integration test project");
        testProject.setOwner(adminUser);
        testProject.setWorkspace(testWorkspace);
        testProject.setActive(true);
        testProject.setCreatedAt(LocalDateTime.now());
        testProject = projectRepository.save(testProject);

        defaultStatus.setProject(testProject);
        workflowStatusRepository.save(defaultStatus);

        doneStatus.setProject(testProject);
        workflowStatusRepository.save(doneStatus);

        ProjectMember pm = new ProjectMember();
        pm.setProject(testProject);
        pm.setUser(adminUser);
        pm.setProjectRole(projectManagerRole);
        pm.setIsActive(true);
        projectMemberRepository.save(pm);

        ProjectMember pm2 = new ProjectMember();
        pm2.setProject(testProject);
        pm2.setUser(normalUser);
        pm2.setProjectRole(teamMemberRole);
        pm2.setIsActive(true);
        projectMemberRepository.save(pm2);
    }

    protected Task createTask(String title, User assignee, WorkflowStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setProject(testProject);
        task.setCreatedBy(assignee.getId());
        task.setAssignee(assignee);
        task.setStatusEntity(status);
        return taskRepository.save(task);
    }

    protected Sprint createSprint(String name, SprintStatus status) {
        Sprint sprint = new Sprint();
        sprint.setName(name);
        sprint.setProject(testProject);
        sprint.setStatus(status);
        sprint.setStartDate(LocalDate.now().minusDays(5));
        sprint.setEndDate(LocalDate.now().minusDays(1));
        sprint.setCreatedAt(LocalDateTime.now());
        return sprintRepository.save(sprint);
    }
}

