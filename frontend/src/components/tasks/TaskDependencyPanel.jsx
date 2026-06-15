import { useState, useEffect, useCallback } from "react";
import {
  Link2,
  Plus,
  X,
  AlertTriangle,
  ArrowRight,
  Layers,
  Copy,
  GitFork,
  GitBranch,
  Clock,
  CheckCircle2,
} from "lucide-react";
import {
  addTaskDependency,
  removeTaskDependency,
  getTaskDependencies,
  getBlockingTasks,
  getTaskBlockedStatus,
} from "../../api/taskDependencyApi";
import { cn } from "../../utils/cn";
import { toast } from "sonner";

const DEPENDENCY_TYPES = [
  {
    value: "BLOCKED_BY",
    label: "Blocked By",
    icon: AlertTriangle,
    color: "text-danger-600 dark:text-danger-400",
  },
  {
    value: "BLOCKS",
    label: "Blocks",
    icon: ArrowRight,
    color: "text-warning-600 dark:text-warning-400",
  },
  {
    value: "RELATES_TO",
    label: "Relates To",
    icon: Layers,
    color: "text-primary-600 dark:text-primary-400",
  },
  {
    value: "DUPLICATES",
    label: "Duplicates",
    icon: Copy,
    color: "text-purple-600 dark:text-purple-400",
  },
  {
    value: "CAUSED_BY",
    label: "Caused By",
    icon: GitFork,
    color: "text-cyan-600 dark:text-cyan-400",
  },
];

const TYPE_CONFIG = {
  BLOCKED_BY: {
    label: "Blocked By",
    bg: "bg-danger-50 dark:bg-danger-900/20",
    text: "text-danger-700 dark:text-danger-400",
    ring: "ring-danger-200 dark:ring-danger-800/50",
    icon: AlertTriangle,
  },
  BLOCKS: {
    label: "Blocks",
    bg: "bg-warning-50 dark:bg-warning-900/20",
    text: "text-warning-700 dark:text-warning-400",
    ring: "ring-warning-200 dark:ring-warning-800/50",
    icon: ArrowRight,
  },
  RELATES_TO: {
    label: "Relates To",
    bg: "bg-primary-50 dark:bg-primary-900/20",
    text: "text-primary-700 dark:text-primary-400",
    ring: "ring-primary-200 dark:ring-primary-800/50",
    icon: Layers,
  },
  DUPLICATES: {
    label: "Duplicates",
    bg: "bg-purple-50 dark:bg-purple-900/20",
    text: "text-purple-700 dark:text-purple-400",
    ring: "ring-purple-200 dark:ring-purple-800/50",
    icon: Copy,
  },
  CAUSED_BY: {
    label: "Caused By",
    bg: "bg-cyan-50 dark:bg-cyan-900/20",
    text: "text-cyan-700 dark:text-cyan-400",
    ring: "ring-cyan-200 dark:ring-cyan-800/50",
    icon: GitFork,
  },
};

function DependencyItem({ dep, onRemove }) {
  const config = TYPE_CONFIG[dep.dependencyType] || TYPE_CONFIG.BLOCKED_BY;
  const Icon = config.icon;
  const isBlockedBy = dep.dependencyType === "BLOCKED_BY";
  const linkedTitle = isBlockedBy ? dep.dependsOnTaskTitle : dep.taskTitle;
  const linkedId = isBlockedBy ? dep.dependsOnTaskId : dep.taskId;

  return (
    <div className="group/dep flex items-start gap-3 rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-3 transition-all hover:shadow-sm hover:border-surface-300 dark:hover:border-surface-600">
      <div
        className={cn(
          "flex h-8 w-8 shrink-0 items-center justify-center rounded-lg ring-1",
          config.bg,
          config.text,
          config.ring,
        )}
      >
        <Icon className="h-4 w-4" />
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2">
          <span
            className={cn(
              "inline-flex items-center rounded-md px-2 py-0.5 text-xs font-medium",
              config.bg,
              config.text,
            )}
          >
            {config.label}
          </span>
          <span className="text-xs text-surface-400">#{linkedId}</span>
        </div>
        <p className="mt-1 text-sm font-medium text-surface-900 dark:text-surface-100 truncate">
          {linkedTitle || `Task #${linkedId}`}
        </p>
        {dep.description && (
          <p className="mt-0.5 text-xs text-surface-500 dark:text-surface-400 truncate">
            {dep.description}
          </p>
        )}
      </div>
      <button
        onClick={() => onRemove(dep)}
        className="shrink-0 rounded-lg p-1.5 text-surface-400 opacity-0 transition-all hover:bg-danger-50 hover:text-danger-600 dark:hover:bg-danger-900/30 dark:hover:text-danger-400 group-hover/dep:opacity-100"
        title="Remove dependency"
      >
        <X className="h-3.5 w-3.5" />
      </button>
    </div>
  );
}

export default function TaskDependencyPanel({
  taskId,
  onBlockedChange,
}) {
  const [dependencies, setDependencies] = useState([]);
  const [blockingTasks, setBlockingTasks] = useState([]);
  const [blockedStatus, setBlockedStatus] = useState(null);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newDepTaskId, setNewDepTaskId] = useState("");
  const [newDepType, setNewDepType] = useState("BLOCKED_BY");
  const [newDepDesc, setNewDepDesc] = useState("");
  const [adding, setAdding] = useState(false);
  const [loading, setLoading] = useState(true);

  const fetchData = useCallback(async () => {
    if (!taskId) return;
    setLoading(true);
    try {
      const [deps, blocking, blocked] = await Promise.all([
        getTaskDependencies(taskId),
        getBlockingTasks(taskId),
        getTaskBlockedStatus(taskId),
      ]);
      setDependencies(deps);
      setBlockingTasks(blocking);
      setBlockedStatus(blocked);
      if (onBlockedChange) onBlockedChange(blocked?.blocked || false);
    } catch {
      // silent
    } finally {
      setLoading(false);
    }
  }, [taskId, onBlockedChange]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleAdd = async () => {
    if (!newDepTaskId) return;
    setAdding(true);
    try {
      await addTaskDependency(
        taskId,
        parseInt(newDepTaskId),
        newDepType,
        newDepDesc,
      );
      toast.success("Dependency added");
      setShowAddForm(false);
      setNewDepTaskId("");
      setNewDepDesc("");
      await fetchData();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to add dependency");
    } finally {
      setAdding(false);
    }
  };

  const handleRemove = async (dep) => {
    try {
      await removeTaskDependency(dep.taskId, dep.dependsOnTaskId);
      toast.success("Dependency removed");
      await fetchData();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to remove dependency");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="animate-spin rounded-full h-6 w-6 border-2 border-primary-500 border-t-transparent" />
      </div>
    );
  }

  const allDeps = [...dependencies, ...blockingTasks];
  const uniqueDeps = allDeps.filter(
    (dep, i, arr) => arr.findIndex((d) => d.id === dep.id) === i,
  );

  const blockedCount = uniqueDeps.filter(
    (d) => d.dependencyType === "BLOCKED_BY",
  ).length;
  const blocksCount = uniqueDeps.filter(
    (d) => d.dependencyType === "BLOCKS",
  ).length;
  const relatesCount = uniqueDeps.filter(
    (d) => d.dependencyType === "RELATES_TO",
  ).length;

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <GitBranch className="h-5 w-5 text-surface-500 dark:text-surface-400" />
          <h4 className="text-sm font-semibold text-surface-800 dark:text-surface-200">
            Dependencies
          </h4>
          {uniqueDeps.length > 0 && (
            <span className="rounded-full bg-surface-100 dark:bg-surface-800 px-2 py-0.5 text-xs font-medium text-surface-600 dark:text-surface-400">
              {uniqueDeps.length}
            </span>
          )}
        </div>
        <button
          onClick={() => setShowAddForm(!showAddForm)}
          className="inline-flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-medium text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-900/20 transition-colors"
        >
          <Plus className="h-3.5 w-3.5" />
          Add
        </button>
      </div>

      {blockedStatus?.blocked && (
        <div className="mb-3 flex items-start gap-2 rounded-xl bg-danger-50 dark:bg-danger-900/20 p-3 ring-1 ring-danger-200 dark:ring-danger-800/50">
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-danger-100 dark:bg-danger-900/40">
            <AlertTriangle className="h-4 w-4 text-danger-600 dark:text-danger-400" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-semibold text-danger-800 dark:text-danger-300">
              Task is blocked
            </p>
            <ul className="mt-1 space-y-0.5 text-xs text-danger-700 dark:text-danger-400">
              {blockedStatus.blockingTasks?.map((bt) => (
                <li key={bt.taskId} className="flex items-center gap-1">
                  <ArrowRight className="h-3 w-3 shrink-0" />
                  <span className="truncate">
                    Blocked by: {bt.taskTitle} 
                    <span className="text-danger-500">({bt.status})</span>
                  </span>
                </li>
              ))}
            </ul>
          </div>
        </div>
      )}

      {uniqueDeps.length > 0 && (
        <div className="mb-3 flex flex-wrap gap-2">
          {blockedCount > 0 && (
            <span className="inline-flex items-center gap-1 rounded-lg bg-danger-50 dark:bg-danger-900/20 px-2.5 py-1 text-xs font-medium text-danger-700 dark:text-danger-400 ring-1 ring-danger-200 dark:ring-danger-800/50">
              <AlertTriangle className="h-3 w-3" />
              {blockedCount} blocking
            </span>
          )}
          {blocksCount > 0 && (
            <span className="inline-flex items-center gap-1 rounded-lg bg-warning-50 dark:bg-warning-900/20 px-2.5 py-1 text-xs font-medium text-warning-700 dark:text-warning-400 ring-1 ring-warning-200 dark:ring-warning-800/50">
              <ArrowRight className="h-3 w-3" />
              {blocksCount} blocks
            </span>
          )}
          {relatesCount > 0 && (
            <span className="inline-flex items-center gap-1 rounded-lg bg-primary-50 dark:bg-primary-900/20 px-2.5 py-1 text-xs font-medium text-primary-700 dark:text-primary-400 ring-1 ring-primary-200 dark:ring-primary-800/50">
              <Layers className="h-3 w-3" />
              {relatesCount} relates
            </span>
          )}
        </div>
      )}

      {showAddForm && (
        <div className="mb-3 rounded-xl border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-800/50 p-3 space-y-2">
          <div className="grid grid-cols-1 gap-2 sm:grid-cols-2">
            <input
              type="number"
              placeholder="Task ID..."
              value={newDepTaskId}
              onChange={(e) => setNewDepTaskId(e.target.value)}
              className="input-field text-sm"
            />
            <select
              value={newDepType}
              onChange={(e) => setNewDepType(e.target.value)}
              className="input-field text-sm"
            >
              {DEPENDENCY_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </div>
          <input
            type="text"
            placeholder="Description (optional)"
            value={newDepDesc}
            onChange={(e) => setNewDepDesc(e.target.value)}
            className="input-field text-sm"
          />
          <div className="flex gap-2">
            <button
              onClick={handleAdd}
              disabled={adding || !newDepTaskId}
              className="btn btn-primary btn-sm flex-1"
            >
              {adding ? "Adding..." : "Add Dependency"}
            </button>
            <button
              onClick={() => setShowAddForm(false)}
              className="btn btn-secondary btn-sm"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {uniqueDeps.length === 0 ? (
        <div className="flex flex-col items-center gap-2 rounded-xl border border-dashed border-surface-200 dark:border-surface-700 py-6 text-center">
          <GitBranch className="h-6 w-6 text-surface-300 dark:text-surface-600" />
          <p className="text-xs text-surface-400 dark:text-surface-500">
            No dependencies yet. Link tasks to track relationships.
          </p>
        </div>
      ) : (
        <div className="space-y-2 max-h-64 overflow-y-auto scrollbar-thin">
          {uniqueDeps.map((dep) => (
            <DependencyItem key={dep.id} dep={dep} onRemove={handleRemove} />
          ))}
        </div>
      )}
    </div>
  );
}
