/**
 * Centralized Permission Registry
 *
 * All permissions used across the frontend are defined here.
 * These match the backend PermissionConstants.java exactly.
 *
 * Naming convention: MODULE_ACTION (e.g., PROJECT_CREATE, TASK_VIEW)
 * JWT format: UPPERCASE strings (e.g., "CREATE_PROJECT")
 *
 * Namespaces for future ERP modules:
 *   project.*, workspace.*, billing.*, user.*, employee.*,
 *   attendance.*, leave.*, payroll.*, performance.*, finance.*, crm.*
 */

// ─── Project Permissions ────────────────────────────────────────────────────
export const PROJECT_VIEW = "VIEW_PROJECT";
export const PROJECT_VIEW_ALL = "VIEW_ALL_PROJECTS";
export const PROJECT_CREATE = "CREATE_PROJECT";
export const PROJECT_EDIT = "EDIT_PROJECT";
export const PROJECT_DELETE = "DELETE_PROJECT";

// ─── Task Permissions ───────────────────────────────────────────────────────
export const TASK_VIEW = "VIEW_TASK";
export const TASK_VIEW_ALL = "VIEW_ALL_TASKS";
export const TASK_CREATE = "CREATE_TASK";
export const TASK_EDIT = "EDIT_TASK";
export const TASK_EDIT_ANY = "EDIT_ANY_TASK";
export const TASK_EDIT_ASSIGNED = "EDIT_ASSIGNED_TASK";
export const TASK_DELETE = "DELETE_TASK";
export const TASK_MOVE = "MOVE_TASK";
export const TASK_ASSIGN = "ASSIGN_TASK";

// ─── Board Permissions ──────────────────────────────────────────────────────
export const BOARD_CREATE = "CREATE_BOARD";
export const BOARD_EDIT = "EDIT_BOARD";

// ─── Label Permissions ──────────────────────────────────────────────────────
export const LABEL_CREATE = "CREATE_LABEL";
export const LABEL_DELETE = "DELETE_LABEL";
export const LABEL_ASSIGN = "ASSIGN_LABEL";

// ─── Member Permissions ─────────────────────────────────────────────────────
export const MEMBER_VIEW = "VIEW_MEMBERS";
export const MEMBER_ADD = "ADD_MEMBER";
export const MEMBER_REMOVE = "REMOVE_MEMBER";
export const MEMBER_UPDATE_ROLE = "UPDATE_MEMBER_ROLE";

// ─── User Permissions ───────────────────────────────────────────────────────
export const USER_VIEW = "VIEW_USERS";
export const USER_CREATE = "CREATE_USER";
export const USER_UPDATE = "UPDATE_USER";
export const USER_DELETE = "DELETE_USER";

// ─── Role Permissions ───────────────────────────────────────────────────────
export const ROLE_VIEW = "VIEW_ROLES";
export const ROLE_CREATE = "CREATE_ROLE";
export const ROLE_UPDATE = "UPDATE_ROLE";
export const ROLE_DELETE = "DELETE_ROLE";
export const ROLE_ASSIGN_SYSTEM = "ASSIGN_SYSTEM_ROLE";

// ─── Dashboard Permissions ──────────────────────────────────────────────────
export const DASHBOARD_VIEW = "VIEW_DASHBOARD";

// ─── Workflow Permissions ───────────────────────────────────────────────────
export const WORKFLOW_MANAGE = "MANAGE_WORKFLOW";

// ─── Timesheet Permissions ──────────────────────────────────────────────────
export const TIMESHEET_APPROVE = "APPROVE_TIMESHEET";
export const TIMESHEET_VIEW_TEAM = "VIEW_TEAM_TIMESHEET";

// ─── Profile Permissions ────────────────────────────────────────────────────
export const PROFILE_VIEW = "VIEW_PROFILE";

// ─── HR Module Permissions ─────────────────────────────────────────────────
export const EMPLOYEE_VIEW = "EMPLOYEE_VIEW";
export const EMPLOYEE_CREATE = "EMPLOYEE_CREATE";
export const EMPLOYEE_UPDATE = "EMPLOYEE_UPDATE";
export const EMPLOYEE_DELETE = "EMPLOYEE_DELETE";

export const DEPARTMENT_VIEW = "DEPARTMENT_VIEW";
export const DEPARTMENT_CREATE = "DEPARTMENT_CREATE";
export const DEPARTMENT_UPDATE = "DEPARTMENT_UPDATE";
export const DEPARTMENT_DELETE = "DEPARTMENT_DELETE";

export const DESIGNATION_VIEW = "DESIGNATION_VIEW";
export const DESIGNATION_CREATE = "DESIGNATION_CREATE";
export const DESIGNATION_UPDATE = "DESIGNATION_UPDATE";
export const DESIGNATION_DELETE = "DESIGNATION_DELETE";

export const ATTENDANCE_VIEW = "ATTENDANCE_VIEW";
export const ATTENDANCE_MANAGE = "ATTENDANCE_MANAGE";

export const LEAVE_VIEW = "LEAVE_VIEW";
export const LEAVE_APPROVE = "LEAVE_APPROVE";

export const PAYROLL_VIEW = "PAYROLL_VIEW";
export const PAYROLL_RUN = "PAYROLL_RUN";

// ─── Billing Permissions (future) ───────────────────────────────────────────
export const BILLING_VIEW = "BILLING_VIEW";
export const BILLING_MANAGE = "BILLING_MANAGE";

// ─── Finance Permissions ────────────────────────────────────────────────────
export const FINANCE_VIEW = "FINANCE_VIEW";
export const FINANCE_CREATE = "FINANCE_CREATE";
export const FINANCE_EDIT = "FINANCE_EDIT";
export const FINANCE_DELETE = "FINANCE_DELETE";
export const FINANCE_APPROVE_EXPENSE = "FINANCE_APPROVE_EXPENSE";
export const FINANCE_MANAGE_PAYMENTS = "FINANCE_MANAGE_PAYMENTS";
/** @deprecated Use granular FINANCE_* permissions */
export const FINANCE_MANAGE = "FINANCE_MANAGE";

// ─── CRM Permissions ────────────────────────────────────────────────────────
export const CRM_VIEW = "CRM_VIEW";
export const CRM_MANAGE = "CRM_MANAGE";

export const CUSTOMER_VIEW = "CUSTOMER_VIEW";
export const CUSTOMER_CREATE = "CUSTOMER_CREATE";
export const CUSTOMER_UPDATE = "CUSTOMER_UPDATE";
export const CUSTOMER_DELETE = "CUSTOMER_DELETE";

export const LEAD_VIEW = "LEAD_VIEW";
export const LEAD_CREATE = "LEAD_CREATE";
export const LEAD_UPDATE = "LEAD_UPDATE";
export const LEAD_DELETE = "LEAD_DELETE";
export const LEAD_CONVERT = "LEAD_CONVERT";

export const CONTACT_VIEW = "CONTACT_VIEW";
export const CONTACT_CREATE = "CONTACT_CREATE";
export const CONTACT_UPDATE = "CONTACT_UPDATE";
export const CONTACT_DELETE = "CONTACT_DELETE";

export const DEAL_VIEW = "DEAL_VIEW";
export const DEAL_CREATE = "DEAL_CREATE";
export const DEAL_UPDATE = "DEAL_UPDATE";
export const DEAL_DELETE = "DEAL_DELETE";

export const PIPELINE_VIEW = "PIPELINE_VIEW";
export const PIPELINE_MANAGE = "PIPELINE_MANAGE";
export const PIPELINE_CREATE = "PIPELINE_CREATE";
export const PIPELINE_UPDATE = "PIPELINE_UPDATE";
export const PIPELINE_DELETE = "PIPELINE_DELETE";

// ─── Performance Permissions (future) ───────────────────────────────────────
export const PERFORMANCE_VIEW = "PERFORMANCE_VIEW";
export const PERFORMANCE_MANAGE = "PERFORMANCE_MANAGE";

// ─── Workspace Permissions ──────────────────────────────────────────────────
export const WORKSPACE_VIEW = "WORKSPACE_VIEW";
export const WORKSPACE_MANAGE = "WORKSPACE_MANAGE";

// ─── Platform Permissions ───────────────────────────────────────────────────
export const PLATFORM_MANAGE = "PLATFORM_MANAGE";

/**
 * Grouped permissions for convenient bulk checks.
 * Usage: hasAnyPermission(...PROJECT_PERMISSIONS)
 */
export const PROJECT_PERMISSIONS = [
  PROJECT_VIEW,
  PROJECT_VIEW_ALL,
  PROJECT_CREATE,
  PROJECT_EDIT,
  PROJECT_DELETE,
];

export const TASK_PERMISSIONS = [
  TASK_VIEW,
  TASK_VIEW_ALL,
  TASK_CREATE,
  TASK_EDIT,
  TASK_EDIT_ANY,
  TASK_EDIT_ASSIGNED,
  TASK_DELETE,
  TASK_MOVE,
  TASK_ASSIGN,
];

export const MEMBER_PERMISSIONS = [
  MEMBER_VIEW,
  MEMBER_ADD,
  MEMBER_REMOVE,
  MEMBER_UPDATE_ROLE,
];

export const USER_PERMISSIONS = [
  USER_VIEW,
  USER_CREATE,
  USER_UPDATE,
  USER_DELETE,
];

export const ROLE_PERMISSIONS = [
  ROLE_VIEW,
  ROLE_CREATE,
  ROLE_UPDATE,
  ROLE_DELETE,
  ROLE_ASSIGN_SYSTEM,
];

export const HR_PERMISSIONS = [
  EMPLOYEE_VIEW,
  EMPLOYEE_CREATE,
  EMPLOYEE_UPDATE,
  EMPLOYEE_DELETE,
  DEPARTMENT_VIEW,
  DEPARTMENT_CREATE,
  DEPARTMENT_UPDATE,
  DEPARTMENT_DELETE,
  DESIGNATION_VIEW,
  DESIGNATION_CREATE,
  DESIGNATION_UPDATE,
  DESIGNATION_DELETE,
  ATTENDANCE_VIEW,
  ATTENDANCE_MANAGE,
  LEAVE_VIEW,
  LEAVE_APPROVE,
];

export const BILLING_PERMISSIONS = [BILLING_VIEW, BILLING_MANAGE];
export const FINANCE_PERMISSIONS = [
  FINANCE_VIEW,
  FINANCE_CREATE,
  FINANCE_EDIT,
  FINANCE_DELETE,
  FINANCE_APPROVE_EXPENSE,
  FINANCE_MANAGE_PAYMENTS,
  FINANCE_MANAGE,
];
export const CRM_PERMISSIONS = [
  CRM_VIEW,
  CRM_MANAGE,
  CUSTOMER_VIEW,
  CUSTOMER_CREATE,
  CUSTOMER_UPDATE,
  CUSTOMER_DELETE,
  LEAD_VIEW,
  LEAD_CREATE,
  LEAD_UPDATE,
  LEAD_DELETE,
  LEAD_CONVERT,
  CONTACT_VIEW,
  CONTACT_CREATE,
  CONTACT_UPDATE,
  CONTACT_DELETE,
  DEAL_VIEW,
  DEAL_CREATE,
  DEAL_UPDATE,
  DEAL_DELETE,
  PIPELINE_VIEW,
  PIPELINE_MANAGE,
  PIPELINE_CREATE,
  PIPELINE_UPDATE,
  PIPELINE_DELETE,
];

/**
 * All permission constants in a single object for backwards compatibility.
 * Prefer importing individual constants for tree-shaking.
 */
const PERMISSIONS = {
  // Project
  PROJECT_VIEW,
  PROJECT_VIEW_ALL,
  PROJECT_CREATE,
  PROJECT_EDIT,
  PROJECT_DELETE,
  // Task
  TASK_VIEW,
  TASK_VIEW_ALL,
  TASK_CREATE,
  TASK_EDIT,
  TASK_EDIT_ANY,
  TASK_EDIT_ASSIGNED,
  TASK_DELETE,
  TASK_MOVE,
  TASK_ASSIGN,
  // Board
  BOARD_CREATE,
  BOARD_EDIT,
  // Label
  LABEL_CREATE,
  LABEL_DELETE,
  LABEL_ASSIGN,
  // Member
  MEMBER_VIEW,
  MEMBER_ADD,
  MEMBER_REMOVE,
  MEMBER_UPDATE_ROLE,
  // User
  USER_VIEW,
  USER_CREATE,
  USER_UPDATE,
  USER_DELETE,
  // Role
  ROLE_VIEW,
  ROLE_CREATE,
  ROLE_UPDATE,
  ROLE_DELETE,
  ROLE_ASSIGN_SYSTEM,
  // Dashboard
  DASHBOARD_VIEW,
  // Workflow
  WORKFLOW_MANAGE,
  // Timesheet
  TIMESHEET_APPROVE,
  TIMESHEET_VIEW_TEAM,
  // Profile
  PROFILE_VIEW,
  // HR
  EMPLOYEE_VIEW,
  EMPLOYEE_CREATE,
  EMPLOYEE_UPDATE,
  EMPLOYEE_DELETE,
  ATTENDANCE_VIEW,
  ATTENDANCE_MANAGE,
  LEAVE_VIEW,
  LEAVE_APPROVE,
  PAYROLL_VIEW,
  PAYROLL_RUN,
  // Billing
  BILLING_VIEW,
  BILLING_MANAGE,
  // Finance
  FINANCE_VIEW,
  FINANCE_CREATE,
  FINANCE_EDIT,
  FINANCE_DELETE,
  FINANCE_APPROVE_EXPENSE,
  FINANCE_MANAGE_PAYMENTS,
  FINANCE_MANAGE,
  // CRM
  CRM_VIEW,
  CRM_MANAGE,
  CUSTOMER_VIEW,
  CUSTOMER_CREATE,
  CUSTOMER_UPDATE,
  CUSTOMER_DELETE,
  LEAD_VIEW,
  LEAD_CREATE,
  LEAD_UPDATE,
  LEAD_DELETE,
  LEAD_CONVERT,
  CONTACT_VIEW,
  CONTACT_CREATE,
  CONTACT_UPDATE,
  CONTACT_DELETE,
  DEAL_VIEW,
  DEAL_CREATE,
  DEAL_UPDATE,
  DEAL_DELETE,
  PIPELINE_VIEW,
  PIPELINE_CREATE,
  PIPELINE_UPDATE,
  PIPELINE_DELETE,
  // Performance
  PERFORMANCE_VIEW,
  PERFORMANCE_MANAGE,
  // Workspace
  WORKSPACE_VIEW,
  WORKSPACE_MANAGE,
  // Platform
  PLATFORM_MANAGE,
  // Department
  DEPARTMENT_VIEW,
  DEPARTMENT_CREATE,
  DEPARTMENT_UPDATE,
  DEPARTMENT_DELETE,
  // Designation
  DESIGNATION_VIEW,
  DESIGNATION_CREATE,
  DESIGNATION_UPDATE,
  DESIGNATION_DELETE,
};

export default PERMISSIONS;
