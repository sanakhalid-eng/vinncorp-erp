import { Fragment, useEffect, useState } from "react";
import { Dialog, Transition } from "@headlessui/react";
import {
  Briefcase,
  Calendar,
  CheckCircle,
  Tag,
  X,
  Loader2,
  UserPlus,
} from "lucide-react";
import { toast } from "sonner";
import PriorityBadge from "./PriorityBadge.jsx";
import StatusBadge from "./StatusBadge.jsx";
import { createTask, getProjectAssignees, updateTask } from "../../api/taskApi";
import { getProjectStatuses } from "../../../projects/api/statusApi";
import { inviteAssigneeByEmail } from "../../../projects/api/projectMembersApi";
import { usePermission } from "../../../../context/usePermission.js";
import LabelPicker from "./LabelPicker.jsx";
import Modal from "../../../../components/ui/Modal.jsx";

const TaskDrawer = ({
  isOpen,
  onClose,
  task,
  projectId,
  projects = [],
  columns = [],
  onTaskSaved,
}) => {
  const [loading, setLoading] = useState(false);
  const [assignees, setAssignees] = useState([]);
  const [statuses, setStatuses] = useState([]);

  const [inviteModalOpen, setInviteModalOpen] = useState(false);
  const [inviteEmail, setInviteEmail] = useState("");
  const [inviteLoading, setInviteLoading] = useState(false);
  const [inviteError, setInviteError] = useState("");
  const {
    canEditTask,
    canCreateTask,
    canAssignTask,
    isAdmin,
    isProjectManager,
  } = usePermission();
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    priority: "MEDIUM",
    statusId: "",
    assigneeId: "",
    dueDate: "",
    projectId: projectId || "",
    columnId: "",
  });
  const activeProjectId = formData.projectId || projectId || "";

  useEffect(() => {
    if (!isOpen) return;
    if (task) {
      setFormData({
        title: task.title || "",
        description: task.description || "",
        priority: task.priority || "MEDIUM",
        statusId: task.statusId || "",
        assigneeId: task.assignee?.id || "",
        dueDate: task.dueDate ? task.dueDate.split("T")[0] : "",
        projectId: task.project?.id || projectId || "",
      });
    } else {
      setFormData({
        title: "",
        description: "",
        priority: "MEDIUM",
        statusId: "",
        assigneeId: "",
        dueDate: "",
        projectId: projectId || "",
      });
    }
  }, [isOpen, projectId, task]);

  const canEdit = task ? canEditTask(task) : canCreateTask();
  const canChangeAssignee =
    canEdit && (isAdmin() || isProjectManager() || canAssignTask());

  useEffect(() => {
    if (!isOpen || !activeProjectId) {
      setAssignees([]);
      setStatuses([]);
      return;
    }
    loadAssignees(activeProjectId);
    loadStatuses(activeProjectId);
  }, [activeProjectId, isOpen]);

  const loadAssignees = async (currentProjectId) => {
    try {
      const members = await getProjectAssignees(currentProjectId);
      setAssignees(members);
    } catch (error) {
      console.error("Load assignees error:", error);
    }
  };

  const loadStatuses = async (currentProjectId) => {
    try {
      const projectStatuses = await getProjectStatuses(currentProjectId);
      setStatuses(projectStatuses);
      setFormData((prev) => ({
        ...prev,
        statusId: prev.statusId || projectStatuses[0]?.id || "",
      }));
    } catch (error) {
      console.error("Load statuses error:", error);
      setStatuses([]);
    }
  };

  const openInviteModal = () => {
    if (!activeProjectId) {
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
      const newMember = await inviteAssigneeByEmail(activeProjectId, trimmed);
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
      setFormData((prev) => ({
        ...prev,
        assigneeId: newMember.userId || newMember.id,
      }));
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

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (!formData.title || !formData.projectId) return;
    if (!formData.assigneeId) {
      toast.error("Please assign this task to someone");
      return;
    }
    setLoading(true);
    try {
      const payload = {
        ...formData,
        projectId: Number(formData.projectId),
        assigneeId: formData.assigneeId ? Number(formData.assigneeId) : null,
        statusId: formData.statusId ? Number(formData.statusId) : null,
        dueDate: formData.dueDate ? `${formData.dueDate}T00:00:00` : null,
        columnId: formData.columnId ? Number(formData.columnId) : null,
      };
      if (task) {
        await updateTask(task.id, payload);
        toast.success("Task updated successfully!");
      } else {
        await createTask(payload);
        toast.success("Task created successfully!");
      }
      await onTaskSaved?.();
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to save task");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Transition appear show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={loading ? () => {} : onClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm" />
        </Transition.Child>

        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-2xl transform overflow-hidden rounded-2xl bg-white dark:bg-surface-900 shadow-soft-lg border border-surface-200/50 dark:border-surface-800/50 transition-all">
                <div className="flex items-center justify-between px-6 py-4 border-b border-surface-200 dark:border-surface-800">
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-lg bg-primary-100 dark:bg-primary-900/30">
                      <CheckCircle className="h-5 w-5 text-primary-600 dark:text-primary-400" />
                    </div>
                    <div>
                      <Dialog.Title className="text-lg font-semibold text-surface-900 dark:text-surface-100">
                        {task ? "Edit Task" : "Create New Task"}
                      </Dialog.Title>
                      <Dialog.Description className="text-sm text-surface-500 dark:text-surface-400">
                        {task
                          ? `Editing: ${task.title}`
                          : "Fill in the details to create a new task"}
                      </Dialog.Description>
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={onClose}
                    className="rounded-lg p-1.5 text-surface-400 hover:text-surface-600 hover:bg-surface-100 dark:hover:text-surface-200 dark:hover:bg-surface-800 transition-colors"
                    disabled={loading}
                  >
                    <X className="h-5 w-5" />
                  </button>
                </div>

                <form id="task-form" onSubmit={handleSubmit} className="p-6 space-y-5">
                  <div>
                    <label className="mb-1.5 flex items-center gap-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                      <Briefcase className="h-4 w-4" /> Project <span className="text-danger-500">*</span>
                    </label>
                    <select
                      value={formData.projectId}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          projectId: e.target.value,
                          statusId: "",
                          assigneeId: "",
                        })
                      }
                      className="input-field"
                      required
                      disabled={loading || !canEdit}
                    >
                      <option value="">Select a project</option>
                      {projects.map((project) => (
                        <option key={project.id} value={project.id}>
                          {project.name}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div>
                    <label className="mb-1.5 flex items-center gap-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                      <Tag className="h-4 w-4" /> Title <span className="text-danger-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={formData.title}
                      onChange={(e) =>
                        setFormData({ ...formData, title: e.target.value })
                      }
                      className="input-field text-lg font-semibold"
                      placeholder="Enter task title"
                      required
                      disabled={loading || !canEdit}
                    />
                  </div>

                  <div>
                    <label className="mb-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                      Description
                    </label>
                    <textarea
                      rows={3}
                      value={formData.description}
                      onChange={(e) =>
                        setFormData({ ...formData, description: e.target.value })
                      }
                      className="input-field resize-vertical"
                      placeholder="Task description (optional)"
                      disabled={loading || !canEdit}
                    />
                  </div>

                  {task && task.project && (
                    <LabelPicker
                      taskId={task.id}
                      projectId={task.project.id}
                      selectedLabels={task.labels || []}
                      onLabelsChanged={() => {}}
                    />
                  )}

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label className="mb-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                        Priority
                      </label>
                      <div className="grid grid-cols-2 gap-2">
                        {["LOW", "MEDIUM", "HIGH", "CRITICAL"].map((priority) => (
                          <label
                            key={priority}
                            className={`cursor-pointer rounded-lg border-2 p-2 transition-all ${
                              formData.priority === priority
                                ? "border-primary-500 bg-primary-50 dark:bg-primary-900/20 shadow-sm"
                                : "border-surface-200 dark:border-surface-700 hover:border-surface-300 dark:hover:border-surface-600"
                            }`}
                          >
                            <input
                              type="radio"
                              name="priority"
                              value={priority}
                              checked={formData.priority === priority}
                              onChange={() =>
                                setFormData({ ...formData, priority })
                              }
                              className="sr-only"
                              disabled={!canEdit}
                            />
                            <PriorityBadge
                              priority={priority}
                              className="w-full justify-center"
                            />
                          </label>
                        ))}
                      </div>
                    </div>

                    <div>
                      <label className="mb-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                        Status
                      </label>
                      <div className="grid grid-cols-2 gap-2">
                        {statuses.map((status) => (
                          <label
                            key={status.id}
                            className={`cursor-pointer rounded-lg border-2 p-2 transition-all ${
                              String(formData.statusId) === String(status.id)
                                ? "border-primary-500 bg-primary-50 dark:bg-primary-900/20 shadow-sm"
                                : "border-surface-200 dark:border-surface-700 hover:border-surface-300 dark:hover:border-surface-600"
                            }`}
                          >
                            <input
                              type="radio"
                              name="status"
                              value={status.id}
                              checked={
                                String(formData.statusId) === String(status.id)
                              }
                              onChange={() =>
                                setFormData({ ...formData, statusId: status.id })
                              }
                              className="sr-only"
                              disabled={!canEdit}
                            />
                            <StatusBadge
                              status={status.name}
                              color={status.color}
                              className="w-full justify-center"
                            />
                          </label>
                        ))}
                      </div>
                      {statuses.length === 0 && (
                        <p className="mt-1.5 text-xs text-warning-600 dark:text-warning-400">
                          This project has no workflow statuses yet.
                        </p>
                      )}
                    </div>
                  </div>

                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label className="mb-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                        Assignee <span className="text-danger-500">*</span>
                      </label>
                      <select
                        value={formData.assigneeId}
                        onChange={(e) =>
                          setFormData({ ...formData, assigneeId: e.target.value })
                        }
                        className="input-field"
                        disabled={loading || !canChangeAssignee}
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
                      {canChangeAssignee && (
                        <button
                          type="button"
                          onClick={openInviteModal}
                          className="mt-2 inline-flex items-center gap-1.5 text-sm font-medium text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 transition-colors"
                        >
                          <UserPlus className="h-4 w-4" />
                          Invite new assignee
                        </button>
                      )}
                    </div>

                    <div>
                      <label className="mb-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                        Due Date
                      </label>
                      <div className="relative">
                        <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-surface-400" />
                        <input
                          type="date"
                          value={formData.dueDate}
                          onChange={(e) =>
                            setFormData({ ...formData, dueDate: e.target.value })
                          }
                          className="input-field pl-10"
                          disabled={loading || !canEdit}
                        />
                      </div>
                    </div>
                  </div>

                  {columns.length > 0 && (
                    <div>
                      <label className="mb-1.5 text-sm font-medium text-surface-700 dark:text-surface-300">
                        Board Column
                      </label>
                      <select
                        value={formData.columnId}
                        onChange={(e) =>
                          setFormData({ ...formData, columnId: e.target.value })
                        }
                        className="input-field"
                        disabled={loading || !canEdit}
                      >
                        <option value="">No Column</option>
                        {columns.map((col) => (
                          <option key={col.id} value={col.id}>
                            {col.name}
                          </option>
                        ))}
                      </select>
                    </div>
                  )}
                </form>

                <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-surface-200 dark:border-surface-800 bg-surface-50/50 dark:bg-surface-900/50">
                  <button
                    type="button"
                    onClick={onClose}
                    className="btn-secondary"
                    disabled={loading}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    form="task-form"
                    disabled={loading || !canEdit}
                    className="btn-primary"
                  >
                    {loading ? (
                      <>
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Saving...
                      </>
                    ) : task ? (
                      "Update Task"
                    ) : (
                      "Create Task"
                    )}
                  </button>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>

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
    </Transition>
  );
};

export default TaskDrawer;
