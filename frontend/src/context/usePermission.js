import { useMemo, useState, useEffect, useCallback } from "react";
import { useAuth } from "../context/useAuth.js";
import { useProjectPermission } from "../context/ProjectPermissionContext.jsx";
import { getMyPermissions } from "../api/permissionApi";
import {
  PROJECT_VIEW,
  PROJECT_VIEW_ALL,
  PROJECT_CREATE,
  PROJECT_EDIT,
  PROJECT_DELETE,
  TASK_VIEW,
  TASK_VIEW_ALL,
  TASK_CREATE,
  TASK_EDIT,
  TASK_EDIT_ANY,
  TASK_EDIT_ASSIGNED,
  TASK_DELETE,
  TASK_MOVE,
  TASK_ASSIGN,
  BOARD_CREATE,
  BOARD_EDIT,
  LABEL_CREATE,
  LABEL_DELETE,
  LABEL_ASSIGN,
  MEMBER_VIEW,
  MEMBER_ADD,
  MEMBER_REMOVE,
  MEMBER_UPDATE_ROLE,
  USER_VIEW,
  USER_CREATE,
  USER_UPDATE,
  USER_DELETE,
  ROLE_VIEW,
  ROLE_CREATE,
  ROLE_UPDATE,
  ROLE_DELETE,
  ROLE_ASSIGN_SYSTEM,
  DASHBOARD_VIEW,
  WORKFLOW_MANAGE,
  TIMESHEET_APPROVE,
  TIMESHEET_VIEW_TEAM,
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
  WORKSPACE_MANAGE,
  PLATFORM_MANAGE,
} from "../constants/permissions.js";

/**
 * Re-export the Permissions enum for backwards compatibility.
 * New code should import individual constants from constants/permissions.js.
 */
export const Permissions = {
  CREATE_TASK: TASK_CREATE,
  EDIT_TASK: TASK_EDIT,
  EDIT_ANY_TASK: TASK_EDIT_ANY,
  EDIT_ASSIGNED_TASK: TASK_EDIT_ASSIGNED,
  DELETE_TASK: TASK_DELETE,
  MOVE_TASK: TASK_MOVE,
  ASSIGN_TASK: TASK_ASSIGN,
  VIEW_TASK: TASK_VIEW,
  VIEW_ALL_TASKS: TASK_VIEW_ALL,
  CREATE_PROJECT: PROJECT_CREATE,
  EDIT_PROJECT: PROJECT_EDIT,
  DELETE_PROJECT: PROJECT_DELETE,
  VIEW_PROJECT: PROJECT_VIEW,
  VIEW_ALL_PROJECTS: PROJECT_VIEW_ALL,
  CREATE_USER: USER_CREATE,
  VIEW_USERS: USER_VIEW,
  UPDATE_USER: USER_UPDATE,
  DELETE_USER: USER_DELETE,
  ADD_MEMBER: MEMBER_ADD,
  REMOVE_MEMBER: MEMBER_REMOVE,
  VIEW_MEMBERS: MEMBER_VIEW,
  UPDATE_MEMBER_ROLE: MEMBER_UPDATE_ROLE,
  VIEW_DASHBOARD: DASHBOARD_VIEW,
  VIEW_ROLES: ROLE_VIEW,
  CREATE_ROLE: ROLE_CREATE,
  UPDATE_ROLE: ROLE_UPDATE,
  DELETE_ROLE: ROLE_DELETE,
  ASSIGN_SYSTEM_ROLE: ROLE_ASSIGN_SYSTEM,
  MANAGE_WORKFLOW: WORKFLOW_MANAGE,
  CREATE_BOARD: BOARD_CREATE,
  EDIT_BOARD: BOARD_EDIT,
  CREATE_LABEL: LABEL_CREATE,
  DELETE_LABEL: LABEL_DELETE,
  ASSIGN_LABEL: LABEL_ASSIGN,
  APPROVE_TIMESHEET: TIMESHEET_APPROVE,
  VIEW_TEAM_TIMESHEET: TIMESHEET_VIEW_TEAM,
};

export const usePermission = () => {
  const { user, workspacePermissions, workspaceRoles, globalRoles } = useAuth();
  const { projectId } = useProjectPermission();
  const [projectPermissions, setProjectPermissions] = useState([]);
  const [projectPermissionsLoading, setProjectPermissionsLoading] =
    useState(false);

  // SUPER_ADMIN bypass: if user has SUPER_ADMIN global role, all permissions are granted
  const isSuperAdmin = useCallback(
    () => globalRoles.includes("SUPER_ADMIN"),
    [globalRoles],
  );

  const isSupportAdmin = useCallback(
    () => globalRoles.includes("SUPPORT_ADMIN"),
    [globalRoles],
  );

  useEffect(() => {
    if (!projectId) {
      setProjectPermissions([]);
      setProjectPermissionsLoading(false);
      return;
    }
    setProjectPermissionsLoading(true);
    let cancelled = false;
    const fetch = async () => {
      const perms = await getMyPermissions(projectId);
      if (!cancelled) {
        setProjectPermissions(perms);
        setProjectPermissionsLoading(false);
      }
    };
    fetch();
    return () => {
      cancelled = true;
    };
  }, [projectId]);

  const permissions = useMemo(() => {
    if (projectId && projectPermissions.length > 0) {
      return projectPermissions;
    }
    return workspacePermissions || [];
  }, [projectId, projectPermissions, workspacePermissions]);

  const isCheckingProjectPermissions = useMemo(() => {
    return !!projectId && projectPermissionsLoading;
  }, [projectId, projectPermissionsLoading]);

  // SUPER_ADMIN bypass: always returns true for SUPER_ADMIN
  const hasPermission = useCallback(
    (permission) => {
      if (isSuperAdmin()) return true;
      return permissions.includes(permission);
    },
    [permissions, isSuperAdmin],
  );

  const hasAnyPermission = useCallback(
    (...perms) => {
      if (isSuperAdmin()) return true;
      return perms.some((p) => permissions.includes(p));
    },
    [permissions, isSuperAdmin],
  );

  const canEditTask = useCallback(
    (task) => {
      if (hasPermission(TASK_EDIT_ANY) || hasPermission(TASK_EDIT))
        return true;
      if (hasPermission(TASK_EDIT_ASSIGNED)) {
        return task?.assignee?.id === user?.id;
      }
      return false;
    },
    [hasPermission, user?.id],
  );

  const canDeleteTask = useCallback(
    () => hasPermission(TASK_DELETE),
    [hasPermission],
  );
  const canCreateTask = useCallback(
    () => hasPermission(TASK_CREATE),
    [hasPermission],
  );
  const canCreateProject = useCallback(
    () => hasPermission(PROJECT_CREATE),
    [hasPermission],
  );
  const canEditProject = useCallback(
    () => hasPermission(PROJECT_EDIT),
    [hasPermission],
  );
  const canDeleteProject = useCallback(
    () => hasPermission(PROJECT_DELETE),
    [hasPermission],
  );
  const canViewAllProjects = useCallback(
    () => hasPermission(PROJECT_VIEW_ALL),
    [hasPermission],
  );
  const canCreateUser = useCallback(
    () => hasPermission(USER_CREATE),
    [hasPermission],
  );
  const canUpdateUser = useCallback(
    () => hasPermission(USER_UPDATE),
    [hasPermission],
  );
  const canDeleteUser = useCallback(
    () => hasPermission(USER_DELETE),
    [hasPermission],
  );
  const canViewUsers = useCallback(
    () => hasPermission(USER_VIEW),
    [hasPermission],
  );
  const canAddMember = useCallback(
    () => hasPermission(MEMBER_ADD),
    [hasPermission],
  );
  const canRemoveMember = useCallback(
    () => hasPermission(MEMBER_REMOVE),
    [hasPermission],
  );
  const canUpdateMemberRole = useCallback(
    () => hasPermission(MEMBER_UPDATE_ROLE),
    [hasPermission],
  );
  const canViewMembers = useCallback(
    () => hasPermission(MEMBER_VIEW),
    [hasPermission],
  );
  const canViewTask = useCallback(
    () => hasPermission(TASK_VIEW) || hasPermission(TASK_VIEW_ALL),
    [hasPermission],
  );
  const canViewRoles = useCallback(
    () => hasPermission(ROLE_VIEW),
    [hasPermission],
  );
  const canCreateRole = useCallback(
    () => hasPermission(ROLE_CREATE),
    [hasPermission],
  );
  const canUpdateRole = useCallback(
    () => hasPermission(ROLE_UPDATE),
    [hasPermission],
  );
  const canDeleteRole = useCallback(
    () => hasPermission(ROLE_DELETE),
    [hasPermission],
  );
  const canManageWorkflow = useCallback(
    () => hasPermission(WORKFLOW_MANAGE),
    [hasPermission],
  );
  const canCreateBoard = useCallback(
    () => hasPermission(BOARD_CREATE),
    [hasPermission],
  );
  const canEditBoard = useCallback(
    () => hasPermission(BOARD_EDIT),
    [hasPermission],
  );
  const canCreateLabel = useCallback(
    () => hasPermission(LABEL_CREATE),
    [hasPermission],
  );
  const canDeleteLabel = useCallback(
    () => hasPermission(LABEL_DELETE),
    [hasPermission],
  );
  const canAssignLabel = useCallback(
    () => hasPermission(LABEL_ASSIGN),
    [hasPermission],
  );
  const canApproveTimesheet = useCallback(
    () => hasPermission(TIMESHEET_APPROVE),
    [hasPermission],
  );
  const canViewTeamTimesheet = useCallback(
    () => hasPermission(TIMESHEET_VIEW_TEAM),
    [hasPermission],
  );
  const canAssignTask = useCallback(
    () => hasPermission(TASK_ASSIGN),
    [hasPermission],
  );

  const userRole = useMemo(() => {
    if (!workspaceRoles || workspaceRoles.length === 0) return null;
    return workspaceRoles[0];
  }, [workspaceRoles]);

  const isAdmin = useCallback(
    () => workspaceRoles.includes("ADMIN"),
    [workspaceRoles],
  );
  const isProjectManager = useCallback(
    () => workspaceRoles.includes("PROJECT_MANAGER"),
    [workspaceRoles],
  );
  const isTeamMember = useCallback(
    () => workspaceRoles.includes("TEAM_MEMBER"),
    [workspaceRoles],
  );
  const isUser = useCallback(
    () => workspaceRoles.includes("USER"),
    [workspaceRoles],
  );
  const hasRole = useCallback(
    (role) => workspaceRoles.includes(role),
    [workspaceRoles],
  );
  const hasAnyRole = useCallback(
    (roles) => {
      if (!roles || !Array.isArray(roles)) return false;
      return roles.some((r) => workspaceRoles.includes(r));
    },
    [workspaceRoles],
  );

  const getScope = useCallback(() => {
    if (!userRole) return null;
    if (userRole === "ADMIN" || userRole === "USER") return "SYSTEM";
    return "PROJECT";
  }, [userRole]);

  const userRoleLevel = useMemo(() => {
    const levels = { ADMIN: 5, PROJECT_MANAGER: 4, TEAM_MEMBER: 3, USER: 2 };
    const highestRole = workspaceRoles.reduce((max, role) => {
      const level = levels[role] || 0;
      return level > max ? level : max;
    }, 0);
    return highestRole;
  }, [workspaceRoles]);

  const hasMinRoleLevel = useCallback(
    (minRole) => {
      const levels = { ADMIN: 5, PROJECT_MANAGER: 4, TEAM_MEMBER: 3, USER: 2 };
      const minLevel = levels[minRole] || 0;
      return userRoleLevel >= minLevel;
    },
    [userRoleLevel],
  );

  const canManageAdmins = useCallback(
    () => user?.workspaceOwner === true,
    [user?.workspaceOwner],
  );
  const canTransferOwnership = useCallback(
    () => user?.workspaceOwner === true,
    [user?.workspaceOwner],
  );
  const canManageSystem = useCallback(
    () => user?.workspaceOwner === true || isAdmin(),
    [user?.workspaceOwner, isAdmin],
  );
  const canManageWebhooks = useCallback(
    () => hasPermission(ROLE_ASSIGN_SYSTEM),
    [hasPermission],
  );

  // Employee permissions
  const canViewEmployees = useCallback(
    () => hasPermission(EMPLOYEE_VIEW),
    [hasPermission],
  );
  const canCreateEmployee = useCallback(
    () => hasPermission(EMPLOYEE_CREATE),
    [hasPermission],
  );
  const canUpdateEmployee = useCallback(
    () => hasPermission(EMPLOYEE_UPDATE),
    [hasPermission],
  );
  const canDeleteEmployee = useCallback(
    () => hasPermission(EMPLOYEE_DELETE),
    [hasPermission],
  );

  // Department permissions
  const canViewDepartments = useCallback(
    () => hasPermission(DEPARTMENT_VIEW),
    [hasPermission],
  );
  const canCreateDepartment = useCallback(
    () => hasPermission(DEPARTMENT_CREATE),
    [hasPermission],
  );
  const canUpdateDepartment = useCallback(
    () => hasPermission(DEPARTMENT_UPDATE),
    [hasPermission],
  );
  const canDeleteDepartment = useCallback(
    () => hasPermission(DEPARTMENT_DELETE),
    [hasPermission],
  );

  // Designation permissions
  const canViewDesignations = useCallback(
    () => hasPermission(DESIGNATION_VIEW),
    [hasPermission],
  );
  const canCreateDesignation = useCallback(
    () => hasPermission(DESIGNATION_CREATE),
    [hasPermission],
  );
  const canUpdateDesignation = useCallback(
    () => hasPermission(DESIGNATION_UPDATE),
    [hasPermission],
  );
  const canDeleteDesignation = useCallback(
    () => hasPermission(DESIGNATION_DELETE),
    [hasPermission],
  );

  // Platform permissions
  const canManageWorkspace = useCallback(
    () => hasPermission(WORKSPACE_MANAGE),
    [hasPermission],
  );
  const canManagePlatform = useCallback(
    () => hasPermission(PLATFORM_MANAGE),
    [hasPermission],
  );

  return {
    userRole,
    userRoles: workspaceRoles,
    globalRoles,
    permissions,
    permissionsLoading: false,
    projectPermissionsLoading,
    isCheckingProjectPermissions,
    hasPermission,
    hasAnyPermission,
    canEditTask,
    canDeleteTask,
    canCreateTask,
    canCreateProject,
    canEditProject,
    canDeleteProject,
    canViewAllProjects,
    canCreateUser,
    canUpdateUser,
    canDeleteUser,
    canViewUsers,
    canAddMember,
    canRemoveMember,
    canUpdateMemberRole,
    canViewMembers,
    canViewTask,
    canViewRoles,
    canCreateRole,
    canUpdateRole,
    canDeleteRole,
    canManageWorkflow,
    canCreateBoard,
    canEditBoard,
    canCreateLabel,
    canDeleteLabel,
    canAssignLabel,
    canApproveTimesheet,
    canViewTeamTimesheet,
    canAssignTask,
    canManageAdmins,
    canTransferOwnership,
    canManageSystem,
    canManageWebhooks,
    canViewEmployees,
    canCreateEmployee,
    canUpdateEmployee,
    canDeleteEmployee,
    canViewDepartments,
    canCreateDepartment,
    canUpdateDepartment,
    canDeleteDepartment,
    canViewDesignations,
    canCreateDesignation,
    canUpdateDesignation,
    canDeleteDesignation,
    canManageWorkspace,
    canManagePlatform,
    isAdmin,
    isSuperAdmin,
    isSupportAdmin,
    isUser,
    isProjectManager,
    isTeamMember,
    hasRole,
    hasAnyRole,
    getScope,
    hasMinRoleLevel,
    userRoleLevel,
  };
};
