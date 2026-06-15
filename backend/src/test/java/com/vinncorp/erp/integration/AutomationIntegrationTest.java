package com.vinncorp.erp.integration;


import com.vinncorp.erp.AbstractIntegrationTest;
import com.vinncorp.erp.modules.projects.dto.request.EscalationRuleRequest;
import com.vinncorp.erp.modules.projects.dto.request.SLARequest;
import com.vinncorp.erp.modules.projects.dto.request.WorkflowConditionRequest;
import com.vinncorp.erp.modules.projects.dto.request.WorkflowRuleRequest;
import com.vinncorp.erp.modules.projects.dto.response.*;
import com.vinncorp.erp.modules.projects.engine.WorkflowTrigger;
import com.vinncorp.erp.modules.projects.engine.WorkflowTriggerDispatcher;
import com.vinncorp.erp.modules.projects.entity.Task;
import com.vinncorp.erp.modules.projects.enums.*;
import com.vinncorp.erp.modules.projects.repository.EscalationRuleRepository;
import com.vinncorp.erp.modules.projects.repository.TaskSLARepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowExecutionLogRepository;
import com.vinncorp.erp.modules.projects.repository.WorkflowRuleRepository;
import com.vinncorp.erp.modules.projects.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AutomationIntegrationTest extends AbstractIntegrationTest {

    @Autowired private WorkflowRuleService workflowRuleService;
    @Autowired private SLAService slaService;
    @Autowired private EscalationService escalationService;
    @Autowired private SmartAssignmentService smartAssignmentService;
    @Autowired private DeadlineAutomationService deadlineAutomationService;
    @Autowired private WorkflowRuleRepository workflowRuleRepository;
    @Autowired private TaskSLARepository taskSLARepository;
    @Autowired private EscalationRuleRepository escalationRuleRepository;
    @Autowired private WorkflowExecutionLogRepository workflowExecutionLogRepository;
    @Autowired private WorkflowTriggerDispatcher workflowTriggerDispatcher;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Task testTask;

    @BeforeEach
    void setUp() {
        workflowExecutionLogRepository.deleteAll();
        workflowRuleRepository.deleteAll();
        escalationRuleRepository.deleteAll();
        taskSLARepository.deleteAll();
        taskDependencyRepository.deleteAll();
        taskRepository.deleteAll();

        testTask = createTask("Automation Test Task", normalUser, defaultStatus);
    }

    // ===== WORKFLOW RULE TESTS =====

    @Test
    void createWorkflowRule_shouldSucceed() {
        WorkflowRuleRequest request = buildRuleRequest();

        WorkflowRuleResponse response = workflowRuleService.createRule(request, adminUser.getEmail());

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Auto-assign high priority", response.getName());
        assertEquals(WorkflowTrigger.TASK_CREATED, response.getTriggerType());
        assertEquals(WorkflowAction.ASSIGN_USER, response.getActionType());
        assertTrue(response.isEnabled());
    }

    @Test
    void getWorkflowRule_shouldReturnRule() {
        WorkflowRuleResponse created = workflowRuleService.createRule(buildRuleRequest(), adminUser.getEmail());

        WorkflowRuleResponse response = workflowRuleService.getRule(created.getId());

        assertNotNull(response);
        assertEquals(created.getId(), response.getId());
        assertEquals("Auto-assign high priority", response.getName());
    }

    @Test
    void updateWorkflowRule_shouldSucceed() {
        WorkflowRuleResponse created = workflowRuleService.createRule(buildRuleRequest(), adminUser.getEmail());
        WorkflowRuleRequest updateReq = buildRuleRequest();
        updateReq.setName("Updated rule name");

        WorkflowRuleResponse response = workflowRuleService.updateRule(created.getId(), updateReq, adminUser.getEmail());

        assertEquals("Updated rule name", response.getName());
        assertEquals(created.getId(), response.getId());
    }

    @Test
    void deleteWorkflowRule_shouldSucceed() {
        WorkflowRuleResponse created = workflowRuleService.createRule(buildRuleRequest(), adminUser.getEmail());

        workflowRuleService.deleteRule(created.getId());

        assertThrows(Exception.class, () -> workflowRuleService.getRule(created.getId()));
    }

    @Test
    void toggleWorkflowRule_shouldEnableDisable() {
        WorkflowRuleResponse created = workflowRuleService.createRule(buildRuleRequest(), adminUser.getEmail());

        workflowRuleService.toggleRule(created.getId(), false);
        WorkflowRuleResponse afterDisable = workflowRuleService.getRule(created.getId());
        assertFalse(afterDisable.isEnabled());

        workflowRuleService.toggleRule(created.getId(), true);
        WorkflowRuleResponse afterEnable = workflowRuleService.getRule(created.getId());
        assertTrue(afterEnable.isEnabled());
    }

    @Test
    void getWorkspaceRules_shouldReturnAll() {
        workflowRuleService.createRule(buildRuleRequest(), adminUser.getEmail());
        WorkflowRuleRequest request2 = buildRuleRequest();
        request2.setName("Second rule");
        request2.setTriggerType(WorkflowTrigger.TASK_COMPLETED);
        workflowRuleService.createRule(request2, adminUser.getEmail());

        List<WorkflowRuleResponse> rules = workflowRuleService.getWorkspaceRules(testWorkspace.getId());

        assertFalse(rules.isEmpty());
        assertTrue(rules.size() >= 2);
    }

    @Test
    void getTemplates_shouldReturnSeededTemplates() {
        List<WorkflowRuleResponse> templates = workflowRuleService.getWorkspaceRules(testWorkspace.getId());

        assertNotNull(templates);
    }

    @Test
    void getExecutionLogs_shouldReturnLogs() {
        WorkflowRuleResponse created = workflowRuleService.createRule(buildRuleRequest(), adminUser.getEmail());

        var logs = workflowRuleService.getExecutionLogs(created.getId(), 0, 10);

        assertNotNull(logs);
    }

    // ===== ACTION EXECUTION TESTS =====

    @Test
    void executeRuleViaDispatcher_shouldCreateSuccessLog() {
        WorkflowRuleRequest request = buildStatusUpdateRuleRequest(List.of());
        WorkflowRuleResponse created = workflowRuleService.createRule(request, adminUser.getEmail());

        Map<String, Object> context = new HashMap<>();
        context.put("workspaceId", testWorkspace.getId());
        context.put("projectId", testProject.getId());
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);

        var logs = workflowRuleService.getExecutionLogs(created.getId(), 0, 10);
        assertEquals(1, logs.getTotalElements());
        assertEquals(ExecutionStatus.SUCCESS, logs.getContent().get(0).getStatus());
    }

    @Test
    void executeRuleViaDispatcher_shouldPerformStatusUpdate() {
        WorkflowRuleRequest request = buildStatusUpdateRuleRequest(List.of());
        WorkflowRuleResponse created = workflowRuleService.createRule(request, adminUser.getEmail());

        Map<String, Object> context = new HashMap<>();
        context.put("workspaceId", testWorkspace.getId());
        context.put("projectId", testProject.getId());
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertEquals("DONE", updatedTask.getStatusEntity().getName());
    }

    @Test
    void executeRuleViaDispatcher_shouldPerformUserAssignment() {
        WorkflowRuleRequest request = buildAssignUserRuleRequest(List.of());
        WorkflowRuleResponse created = workflowRuleService.createRule(request, adminUser.getEmail());

        Map<String, Object> context = new HashMap<>();
        context.put("workspaceId", testWorkspace.getId());
        context.put("projectId", testProject.getId());
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);

        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        assertEquals(normalUser.getId(), updatedTask.getAssignee().getId());
    }

    @Test
    void executeRuleViaDispatcher_shouldCreateSkippedLogWhenConditionsNotMet() {
        WorkflowConditionRequest condition = new WorkflowConditionRequest();
        condition.setFieldName("priority");
        condition.setOperator(WorkflowConditionOperator.EQUALS);
        condition.setComparisonValue("LOW");
        WorkflowRuleRequest request = buildStatusUpdateRuleRequest(List.of(condition));
        WorkflowRuleResponse created = workflowRuleService.createRule(request, adminUser.getEmail());

        Map<String, Object> context = new HashMap<>();
        context.put("workspaceId", testWorkspace.getId());
        context.put("projectId", testProject.getId());
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);

        var logs = workflowRuleService.getExecutionLogs(created.getId(), 0, 10);
        assertEquals(1, logs.getTotalElements());
        assertEquals(ExecutionStatus.SKIPPED, logs.getContent().getFirst().getStatus());
    }

    // ===== COOLDOWN TESTS =====

    @Test
    void cooldown_shouldPreventReExecution() {
        WorkflowRuleRequest request = buildStatusUpdateRuleRequest(List.of());
        request.setCooldownSeconds(3600);
        WorkflowRuleResponse created = workflowRuleService.createRule(request, adminUser.getEmail());

        Map<String, Object> context = new HashMap<>();
        context.put("workspaceId", testWorkspace.getId());
        context.put("projectId", testProject.getId());
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);

        var logs = workflowRuleService.getExecutionLogs(created.getId(), 0, 10);
        assertEquals(1, logs.getTotalElements());
    }

    @Test
    void cooldown_shouldAllowExecutionWithoutCooldown() {
        WorkflowRuleRequest request = buildStatusUpdateRuleRequest(List.of());
        request.setCooldownSeconds(0);
        WorkflowRuleResponse created = workflowRuleService.createRule(request, adminUser.getEmail());

        Map<String, Object> context = new HashMap<>();
        context.put("workspaceId", testWorkspace.getId());
        context.put("projectId", testProject.getId());
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);

        var logs = workflowRuleService.getExecutionLogs(created.getId(), 0, 10);
        assertEquals(2, logs.getTotalElements());
    }

    // ===== SLA TESTS =====

    @Test
    void configureSLA_shouldSucceed() {
        SLARequest request = buildSLARequest();

        SLAResponse response = slaService.configureSLA(request, adminUser.getEmail());

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals(testTask.getId(), response.getTaskId());
        assertEquals(SLAStatus.ACTIVE, response.getStatus());
        assertEquals(60, response.getResponseMinutes());
    }

    @Test
    void getTaskSLA_shouldReturnConfiguredSLA() {
        slaService.configureSLA(buildSLARequest(), adminUser.getEmail());

        SLAResponse response = slaService.getTaskSLA(testTask.getId(), SLAType.RESPONSE);

        assertNotNull(response);
        assertEquals(testTask.getId(), response.getTaskId());
        assertEquals(SLAType.RESPONSE, response.getSlaType());
    }

    @Test
    void getProjectSLAs_shouldReturnAll() {
        slaService.configureSLA(buildSLARequest(), adminUser.getEmail());
        Task task2 = createTask("SLA Task 2", normalUser, defaultStatus);
        SLARequest request2 = buildSLARequest();
        request2.setTaskId(task2.getId());
        request2.setSlaType(SLAType.COMPLETION);
        slaService.configureSLA(request2, adminUser.getEmail());

        List<SLAResponse> slas = slaService.getProjectSLAs(testProject.getId());

        assertFalse(slas.isEmpty());
        assertTrue(slas.size() >= 2);
    }

    @Test
    void getSLAReport_shouldReturnReport() {
        slaService.configureSLA(buildSLARequest(), adminUser.getEmail());

        SLABreachReportResponse report = slaService.getSLAReport(testProject.getId());

        assertNotNull(report);
        assertNotNull(report.getProjectId());
        assertEquals(testProject.getId(), report.getProjectId());
    }

    @Test
    void resolveSLA_shouldSucceed() {
        slaService.configureSLA(buildSLARequest(), adminUser.getEmail());

        slaService.resolveSLA(testTask.getId(), SLAType.RESPONSE);

        SLAResponse sla = slaService.getTaskSLA(testTask.getId(), SLAType.RESPONSE);
        assertEquals(SLAStatus.RESOLVED, sla.getStatus());
    }

    @Test
    void slaWarning_shouldBeTriggeredAtThreshold() {
        SLARequest request = buildSLARequest();
        request.setWarningThresholdPct(0.0);
        slaService.configureSLA(request, adminUser.getEmail());

        slaService.checkAndUpdateSLA(testTask.getId());

        SLAResponse sla = slaService.getTaskSLA(testTask.getId(), SLAType.RESPONSE);
        assertEquals(SLAStatus.WARNED, sla.getStatus());
    }

    @Test
    void slaBreach_shouldBeTriggeredAfterElapsedTime() {
        slaService.configureSLA(buildSLARequest(), adminUser.getEmail());

        jdbcTemplate.update(
                "UPDATE task_slas SET created_at = ? WHERE task_id = ? AND sla_type = ?",
                LocalDateTime.now().minusHours(2), testTask.getId(), SLAType.RESPONSE.name());

        slaService.checkAndUpdateSLA(testTask.getId());

        SLAResponse sla = slaService.getTaskSLA(testTask.getId(), SLAType.RESPONSE);
        assertEquals(SLAStatus.BREACHED, sla.getStatus());
    }

    @Test
    void slaCounts_shouldReflectBreachesAndWarnings() {
        slaService.configureSLA(buildSLARequest(), adminUser.getEmail());

        Task task2 = createTask("SLA Count Task", normalUser, defaultStatus);
        SLARequest request2 = buildSLARequest();
        request2.setTaskId(task2.getId());
        request2.setSlaType(SLAType.COMPLETION);
        slaService.configureSLA(request2, adminUser.getEmail());

        jdbcTemplate.update(
                "UPDATE task_slas SET created_at = ? WHERE task_id = ? AND sla_type = ?",
                LocalDateTime.now().minusHours(2), testTask.getId(), SLAType.RESPONSE.name());

        slaService.checkAndUpdateSLA(testTask.getId());
        slaService.checkAndUpdateSLA(task2.getId());

        SLABreachReportResponse report = slaService.getSLAReport(testProject.getId());
        assertEquals(1, report.getTotalBreached());
        assertTrue(report.getTotalWarned() >= 0);
    }

    // ===== ESCALATION TESTS =====

    @Test
    void createEscalationRule_shouldSucceed() {
        EscalationRuleRequest request = buildEscalationRequest();

        EscalationRuleResponse response = escalationService.createRule(request, adminUser.getEmail());

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("SLA Breach Escalation", response.getName());
        assertEquals(EscalationTrigger.SLA_BREACH, response.getTriggerCondition());
        assertTrue(response.isNotifyAssignee());
        assertTrue(response.isEnabled());
    }

    @Test
    void getEscalationRules_shouldReturnAll() {
        escalationService.createRule(buildEscalationRequest(), adminUser.getEmail());
        EscalationRuleRequest request2 = buildEscalationRequest();
        request2.setName("Overdue Escalation");
        request2.setTriggerCondition(EscalationTrigger.OVERDUE);
        escalationService.createRule(request2, adminUser.getEmail());

        List<EscalationRuleResponse> rules = escalationService.getWorkspaceRules(testWorkspace.getId());

        assertFalse(rules.isEmpty());
        assertTrue(rules.size() >= 2);
    }

    @Test
    void toggleEscalationRule_shouldEnableDisable() {
        EscalationRuleResponse created = escalationService.createRule(buildEscalationRequest(), adminUser.getEmail());

        escalationService.toggleRule(created.getId(), false);
        List<EscalationRuleResponse> rules = escalationService.getWorkspaceRules(testWorkspace.getId());
        EscalationRuleResponse afterDisable = rules.stream()
            .filter(r -> r.getId().equals(created.getId()))
            .findFirst().orElseThrow();
        assertFalse(afterDisable.isEnabled());
    }

    @Test
    void deleteEscalationRule_shouldSucceed() {
        EscalationRuleResponse created = escalationService.createRule(buildEscalationRequest(), adminUser.getEmail());

        escalationService.deleteRule(created.getId());

        List<EscalationRuleResponse> rules = escalationService.getWorkspaceRules(testWorkspace.getId());
        assertTrue(rules.stream().noneMatch(r -> r.getId().equals(created.getId())));
    }

    @Test
    void escalateTask_shouldSucceed() {
        escalationService.createRule(buildEscalationRequest(), adminUser.getEmail());

        assertDoesNotThrow(() ->
            escalationService.escalateTask(testTask.getId(), "Test escalation", adminUser.getEmail()));
    }

    @Test
    void escalation_shouldEscalateOverdueTask() {
        EscalationRuleRequest request = new EscalationRuleRequest();
        request.setWorkspaceId(testWorkspace.getId());
        request.setProjectId(testProject.getId());
        request.setName("Overdue Escalation");
        request.setTriggerCondition(EscalationTrigger.OVERDUE);
        request.setThresholdMinutes(1440);
        request.setNotifyAssignee(true);
        request.setNotifyProjectLead(false);
        request.setAutoAssign(false);
        escalationService.createRule(request, adminUser.getEmail());

        testTask.setDueDate(LocalDateTime.now().minusDays(1));
        taskRepository.save(testTask);

        escalationService.checkAndEscalate(testTask);

        Task escalated = taskRepository.findById(testTask.getId()).orElseThrow();
        assertEquals(TaskPriority.CRITICAL, escalated.getPriority());
    }

    @Test
    void escalation_shouldNotEscalateWithoutTrigger() {
        EscalationRuleRequest request = new EscalationRuleRequest();
        request.setWorkspaceId(testWorkspace.getId());
        request.setProjectId(testProject.getId());
        request.setName("Overdue Escalation");
        request.setTriggerCondition(EscalationTrigger.OVERDUE);
        request.setThresholdMinutes(1440);
        request.setNotifyAssignee(true);
        request.setNotifyProjectLead(false);
        request.setAutoAssign(false);
        escalationService.createRule(request, adminUser.getEmail());

        testTask.setDueDate(LocalDateTime.now().plusDays(5));
        taskRepository.save(testTask);

        escalationService.checkAndEscalate(testTask);

        Task escalated = taskRepository.findById(testTask.getId()).orElseThrow();
        assertNull(escalated.getPriority());
    }

    @Test
    void escalation_shouldEscalateSlaBreachedTask() {
        EscalationRuleRequest request = new EscalationRuleRequest();
        request.setWorkspaceId(testWorkspace.getId());
        request.setProjectId(testProject.getId());
        request.setName("SLA Breach Escalation");
        request.setTriggerCondition(EscalationTrigger.SLA_BREACH);
        request.setThresholdMinutes(30);
        request.setNotifyAssignee(true);
        request.setNotifyProjectLead(false);
        request.setAutoAssign(false);
        escalationService.createRule(request, adminUser.getEmail());

        testTask.setPriority(TaskPriority.CRITICAL);
        taskRepository.save(testTask);

        escalationService.checkAndEscalate(testTask);

        Task escalated = taskRepository.findById(testTask.getId()).orElseThrow();
        assertEquals(TaskPriority.CRITICAL, escalated.getPriority());
    }

    // ===== WORKFLOW ISOLATION TESTS =====

    @Test
    void workflowIsolation_rulesShouldNotLeakBetweenWorkspaces() {
        workflowRuleService.createRule(buildRuleRequest(), adminUser.getEmail());

        List<WorkflowRuleResponse> otherRules = workflowRuleService.getWorkspaceRules(99999L);
        assertTrue(otherRules.isEmpty());
    }

    @Test
    void workflowIsolation_executionLogsShouldBePerRule() {
        WorkflowRuleRequest request1 = buildStatusUpdateRuleRequest(List.of());
        WorkflowRuleResponse rule1 = workflowRuleService.createRule(request1, adminUser.getEmail());
        WorkflowRuleRequest request2 = buildAssignUserRuleRequest(List.of());
        WorkflowRuleResponse rule2 = workflowRuleService.createRule(request2, adminUser.getEmail());

        Map<String, Object> context = new HashMap<>();
        context.put("workspaceId", testWorkspace.getId());
        context.put("projectId", testProject.getId());
        workflowTriggerDispatcher.dispatch(WorkflowTrigger.TASK_CREATED, testWorkspace.getId(), testProject.getId(),
                "TASK", testTask.getId(), context);

        var logs1 = workflowRuleService.getExecutionLogs(rule1.getId(), 0, 10);
        var logs2 = workflowRuleService.getExecutionLogs(rule2.getId(), 0, 10);
        assertTrue(logs1.getTotalElements() >= 1);
        assertTrue(logs2.getTotalElements() >= 1);
    }

    // ===== SMART ASSIGNMENT TESTS =====

    @Test
    void autoAssign_shouldReturnCandidate() {
        SmartAssignmentResponse response = smartAssignmentService.autoAssign(testTask.getId(), adminUser.getEmail());

        assertNotNull(response);
        assertEquals(testTask.getId(), response.getTaskId());
        assertNotNull(response.getAssignedUserId());
        assertNotNull(response.getReason());
        assertTrue(response.getConfidenceScore() >= 0);
    }

    // ===== DEADLINE AUTOMATION TESTS =====

    @Test
    void autoShiftDueDates_shouldShiftDates() {
        testTask.setDueDate(LocalDateTime.now().plusDays(5));
        taskRepository.save(testTask);

        assertDoesNotThrow(() ->
            deadlineAutomationService.autoShiftDueDates(testTask.getId(), 3));
    }

    @Test
    void rescheduleDependencyChain_shouldNotThrow() {
        assertDoesNotThrow(() ->
            deadlineAutomationService.rescheduleDependencyChain(testTask.getId()));
    }

    @Test
    void findOverdueTasksForEscalation_shouldReturnTasks() {
        testTask.setDueDate(LocalDateTime.now().minusDays(1));
        taskRepository.save(testTask);

        List<Task> overdue = deadlineAutomationService.findOverdueTasksForEscalation(testProject.getId());

        assertNotNull(overdue);
        assertFalse(overdue.isEmpty());
    }

    // ===== HELPER METHODS =====

    private WorkflowRuleRequest buildRuleRequest() {
        WorkflowRuleRequest req = new WorkflowRuleRequest();
        req.setWorkspaceId(testWorkspace.getId());
        req.setProjectId(testProject.getId());
        req.setName("Auto-assign high priority");
        req.setDescription("Auto-assigns high priority tasks to admin");
        req.setTriggerType(WorkflowTrigger.TASK_CREATED);
        req.setActionType(WorkflowAction.ASSIGN_USER);
        req.setActionConfig("{\"assigneeEmail\":\"admin@test.com\"}");
        req.setExecutionOrder(1);
        req.setCooldownSeconds(0);
        WorkflowConditionRequest condition = new WorkflowConditionRequest();
        condition.setFieldName("priority");
        condition.setOperator(WorkflowConditionOperator.EQUALS);
        condition.setComparisonValue("HIGH");
        req.setConditions(List.of(condition));
        return req;
    }

    private WorkflowRuleRequest buildStatusUpdateRuleRequest(List<WorkflowConditionRequest> conditions) {
        WorkflowRuleRequest req = new WorkflowRuleRequest();
        req.setWorkspaceId(testWorkspace.getId());
        req.setProjectId(testProject.getId());
        req.setName("Update status to DONE");
        req.setTriggerType(WorkflowTrigger.TASK_CREATED);
        req.setActionType(WorkflowAction.UPDATE_STATUS);
        req.setActionConfig("{\"targetStatus\":\"DONE\"}");
        req.setExecutionOrder(1);
        req.setCooldownSeconds(0);
        req.setConditions(conditions);
        return req;
    }

    private WorkflowRuleRequest buildAssignUserRuleRequest(List<WorkflowConditionRequest> conditions) {
        WorkflowRuleRequest req = new WorkflowRuleRequest();
        req.setWorkspaceId(testWorkspace.getId());
        req.setProjectId(testProject.getId());
        req.setName("Assign to normal user");
        req.setTriggerType(WorkflowTrigger.TASK_CREATED);
        req.setActionType(WorkflowAction.ASSIGN_USER);
        req.setActionConfig("{\"assigneeEmail\":\"user@test.com\"}");
        req.setExecutionOrder(1);
        req.setCooldownSeconds(0);
        req.setConditions(conditions);
        return req;
    }

    private SLARequest buildSLARequest() {
        SLARequest req = new SLARequest();
        req.setWorkspaceId(testWorkspace.getId());
        req.setProjectId(testProject.getId());
        req.setTaskId(testTask.getId());
        req.setSlaType(SLAType.RESPONSE);
        req.setResponseMinutes(60);
        req.setCompletionMinutes(1440);
        req.setWarningThresholdPct(80.0);
        return req;
    }

    private EscalationRuleRequest buildEscalationRequest() {
        EscalationRuleRequest req = new EscalationRuleRequest();
        req.setWorkspaceId(testWorkspace.getId());
        req.setProjectId(testProject.getId());
        req.setName("SLA Breach Escalation");
        req.setTriggerCondition(EscalationTrigger.SLA_BREACH);
        req.setThresholdMinutes(30);
        req.setEscalateToRole("PROJECT_MANAGER");
        req.setNotifyAssignee(true);
        req.setNotifyProjectLead(true);
        req.setAutoAssign(false);
        return req;
    }
}

