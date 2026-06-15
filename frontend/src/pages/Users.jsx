import { useState, useEffect } from "react";
import UserTable from "../components/UserTable";
import UserFormModal from "../components/UserFormModal";
import AssignRoleModal from "../components/AssignRoleModal";
import SystemRoleModal from "../components/SystemRoleModal";
import RoleBadge from "../components/members/RoleBadge";
import ConfirmationDialog from "../components/members/ConfirmationDialog";
import { User, Plus, ChevronLeft, ChevronRight, UserPlus } from "lucide-react";
import { Toaster } from "sonner";
import { getAllUsers, deleteUser } from "../api/userApi.js";
import { getAllRoles } from "../api/roleApi.js";
import { getUserProjects } from "../api/projectApi.js";
import { toast } from "sonner";
import { usePermission } from "../context/usePermission.js";

export default function Users() {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [showSystemRoleModal, setShowSystemRoleModal] = useState(false);
  const [selectedUserForSystemRole, setSelectedUserForSystemRole] =
    useState(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const {
    canCreateUser,
    canUpdateUser,
    canDeleteUser,
    canUpdateMemberRole,
    canManageAdmins,
    canTransferOwnership,
    isAdmin,
    hasPermission,
  } = usePermission();

  useEffect(() => {
    loadData();

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [usersRes, rolesRes, projectsRes] = await Promise.all([
        getAllUsers(),
        getAllRoles(),
        getUserProjects(),
      ]);
      setUsers(usersRes);
      setRoles(rolesRes);
      setProjects(projectsRes);
      if (!selectedProjectId && projectsRes.length > 0) {
        setSelectedProjectId(String(projectsRes[0].id));
      }
    } catch (error) {
      console.error("Load data error:", error);
      toast.error("Failed to load data");
    } finally {
      setLoading(false);
    }
  };

  const handleCreateUser = () => {
    setEditingUser(null);
    setShowCreateModal(true);
  };

  const handleEditUser = (user) => {
    setEditingUser(user);
    setShowEditModal(true);
  };

  const handleDeleteUser = (user) => {
    setConfirmAction(() => async () => {
      try {
        await deleteUser(user.id);
        setUsers(users.filter((u) => u.id !== user.id));
        toast.success("User deleted successfully");
      } catch {
        toast.error("Failed to delete user");
      }
      setShowConfirmDialog(false);
    });
    setShowConfirmDialog(true);
  };

  const handleModalSuccess = () => {
    loadData();
    setShowCreateModal(false);
    setShowEditModal(false);
  };

  const userCount = users.length;
  const adminCount = users.filter((u) => u.role === "ADMIN").length;
  const selectedProject = projects.find(
    (p) => String(p.id) === String(selectedProjectId),
  );

  return (
    <>
      <div className="page-container space-y-6">
        <div className="page-header">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 md:w-12 md:h-12 bg-gradient-to-r from-emerald-500 to-teal-600 rounded-2xl flex items-center justify-center shadow-lg">
              <User className="w-5 h-5 md:w-6 md:h-6 text-white" />
            </div>
            <div>
              <h1 className="text-xl md:text-2xl lg:text-3xl font-bold text-surface-900 dark:text-surface-100">
                User Management
              </h1>
              <p className="text-sm md:text-base text-surface-600 dark:text-surface-400">
                Manage team members across 
                <span className="font-semibold">{selectedProject?.name}</span>
              </p>
            </div>
          </div>
        </div>

        <div className="flex flex-wrap gap-3">
          <div className="flex items-center gap-2 px-4 py-2 bg-emerald-100 dark:bg-emerald-900/20 text-emerald-800 dark:text-emerald-300 rounded-xl">
            <RoleBadge role="ADMIN" />
            <span className="text-lg font-bold">{adminCount}</span>
            <span className="text-sm font-medium">Admins</span>
          </div>
          <div className="flex items-center gap-2 px-4 py-2 bg-indigo-100 dark:bg-indigo-900/20 text-indigo-800 dark:text-indigo-300 rounded-xl">
            <RoleBadge role="PROJECT_MANAGER" />
            <span className="text-lg font-bold">
              {users.length - adminCount}
            </span>
            <span className="text-sm font-medium">Team</span>
          </div>
          <span className="px-4 py-2 bg-white dark:bg-surface-900 text-sm font-bold text-surface-900 dark:text-surface-100 rounded-xl border border-surface-200 dark:border-surface-700">
            {userCount} total
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
          </div>

          <div className="flex flex-wrap gap-2">
            {canUpdateMemberRole() && (
              <button
                onClick={() => setShowAssignModal(true)}
                className="btn-primary btn-sm"
              >
                <User className="w-4 h-4" />
                Assign Project Role
              </button>
            )}
            {canCreateUser() && (
              <button
                onClick={handleCreateUser}
                className="btn-primary btn-sm bg-gradient-to-r from-emerald-500 to-teal-600"
              >
                <Plus className="w-4 h-4" />
                Create User
              </button>
            )}
          </div>
        </div>

        <UserTable
          users={users}
          loading={loading}
          onEdit={handleEditUser}
          onDelete={handleDeleteUser}
          onAssignSystemRole={(user) => {
            setSelectedUserForSystemRole(user);
            setShowSystemRoleModal(true);
          }}
          canAssignSystemRole={canManageAdmins()}
        />
      </div>

      <Toaster position="top-right" richColors />

      <UserFormModal
        isOpen={showCreateModal || showEditModal}
        onClose={() => {
          setShowCreateModal(false);
          setShowEditModal(false);
          setEditingUser(null);
        }}
        user={editingUser}
        availableRoles={roles}
        onSuccess={handleModalSuccess}
      />

      <AssignRoleModal
        isOpen={showAssignModal}
        onClose={() => setShowAssignModal(false)}
        projectId={selectedProjectId}
        onSuccess={loadData}
      />

      <SystemRoleModal
        isOpen={showSystemRoleModal}
        onClose={() => setShowSystemRoleModal(false)}
        userId={selectedUserForSystemRole?.id}
        userName={selectedUserForSystemRole?.name}
        onSuccess={loadData}
      />

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => setShowConfirmDialog(false)}
        onConfirm={confirmAction}
        title="Delete User?"
        message="This action cannot be undone. Associated data may be affected."
      />
    </>
  );
}
