import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  ArrowLeft,
  Calendar,
  Clock,
  User,
  Tag,
  Copy,
  Edit3,
  Trash2,
  BarChart3,
  ListTodo,
  MessageSquare,
  Activity,
  Link2,
  Paperclip,
  Loader2,
} from "lucide-react";
import { toast } from "sonner";
import { getTaskById, deleteTask, cloneTask } from "../api/taskApi";
import { getUserProjects } from "../api/projectMembersApi";
import PriorityBadge from "../components/tasks/PriorityBadge";
import StatusBadge from "../components/tasks/StatusBadge";
import SubtaskList from "../components/subtasks/SubtaskList";
import CommentSection from "../components/tasks/CommentSection";
import AttachmentSection from "../components/tasks/AttachmentSection";
import ActivityTimeline from "../components/tasks/ActivityTimeline";
import LabelPicker from "../components/tasks/LabelPicker";
import TaskDrawer from "../components/tasks/TaskDrawer";
import TaskDependencyPanel from "../components/tasks/TaskDependencyPanel";
import ConfirmationDialog from "../components/members/ConfirmationDialog";
import { usePermission } from "../context/usePermission";

const TABS = [
  { id: "overview", label: "Overview", icon: BarChart3 },
  { id: "subtasks", label: "Subtasks", icon: ListTodo },
  { id: "comments", label: "Comments", icon: MessageSquare },
  { id: "activity", label: "Activity", icon: Activity },
  { id: "attachments", label: "Files", icon: Paperclip },
  { id: "dependencies", label: "Dependencies", icon: Link2 },
];

export default function TaskDetailPage() {
  const { taskId, workspaceSlug } = useParams();
  const navigate = useNavigate();
  const { canEditTask, canDeleteTask } = usePermission();

  const [task, setTask] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("overview");
  const [cloning, setCloning] = useState(false);
  const [projects, setProjects] = useState([]);

  const [showTaskDrawer, setShowTaskDrawer] = useState(false);
  const [drawerTask, setDrawerTask] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const loadTask = useCallback(async () => {
    if (!taskId) return;
    setLoading(true);
    try {
      const data = await getTaskById(taskId);
      setTask(data);
    } catch {
      toast.error("Failed to load task");
      navigate(`/w/${workspaceSlug}/tasks`, { replace: true });
    } finally {
      setLoading(false);
    }
  }, [taskId, workspaceSlug, navigate]);

  useEffect(() => {
    if (taskId) loadTask();
  }, [taskId, loadTask]);

  useEffect(() => {
    getUserProjects()
      .then(setProjects)
      .catch(() => {});
  }, []);

  const handleDelete = async () => {
    try {
      await deleteTask(taskId);
      toast.success("Task deleted");
      navigate(`/w/${workspaceSlug}/tasks`, { replace: true });
    } catch {
      toast.error("Failed to delete task");
    }
    setShowDeleteConfirm(false);
  };

  const handleClone = async () => {
    setCloning(true);
    try {
      const cloned = await cloneTask(taskId);
      toast.success("Task duplicated");
      navigate(`/w/${workspaceSlug}/tasks/${cloned.id}`, { replace: true });
    } catch {
      toast.error("Failed to clone task");
    } finally {
      setCloning(false);
    }
  };

  const handleEditTask = () => {
    setDrawerTask(task);
    setShowTaskDrawer(true);
  };

  const handleTaskSaved = () => {
    setShowTaskDrawer(false);
    setDrawerTask(null);
    loadTask();
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Loader2 className="h-8 w-8 text-indigo-500 animate-spin" />
      </div>
    );
  }

  if (!task) return null;

  const formatDate = (d) => (d ? new Date(d).toLocaleDateString() : "—");

  return (
    <div className="max-w-5xl mx-auto px-4 py-6">
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate(-1)}
          className="p-2 rounded-xl hover:bg-gray-100 text-gray-500 transition-colors"
        >
          <ArrowLeft className="h-5 w-5" />
        </button>
        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <h1 className="text-2xl font-bold text-gray-900 truncate">{task.title}</h1>
            {task.project && (
              <Link
                to={`/w/${workspaceSlug}/projects/${task.project.id}`}
                className="text-sm text-indigo-600 hover:text-indigo-700 font-medium shrink-0"
              >
                {task.project.name}
              </Link>
            )}
          </div>
          <div className="flex items-center gap-3 mt-1 flex-wrap">
            <PriorityBadge priority={task.priority} />
            <StatusBadge status={task.status} />
            <span className="text-xs text-gray-400">
              Created {formatDate(task.createdAt)}
            </span>
            {task.updatedAt && task.updatedAt !== task.createdAt && (
              <span className="text-xs text-gray-400">
                Updated {formatDate(task.updatedAt)}
              </span>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          {canEditTask(task) && (
            <>
              <button
                type="button"
                onClick={handleClone}
                disabled={cloning}
                className="btn btn-secondary !px-3 !py-2"
                title="Duplicate task"
              >
                {cloning ? (
                  <Loader2 className="h-4 w-4 animate-spin" />
                ) : (
                  <Copy className="h-4 w-4" />
                )}
              </button>
              <button
                type="button"
                className="btn btn-secondary !px-3 !py-2"
                title="Edit task"
                onClick={handleEditTask}
              >
                <Edit3 className="h-4 w-4" />
              </button>
            </>
          )}
          {canDeleteTask() && (
            <button
              type="button"
              onClick={() => setShowDeleteConfirm(true)}
              className="btn btn-secondary !px-3 !py-2 text-red-600 hover:bg-red-50"
              title="Delete task"
            >
              <Trash2 className="h-4 w-4" />
            </button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8 p-4 rounded-2xl bg-gray-50 border border-gray-100">
        <div>
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-1">
            <User className="h-3.5 w-3.5 inline mr-1" />
            Assignee
          </p>
          <p className="text-sm font-medium text-gray-900">
            {task.assignee?.name || "Unassigned"}
          </p>
        </div>
        <div>
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-1">
            <Calendar className="h-3.5 w-3.5 inline mr-1" />
            Due Date
          </p>
          <p className="text-sm font-medium text-gray-900">{formatDate(task.dueDate)}</p>
        </div>
        <div>
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-1">
            <BarChart3 className="h-3.5 w-3.5 inline mr-1" />
            Story Points
          </p>
          <p className="text-sm font-medium text-gray-900">{task.storyPoints ?? "—"}</p>
        </div>
        <div>
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wider mb-1">
            <Clock className="h-3.5 w-3.5 inline mr-1" />
            Created by
          </p>
          <p className="text-sm font-medium text-gray-900">
            {task.createdBy?.name || task.createdBy?.username || "—"}
          </p>
        </div>
      </div>

      {task.project && (
        <div className="mb-6">
          <LabelPicker
            taskId={task.id}
            projectId={task.project.id}
            selectedLabels={task.labels || []}
            onLabelsChanged={loadTask}
          />
        </div>
      )}

      {task.subtaskCount > 0 && (
        <div className="mb-6">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-medium text-gray-500 uppercase tracking-wider">
              Subtask Progress
            </span>
            <span className="text-xs font-medium text-gray-600">
              {task.completedSubtaskCount || 0}/{task.subtaskCount}
            </span>
          </div>
          <div className="w-full h-2 bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-gradient-to-r from-indigo-500 to-green-500 rounded-full transition-all"
              style={{
                width: `${((task.completedSubtaskCount || 0) / task.subtaskCount) * 100}%`,
              }}
            />
          </div>
        </div>
      )}

      <div className="border-b border-gray-200 mb-6 overflow-x-auto">
        <div className="flex gap-1 min-w-max">
          {TABS.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                  activeTab === tab.id
                    ? "border-indigo-500 text-indigo-600"
                    : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                }`}
              >
                <Icon className="h-4 w-4" />
                {tab.label}
              </button>
            );
          })}
        </div>
      </div>

      <div>
        {activeTab === "overview" && (
          <div className="space-y-6">
            <div>
              <h4 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-2">
                Description
              </h4>
              <p className="text-gray-800 whitespace-pre-wrap">
                {task.description || "No description provided."}
              </p>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <h4 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-2">
                  Date Range
                </h4>
                <p className="text-sm text-gray-800">
                  {task.startDate ? formatDate(task.startDate) : "—"} →{" "}
                  {task.endDate ? formatDate(task.endDate) : "—"}
                </p>
              </div>
              {task.project && (
                <div>
                  <h4 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-2">
                    Gantt View
                  </h4>
                  <Link
                    to={`/w/${workspaceSlug}/projects/${task.project.id}/gantt`}
                    className="text-sm text-indigo-600 hover:text-indigo-700 font-medium"
                  >
                    View in Gantt chart →
                  </Link>
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === "subtasks" && (
          <SubtaskList taskId={task.id} />
        )}

        {activeTab === "comments" && (
          <CommentSection taskId={task.id} />
        )}

        {activeTab === "activity" && (
          <ActivityTimeline taskId={task.id} />
        )}

        {activeTab === "attachments" && (
          <AttachmentSection taskId={task.id} />
        )}

        {activeTab === "dependencies" && (
          <div>
            <TaskDependencyPanel
              taskId={task.id}
              onBlockedChange={() => {}}
            />
          </div>
        )}
      </div>

      <TaskDrawer
        isOpen={showTaskDrawer}
        onClose={() => {
          setShowTaskDrawer(false);
          setDrawerTask(null);
        }}
        task={drawerTask}
        projectId={task.project?.id}
        projects={projects}
        onTaskSaved={handleTaskSaved}
      />

      <ConfirmationDialog
        isOpen={showDeleteConfirm}
        onClose={() => setShowDeleteConfirm(false)}
        onConfirm={handleDelete}
        title="Delete Task?"
        message="This action cannot be undone."
        confirmText="Delete"
        cancelText="Cancel"
      />
    </div>
  );
}
