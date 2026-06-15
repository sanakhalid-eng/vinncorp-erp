import { useCallback, useEffect, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import {
  getUserProjects,
  getProjectMembers,
  inviteAssigneeByEmail,
} from "../api/projectMembersApi";
import { getProjectStatuses } from "../api/statusApi";
import { createTask } from "../api/taskApi";
import { toast } from "sonner";
import { usePermission } from "../context/usePermission";
import { useProjectPermission } from "../context/ProjectPermissionContext.jsx";
import { PageSkeleton } from "../components/LoadingSkeleton";
import { TaskCardSkeleton } from "../components/LoadingSkeleton";
import Button from "../components/Button.jsx";
import Modal from "../components/ui/Modal.jsx";
import { CheckSquare, Loader2, Plus, Settings, Table, UserPlus, Users } from "lucide-react";

const CreateTaskPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { canCreateTask } = usePermission();
  const { setProjectId, clearProjectId } = useProjectPermission();

  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [statuses, setStatuses] = useState([]);
  const [assignees, setAssignees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    assigneeId: null,
    statusId: null,
    priority: "MEDIUM",
    dueDate: null,
  });
  const [submitLoading, setSubmitLoading] = useState(false);

  const [inviteModalOpen, setInviteModalOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteLoading, setInviteLoading] = useState(false);
  const [inviteError, setInviteError] = useState("");

  // Extract projectId from URL query params or location state if available (e.g., from quick create)
  const queryParams = new URLSearchParams(location.search);
  const projectIdFromQuery = queryParams.get("projectId");
  const projectIdFromState = location.state?.projectId;

  useEffect(() => {
    const loadInitialData = async () => {
      try {
        // Load user's projects for the project selector
        const userProjects = await getUserProjects();
        setProjects(userProjects);

        // If projectId is provided in query params or state, use it; otherwise, default to first project
        const projectIdToUse =
          projectIdFromQuery ||
          projectIdFromState ||
          (userProjects.length > 0 ? userProjects[0].id : "");
        if (projectIdToUse) {
          setSelectedProjectId(String(projectIdToUse));
          await loadProjectData(projectIdToUse);
        }
      } catch (error) {
        console.error("Failed to load initial data:", error);
        toast.error("Failed to load projects");
      } finally {
        setLoading(false);
      }
    };

    loadInitialData();
  }, [projectIdFromQuery, projectIdFromState]);

  const loadProjectData = useCallback(async (projectId) => {
    try {
      setLoading(true);
      const [projectStatuses, projectMembers] = await Promise.all([
        getProjectStatuses(projectId),
        getProjectMembers(projectId),
      ]);
      setStatuses(projectStatuses);
      setAssignees(projectMembers);
    } catch (error) {
      console.error("Failed to load project data:", error);
      toast.error("Failed to load project data");
    } finally {
      setLoading(false);
    }
  }, []);

  const handleProjectChange = (e) => {
    const projectId = e.target.value;
    setSelectedProjectId(projectId);
    if (projectId) {
      loadProjectData(projectId);
    } else {
      setStatuses([]);
      setAssignees([]);
    }
  };

  const handleFieldChange = (field, value) => {
    setFormData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const openInviteModal = () => {
    if (!selectedProjectId) {
      toast.error("Select a project first");
      return;
    }
    setInviteEmail("");
    setInviteError("");
    setInviteModalOpen(true);
  };

  const closeInviteModal = () => {
    if (inviteLoading) return;
    setInviteModalOpen(false);
    setInviteEmail("");
    setInviteError("");
  };

  const handleInviteSubmit = async (event) => {
    event.preventDefault();
    const trimmed = inviteEmail.trim();
    if (!trimmed) {
      setInviteError("Email is required");
      return;
    }
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(trimmed)) {
      setInviteError("Please enter a valid email address");
      return;
    }

    setInviteLoading(true);
    setInviteError("");
    try {
      const newMember = await inviteAssigneeByEmail(selectedProjectId, trimmed);
      setAssignees((prev) => {
        const exists = prev.some(
          (m) => (m.userId || m.id) === (newMember.userId || newMember.id)
        );
        if (exists) {
          return prev.map((m) =>
            (m.userId || m.id) === (newMember.userId || newMember.id)
              ? newMember
              : m
          );
        }
        return [...prev, newMember];
      });
      handleFieldChange("assigneeId", newMember.userId || newMember.id);
      toast.success(
        `Invitation sent to ${trimmed}. They've been added as the assignee.`
      );
      setInviteModalOpen(false);
      setInviteEmail("");
    } catch (error) {
      const message =
        error.response?.data?.message ||
        error.message ||
        "Failed to invite assignee";
      setInviteError(message);
    } finally {
      setInviteLoading(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.title.trim()) {
      toast.error("Task title is required");
      return;
    }
    if (!selectedProjectId) {
      toast.error("Please select a project");
      return;
    }
    if (!formData.assigneeId) {
      toast.error("Please assign this task to someone");
      return;
    }

    setSubmitLoading(true);
    try {
      const taskData = {
        ...formData,
        projectId: Number(selectedProjectId),
        // Convert empty strings to null for numeric fields
        assigneeId: formData.assigneeId ? Number(formData.assigneeId) : null,
        statusId: formData.statusId ? Number(formData.statusId) : null,
        dueDate: formData.dueDate || null,
      };

      await createTask(taskData);
      toast.success("Task created successfully");
      // Reset form
      setFormData({
        title: "",
        description: "",
        assigneeId: null,
        statusId: null,
        priority: "MEDIUM",
        dueDate: null,
      });
      // Optionally navigate back or show success
      navigate(-1); // Go back to previous page
    } catch (error) {
      console.error("Failed to create task:", error);
      toast.error(error.response?.data?.message || "Failed to create task");
    } finally {
      setSubmitLoading(false);
    }
  };

  if (loading && !selectedProjectId) {
    return (
      <div className="min-h-screen p-4">
        <PageSkeleton />
      </div>
    );
  }

  return (
    <div className="min-h-screen p-4 md:p-6">
      <div className="mb-6">
        <div className="flex items-center gap-3 mb-4">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-r from-primary-500 to-primary-600 shadow-lg">
            <Plus className="h-5 w-5 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-surface-900 dark:text-surface-100">
            Create New Task
          </h1>
        </div>
        <p className="text-sm text-surface-500 dark:text-surface-400">
          Create a new task and assign it to a project
        </p>
      </div>

      <div className="rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-6">
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Project Selector */}
          <div>
            <label className="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
              Project
            </label>
            <select
              value={selectedProjectId}
              onChange={handleProjectChange}
              className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm"
              disabled={loading}
            >
              <option value="">Select a project</option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.name}
                </option>
              ))}
            </select>
          </div>

          {/* Task Form - only show when project is selected */}
          {selectedProjectId && (
            <>
              {/* Title */}
              <div>
                <label className="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                  Task Title
                </label>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => handleFieldChange("title", e.target.value)}
                  placeholder="Enter task title"
                  className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm"
                  required
                />
              </div>

              {/* Description */}
              <div>
                <label className="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                  Description (Optional)
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => handleFieldChange("description", e.target.value)}
                  placeholder="Enter task description"
                  className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm min-h-[100px] resize-y"
                />
              </div>

              {/* Assignee */}
              <div>
                <label className="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                  Assignee <span className="text-danger-500">*</span>
                </label>
                <select
                  value={formData.assigneeId || ""}
                  onChange={(e) =>
                    handleFieldChange("assigneeId", e.target.value)
                  }
                  className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm"
                  required
                >
                  <option value="" disabled>
                    Select an assignee
                  </option>
                  {assignees.map((member) => (
                    <option
                      key={member.userId || member.id}
                      value={member.userId || member.id}
                    >
                      {member.name || member.email}
                    </option>
                  ))}
                </select>
                <button
                  type="button"
                  onClick={openInviteModal}
                  className="mt-2 inline-flex items-center gap-1.5 text-sm font-medium text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 transition-colors"
                >
                  <UserPlus className="h-4 w-4" />
                  Invite new assignee
                </button>
              </div>

              {/* Status */}
              <div>
                <label className="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                  Status
                </label>
                <select
                  value={formData.statusId || ""}
                  onChange={(e) =>
                    handleFieldChange("statusId", e.target.value)
                  }
                  className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm"
                >
                  <option value="">No Status</option>
                  {statuses.map((status) => (
                    <option key={status.id} value={status.id}>
                      {status.name}
                    </option>
                  ))}
                </select>
              </div>

              {/* Priority */}
              <div>
                <label className="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                  Priority
                </label>
                <select
                  value={formData.priority}
                  onChange={(e) =>
                    handleFieldChange("priority", e.target.value)
                  }
                  className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm"
                >
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
              </div>

              {/* Due Date */}
              <div>
                <label className="block text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                  Due Date (Optional)
                </label>
                <input
                  type="date"
                  value={formData.dueDate || ""}
                  onChange={(e) => handleFieldChange("dueDate", e.target.value)}
                  className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm"
                />
              </div>


            </>
          )}

          {/* Submit Button */}
          <div className="flex justify-end">
            <Button
              type="submit"
              loading={submitLoading}
              disabled={!selectedProjectId || !canCreateTask()}
              className="w-auto"
            >
              {submitLoading ? "Creating..." : "Create Task"}
            </Button>
          </div>
        </form>
      </div>

      <Modal
        open={inviteModalOpen}
        onClose={closeInviteModal}
        title="Invite new assignee"
        description="Send an invitation email and add them to this project."
        size="sm"
        footer={
          <>
            <button
              type="button"
              onClick={closeInviteModal}
              disabled={inviteLoading}
              className="btn-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              form="invite-assignee-form"
              disabled={inviteLoading}
              className="btn-primary"
            >
              {inviteLoading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Sending...
                </>
              ) : (
                "Send invitation"
              )}
            </button>
          </>
        }
      >
        <form
          id="invite-assignee-form"
          onSubmit={handleInviteSubmit}
          className="space-y-3"
        >
          <label className="block text-sm font-medium text-surface-700 dark:text-surface-300">
            Email address
          </label>
          <input
            type="email"
            value={inviteEmail}
            onChange={(e) => {
              setInviteEmail(e.target.value);
              if (inviteError) setInviteError("");
            }}
            placeholder="name@company.com"
            className="w-full px-4 py-3 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm"
            autoFocus
            disabled={inviteLoading}
            required
          />
          {inviteError && (
            <p className="text-sm text-danger-600 dark:text-danger-400">
              {inviteError}
            </p>
          )}
          <p className="text-xs text-surface-500 dark:text-surface-400">
            If the person doesn't have an account yet, we'll create one for them
            and email them an activation link. Existing users will be added to
            the project and notified by email.
          </p>
        </form>
      </Modal>
    </div>
  );
};

export default CreateTaskPage;
