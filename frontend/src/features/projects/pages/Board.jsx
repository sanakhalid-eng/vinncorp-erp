import { useCallback, useEffect, useState, useMemo, useRef } from "react";
import { useParams } from "react-router-dom";
import {
  Plus,
  Trash2,
  GripVertical,
  X,
  Clock,
  User,
  Calendar,
  Paperclip,
  Timer,
  Play,
  Square,
  AlertTriangle,
  GripHorizontal,
  ChevronDown,
} from "lucide-react";
import { FaSlack } from "react-icons/fa";
import { PageSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../components/members/ConfirmationDialog";
import {
  logTime,
  getTaskTimeLogs,
  deleteTimeLog,
} from "../../../api/timeTrackingApi";
import { toast } from "sonner";
import {
  getBoardByProject,
  createBoard,
  addColumn,
  deleteColumn,
  updateColumn,
} from "../api/boardApi";
import { getProjectById } from "../api/projectApi";
import { moveTask, deleteTask } from "../../tasks/api/taskApi";
import { usePermission } from "../../../context/usePermission";
import { BOARD_EDIT, BOARD_CREATE, TASK_CREATE } from "../../../constants/permissions";
import { useProjectPermission } from "../../../context/ProjectPermissionContext.jsx";
import { useAuth } from "../../../context/useAuth";
import { useIsMobile } from "../../../hooks/useBreakpoint";
import { useWebSocket } from "../../../hooks/useWebSocket";
import { useWorkspace } from "../../../context/WorkspaceContext";
import { wsService } from "../../../api/websocket";
import TaskDrawer from "../../tasks/components/tasks/TaskDrawer";
import DependencyBadge from "../../tasks/components/tasks/DependencyBadge";
import CommentsList from "../../tasks/components/comments/CommentsList";
import AttachmentList from "../../tasks/components/attachments/AttachmentList";
import SubtaskList from "../../tasks/components/subtasks/SubtaskList";
import LabelChips from "../components/labels/LabelChips";
import LabelPicker from "../components/labels/LabelPicker";
import TimeTrackingSection from "../../tasks/components/TimeTrackingSection";
import TaskDependencyPanel from "../../tasks/components/tasks/TaskDependencyPanel";
import DependencyGraphModal from "../../tasks/components/tasks/DependencyGraphModal";
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  MouseSensor,
  TouchSensor,
  useSensor,
  useSensors,
  closestCorners,
} from "@dnd-kit/core";
import {
  SortableContext,
  useSortable,
  horizontalListSortingStrategy,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

const PriorityBadge = ({ priority }) => {
  const colors = {
    CRITICAL:
      "bg-danger-100 text-danger-800 dark:bg-danger-900/30 dark:text-danger-400",
    HIGH: "bg-warning-100 text-warning-800 dark:bg-warning-900/30 dark:text-warning-400",
    MEDIUM:
      "bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400",
    LOW: "bg-success-100 text-success-800 dark:bg-success-900/30 dark:text-success-400",
  };
  return (
    <span
      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${colors[priority] || colors.MEDIUM}`}
    >
      {priority}
    </span>
  );
};

function SortableTaskCard({ task, onClick, isOverlay }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: `task-${task.id}`,
    data: { type: "task", task },
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition: isDragging
      ? "none"
      : transition || "transform 200ms cubic-bezier(0.2, 0, 0, 1)",
    opacity: isDragging ? 0.4 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      onClick={() => onClick(task)}
      className={`group/card cursor-grab rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-4 shadow-sm transition-all hover:shadow-md hover:border-primary-300 dark:hover:border-primary-600 active:cursor-grabbing ${
        isOverlay
          ? "shadow-lg scale-105 rotate-2 ring-2 ring-primary-500/30"
          : ""
      } ${isDragging && !isOverlay ? "opacity-40" : ""}`}
    >
      <div className="flex items-start gap-2">
          <div
            {...listeners}
            {...attributes}
            className="mt-1 cursor-grab text-surface-400 hover:text-surface-600 dark:hover:text-surface-300 active:cursor-grabbing focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
            aria-label="Drag to reorder task"
          >
            <GripHorizontal className="h-4 w-4" />
          </div>
        <div className="min-w-0 flex-1">
          <div className="mb-2 flex items-start justify-between gap-2">
            <PriorityBadge priority={task.priority} />
            <DependencyBadge
              blocked={task.blocked}
              dependencyCount={task.dependencyCount}
            />
          </div>
          <h4 className="mb-2 line-clamp-2 text-sm font-medium text-surface-900 dark:text-surface-100">
            {task.title}
          </h4>
          {task.labels && task.labels.length > 0 && (
            <div className="mb-2">
              <LabelChips labels={task.labels} maxShow={3} />
            </div>
          )}
          <div className="flex flex-wrap items-center gap-3 text-xs text-surface-500 dark:text-surface-400">
            {task.assignee && (
              <div className="flex items-center gap-1">
                <User className="h-3 w-3" />
                <span className="truncate">{task.assignee.name}</span>
              </div>
            )}
            {task.dueDate && (
              <div className="flex items-center gap-1">
                <Calendar className="h-3 w-3" />
                <span>{new Date(task.dueDate).toLocaleDateString()}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function SortableColumn({ column, tasks, onTaskClick, onTaskSaved, activeId }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: `column-${column.id}`,
    data: { type: "column", column },
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition: isDragging
      ? "none"
      : transition || "transform 200ms cubic-bezier(0.2, 0, 0, 1)",
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      className="flex min-w-[280px] max-w-[280px] flex-col rounded-2xl border border-surface-200 dark:border-surface-700 bg-surface-50/50 dark:bg-surface-900/50"
    >
      <div className="flex items-center justify-between border-b border-surface-200 dark:border-surface-700 p-4">
                        <div className="flex items-center gap-2">
                          <span aria-label="Drag to reorder column"><GripVertical className="h-4 w-4 cursor-grab text-surface-400" /></span>
          <h3 className="text-sm font-semibold text-surface-900 dark:text-surface-100">
            {column.name}
          </h3>
          <span className="rounded-full bg-surface-200 dark:bg-surface-700 px-2 py-0.5 text-xs text-surface-600 dark:text-surface-400">
            {column.tasks?.length || 0}
          </span>
        </div>
      </div>

      <SortableContext
        items={tasks.map((t) => `task-${t.id}`)}
        strategy={verticalListSortingStrategy}
      >
        <div className="flex-1 space-y-2 overflow-y-auto p-3">
          {tasks.map((task) => (
            <SortableTaskCard
              key={task.id}
              task={task}
              onClick={onTaskClick}
              isOverlay={String(activeId) === `task-${task.id}`}
            />
          ))}
        </div>
      </SortableContext>

      {(!column.tasks || column.tasks.length === 0) && (
        <div className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-surface-200 dark:border-surface-700 py-8 text-surface-400 dark:text-surface-500 m-3">
          <p className="text-sm">Drop tasks here</p>
        </div>
      )}
    </div>
  );
}

function MobileTaskItem({ task, onClick }) {
  return (
    <div
      onClick={() => onClick(task)}
      className="flex cursor-pointer items-center gap-3 rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-3 shadow-sm transition-transform active:scale-[0.98]"
    >
      <PriorityBadge priority={task.priority} />
      <div className="min-w-0 flex-1">
        <p className="truncate text-sm font-medium text-surface-900 dark:text-surface-100">
          {task.title}
        </p>
        <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-surface-500 dark:text-surface-400">
          {task.assignee && (
            <div className="flex items-center gap-1">
              <div className="flex h-5 w-5 items-center justify-center rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400 text-[10px] font-semibold">
                {task.assignee.name?.charAt(0)?.toUpperCase() || "?"}
              </div>
              <span className="max-w-[80px] truncate">{task.assignee.name}</span>
            </div>
          )}
          {task.dueDate && (
            <div className="flex items-center gap-1">
              <Calendar className="h-3 w-3" />
              <span>{new Date(task.dueDate).toLocaleDateString()}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

const Board = () => {
  const { projectId } = useParams();
  const [board, setBoard] = useState(null);
  const [project, setProject] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showTaskDrawer, setShowTaskDrawer] = useState(false);
  const [drawerTask, setDrawerTask] = useState(null);
  const [isCreating, setIsCreating] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [showDepGraph, setShowDepGraph] = useState(false);
  const [newColumnName, setNewColumnName] = useState("");
  const [showAddColumn, setShowAddColumn] = useState(false);
  const [editingColumnId, setEditingColumnId] = useState(null);
  const [editingColumnName, setEditingColumnName] = useState("");
  const [dragState, setDragState] = useState({ task: null, columnId: null });
  const [movingTasks, setMovingTasks] = useState({});
  const [activeId, setActiveId] = useState(null);
  const [collapsedColumns, setCollapsedColumns] = useState({});
  const [error, setError] = useState(null);
  const [showColumnConfirm, setShowColumnConfirm] = useState(false);
  const [pendingColumnId, setPendingColumnId] = useState(null);

  const { hasPermission } = usePermission();
  const { setProjectId, clearProjectId } = useProjectPermission();
  const { user } = useAuth();
  const isMobile = useIsMobile();
  const { workspace } = useWorkspace();

  useWebSocket(workspace?.id);

  const canEditBoard = hasPermission(BOARD_EDIT);
  const canCreateBoard = hasPermission(BOARD_CREATE);
  const canCreateTask = hasPermission(TASK_CREATE);
  const canUploadAttachments = canCreateTask;

  useEffect(() => {
    if (projectId) {
      setProjectId(Number(projectId));
    } else {
      clearProjectId();
    }
    return () => clearProjectId();
  }, [projectId, setProjectId, clearProjectId]);

  useEffect(() => {
    loadData();
  }, [projectId]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [projectData, boardData] = await Promise.all([
        getProjectById(projectId),
        getBoardByProject(projectId),
      ]);
      setProject(projectData);
      setBoard(boardData);
    } catch (error) {
      console.error("Load data error:", error);
      setError(error.response?.data?.message || "Failed to load board data");
    } finally {
      setLoading(false);
    }
  };

  const debounceRef = useRef(null);

  useEffect(() => {
    const handler = (event) => {
      if (event?.entityType === "task") {
        if (debounceRef.current) clearTimeout(debounceRef.current);
        debounceRef.current = setTimeout(() => loadData(), 300);
      }
    };
    wsService.on("task_update", handler);
    return () => {
      wsService.off("task_update", handler);
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
  }, [projectId]);

  const handleCreateBoard = async () => {
    try {
      const newBoard = await createBoard(projectId);
      setBoard(newBoard);
      toast.success("Board created successfully");
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to create board");
    }
  };

  const handleAddColumn = async () => {
    if (!newColumnName.trim()) return;
    try {
      const column = await addColumn(board.id, newColumnName);
      setBoard({
        ...board,
        columns: [...board.columns, column],
      });
      setNewColumnName("");
      setShowAddColumn(false);
      toast.success("Column added");
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to add column");
    }
  };

  const handleUpdateColumn = async (columnId) => {
    if (!editingColumnName.trim()) {
      setEditingColumnId(null);
      return;
    }
    try {
      await updateColumn(columnId, editingColumnName);
      setBoard({
        ...board,
        columns: board.columns.map((c) =>
          c.id === columnId ? { ...c, name: editingColumnName } : c,
        ),
      });
      setEditingColumnId(null);
      toast.success("Column renamed");
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to rename column");
    }
  };

  const handleDeleteColumn = async (columnId) => {
    setPendingColumnId(columnId);
    setShowColumnConfirm(true);
  };

  const handleConfirmDeleteColumn = async () => {
    if (!pendingColumnId) return;
    setShowColumnConfirm(false);
    try {
      const columnId = pendingColumnId;
      setPendingColumnId(null);
      await deleteColumn(columnId);
      setBoard({
        ...board,
        columns: board.columns.filter((c) => c.id !== columnId),
      });
      toast.success("Column deleted");
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to delete column");
    }
  };

  const sensors = useSensors(
    useSensor(MouseSensor, {
      activationConstraint: { distance: 8 },
    }),
    useSensor(TouchSensor, {
      activationConstraint: { delay: 250, tolerance: 5 },
    }),
    useSensor(PointerSensor, {
      activationConstraint: { distance: 8 },
    }),
  );

  const handleDragStart = useCallback(
    (event) => {
      const { active } = event;
      setActiveId(active.id);
      const taskId = active.id.replace("task-", "");
      const task = board?.columns
        .flatMap((c) => c.tasks || [])
        .find((t) => String(t.id) === taskId);
      if (task) {
        setDragState({ task, columnId: task.columnId || null });
      }
    },
    [board],
  );

  const handleDragEnd = useCallback(
    async (event) => {
      const { active, over } = event;
      setActiveId(null);

      if (!over || !dragState.task) {
        setDragState({ task: null, columnId: null });
        return;
      }

      const { task: draggedTask, columnId: sourceColumnId } = dragState;

      if (!sourceColumnId) {
        setDragState({ task: null, columnId: null });
        return;
      }

      let targetColumnId = null;
      const overData = over.data.current;

      if (overData?.type === "column") {
        targetColumnId = overData.column.id;
      } else if (overData?.type === "task") {
        const overTask = overData.task;
        targetColumnId = overTask.columnId;
      }

      if (!targetColumnId || sourceColumnId === targetColumnId) {
        setDragState({ task: null, columnId: null });
        return;
      }

      const targetCol = board.columns.find((c) => c.id === targetColumnId);
      const newPosition = targetCol.tasks?.length || 0;

      setMovingTasks((prev) => ({ ...prev, [draggedTask.id]: true }));

      const previousBoard = JSON.parse(JSON.stringify(board));
      setBoard((prev) => ({
        ...prev,
        columns: prev.columns.map((col) => {
          if (col.id === sourceColumnId) {
            return {
              ...col,
              tasks: col.tasks.filter((t) => t.id !== draggedTask.id),
            };
          }
          if (col.id === targetColumnId) {
            return {
              ...col,
              tasks: [
                ...(col.tasks || []),
                { ...draggedTask, columnId: targetColumnId },
              ],
            };
          }
          return col;
        }),
      }));

      try {
        await moveTask(
          draggedTask.id,
          sourceColumnId,
          targetColumnId,
          newPosition,
        );
        toast.success(`Moved to ${targetCol.name}`);
      } catch (error) {
        setBoard(previousBoard);
        toast.error(error.response?.data?.message || "Failed to move task");
      } finally {
        setMovingTasks((prev) => ({ ...prev, [draggedTask.id]: false }));
        setDragState({ task: null, columnId: null });
      }
    },
    [dragState, board],
  );

  const handleDragCancel = useCallback(() => {
    setActiveId(null);
    setDragState({ task: null, columnId: null });
  }, []);

  const handleTaskClick = (task) => {
    setDrawerTask(task);
    setIsCreating(false);
    setShowTaskDrawer(true);
  };

  const handleTaskSaved = () => {
    loadData();
    setShowTaskDrawer(false);
    setDrawerTask(null);
    setIsCreating(false);
  };

  const handleDeleteTask = async () => {
    if (!drawerTask) return;
    try {
      await deleteTask(drawerTask.id);
      toast.success("Task deleted");
      setShowDeleteConfirm(false);
      setShowTaskDrawer(false);
      loadData();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to delete task");
      setShowDeleteConfirm(false);
    }
  };

  const activeTask = useMemo(() => {
    if (!activeId) return null;
    const taskId = activeId.replace("task-", "");
    return (
      board?.columns
        .flatMap((c) => c.tasks || [])
        .find((t) => String(t.id) === taskId) || null
    );
  }, [activeId, board]);

  if (loading) {
    return <PageSkeleton />;
  }

  if (error) {
    return <ErrorState title="Failed to load board" message={error} onRetry={loadData} />;
  }

  if (!board) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-4">
        <div className="card text-center">
          <h2 className="mb-2 text-2xl font-bold text-surface-900 dark:text-surface-100">
            No Board Yet
          </h2>
          <p className="mb-6 text-surface-500 dark:text-surface-400">
            Create a Kanban board to organize your project tasks
          </p>
          {canCreateBoard && (
            <button onClick={handleCreateBoard} className="btn btn-primary">
              Create Board
            </button>
          )}
        </div>
      </div>
    );
  }

  if (isMobile) {
    return (
      <>
        <div className="flex h-full flex-col">
          <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="text-2xl font-bold text-surface-900 dark:text-surface-100 md:text-3xl">
                {project?.name} Board
              </h1>
              <p className="text-surface-500 dark:text-surface-400">
                Kanban board for task management
              </p>
            </div>
            {canCreateTask && (
              <button
                onClick={() => {
                  setDrawerTask(null);
                  setIsCreating(true);
                  setShowTaskDrawer(true);
                }}
                className="btn btn-primary self-start"
              >
                <Plus className="h-4 w-4" />
                Add Task
              </button>
            )}
          </div>

          <div className="flex-1 space-y-2 overflow-y-auto pb-4">
            {(board?.columns || [])
              .sort((a, b) => (a.order || 0) - (b.order || 0))
              .map((column) => {
                const isCollapsed = collapsedColumns[column.id];
                return (
                  <div
                    key={column.id}
                    className="overflow-hidden rounded-2xl border border-surface-200 dark:border-surface-700 bg-surface-50/50 dark:bg-surface-900/50"
                  >
                    <button
                      onClick={() =>
                        setCollapsedColumns((prev) => ({
                          ...prev,
                          [column.id]: !prev[column.id],
                        }))
                      }
                      className="flex w-full items-center justify-between p-3 text-sm font-semibold text-surface-900 dark:text-surface-100"
                    >
                      <div className="flex items-center gap-2">
                        <span>{column.name}</span>
                        <span className="rounded-full bg-surface-200 dark:bg-surface-700 px-2 py-0.5 text-xs text-surface-600 dark:text-surface-400">
                          {column.tasks?.length || 0}
                        </span>
                      </div>
                      <ChevronDown
                        className={`h-4 w-4 text-surface-400 transition-transform ${isCollapsed ? "-rotate-90" : ""}`}
                      />
                    </button>
                    {!isCollapsed && (
                      <div className="space-y-2 p-3 pt-0">
                        {(column.tasks || []).map((task) => (
                          <MobileTaskItem
                            key={task.id}
                            task={task}
                            onClick={handleTaskClick}
                          />
                        ))}
                        {(column.tasks || []).length === 0 && (
                          <p className="py-4 text-center text-sm text-surface-400 dark:text-surface-500">
                            No tasks
                          </p>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
          </div>
        </div>

        <TaskDrawer
          isOpen={showTaskDrawer}
          onClose={() => {
            setShowTaskDrawer(false);
            setDrawerTask(null);
            setIsCreating(false);
          }}
          task={isCreating ? null : drawerTask}
          projectId={Number(projectId)}
          columns={board?.columns || []}
          onTaskSaved={handleTaskSaved}
        />

        {showDepGraph && drawerTask && (
          <DependencyGraphModal
            taskId={drawerTask.id}
            onClose={() => setShowDepGraph(false)}
          />
        )}

        {showDeleteConfirm && (
          <div className="fixed inset-0 z-[60] flex items-center justify-center">
            <div
              className="fixed inset-0 bg-black/40"
              onClick={() => setShowDeleteConfirm(false)}
            />
            <div className="relative w-full max-w-sm mx-4 rounded-2xl bg-white p-6 shadow-2xl dark:bg-surface-800">
              <div className="mb-4 flex items-center gap-3">
                <div className="flex h-10 w-10 items-center justify-center rounded-full bg-danger-100 dark:bg-danger-900/30">
                  <AlertTriangle className="h-5 w-5 text-danger-600 dark:text-danger-400" />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-surface-900 dark:text-surface-100">
                    Delete Task
                  </h3>
                  <p className="text-sm text-surface-500 dark:text-surface-400">
                    This action cannot be undone.
                  </p>
                </div>
              </div>
              <p className="mb-6 text-sm text-surface-600 dark:text-surface-300">
                Are you sure you want to delete "{drawerTask?.title}"?
              </p>
              <div className="flex gap-3">
                <button
                  onClick={() => setShowDeleteConfirm(false)}
                  className="btn btn-secondary flex-1"
                >
                  Cancel
                </button>
                <button
                  onClick={handleDeleteTask}
                  className="btn btn-danger flex-1"
                >
                  Delete
                </button>
              </div>
            </div>
          </div>
        )}

        {showTaskDrawer && drawerTask && !isCreating && (
          <div className="fixed inset-0 z-50 flex justify-end">
            <div
              className="fixed inset-0 bg-black/30 backdrop-blur-sm"
              onClick={() => setShowTaskDrawer(false)}
            />
            <div className="relative flex h-full w-full max-w-2xl flex-col bg-white shadow-2xl dark:bg-surface-800">
              <div className="flex items-center justify-between border-b border-surface-200 p-6 dark:border-surface-700">
                <h2 className="text-xl font-bold text-surface-900 dark:text-surface-100">
                  Task Details
                </h2>
                <button
                  onClick={() => setShowTaskDrawer(false)}
                  className="rounded-lg p-2 hover:bg-surface-100 dark:hover:bg-surface-700"
                >
                  <X className="h-5 w-5 text-surface-500" />
                </button>
              </div>
              <div className="flex-1 space-y-6 overflow-y-auto p-6">
                <div>
                  <h3 className="mb-2 text-2xl font-bold text-surface-900 dark:text-surface-100">
                    {drawerTask.title}
                  </h3>
                  <PriorityBadge priority={drawerTask.priority} />
                </div>

                {drawerTask.description && (
                  <div>
                    <h4 className="mb-2 text-sm font-semibold text-surface-500 dark:text-surface-400">
                      Description
                    </h4>
                    <p className="text-surface-700 dark:text-surface-300">
                      {drawerTask.description}
                    </p>
                  </div>
                )}

                <div className="space-y-3">
                  {drawerTask.assignee && (
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 text-primary-600 dark:bg-primary-900/30 dark:text-primary-400">
                        <User className="h-5 w-5" />
                      </div>
                      <div>
                        <p className="text-sm text-surface-500 dark:text-surface-400">
                          Assignee
                        </p>
                        <p className="font-medium text-surface-900 dark:text-surface-100">
                          {drawerTask.assignee.name}
                        </p>
                      </div>
                    </div>
                  )}

                  {drawerTask.dueDate && (
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-warning-100 text-warning-600 dark:bg-warning-900/30 dark:text-warning-400">
                        <Clock className="h-5 w-5" />
                      </div>
                      <div>
                        <p className="text-sm text-surface-500 dark:text-surface-400">
                          Due Date
                        </p>
                        <p className="font-medium text-surface-900 dark:text-surface-100">
                          {new Date(drawerTask.dueDate).toLocaleDateString()}
                        </p>
                      </div>
                    </div>
                  )}

                  {drawerTask.status && (
                    <div className="flex items-center gap-3">
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-success-100 text-success-600 dark:bg-success-900/30 dark:text-success-400">
                        <Calendar className="h-5 w-5" />
                      </div>
                      <div>
                        <p className="text-sm text-surface-500 dark:text-surface-400">
                          Status
                        </p>
                        <p className="font-medium text-surface-900 dark:text-surface-100">
                          {drawerTask.status}
                        </p>
                      </div>
                    </div>
                  )}

                  <div className="mt-4">
                    <span className="text-sm font-semibold text-surface-500 dark:text-surface-400">
                      Labels:
                    </span>
                    {drawerTask.labels && drawerTask.labels.length > 0 && (
                      <div className="mt-1.5">
                        <LabelChips
                          labels={drawerTask.labels}
                          taskId={drawerTask.id}
                          editable
                          onRemove={(labelId) => {
                            setDrawerTask((prev) => ({
                              ...prev,
                              labels: prev.labels.filter((l) => l.id !== labelId),
                            }));
                          }}
                        />
                      </div>
                    )}
                    <div className="mt-2">
                      <LabelPicker
                        projectId={Number(projectId)}
                        taskId={drawerTask.id}
                        currentLabels={drawerTask.labels || []}
                        onLabelsChange={(newLabels) => {
                          setDrawerTask((prev) => ({
                            ...prev,
                            labels: newLabels,
                          }));
                        }}
                      />
                    </div>
                  </div>
                </div>

                <div className="border-t border-surface-200 pt-6 dark:border-surface-700">
                  <TaskDependencyPanel
                    taskId={drawerTask.id}
                    projectId={projectId}
                    onBlockedChange={(blocked) =>
                      setDrawerTask((prev) => ({ ...prev, blocked }))
                    }
                  />
                  <button
                    onClick={() => setShowDepGraph(true)}
                    className="btn btn-secondary btn-sm mt-2 w-full"
                  >
                    View Dependency Graph
                  </button>
                </div>

                <div className="border-t border-surface-200 pt-6 dark:border-surface-700">
                  <div className="mb-4 flex items-center gap-2">
                    <svg
                      className="h-5 w-5 text-surface-500"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                    >
                      <path d="M9 6H20M9 12H20M9 18H20M4 6h.01M4 12h.01M4 18h.01" />
                    </svg>
                    <h4 className="text-sm font-semibold text-surface-700 dark:text-surface-300">
                      Subtasks
                    </h4>
                  </div>
                  <SubtaskList taskId={drawerTask.id} />
                </div>

                <div className="border-t border-surface-200 pt-6 dark:border-surface-700">
                  <div className="mb-4 flex items-center gap-2">
                    <Paperclip className="h-5 w-5 text-surface-500" />
                    <h4 className="text-sm font-semibold text-surface-700 dark:text-surface-300">
                      Attachments
                    </h4>
                  </div>
                  <AttachmentList
                    taskId={drawerTask.id}
                    currentUser={user}
                    canUpload={canUploadAttachments}
                  />
                </div>

                <div className="border-t border-surface-200 pt-6 dark:border-surface-700">
                  <div className="mb-4 flex items-center gap-2">
                    <FaSlack className="h-5 w-5 text-surface-500" />
                    <h4 className="text-sm font-semibold text-surface-700 dark:text-surface-300">
                      Comments
                    </h4>
                  </div>
                  <CommentsList taskId={drawerTask.id} currentUser={user} />
                </div>

                {user && (
                  <TimeTrackingSection taskId={drawerTask.id} userId={user.id} />
                )}
              </div>
              <div className="space-y-3 border-t border-surface-200 p-6 dark:border-surface-700">
                <button
                  onClick={() => {
                    setIsCreating(false);
                  }}
                  className="btn btn-primary w-full"
                >
                  Edit Task
                </button>
                <button
                  onClick={() => setShowDeleteConfirm(true)}
                  className="btn btn-outline-danger w-full"
                >
                  <Trash2 className="h-4 w-4" />
                  Delete Task
                </button>
              </div>
            </div>
          </div>
        )}
      </>
    );
  }

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCorners}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
      onDragCancel={handleDragCancel}
    >
      <div className="flex h-full flex-col">
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-surface-900 dark:text-surface-100 md:text-3xl">
              {project?.name} Board
            </h1>
            <p className="text-surface-500 dark:text-surface-400">
              Kanban board for task management
            </p>
          </div>
          {canCreateTask && (
            <button
              onClick={() => {
                setDrawerTask(null);
                setIsCreating(true);
                setShowTaskDrawer(true);
              }}
              className="btn btn-primary self-start"
            >
              <Plus className="h-4 w-4" />
              Add Task
            </button>
          )}
        </div>

        <div className="flex flex-1 gap-4 overflow-x-auto pb-4 scrollbar-thin">
          <SortableContext
            items={(board?.columns || []).map((c) => `column-${c.id}`)}
            strategy={horizontalListSortingStrategy}
          >
            {(board?.columns || [])
              .sort((a, b) => (a.order || 0) - (b.order || 0))
              .map((column) => (
                <SortableColumn
                  key={column.id}
                  column={column}
                  tasks={column.tasks || []}
                  onTaskClick={handleTaskClick}
                  activeId={activeId}
                />
              ))}
          </SortableContext>

          {canEditBoard && (
            <div className="min-w-[280px] max-w-[280px]">
              {showAddColumn ? (
                <div className="rounded-2xl border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-900/50 p-4">
                  <input
                    type="text"
                    value={newColumnName}
                    onChange={(e) => setNewColumnName(e.target.value)}
                    placeholder="Column name"
                    className="input-field mb-3 text-sm"
                    autoFocus
                    onKeyDown={(e) => e.key === "Enter" && handleAddColumn()}
                  />
                  <div className="flex gap-2">
                    <button
                      onClick={handleAddColumn}
                      className="btn btn-primary btn-sm flex-1"
                    >
                      Add
                    </button>
                    <button
                      onClick={() => {
                        setShowAddColumn(false);
                        setNewColumnName("");
                      }}
                      className="btn btn-secondary btn-sm flex-1"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              ) : (
                <button
                  onClick={() => setShowAddColumn(true)}
                  className="flex w-full items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-surface-300 dark:border-surface-600 p-4 text-surface-500 dark:text-surface-400 transition-all hover:border-primary-400 hover:text-primary-600 dark:hover:border-primary-500 dark:hover:text-primary-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
                  aria-label="Add column"
                >
                  <Plus className="h-5 w-5" />
                  Add Column
                </button>
              )}
            </div>
          )}
        </div>
      </div>

      <DragOverlay>
        {activeTask ? (
          <div className="w-[260px]">
            <SortableTaskCard task={activeTask} onClick={() => {}} isOverlay />
          </div>
        ) : null}
      </DragOverlay>

      <TaskDrawer
        isOpen={showTaskDrawer}
        onClose={() => {
          setShowTaskDrawer(false);
          setDrawerTask(null);
          setIsCreating(false);
        }}
        task={isCreating ? null : drawerTask}
        projectId={Number(projectId)}
        columns={board?.columns || []}
        onTaskSaved={handleTaskSaved}
      />

      {showDepGraph && drawerTask && (
        <DependencyGraphModal
          taskId={drawerTask.id}
          onClose={() => setShowDepGraph(false)}
        />
      )}

      {showDeleteConfirm && (
        <div className="fixed inset-0 z-[60] flex items-center justify-center">
          <div
            className="fixed inset-0 bg-black/40"
            onClick={() => setShowDeleteConfirm(false)}
          />
          <div className="relative rounded-2xl bg-white dark:bg-surface-800 p-6 shadow-2xl w-full max-w-sm mx-4">
            <div className="flex items-center gap-3 mb-4">
              <div className="flex h-10 w-10 items-center justify-center rounded-full bg-danger-100 dark:bg-danger-900/30">
                <AlertTriangle className="h-5 w-5 text-danger-600 dark:text-danger-400" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-surface-900 dark:text-surface-100">
                  Delete Task
                </h3>
                <p className="text-sm text-surface-500 dark:text-surface-400">
                  This action cannot be undone.
                </p>
              </div>
            </div>
            <p className="mb-6 text-sm text-surface-600 dark:text-surface-300">
              Are you sure you want to delete "{drawerTask?.title}"?
            </p>
            <div className="flex gap-3">
              <button
                onClick={() => setShowDeleteConfirm(false)}
                className="btn btn-secondary flex-1"
                aria-label="Cancel delete"
              >
                Cancel
              </button>
              <button
                onClick={handleDeleteTask}
                className="btn btn-danger flex-1"
                aria-label="Confirm delete task"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {showTaskDrawer && drawerTask && !isCreating && (
        <div className="fixed inset-0 z-50 flex justify-end">
          <div
            className="fixed inset-0 bg-black/30 backdrop-blur-sm"
            onClick={() => setShowTaskDrawer(false)}
          />
          <div className="relative flex h-full w-full max-w-2xl flex-col bg-white dark:bg-surface-800 shadow-2xl">
            <div className="flex items-center justify-between border-b border-surface-200 dark:border-surface-700 p-6">
              <h2 className="text-xl font-bold text-surface-900 dark:text-surface-100">
                Task Details
              </h2>
              <button
                onClick={() => setShowTaskDrawer(false)}
                className="rounded-lg p-2 hover:bg-surface-100 dark:hover:bg-surface-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
                aria-label="Close task details"
              >
                <X className="h-5 w-5 text-surface-500" />
              </button>
            </div>
            <div className="flex-1 space-y-6 overflow-y-auto p-6">
              <div>
                <h3 className="mb-2 text-2xl font-bold text-surface-900 dark:text-surface-100">
                  {drawerTask.title}
                </h3>
                <PriorityBadge priority={drawerTask.priority} />
              </div>

              {drawerTask.description && (
                <div>
                  <h4 className="mb-2 text-sm font-semibold text-surface-500 dark:text-surface-400">
                    Description
                  </h4>
                  <p className="text-surface-700 dark:text-surface-300">
                    {drawerTask.description}
                  </p>
                </div>
              )}

              <div className="space-y-3">
                {drawerTask.assignee && (
                  <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary-100 dark:bg-primary-900/30 text-primary-600 dark:text-primary-400">
                      <User className="h-5 w-5" />
                    </div>
                    <div>
                      <p className="text-sm text-surface-500 dark:text-surface-400">
                        Assignee
                      </p>
                      <p className="font-medium text-surface-900 dark:text-surface-100">
                        {drawerTask.assignee.name}
                      </p>
                    </div>
                  </div>
                )}

                {drawerTask.dueDate && (
                  <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-warning-100 dark:bg-warning-900/30 text-warning-600 dark:text-warning-400">
                      <Clock className="h-5 w-5" />
                    </div>
                    <div>
                      <p className="text-sm text-surface-500 dark:text-surface-400">
                        Due Date
                      </p>
                      <p className="font-medium text-surface-900 dark:text-surface-100">
                        {new Date(drawerTask.dueDate).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                )}

                {drawerTask.status && (
                  <div className="flex items-center gap-3">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-success-100 dark:bg-success-900/30 text-success-600 dark:text-success-400">
                      <Calendar className="h-5 w-5" />
                    </div>
                    <div>
                      <p className="text-sm text-surface-500 dark:text-surface-400">
                        Status
                      </p>
                      <p className="font-medium text-surface-900 dark:text-surface-100">
                        {drawerTask.status}
                      </p>
                    </div>
                  </div>
                )}

                <div className="mt-4">
                  <span className="text-sm font-semibold text-surface-500 dark:text-surface-400">
                    Labels:
                  </span>
                  {drawerTask.labels && drawerTask.labels.length > 0 && (
                    <div className="mt-1.5">
                      <LabelChips
                        labels={drawerTask.labels}
                        taskId={drawerTask.id}
                        editable
                        onRemove={(labelId) => {
                          setDrawerTask((prev) => ({
                            ...prev,
                            labels: prev.labels.filter((l) => l.id !== labelId),
                          }));
                        }}
                      />
                    </div>
                  )}
                  <div className="mt-2">
                    <LabelPicker
                      projectId={Number(projectId)}
                      taskId={drawerTask.id}
                      currentLabels={drawerTask.labels || []}
                      onLabelsChange={(newLabels) => {
                        setDrawerTask((prev) => ({
                          ...prev,
                          labels: newLabels,
                        }));
                      }}
                    />
                  </div>
                </div>
              </div>

              <div className="border-t border-surface-200 dark:border-surface-700 pt-6">
                <TaskDependencyPanel
                  taskId={drawerTask.id}
                  projectId={projectId}
                  onBlockedChange={(blocked) =>
                    setDrawerTask((prev) => ({ ...prev, blocked }))
                  }
                />
                <button
                  onClick={() => setShowDepGraph(true)}
                  className="btn btn-secondary btn-sm mt-2 w-full"
                >
                  View Dependency Graph
                </button>
              </div>

              <div className="border-t border-surface-200 dark:border-surface-700 pt-6">
                <div className="mb-4 flex items-center gap-2">
                  <svg
                    className="h-5 w-5 text-surface-500"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                  >
                    <path d="M9 6H20M9 12H20M9 18H20M4 6h.01M4 12h.01M4 18h.01" />
                  </svg>
                  <h4 className="text-sm font-semibold text-surface-700 dark:text-surface-300">
                    Subtasks
                  </h4>
                </div>
                <SubtaskList taskId={drawerTask.id} />
              </div>

              <div className="border-t border-surface-200 dark:border-surface-700 pt-6">
                <div className="mb-4 flex items-center gap-2">
                  <Paperclip className="h-5 w-5 text-surface-500" />
                  <h4 className="text-sm font-semibold text-surface-700 dark:text-surface-300">
                    Attachments
                  </h4>
                </div>
                <AttachmentList
                  taskId={drawerTask.id}
                  currentUser={user}
                  canUpload={canUploadAttachments}
                />
              </div>

              <div className="border-t border-surface-200 dark:border-surface-700 pt-6">
                <div className="mb-4 flex items-center gap-2">
                  <FaSlack className="h-5 w-5 text-surface-500" />
                  <h4 className="text-sm font-semibold text-surface-700 dark:text-surface-300">
                    Comments
                  </h4>
                </div>
                <CommentsList taskId={drawerTask.id} currentUser={user} />
              </div>

              {user && (
                <TimeTrackingSection taskId={drawerTask.id} userId={user.id} />
              )}
            </div>
            <div className="border-t border-surface-200 dark:border-surface-700 p-6 space-y-3">
              <button
                onClick={() => {
                  setIsCreating(false);
                }}
                className="btn btn-primary w-full"
              >
                Edit Task
              </button>
              <button
                onClick={() => setShowDeleteConfirm(true)}
                className="btn btn-outline-danger w-full"
              >
                <Trash2 className="h-4 w-4" />
                Delete Task
              </button>
            </div>
          </div>
        </div>
      )}

      <ConfirmationDialog
        isOpen={showColumnConfirm}
        onClose={() => { setShowColumnConfirm(false); setPendingColumnId(null); }}
        onConfirm={handleConfirmDeleteColumn}
        title="Delete Column"
        message="Are you sure you want to delete this column? Tasks in this column will not be deleted."
        confirmText="Delete"
        cancelText="Cancel"
      />
    </DndContext>
  );
};

export default Board;

