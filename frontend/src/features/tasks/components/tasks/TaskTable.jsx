import { useCallback, useEffect, useState } from "react";
import {
  ChevronDown,
  Edit3,
  Search,
  Trash2,
  Copy,
  ExternalLink,
  Check,
  X,
  Loader2,
} from "lucide-react";
import { toast } from "sonner";
import PriorityBadge from "./PriorityBadge.jsx";
import StatusBadge from "./StatusBadge.jsx";
import FilterPresets from "./FilterPresets.jsx";
import { getTasksByProject, updateTask, cloneTask } from "../../api/taskApi";
import { getProjectAssignees } from "../../api/taskApi";
import { usePermission } from "../../../../context/usePermission.js";
import { useNavigate } from "react-router-dom";
function InlineSelect({ value, onChange, options, loading, disabled }) {
  const [open, setOpen] = useState(false);
  const [tempValue, setTempValue] = useState(value);
  useEffect(() => {
    setTempValue(value);
  }, [value]);
  const handleSelect = async (newValue) => {
    setTempValue(newValue);
    setOpen(false);
    try {
      await onChange(newValue);
    } catch {
      setTempValue(value);
    }
  };
  const selectedOption = options.find(
    (o) => String(o.value) === String(tempValue),
  );
  return (
    <div className="relative">
       
      <button
        onClick={() => !disabled && setOpen(!open)}
        className={`inline-flex items-center gap-1 rounded-lg px-2 py-1 text-xs font-medium transition-colors hover:bg-surface-100 dark:hover:bg-surface-700 ${disabled ? "opacity-50 cursor-not-allowed" : "cursor-pointer"}`}
        disabled={disabled}
      >
         
        {loading ? (
          <Loader2 className="h-3 w-3 animate-spin" />
        ) : selectedOption ? (
          selectedOption.label
        ) : (
          "Select..."
        )} 
        <ChevronDown className="h-3 w-3 text-surface-400" /> 
      </button> 
      {open && (
        <>
           
          <div
            className="fixed inset-0 z-40"
            onClick={() => setOpen(false)}
          /> 
          <div className="absolute left-0 top-full z-50 mt-1 min-w-[160px] rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 py-1 shadow-lg">
             
            {options.map((option) => (
              <button
                key={option.value}
                onClick={() => handleSelect(option.value)}
                className={`w-full px-3 py-2 text-left text-sm transition-colors hover:bg-surface-50 dark:hover:bg-surface-700 ${String(option.value) === String(tempValue) ? "bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-400" : "text-surface-700 dark:text-surface-300"}`}
              >
                 
                {option.label} 
              </button>
            ))} 
          </div> 
        </>
      )} 
    </div>
  );
}
const TaskTable = ({
  projectId,
  tasks: initialTasks = [],
  statuses = [],
  onEdit,
  onDelete,
  selectedIds = new Set(),
  onSelectionChange,
  onUpdate,
}) => {
  const navigate = useNavigate();
  const [tasks, setTasks] = useState(initialTasks);
  const [loading, setLoading] = useState(false);
  const [assignees, setAssignees] = useState([]);
  const [editingCell, setEditingCell] = useState(null);
  const [savingCell, setSavingCell] = useState(null);
  const [filters, setFilters] = useState({
    search: "",
    statusId: "",
    priority: "all",
    assigneeId: "",
    startDate: "",
    endDate: "",
    page: 0,
    size: 10,
    sortBy: "createdAt",
    sortDir: "desc",
  });
  const [sortConfig, setSortConfig] = useState({
    key: "createdAt",
    direction: "desc",
  });
  const [totalPages, setTotalPages] = useState(0);
  const { canEditTask, canDeleteTask } = usePermission();
  useEffect(() => {
    setSortConfig({ key: filters.sortBy, direction: filters.sortDir });
  }, [filters.sortBy, filters.sortDir]);
  useEffect(() => {
    setTasks(initialTasks);
  }, [initialTasks]);
  useEffect(() => {
    if (projectId) {
      loadAssignees(projectId);
    }
  }, [projectId]);
  const loadAssignees = async (currentProjectId) => {
    try {
      const members = await getProjectAssignees(currentProjectId);
      setAssignees(members);
    } catch (error) {
      console.error("Load assignees error:", error);
    }
  };
  const loadTasks = useCallback(async () => {
    if (!projectId) return;
    setLoading(true);
    try {
      const pageData = await getTasksByProject(projectId, filters);
      setTasks(pageData.content || []);
      setTotalPages(pageData.totalPages || 0);
    } catch (error) {
      console.error(error);
      toast.error(error.response?.data?.message || "Failed to load tasks");
    } finally {
      setLoading(false);
    }
  }, [filters, projectId]);
  useEffect(() => {
    loadTasks();
  }, [loadTasks]);
  const handleSort = (key) => {
    const newDirection =
      sortConfig.key === key && sortConfig.direction === "asc" ? "desc" : "asc";
    handleFilterChange("sortBy", key);
    handleFilterChange("sortDir", newDirection);
  };
  const handleFilterChange = (key, value) => {
    setFilters((prev) => ({ ...prev, [key]: value, page: 0 }));
  };
  const handleInlineUpdate = async (taskId, field, value) => {
    const cellKey = `${taskId}-${field}`;
    setSavingCell(cellKey);
    try {
      const task = tasks.find((t) => t.id === taskId);
      const payload = {
        ...task,
        [field]: value,
        projectId: Number(task.project?.id || projectId),
        assigneeId: task.assignee?.id ? Number(task.assignee.id) : null,
        statusId: task.statusId ? Number(task.statusId) : null,
        dueDate: task.dueDate ? `${task.dueDate.split("T")[0]}T00:00:00` : null,
      };
      await updateTask(taskId, payload);
      setTasks((prev) =>
        prev.map((t) => (t.id === taskId ? { ...t, [field]: value } : t)),
      );
      toast.success(
        `${field.charAt(0).toUpperCase() + field.slice(1)} updated`,
      );
    } catch (error) {
      toast.error(error.response?.data?.message || `Failed to update ${field}`);
      throw error;
    } finally {
      setSavingCell(null);
    }
  };
  const priorityOptions = [
    { value: "LOW", label: "Low" },
    { value: "MEDIUM", label: "Medium" },
    { value: "HIGH", label: "High" },
    { value: "CRITICAL", label: "Critical" },
  ];
  const statusOptions = statuses.map((s) => ({ value: s.id, label: s.name }));
  const assigneeOptions = [
    { value: "", label: "Unassigned" },
    ...assignees.map((a) => ({ value: a.userId || a.id, label: a.name })),
  ];
  if (loading && tasks.length === 0) {
    return (
      <div className="animate-pulse space-y-4">
         
        {[...Array(8)].map((_, index) => (
          <div
            key={index}
            className="h-16 rounded-2xl bg-surface-200 dark:bg-surface-800 p-4"
          />
        ))} 
      </div>
    );
  }
  return (
    <div className="space-y-6">
       
      <div className="flex flex-col gap-4 rounded-2xl bg-surface-50 dark:bg-surface-900/50 p-4 lg:flex-row lg:items-center">
         
        <div className="relative max-w-md flex-1">
           
          <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-surface-400" /> 
          <input
            type="text"
            placeholder="Search tasks..."
            value={filters.search}
            onChange={(e) => handleFilterChange("search", e.target.value)}
            className="input-field pl-10"
          /> 
        </div> 
        <select
          value={filters.statusId}
          onChange={(e) => handleFilterChange("statusId", e.target.value)}
          className="input-field"
        >
           
          <option value="">All Status</option> 
          {statuses.map((status) => (
            <option key={status.id} value={status.id}>
               
              {status.name} 
            </option>
          ))} 
        </select> 
        <select
          value={filters.priority}
          onChange={(e) => handleFilterChange("priority", e.target.value)}
          className="input-field"
        >
           
          <option value="all">All Priority</option> 
          <option value="LOW">Low</option> 
          <option value="MEDIUM">Medium</option> 
          <option value="HIGH">High</option> 
          <option value="CRITICAL">Critical</option> 
        </select> 
        <FilterPresets
          projectId={projectId}
          currentFilters={filters}
          onApply={(presetFilters) => setFilters(presetFilters)}
        /> 
      </div> 
      <div className="overflow-hidden rounded-2xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 shadow-sm">
         
        <div className="overflow-x-auto">
           
          <table className="w-full">
             
            <thead>
               
              <tr className="border-b border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-900/50">
                 
                <th className="w-12 py-4 px-4 text-left">
                   
                  {onSelectionChange && (
                    <input
                      type="checkbox"
                      checked={
                        tasks.length > 0 && selectedIds.size === tasks.length
                      }
                      indeterminate={
                        selectedIds.size > 0 &&
                        selectedIds.size < tasks.length
                          ? true
                          : undefined
                      }
                      onChange={() => {
                        if (selectedIds.size === tasks.length) {
                          onSelectionChange(new Set());
                        } else {
                          onSelectionChange(new Set(tasks.map((t) => t.id)));
                        }
                      }}
                      className="h-4 w-4 rounded border-surface-300 text-primary-600 focus:ring-primary-500"
                      aria-label={
                        selectedIds.size === tasks.length
                          ? "Deselect all"
                          : "Select all"
                      }
                    />
                  )} 
                </th>
                <th
                  className="cursor-pointer py-4 px-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400 hover:text-primary-600 dark:hover:text-primary-400"
                  onClick={() => handleSort("title")}
                >
                   
                  <div className="flex items-center gap-1">
                     
                    Title <ChevronDown className="h-3 w-3 opacity-50" /> 
                  </div> 
                </th>
                <th className="py-4 px-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Priority
                </th>
                <th className="py-4 px-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Status
                </th>
                <th className="py-4 px-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Assignee
                </th>
                <th
                  className="cursor-pointer py-4 px-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400 hover:text-primary-600 dark:hover:text-primary-400"
                  onClick={() => handleSort("dueDate")}
                >
                   
                  <div className="flex items-center gap-1">
                     
                    Due Date <ChevronDown className="h-3 w-3 opacity-50" /> 
                  </div> 
                </th>
                <th className="py-4 px-4 text-right text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Actions
                </th>
              </tr> 
            </thead> 
            <tbody className="divide-y divide-surface-100 dark:divide-surface-700/50">
               
              {tasks.map((task) => (
                <tr
                  key={task.id}
                  className="group hover:bg-surface-50/50 dark:hover:bg-surface-800/50 transition-colors"
                >
                   
                  <td className="py-4 px-4">
                     
                    {onSelectionChange ? (
                      <input
                        type="checkbox"
                        checked={selectedIds.has(task.id)}
                        onChange={() => {
                          const next = new Set(selectedIds);
                          if (next.has(task.id)) {
                            next.delete(task.id);
                          } else {
                            next.add(task.id);
                          }
                          onSelectionChange(next);
                        }}
                        className="h-4 w-4 rounded border-surface-300 text-primary-600 focus:ring-primary-500"
                        aria-label={`Select task ${task.title}`}
                      />
                    ) : (
                      <div className="h-2 w-2 rounded-full bg-primary-500" />
                    )} 
                  </td>
                  <td className="py-4 px-4">
                     
                    <div className="flex items-center gap-3">
                       
                      <button
                        onClick={() => onEdit?.(task)}
                        className="text-left font-medium text-surface-900 dark:text-surface-100 hover:text-primary-600 dark:hover:text-primary-400 transition-colors"
                      >
                         
                        {task.title} 
                      </button> 
                    </div> 
                    {task.description && (
                      <p className="mt-1 line-clamp-1 text-xs text-surface-500 dark:text-surface-400">
                        {task.description}
                      </p>
                    )} 
                  </td>
                  <td className="py-4 px-4">
                     
                    {canEditTask(task) ? (
                      <InlineSelect
                        value={task.priority}
                        onChange={(value) =>
                          handleInlineUpdate(task.id, "priority", value)
                        }
                        options={priorityOptions}
                        loading={savingCell === `${task.id}-priority`}
                      />
                    ) : (
                      <PriorityBadge priority={task.priority} />
                    )} 
                  </td>
                  <td className="py-4 px-4">
                     
                    {canEditTask(task) ? (
                      <InlineSelect
                        value={task.statusId}
                        onChange={(value) =>
                          handleInlineUpdate(task.id, "statusId", value)
                        }
                        options={statusOptions}
                        loading={savingCell === `${task.id}-statusId`}
                      />
                    ) : (
                      <StatusBadge status={task.status} />
                    )} 
                  </td>
                  <td className="py-4 px-4">
                     
                    {canEditTask(task) ? (
                      <InlineSelect
                        value={task.assignee?.id || ""}
                        onChange={(value) =>
                          handleInlineUpdate(task.id, "assigneeId", value)
                        }
                        options={assigneeOptions}
                        loading={savingCell === `${task.id}-assigneeId`}
                      />
                    ) : (
                      <div className="flex items-center gap-2">
                         
                        {task.assignee && (
                          <div className="flex h-6 w-6 items-center justify-center rounded-full bg-primary-100 dark:bg-primary-900/40 text-primary-700 dark:text-primary-300 text-xs font-bold">
                             
                            {task.assignee.name?.charAt(0)?.toUpperCase()} 
                          </div>
                        )} 
                        <span className="text-sm text-surface-700 dark:text-surface-300">
                          {task.assignee?.name || "Unassigned"}
                        </span> 
                      </div>
                    )} 
                  </td>
                  <td className="py-4 px-4 text-sm">
                     
                    <span
                      className={`inline-flex items-center rounded-md px-2 py-1 text-xs font-medium ${task.dueDate && new Date(task.dueDate) < new Date() ? "bg-danger-100 dark:bg-danger-900/30 text-danger-700 dark:text-danger-400" : "bg-success-100 dark:bg-success-900/30 text-success-700 dark:text-success-400"}`}
                    >
                       
                      {task.dueDate
                        ? new Date(task.dueDate).toLocaleDateString()
                        : "No date"} 
                    </span> 
                  </td>
                  <td className="space-x-2 py-4 px-4 text-right opacity-100 md:opacity-0 md:transition-all md:group-hover:opacity-100">
                    {task.id && (
                      <button
                        onClick={() => {
                          const slug = localStorage.getItem("activeWorkspaceSlug");
                          navigate(`/w/${slug}/tasks/${task.id}`);
                        }}
                        className="rounded-lg p-1.5 text-surface-500 hover:bg-surface-100 dark:hover:bg-surface-700"
                        title="View details"
                      >
                        <ExternalLink className="h-4 w-4" />
                      </button>
                    )}
                    {canEditTask(task) && (
                      <button
                        onClick={() => onEdit(task)}
                        className="rounded-lg p-1.5 text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-900/20"
                        title="Edit"
                      >
                        <Edit3 className="h-4 w-4" />
                      </button>
                    )}
                    {canEditTask(task) && (
                      <button
                        onClick={async () => {
                          try {
                            await cloneTask(task.id);
                            toast.success("Task duplicated");
                            onUpdate?.();
                          } catch {
                            toast.error("Failed to clone");
                          }
                        }}
                        className="rounded-lg p-1.5 text-success-600 hover:bg-success-50 dark:hover:bg-success-900/20"
                        title="Clone"
                      >
                        <Copy className="h-4 w-4" />
                      </button>
                    )}
                    {canDeleteTask() && (
                      <button
                        onClick={() => onDelete(task.id)}
                        className="rounded-lg p-1.5 text-danger-600 hover:bg-danger-50 dark:hover:bg-danger-900/20"
                        title="Delete"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    )}
                  </td>
                </tr>
              ))} 
            </tbody> 
          </table> 
        </div> 
        {tasks.length === 0 && (
          <div className="py-24 text-center text-surface-500 dark:text-surface-400">
             
            {filters.search || filters.statusId || filters.priority !== "all"
              ? "No matching tasks found"
              : "No tasks yet. Create your first task!"} 
          </div>
        )} 
        {totalPages > 1 && (
          <div className="flex items-center justify-between border-t border-surface-200 dark:border-surface-700 px-6 py-4">
             
            <span className="text-sm text-surface-500 dark:text-surface-400">
               
              Page {filters.page + 1} of {totalPages} 
            </span> 
            <div className="flex gap-2">
               
              <button
                onClick={() =>
                  setFilters((prev) => ({
                    ...prev,
                    page: Math.max(prev.page - 1, 0),
                  }))
                }
                disabled={filters.page === 0}
                className="btn btn-secondary btn-sm disabled:opacity-50"
              >
                 
                Prev 
              </button> 
              <span className="rounded-lg bg-primary-100 dark:bg-primary-900/30 px-3 py-1.5 text-sm font-semibold text-primary-700 dark:text-primary-400">
                 
                {filters.page + 1} 
              </span> 
              <button
                onClick={() =>
                  setFilters((prev) => ({ ...prev, page: prev.page + 1 }))
                }
                disabled={filters.page + 1 >= totalPages}
                className="btn btn-secondary btn-sm disabled:opacity-50"
              >
                 
                Next 
              </button> 
            </div> 
          </div>
        )} 
      </div> 
    </div>
  );
};
export default TaskTable;
