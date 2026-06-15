import { useState, useEffect } from "react";
import RoleTable from "../components/RoleTable";
import RoleFormModal from "../components/RoleFormModal";
import AssignRoleModal from "../components/AssignRoleModal";
import RoleBadge from "../components/members/RoleBadge";
import ConfirmationDialog from "../components/members/ConfirmationDialog";
import {
  Shield,
  Plus,
  Users,
  ChevronLeft,
  ChevronRight,
  Settings,
} from "lucide-react";
import { Toaster } from "sonner";
import { getAllRoles, deleteRole, updateRolePermissions } from "../api/roleApi.js";
import { getUserProjects } from "../api/projectApi.js";
import PermissionMatrix from "../components/PermissionMatrix.jsx";
import { ShieldCheck, Save } from "lucide-react";
import { toast } from "sonner";
import { usePermission } from "../context/usePermission.js";

export default function Roles() {
  const [roles, setRoles] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingRole, setEditingRole] = useState(null);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [permissionsMap, setPermissionsMap] = useState({});
  const [unsavedChanges, setUnsavedChanges] = useState(false);
  const [permissionsLoading] = useState(false);
  const [showMatrix, setShowMatrix] = useState(false);
  const [saveLoading, setSaveLoading] = useState(false);
  const { isAdmin, canUpdateMemberRole, canManageSystem, canManageAdmins } =
    usePermission();

  useEffect(() => {
    loadRoles();

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadRoles = async () => {
    setLoading(true);
    try {
      const roleList = await getAllRoles();
      const projectList = await getUserProjects();
      setRoles(roleList);
      setProjects(projectList);
      if (!selectedProjectId && projectList.length > 0) {
        setSelectedProjectId(String(projectList[0].id));
      }
      const map = {};
      roleList.forEach((role) => {
        map[role.id] = new Set(role.permissions || []);
      });
      setPermissionsMap(map);
    } catch (error) {
      console.error("Roles API error:", error);
      setRoles([]);
      setPermissionsMap({});
      toast.error(error.response?.data?.message || "Failed to load roles");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateRole = () => {
    setEditingRole(null);
    setShowCreateModal(true);
  };

  const handleEditRole = (role) => {
    setEditingRole(role);
    setShowEditModal(true);
  };

  const handleDeleteRole = (role) => {
    setConfirmAction(() => async () => {
      try {
        await deleteRole(role.id);
        setRoles(roles.filter((r) => r.id !== role.id));
        toast.success("Role deleted successfully");
      } catch {
        toast.error("Failed to delete role");
      }
      setShowConfirmDialog(false);
    });
    setShowConfirmDialog(true);
  };

  const handlePermissionToggle = (roleId, permId, enabled) => {
    setPermissionsMap((prev) => {
      const newMap = { ...prev };
      const rolePerms = new Set(newMap[roleId]);
      if (enabled) {
        rolePerms.add(permId);
      } else {
        rolePerms.delete(permId);
      }
      newMap[roleId] = rolePerms;
      return newMap;
    });
    setUnsavedChanges(true);
  };

  const handleSavePermissions = async () => {
    setSaveLoading(true);
    try {
      // Save permissions for each role that changed
      const savePromises = Object.entries(permissionsMap).map(
        async ([roleId, permSet]) => {
          const permissionIds = Array.from(permSet).map(Number);
          return updateRolePermissions(Number(roleId), permissionIds);
        }
      );

      await Promise.all(savePromises);

      toast.success("Permissions updated successfully");
      setUnsavedChanges(false);
      loadRoles();
    } catch (error) {
      console.error("Save permissions error:", error);
      toast.error(error?.response?.data?.message || "Failed to update permissions");
    } finally {
      setSaveLoading(false);
    }
  };

  const handleModalSuccess = () => {
    loadRoles();
    setShowCreateModal(false);
    setShowEditModal(false);
  };

  const selectedProject = projects.find(
    (p) => String(p.id) === String(selectedProjectId),
  );
  const roleCount = roles.length;
  const systemRoles = Array.isArray(roles)
    ? roles.filter((r) => ["ADMIN", "PROJECT_MANAGER"].includes(r.name)).length
    : 0;

  return (
    <>
      <div className="page-container space-y-6">
        <div className="page-header">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 md:w-12 md:h-12 bg-gradient-to-r from-red-500 to-pink-600 rounded-2xl flex items-center justify-center shadow-lg">
              <Shield className="w-5 h-5 md:w-6 md:h-6 text-white" />
            </div>
            <div>
              <h1 className="text-xl md:text-2xl lg:text-3xl font-bold text-surface-900 dark:text-surface-100">
                Roles & Permissions
              </h1>
              <p className="text-sm md:text-base text-surface-600 dark:text-surface-400">
                Manage roles across 
                <span className="font-semibold">{selectedProject?.name}</span>
              </p>
            </div>
          </div>
        </div>

        <div className="flex flex-wrap gap-3">
          <div className="flex items-center gap-2 px-4 py-2 bg-emerald-100 dark:bg-emerald-900/20 text-emerald-800 dark:text-emerald-300 rounded-xl">
            <RoleBadge role="ADMIN" />
            <span className="text-lg font-bold">{systemRoles}</span>
            <span className="text-sm font-medium">System</span>
          </div>
          <div className="flex items-center gap-2 px-4 py-2 bg-indigo-100 dark:bg-indigo-900/20 text-indigo-800 dark:text-indigo-300 rounded-xl">
            <RoleBadge role="PROJECT_MANAGER" />
            <span className="text-lg font-bold">
              {roles.length - systemRoles}
            </span>
            <span className="text-sm font-medium">Custom</span>
          </div>
          <span className="px-4 py-2 bg-white dark:bg-surface-900 text-sm font-bold text-surface-900 dark:text-surface-100 rounded-xl border border-surface-200 dark:border-surface-700">
            {roleCount} total
          </span>
        </div>

        <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between bg-white dark:bg-surface-900 rounded-xl p-4 md:p-6 shadow-soft border border-surface-200/50 dark:border-surface-800/50">
          <div className="flex items-center gap-3 flex-1 flex-wrap">
            <div className="flex items-center gap-2 px-4 py-2 bg-surface-50 dark:bg-surface-800 rounded-xl border border-surface-200 dark:border-surface-700">
              <label className="text-sm font-medium text-surface-700 dark:text-surface-300">
                Project:
              </label>
              <select
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(e.target.value)}
                className="bg-transparent font-medium text-surface-900 dark:text-surface-100 focus:outline-none appearance-none text-sm"
              >
                {projects.map((project) => (
                  <option key={project.id} value={project.id}>
                    {project.name}
                  </option>
                ))}
              </select>
            </div>

            <button
              onClick={() => setShowMatrix(!showMatrix)}
              className="btn-primary btn-sm"
            >
              <ShieldCheck className="w-4 h-4" />
              {showMatrix ? "Role List" : "Permission Matrix"}
            </button>
          </div>

          <div className="flex flex-wrap gap-2">
            {unsavedChanges && (
              <button
                onClick={handleSavePermissions}
                disabled={saveLoading}
                className="btn btn-sm bg-gradient-to-r from-orange-500 to-red-600 text-white disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                {saveLoading ? "Saving..." : "Save Permissions"}
              </button>
            )}
            {canUpdateMemberRole() && (
              <button
                onClick={() => setShowAssignModal(true)}
                className="btn-primary btn-sm"
              >
                <Users className="w-4 h-4" />
                Assign Role
              </button>
            )}
            {isAdmin() && (
              <button
                onClick={handleCreateRole}
                className="btn-primary btn-sm bg-gradient-to-r from-indigo-600 to-purple-600"
              >
                <Plus className="w-4 h-4" />
                Create Role
              </button>
            )}
          </div>
        </div>

        {showMatrix ? (
          <PermissionMatrix
            roles={roles}
            permissionsMap={permissionsMap}
            unsavedChanges={unsavedChanges}
            onPermissionToggle={handlePermissionToggle}
            onSave={handleSavePermissions}
            loading={permissionsLoading}
            projectId={selectedProjectId}
          />
        ) : (
          <RoleTable
            roles={roles}
            loading={loading}
            onEdit={handleEditRole}
            onDelete={handleDeleteRole}
          />
        )}
      </div>

      <Toaster position="top-right" richColors />

      <RoleFormModal
        isOpen={showCreateModal || showEditModal}
        onClose={() => {
          setShowCreateModal(false);
          setShowEditModal(false);
          setEditingRole(null);
        }}
        role={editingRole}
        onSuccess={handleModalSuccess}
      />

      <AssignRoleModal
        isOpen={showAssignModal}
        onClose={() => setShowAssignModal(false)}
        projectId={selectedProjectId}
        onSuccess={loadRoles}
      />

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => setShowConfirmDialog(false)}
        onConfirm={confirmAction}
        title="Delete Role?"
        message="This action cannot be undone. Users with this role will lose their permissions."
      />
    </>
  );
}
