import { useState, useEffect } from "react";
import {
  Plus,
  ChevronDown,
  ChevronRight,
  CheckCircle2,
  Circle,
  Trash2,
  Pencil,
  Check,
  X,
} from "lucide-react";
import {
  getSubtaskProgress,
  createSubtask,
  toggleSubtaskCompletion,
  deleteSubtask,
  updateSubtask,
} from "../../api/subtaskApi";
import ConfirmationDialog from "../members/ConfirmationDialog";
import { toast } from "sonner";

const PRIORITY_COLORS = {
  LOW: "bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-400",
  MEDIUM:
    "bg-primary-100 dark:bg-primary-900/40 text-primary-700 dark:text-primary-300",
  HIGH: "bg-warning-100 dark:bg-warning-900/40 text-warning-700 dark:text-warning-300",
  CRITICAL:
    "bg-danger-100 dark:bg-danger-900/40 text-danger-700 dark:text-danger-300",
};

export default function SubtaskList({ taskId }) {
  const [subtasks, setSubtasks] = useState([]);
  const [progress, setProgress] = useState({
    total: 0,
    completed: 0,
    percentage: 0,
  });
  const [expanded, setExpanded] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [newTitle, setNewTitle] = useState("");
  const [newPriority, setNewPriority] = useState("MEDIUM");
  const [editingId, setEditingId] = useState(null);
  const [editTitle, setEditTitle] = useState("");
  const [showConfirm, setShowConfirm] = useState(false);
  const [deletingId, setDeletingId] = useState(null);

  const fetchSubtasks = async () => {
    try {
      const res = await getSubtaskProgress(taskId);
      setSubtasks(res.data?.subtasks || []);
      setProgress({
        total: res.data?.total || 0,
        completed: res.data?.completed || 0,
        percentage: res.data?.completionPercentage || 0,
      });
    } catch {
      setSubtasks([]);
      setProgress({ total: 0, completed: 0, percentage: 0 });
    }
  };

  useEffect(() => {
    fetchSubtasks();
  }, [taskId]);

  const handleCreate = async () => {
    if (!newTitle.trim()) return;
    setLoading(true);
    try {
      await createSubtask(taskId, {
        title: newTitle.trim(),
        priority: newPriority,
      });
      setNewTitle("");
      setShowForm(false);
      await fetchSubtasks();
      toast.success("Subtask created");
    } catch {
      toast.error("Failed to create subtask");
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = async (subtaskId) => {
    try {
      await toggleSubtaskCompletion(subtaskId);
      await fetchSubtasks();
    } catch {
      toast.error("Failed to update subtask");
    }
  };

  const handleDelete = async () => {
    if (!deletingId) return;
    try {
      await deleteSubtask(deletingId);
      await fetchSubtasks();
      toast.success("Subtask deleted");
    } catch {
      toast.error("Failed to delete subtask");
    } finally {
      setShowConfirm(false);
      setDeletingId(null);
    }
  };

  const startEdit = (subtask) => {
    setEditingId(subtask.id);
    setEditTitle(subtask.title);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditTitle("");
  };

  const saveEdit = async (subtaskId) => {
    if (!editTitle.trim()) return;
    try {
      await updateSubtask(subtaskId, { title: editTitle.trim() });
      await fetchSubtasks();
      toast.success("Subtask updated");
      setEditingId(null);
      setEditTitle("");
    } catch {
      toast.error("Failed to update subtask");
    }
  };

  const hasSubtasks = subtasks.length > 0;

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <button
          onClick={() => setExpanded(!expanded)}
          className="flex items-center gap-2 text-sm font-semibold text-surface-700 dark:text-surface-300 hover:text-surface-900 dark:hover:text-surface-100"
        >
          {expanded ? (
            <ChevronDown className="h-4 w-4" />
          ) : (
            <ChevronRight className="h-4 w-4" />
          )}
          Subtasks ({progress.completed}/{progress.total})
        </button>
        <button
          onClick={() => setShowForm(!showForm)}
          className="inline-flex items-center gap-1.5 rounded-xl px-3 py-1.5 text-sm font-medium text-primary-600 dark:text-primary-400 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition-colors"
        >
          <Plus className="h-3.5 w-3.5" />
          Add
        </button>
      </div>

      {hasSubtasks && (
        <div className="space-y-1">
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-surface-200 dark:bg-surface-700">
            <div
              className={`h-full rounded-full transition-all ${
                progress.percentage === 100
                  ? "bg-success-500"
                  : "bg-primary-500"
              }`}
              style={{ width: `${progress.percentage}%` }}
            />
          </div>
          <span className="text-xs text-surface-400">
            {progress.percentage}% complete
          </span>
        </div>
      )}

      {showForm && (
        <div className="flex items-center gap-2">
          <input
            type="text"
            value={newTitle}
            onChange={(e) => setNewTitle(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleCreate()}
            placeholder="Enter subtask title..."
            className="input-field flex-1"
            autoFocus
          />
          <select
            value={newPriority}
            onChange={(e) => setNewPriority(e.target.value)}
            className="input-field w-auto text-xs"
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
          <button
            onClick={handleCreate}
            disabled={loading || !newTitle.trim()}
            className="btn-primary px-3 py-1.5 text-sm"
          >
            {loading ? (
              <span className="flex items-center gap-1">
                <span className="h-3 w-3 animate-spin rounded-full border-2 border-white border-t-transparent" />
                ...
              </span>
            ) : (
              "Add"
            )}
          </button>
        </div>
      )}

      {expanded && hasSubtasks && (
        <ul className="space-y-1">
          {subtasks.map((subtask) => (
            <li
              key={subtask.id}
              className="group flex items-center gap-2 rounded-xl px-3 py-2 hover:bg-surface-50 dark:hover:bg-surface-800/50 transition-colors"
            >
              <button
                onClick={() => handleToggle(subtask.id)}
                className="shrink-0 text-surface-300 dark:text-surface-600 hover:text-surface-500 dark:hover:text-surface-400"
              >
                {subtask.status === "DONE" ||
                subtask.status === "COMPLETED" ? (
                  <CheckCircle2 className="h-4 w-4 text-success-500" />
                ) : (
                  <Circle className="h-4 w-4" />
                )}
              </button>

              {editingId === subtask.id ? (
                <div className="flex flex-1 items-center gap-1.5">
                  <input
                    type="text"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter") saveEdit(subtask.id);
                      if (e.key === "Escape") cancelEdit();
                    }}
                    className="input-field flex-1 py-1 text-sm"
                    autoFocus
                  />
                  <button
                    onClick={() => saveEdit(subtask.id)}
                    className="rounded-lg p-1 text-success-600 hover:bg-success-100 dark:hover:bg-success-900/30 transition-colors"
                  >
                    <Check className="h-3.5 w-3.5" />
                  </button>
                  <button
                    onClick={cancelEdit}
                    className="rounded-lg p-1 text-surface-400 hover:bg-surface-100 dark:hover:bg-surface-800 transition-colors"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
              ) : (
                <>
                  <span
                    className={`flex-1 text-sm ${
                      subtask.status === "DONE" ||
                      subtask.status === "COMPLETED"
                        ? "text-surface-400 line-through"
                        : "text-surface-700 dark:text-surface-300"
                    }`}
                  >
                    {subtask.title}
                  </span>
                  {subtask.priority && (
                    <span
                      className={`rounded-lg px-1.5 py-0.5 text-[10px] font-medium ${
                        PRIORITY_COLORS[subtask.priority] ||
                        "bg-surface-100 dark:bg-surface-800 text-surface-600"
                      }`}
                    >
                      {subtask.priority}
                    </span>
                  )}
                  {subtask.assignee && (
                    <span
                      className="text-xs text-surface-400"
                      title={subtask.assignee.name}
                    >
                      {subtask.assignee.name.split(" ")[0]}
                    </span>
                  )}
                  <div className="flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={() => startEdit(subtask)}
                      className="rounded-lg p-1 text-surface-400 hover:text-primary-600 hover:bg-primary-100 dark:hover:bg-primary-900/30 transition-colors"
                      title="Edit"
                    >
                      <Pencil className="h-3.5 w-3.5" />
                    </button>
                    <button
                      onClick={() => {
                        setDeletingId(subtask.id);
                        setShowConfirm(true);
                      }}
                      className="rounded-lg p-1 text-surface-400 hover:text-danger-600 hover:bg-danger-100 dark:hover:bg-danger-900/30 transition-colors"
                      title="Delete"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </>
              )}
            </li>
          ))}
        </ul>
      )}

      {!showForm && !hasSubtasks && (
        <p className="text-sm text-surface-400 italic">
          No subtasks yet. Click &quot;Add&quot; to create one.
        </p>
      )}

      <ConfirmationDialog
        isOpen={showConfirm}
        onClose={() => {
          setShowConfirm(false);
          setDeletingId(null);
        }}
        onConfirm={handleDelete}
        title="Delete Subtask?"
        message="This action cannot be undone."
      />
    </div>
  );
}
