package com.vinncorp.erp.integration;

import com.vinncorp.erp.AbstractIntegrationTest;
import com.vinncorp.erp.core.workspace.entity.Workspace;
import com.vinncorp.erp.modules.projects.dto.response.BlockedStatusResponse;
import com.vinncorp.erp.modules.projects.dto.response.DependencyGraphResponse;
import com.vinncorp.erp.modules.projects.dto.response.TaskDependencyResponse;
import com.vinncorp.erp.modules.projects.entity.Project;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.entity.TaskDependency;
import com.vinncorp.erp.modules.projects.enums.DependencyType;
import com.vinncorp.erp.shared.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DependencyValidationIntegrationTest extends AbstractIntegrationTest {

    private Task taskA;
    private Task taskB;
    private Task taskC;
    private Task taskD;

    @BeforeEach
    void setUp() {
        taskDependencyRepository.deleteAll();
        taskRepository.deleteAll();

        taskA = createTask("Task A", normalUser, defaultStatus);
        taskB = createTask("Task B", normalUser, defaultStatus);
        taskC = createTask("Task C", normalUser, defaultStatus);
        taskD = createTask("Task D", normalUser, defaultStatus);
    }

    @Test
    void addDependency_shouldSucceed() {
        TaskDependencyResponse response = taskDependencyService.addDependency(
                taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, "Test dependency", adminUser.getEmail());

        assertNotNull(response);
        assertNotNull(response.getTaskId());
        assertNotNull(response.getDependsOnTaskId());
        assertEquals(DependencyType.BLOCKED_BY, response.getDependencyType());
        assertEquals("Test dependency", response.getDescription());
    }

    @Test
    void addDependency_defaultsToBlockedBy() {
        TaskDependencyResponse response = taskDependencyService.addDependency(
                taskA.getId(), taskB.getId(), null, null, adminUser.getEmail());

        assertEquals(DependencyType.BLOCKED_BY, response.getDependencyType());
    }

    @Test
    void addSelfDependency_shouldFail() {
        assertThrows(BadRequestException.class, () ->
                taskDependencyService.addDependency(taskA.getId(), taskA.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail()));
    }

    @Test
    void addDuplicateDependency_shouldFail() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        assertThrows(BadRequestException.class, () ->
                taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail()));
    }

    @Test
    void addCircularDependency_shouldFail() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());
        taskDependencyService.addDependency(taskB.getId(), taskC.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        assertThrows(BadRequestException.class, () ->
                taskDependencyService.addDependency(taskC.getId(), taskA.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail()));
    }

    @Test
    void addIndirectCircularDependency_shouldFail() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());
        taskDependencyService.addDependency(taskB.getId(), taskC.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());
        taskDependencyService.addDependency(taskC.getId(), taskD.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        assertThrows(BadRequestException.class, () ->
                taskDependencyService.addDependency(taskD.getId(), taskA.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail()));
    }

    @Test
    void addCrossProjectDependency_shouldFail() {
        Project otherProject = new Project();
        otherProject.setName("Other Project");
        otherProject.setOwner(adminUser);
        otherProject.setWorkspace(testWorkspace);
        otherProject.setActive(true);
        Project savedProject = projectRepository.save(otherProject);

        Task crossTask = createTask("Cross Task", normalUser, defaultStatus);
        crossTask.setProject(savedProject);
        Task savedCrossTask = taskRepository.save(crossTask);

        assertThrows(BadRequestException.class, () ->
                taskDependencyService.addDependency(taskA.getId(), savedCrossTask.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail()));
    }

    @Test
    void addCrossWorkspaceDependency_shouldFail() {
        Workspace otherWorkspace = new Workspace();
        otherWorkspace.setName("Other Workspace");
        otherWorkspace.setSlug("other-workspace");
        otherWorkspace.setActive(true);
        otherWorkspace.setCreatedAt(LocalDateTime.now());
        otherWorkspace = workspaceRepository.save(otherWorkspace);

        Project otherProject = new Project();
        otherProject.setName("Other WS Project");
        otherProject.setOwner(adminUser);
        otherProject.setWorkspace(otherWorkspace);
        otherProject.setActive(true);
        Project savedProject = projectRepository.save(otherProject);

        Task crossWsTask = createTask("Cross WS Task", normalUser, defaultStatus);
        crossWsTask.setProject(savedProject);
        Task savedCrossWsTask = taskRepository.save(crossWsTask);

        assertThrows(BadRequestException.class, () ->
                taskDependencyService.addDependency(taskA.getId(), savedCrossWsTask.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail()));
    }

    @Test
    void removeDependency_shouldSucceed() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        taskDependencyService.removeDependency(taskA.getId(), taskB.getId(), adminUser.getEmail());

        List<TaskDependencyResponse> deps = taskDependencyService.getDependencies(taskA.getId());
        assertTrue(deps.isEmpty());
    }

    @Test
    void getDependencies_shouldReturnAll() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());
        taskDependencyService.addDependency(taskA.getId(), taskC.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        List<TaskDependencyResponse> deps = taskDependencyService.getDependencies(taskA.getId());

        assertEquals(2, deps.size());
    }

    @Test
    void getBlockingTasks_shouldReturnTasksBlockedByThis() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());
        taskDependencyService.addDependency(taskC.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        List<TaskDependencyResponse> blocked = taskDependencyService.getBlockingTasks(taskB.getId());

        assertEquals(2, blocked.size());
    }

    @Test
    void validateDependencies_whenBlocked_shouldFail() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        assertThrows(BadRequestException.class, () ->
                taskDependencyService.validateDependenciesBeforeStatusChange(taskA.getId()));
    }

    @Test
    void validateDependencies_whenUnblocked_shouldSucceed() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        taskB.setStatusEntity(doneStatus);
        taskRepository.save(taskB);

        assertDoesNotThrow(() ->
                taskDependencyService.validateDependenciesBeforeStatusChange(taskA.getId()));
    }

    @Test
    void dependencyChain_depthCheck() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());
        taskDependencyService.addDependency(taskB.getId(), taskC.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        List<TaskDependencyResponse> depsA = taskDependencyService.getDependencies(taskA.getId());
        List<TaskDependencyResponse> depsB = taskDependencyService.getDependencies(taskB.getId());
        List<TaskDependencyResponse> depsC = taskDependencyService.getDependencies(taskC.getId());

        assertEquals(1, depsA.size());
        assertEquals(1, depsB.size());
        assertEquals(0, depsC.size());
    }

    @Test
    void getBlockedStatus_whenUnblocked_shouldReturnFalse() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        taskB.setStatusEntity(doneStatus);
        taskRepository.save(taskB);

        BlockedStatusResponse status = taskDependencyService.getBlockedStatus(taskA.getId());
        assertFalse(status.isBlocked());
        assertTrue(status.getBlockingTasks().isEmpty());
    }

    @Test
    void getBlockedStatus_whenBlocked_shouldReturnBlockingTasks() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        BlockedStatusResponse status = taskDependencyService.getBlockedStatus(taskA.getId());
        assertTrue(status.isBlocked());
        assertEquals(1, status.getBlockingTasks().size());
        assertEquals(taskB.getId(), status.getBlockingTasks().get(0).getTaskId());
    }

    @Test
    void getDependencyGraph_shouldReturnNodesAndEdges() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());
        taskDependencyService.addDependency(taskB.getId(), taskC.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        DependencyGraphResponse graph = taskDependencyService.getDependencyGraph(taskA.getId());

        assertFalse(graph.getNodes().isEmpty());
        assertFalse(graph.getEdges().isEmpty());
    }

    @Test
    void getRelatedTasks_shouldReturnBothDirections() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.RELATES_TO, null, adminUser.getEmail());

        List<TaskDependencyResponse> relatedA = taskDependencyService.getRelatedTasks(taskA.getId());
        List<TaskDependencyResponse> relatedB = taskDependencyService.getRelatedTasks(taskB.getId());

        assertEquals(1, relatedA.size());
        assertEquals(1, relatedB.size());
    }

    @Test
    void addSymmetricDuplicate_shouldFail() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.RELATES_TO, null, adminUser.getEmail());

        assertThrows(BadRequestException.class, () ->
                taskDependencyService.addDependency(taskB.getId(), taskA.getId(), DependencyType.RELATES_TO, null, adminUser.getEmail()));
    }

    @Test
    void deleteTask_shouldSoftDeleteDependencies() {
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        taskService.deleteTask(taskA.getId());

        List<TaskDependency> deps = taskDependencyRepository.findByTaskId(taskA.getId());
        assertTrue(deps.isEmpty() || deps.stream().allMatch(TaskDependency::isDeleted));
    }

    @Test
    void getBlockedStatus_withSoftDeletedTask_shouldNotIncludeDeleted() {
        Task taskToDelete = createTask("To Delete", normalUser, defaultStatus);
        taskDependencyService.addDependency(taskA.getId(), taskB.getId(), DependencyType.BLOCKED_BY, null, adminUser.getEmail());

        taskService.deleteTask(taskB.getId());

        BlockedStatusResponse status = taskDependencyService.getBlockedStatus(taskA.getId());
        assertFalse(status.isBlocked());
    }

    @Test
    void addDependency_withBlocksType_shouldSucceed() {
        TaskDependencyResponse response = taskDependencyService.addDependency(
                taskA.getId(), taskB.getId(), DependencyType.BLOCKS, null, adminUser.getEmail());

        assertEquals(DependencyType.BLOCKS, response.getDependencyType());
        assertEquals(taskA.getId(), response.getTaskId());
        assertEquals(taskB.getId(), response.getDependsOnTaskId());
    }

    @Test
    void addDependency_withCausedByType_shouldSucceed() {
        TaskDependencyResponse response = taskDependencyService.addDependency(
                taskA.getId(), taskB.getId(), DependencyType.CAUSED_BY, "This was caused by task B", adminUser.getEmail());

        assertEquals(DependencyType.CAUSED_BY, response.getDependencyType());
        assertEquals("This was caused by task B", response.getDescription());
    }
}

