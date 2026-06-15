package com.vinncorp.erp.shared.cache;

public final class CacheNames {

    private CacheNames() {}

    public static final String PREFIX_WORKSPACE = "workspace";
    public static final String PREFIX_WORKSPACE_MEMBERS = "workspace:members";
    public static final String PREFIX_WORKSPACE_PERMISSIONS = "workspace:permissions";
    public static final String PREFIX_DEFAULT_WORKSPACE = "default_workspace";

    // Data cache keys (workspace-scoped)
    public static final String PREFIX_PROJECTS = "workspace:%d:projects";
    public static final String PREFIX_DASHBOARD = "workspace:%d:dashboard";
    public static final String PREFIX_ANALYTICS = "workspace:%d:analytics";
    public static final String PREFIX_ACTIVITIES = "workspace:%d:activities";
    public static final String PREFIX_NOTIFICATIONS = "workspace:%d:notifications";
    public static final String PREFIX_RECURRING = "workspace:%d:recurring";
    public static final String PREFIX_RECURRING_TEMPLATE = "recurring:template:%d";
    public static final String PREFIX_RECURRING_OCCURRENCES = "recurring:occurrences:%d";

    public static String workspace(Long workspaceId) {
        return PREFIX_WORKSPACE + ":" + workspaceId;
    }

    public static String workspaceMembers(Long workspaceId) {
        return PREFIX_WORKSPACE_MEMBERS + ":" + workspaceId;
    }

    public static String workspacePermissions(Long workspaceId, Long userId) {
        return PREFIX_WORKSPACE_PERMISSIONS + ":" + workspaceId + ":" + userId;
    }

    public static String defaultWorkspace(Long userId) {
        return PREFIX_DEFAULT_WORKSPACE + ":" + userId;
    }

    // Workspace-scoped data cache keys
    public static String projects(Long workspaceId) {
        return String.format(PREFIX_PROJECTS, workspaceId);
    }

    public static String dashboard(Long workspaceId) {
        return String.format(PREFIX_DASHBOARD, workspaceId);
    }

    public static String analytics(Long workspaceId) {
        return String.format(PREFIX_ANALYTICS, workspaceId);
    }

    public static String activities(Long workspaceId) {
        return String.format(PREFIX_ACTIVITIES, workspaceId);
    }

    public static String notifications(Long workspaceId, Long userId) {
        return String.format(PREFIX_NOTIFICATIONS, workspaceId) + ":" + userId;
    }

    public static String recurring(Long workspaceId) {
        return String.format(PREFIX_RECURRING, workspaceId);
    }

    public static String recurringTemplate(Long templateId) {
        return String.format(PREFIX_RECURRING_TEMPLATE, templateId);
    }

    public static String recurringOccurrences(Long templateId) {
        return String.format(PREFIX_RECURRING_OCCURRENCES, templateId);
    }

    // Sprint intelligence cache keys
    public static final String PREFIX_VELOCITY = "sprint:velocity:%d";
    public static final String PREFIX_CAPACITY = "sprint:capacity:%d";
    public static final String PREFIX_HEALTH = "sprint:health:%d";
    public static final String PREFIX_FORECAST = "sprint:forecast:%d";
    public static final String PREFIX_BURNDOWN = "sprint:burndown:%d";
    public static final String PREFIX_BURNUP = "sprint:burnup:%d";
    public static final String PREFIX_WORKLOAD = "sprint:workload:%d";
    public static final String PREFIX_PROJECT_VELOCITY = "project:velocity:%d";

    public static String sprintVelocity(Long sprintId) {
        return String.format(PREFIX_VELOCITY, sprintId);
    }

    public static String sprintCapacity(Long sprintId) {
        return String.format(PREFIX_CAPACITY, sprintId);
    }

    public static String sprintHealth(Long sprintId) {
        return String.format(PREFIX_HEALTH, sprintId);
    }

    public static String sprintForecast(Long sprintId) {
        return String.format(PREFIX_FORECAST, sprintId);
    }

    public static String sprintBurndown(Long sprintId) {
        return String.format(PREFIX_BURNDOWN, sprintId);
    }

    public static String sprintBurnup(Long sprintId) {
        return String.format(PREFIX_BURNUP, sprintId);
    }

    public static String sprintWorkload(Long sprintId) {
        return String.format(PREFIX_WORKLOAD, sprintId);
    }

    public static String projectVelocity(Long projectId) {
        return String.format(PREFIX_PROJECT_VELOCITY, projectId);
    }

    // Workflow automation cache keys
    public static final String PREFIX_WORKFLOW_RULES = "workflow:rules:%d";
    public static final String PREFIX_WORKFLOW_LOGS = "workflow:logs:%d";
    public static final String PREFIX_SLA = "sla:task:%d:%s";
    public static final String PREFIX_ESCALATION = "escalation:rules:%d";

    public static String workflowRules(Long workspaceId) {
        return String.format(PREFIX_WORKFLOW_RULES, workspaceId);
    }

    public static String workflowLogs(Long ruleId) {
        return String.format(PREFIX_WORKFLOW_LOGS, ruleId);
    }

    public static String taskSLA(Long taskId, String slaType) {
        return String.format(PREFIX_SLA, taskId, slaType);
    }

    public static String escalationRules(Long workspaceId) {
        return String.format(PREFIX_ESCALATION, workspaceId);
    }

    // Phase 3A.5 - Advanced Execution Intelligence
    public static final String PREFIX_ESTIMATION = "estimation:task:%d:%d";
    public static final String PREFIX_CRITICAL_PATH = "critical:path:%d";
    public static final String PREFIX_SPRINT_PLANNING = "sprint:planning:%d";
    public static final String PREFIX_EXECUTION_RISK = "risk:project:%d";
    public static final String PREFIX_PRODUCTIVITY = "productivity:project:%d";

    public static String estimation(Long workspaceId, Long taskId) { return String.format(PREFIX_ESTIMATION, workspaceId, taskId); }

    public static String criticalPath(Long projectId) { return String.format(PREFIX_CRITICAL_PATH, projectId); }

    public static String sprintPlanning(Long workspaceId) { return String.format(PREFIX_SPRINT_PLANNING, workspaceId); }

    public static String executionRisk(Long projectId) { return String.format(PREFIX_EXECUTION_RISK, projectId); }

    public static String productivityAnalytics(Long projectId) { return String.format(PREFIX_PRODUCTIVITY, projectId); }

    public static final String PREFIX_MONTE_CARLO = "sprint:monte:%d";
    public static final String PREFIX_CAPACITY_FORECAST = "sprint:capforecast:%d";

    public static String monteCarloForecast(Long sprintId) {
        return String.format(PREFIX_MONTE_CARLO, sprintId);
    }

    public static String capacityForecast(Long sprintId) {
        return String.format(PREFIX_CAPACITY_FORECAST, sprintId);
    }

    // Phase 3A.6–3A.8
    public static final String PREFIX_EXECUTIVE = "workspace:%d:executive";
    public static final String PREFIX_KNOWLEDGE = "workspace:%d:knowledge";
    public static final String PREFIX_SAVED_SEARCH = "workspace:%d:savedsearch:%d";
    public static final String PREFIX_WORKSPACE_NOTES = "workspace:%d:notes";
    public static final String PREFIX_ACTIVITY_INTEL = "workspace:%d:activityintel";
    public static final String PREFIX_PERSONAL_PRODUCTIVITY = "workspace:%d:productivity:%d";

    public static String executive(Long workspaceId) {
        return String.format(PREFIX_EXECUTIVE, workspaceId);
    }

    public static String knowledge(Long workspaceId) {
        return String.format(PREFIX_KNOWLEDGE, workspaceId);
    }

    public static String savedSearch(Long workspaceId, Long userId) {
        return String.format(PREFIX_SAVED_SEARCH, workspaceId, userId);
    }

    public static String workspaceNotes(Long workspaceId) {
        return String.format(PREFIX_WORKSPACE_NOTES, workspaceId);
    }

    public static String activityIntelligence(Long workspaceId) {
        return String.format(PREFIX_ACTIVITY_INTEL, workspaceId);
    }

    public static String personalProductivity(Long workspaceId, Long userId) {
        return String.format(PREFIX_PERSONAL_PRODUCTIVITY, workspaceId, userId);
    }
}


