import { useState } from "react";
import { ShieldCheck, Save, AlertCircle } from "lucide-react";
import RoleBadge from "./members/RoleBadge.jsx";
import { cn } from "../utils/cn.js";
import { toast } from "sonner";
import {
  TASK_CREATE,
  TASK_EDIT,
  TASK_EDIT_ANY,
  TASK_EDIT_ASSIGNED,
  TASK_DELETE,
  TASK_MOVE,
  TASK_ASSIGN,
  TASK_VIEW,
  TASK_VIEW_ALL,
  PROJECT_CREATE,
  PROJECT_EDIT,
  PROJECT_DELETE,
  PROJECT_VIEW,
  PROJECT_VIEW_ALL,
  USER_CREATE,
  USER_VIEW,
  USER_UPDATE,
  USER_DELETE,
  DASHBOARD_VIEW,
  MEMBER_ADD,
  MEMBER_REMOVE,
  MEMBER_VIEW,
  MEMBER_UPDATE_ROLE,
  ROLE_VIEW,
  ROLE_CREATE,
  ROLE_UPDATE,
  ROLE_DELETE,
  ROLE_ASSIGN_SYSTEM,
  WORKFLOW_MANAGE,
  BOARD_CREATE,
  BOARD_EDIT,
  LABEL_CREATE,
  LABEL_DELETE,
  LABEL_ASSIGN,
  TIMESHEET_APPROVE,
  TIMESHEET_VIEW_TEAM,
} from "../constants/permissions.js";

const PERMISSIONS = [
  { id: TASK_CREATE, name: TASK_CREATE, description: "Create tasks", group: "Task" },
  { id: TASK_EDIT, name: TASK_EDIT, description: "Edit tasks", group: "Task" },
  { id: TASK_EDIT_ANY, name: TASK_EDIT_ANY, description: "Edit any task", group: "Task" },
  { id: TASK_EDIT_ASSIGNED, name: TASK_EDIT_ASSIGNED, description: "Edit assigned tasks", group: "Task" },
  { id: TASK_DELETE, name: TASK_DELETE, description: "Delete tasks", group: "Task" },
  { id: TASK_MOVE, name: TASK_MOVE, description: "Move tasks between columns", group: "Task" },
  { id: TASK_ASSIGN, name: TASK_ASSIGN, description: "Assign tasks to users", group: "Task" },
  { id: TASK_VIEW, name: TASK_VIEW, description: "View assigned tasks", group: "Task" },
  { id: TASK_VIEW_ALL, name: TASK_VIEW_ALL, description: "View all tasks", group: "Task" },
  { id: PROJECT_CREATE, name: PROJECT_CREATE, description: "Create new projects", group: "Project" },
  { id: PROJECT_EDIT, name: PROJECT_EDIT, description: "Edit project details", group: "Project" },
  { id: PROJECT_DELETE, name: PROJECT_DELETE, description: "Delete projects", group: "Project" },
  { id: PROJECT_VIEW, name: PROJECT_VIEW, description: "View own/assigned projects", group: "Project" },
  { id: PROJECT_VIEW_ALL, name: PROJECT_VIEW_ALL, description: "View all projects", group: "Project" },
  { id: USER_CREATE, name: USER_CREATE, description: "Create users", group: "User" },
  { id: USER_VIEW, name: USER_VIEW, description: "View users", group: "User" },
  { id: USER_UPDATE, name: USER_UPDATE, description: "Update users", group: "User" },
  { id: USER_DELETE, name: USER_DELETE, description: "Delete users", group: "User" },
  { id: DASHBOARD_VIEW, name: DASHBOARD_VIEW, description: "Access dashboard", group: "Dashboard" },
  { id: MEMBER_ADD, name: MEMBER_ADD, description: "Add member to project", group: "Member" },
  { id: MEMBER_REMOVE, name: MEMBER_REMOVE, description: "Remove member from project", group: "Member" },
  { id: MEMBER_VIEW, name: MEMBER_VIEW, description: "View project members", group: "Member" },
  { id: MEMBER_UPDATE_ROLE, name: MEMBER_UPDATE_ROLE, description: "Update member role", group: "Member" },
  { id: ROLE_VIEW, name: ROLE_VIEW, description: "View all roles", group: "Role" },
  { id: ROLE_CREATE, name: ROLE_CREATE, description: "Create roles", group: "Role" },
  { id: ROLE_UPDATE, name: ROLE_UPDATE, description: "Update roles", group: "Role" },
  { id: ROLE_DELETE, name: ROLE_DELETE, description: "Delete roles", group: "Role" },
  { id: ROLE_ASSIGN_SYSTEM, name: ROLE_ASSIGN_SYSTEM, description: "Assign system role to user", group: "Role" },
  { id: WORKFLOW_MANAGE, name: WORKFLOW_MANAGE, description: "Manage workflow statuses & transitions", group: "Workflow" },
  { id: BOARD_CREATE, name: BOARD_CREATE, description: "Create boards", group: "Board" },
  { id: BOARD_EDIT, name: BOARD_EDIT, description: "Edit boards & columns", group: "Board" },
  { id: LABEL_CREATE, name: LABEL_CREATE, description: "Create labels", group: "Label" },
  { id: LABEL_DELETE, name: LABEL_DELETE, description: "Delete labels", group: "Label" },
  { id: LABEL_ASSIGN, name: LABEL_ASSIGN, description: "Assign labels to tasks", group: "Label" },
  { id: TIMESHEET_APPROVE, name: TIMESHEET_APPROVE, description: "Approve timesheets", group: "Timesheet" },
  { id: TIMESHEET_VIEW_TEAM, name: TIMESHEET_VIEW_TEAM, description: "View team timesheets", group: "Timesheet" },
];

const PermissionMatrix = ({
  roles = [],
  permissionsMap = {},
  // {roleId: Set<permissionId>}
  onPermissionToggle,
  onSave,
  unsavedChanges = false,
  loading = false,
  projectId,
}) => {
  const [localChanges, setLocalChanges] = useState(new Set());

  const handleToggle = (roleId, permissionId) => {
    const key = `${roleId}-${permissionId}`;
    const newChanges = new Set(localChanges);
    if (newChanges.has(key)) {
      newChanges.delete(key);
    } else {
      newChanges.add(key);
    }
    setLocalChanges(newChanges);

    const hasPermission = permissionsMap[roleId]?.has(permissionId);
    onPermissionToggle(roleId, permissionId, !hasPermission, key);
  };

  const handleSave = async () => {
    await onSave(localChanges);
    setLocalChanges(new Set());
  };

  const hasUnsaved = localChanges.size > 0 || unsavedChanges;

  return (
    <div className="bg-white/70 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/50 overflow-hidden">
      {/* Header */}
      <div className="bg-gradient-to-r from-indigo-600 via-purple-600 to-emerald-600 px-8 py-6 text-white">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 bg-white/20 rounded-2xl flex items-center justify-center backdrop-blur-sm">
            <ShieldCheck className="w-7 h-7" />
          </div>
          <div>
            <h2 className="text-2xl font-bold">Permission Matrix</h2>
            <p className="text-indigo-100">
              {roles.length} roles × {PERMISSIONS.length} permissions
            </p>
          </div>
          <div className="ml-auto flex items-center gap-2">
            <span className="text-sm font-medium px-3 py-1 bg-white/20 rounded-xl backdrop-blur-sm">
              {Object.values(permissionsMap).reduce(
                (acc, perms) => acc + perms.size,
                0,
              )} 
              / {roles.length * PERMISSIONS.length}
            </span>
            <button
              onClick={handleSave}
              disabled={!hasUnsaved || loading}
              className={cn(
                "flex items-center gap-2 px-6 py-2.5 font-semibold rounded-2xl transition-all shadow-lg",
                hasUnsaved && !loading
                  ? "bg-white text-indigo-700 hover:shadow-2xl hover:scale-[1.02] hover:bg-indigo-50"
                  : "bg-white/50 text-white/70 cursor-not-allowed",
              )}
            >
              <Save className="w-4 h-4" />
              {loading ? "Saving..." : "Save All Changes"}
            </button>
          </div>
        </div>
        {hasUnsaved && (
          <div className="mt-3 flex items-center gap-2 text-sm bg-yellow-500/20 text-yellow-100 px-4 py-2 rounded-xl">
            <AlertCircle className="w-4 h-4" />
            {localChanges.size} unsaved changes
          </div>
        )}
      </div>

      {/* Legend */}
      <div className="px-8 py-4 bg-gradient-to-r from-gray-50 to-indigo-50 border-b border-gray-200">
        <div className="flex items-center gap-6 text-xs uppercase tracking-wide text-gray-500 font-semibold">
          <div className="flex items-center gap-2 bg-emerald-100 text-emerald-800 px-3 py-1 rounded-lg">
            <div className="w-4 h-4 bg-emerald-500 rounded-full" />
            Enabled
          </div>
          <div className="flex items-center gap-2 bg-gray-100 text-gray-600 px-3 py-1 rounded-lg">
            <div className="w-4 h-4 border-2 border-gray-400 rounded-full" />
            Disabled
          </div>
          <div className="ml-auto text-indigo-600 font-bold">
            Toggle to grant/revoke permissions
          </div>
        </div>
      </div>

      {/* Scrollable Grid */}
      <div className="overflow-auto max-h-[70vh]">
        <table className="w-full table-fixed">
          <thead className="bg-gray-50/50 sticky top-0 z-10">
            <tr>
              {/* Role Header */}
              <th className="w-80 bg-white/80 backdrop-blur border-r-2 border-gray-200 px-6 py-4 text-left">
                <div className="font-bold text-lg text-gray-900">Roles</div>
              </th>
              {/* Permission Headers */}
              {PERMISSIONS.map((perm) => (
                <th
                  key={perm.id}
                  className="bg-white/80 backdrop-blur px-3 py-4 text-left border-l border-gray-100 w-32 min-w-[8rem] max-w-[10rem]"
                  title={perm.description}
                >
                  <div className="font-semibold text-xs text-gray-900 truncate">
                    {perm.name}
                  </div>
                  <div className="text-xs text-gray-500">
                    {perm.description}
                  </div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {roles.map((role) => (
              <tr
                key={role.id}
                className="border-t border-gray-100 hover:bg-indigo-50/50 group"
              >
                {/* Role Column - Sticky */}
                <td className="sticky left-0 bg-white/90 backdrop-blur z-20 border-r-2 border-gray-200 shadow-sm">
                  <div className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <RoleBadge role={role.name} />
                      <div>
                        <div className="font-semibold text-gray-900">
                          {role.name}
                        </div>
                        <div className="text-sm text-gray-500">
                          {role.description || "No description"}
                        </div>
                        <div className="text-xs text-gray-400 mt-1">
                          {permissionsMap[role.id]?.size || 0} / 
                          {PERMISSIONS.length} permissions
                        </div>
                      </div>
                    </div>
                  </div>
                </td>
                {/* Permission Cells */}
                {PERMISSIONS.map((perm) => {
                  const enabled =
                    permissionsMap[role.id]?.has(perm.id) || false;
                  const isChanged = localChanges.has(`${role.id}-${perm.id}`);
                  return (
                    <td
                      key={perm.id}
                      className="px-2 py-3 relative group-hover:bg-indigo-50/30"
                      title={perm.description}
                    >
                      <label className="relative inline-flex items-center cursor-pointer w-11 h-6">
                        <input
                          type="checkbox"
                          checked={enabled}
                          onChange={() => handleToggle(role.id, perm.id)}
                          className="sr-only peer"
                          disabled={loading}
                        />
                        <div
                          className={cn(
                            'w-11 h-6 bg-gray-200 peer-focus:outline-none rounded-full peer-focus:ring-2 peer-focus:ring-indigo-500 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[""] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-emerald-600 shadow-md',
                            isChanged &&
                              "ring-2 ring-yellow-400 ring-opacity-50",
                          )}
                        />
                      </label>
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {roles.length === 0 && (
        <div className="text-center py-24 text-gray-500">
          No roles available. Create roles first to assign permissions.
        </div>
      )}
    </div>
  );
};

export default PermissionMatrix;
