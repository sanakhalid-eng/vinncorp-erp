import { useCallback, useState, useMemo } from "react";
import { Edit3, Trash2, UserCheck, GripVertical } from "lucide-react";
import { toast } from "sonner";
import PriorityBadge from "./PriorityBadge.jsx";
import StatusBadge from "./StatusBadge.jsx";
import { updateTaskStatus } from "../../api/taskApi";
import { usePermission } from "../../context/usePermission.js";
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
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
function KanbanTask({ task, onEdit, onDelete, isOverlay }) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: `task-${task.id}`, data: { type: "task", task } });
  const style = {
    transform: CSS.Transform.toString(transform),
    transition: isDragging
      ? "none"
      : transition || "transform 200ms cubic-bezier(0.2, 0, 0, 1)",
    opacity: isDragging ? 0.4 : 1,
  };
  const { canEditTask, canDeleteTask } = usePermission();
  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`group/task rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-4 shadow-sm transition-all duration-200 hover:shadow-md hover:border-primary-300 dark:hover:border-primary-600 ${isOverlay ? "shadow-lg scale-105 rotate-2 ring-2 ring-primary-500/30" : ""} ${isDragging && !isOverlay ? "opacity-40" : ""}`}
    >
       
      <div className="flex items-start gap-2">
         
        <div
            {...listeners}
            {...attributes}
            className="mt-1 cursor-grab text-surface-400 hover:text-surface-600 dark:hover:text-surface-300 active:cursor-grabbing focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
            aria-label="Drag to reorder task"
          >
            
            <GripVertical className="h-4 w-4" /> 
          </div> 
        <div className="min-w-0 flex-1">
           
          <div className="mb-2 flex items-start justify-between gap-2">
             
            <PriorityBadge priority={task.priority} className="!text-xs" /> 
            <div
              className={`flex gap-1 ${isOverlay ? "opacity-100" : "opacity-0 group-hover/task:opacity-100"} transition-opacity`}
            >
               
              {canEditTask(task) && (
                <button
                  onClick={() => onEdit?.(task)}
                  className="rounded-lg p-1.5 text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-900/30 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
                  aria-label="Edit task"
                  title="Edit"
                >
                  
                  <Edit3 className="h-3.5 w-3.5" /> 
                </button>
              )} 
              {canDeleteTask() && (
                <button
                  onClick={() => onDelete?.(task.id)}
                  className="rounded-lg p-1.5 text-danger-600 hover:bg-danger-50 dark:hover:bg-danger-900/30 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-danger-500"
                  aria-label="Delete task"
                  title="Delete"
                >
                  
                  <Trash2 className="h-3.5 w-3.5" /> 
                </button>
              )} 
            </div> 
          </div> 
          <h4 className="mb-2 line-clamp-2 text-sm font-semibold leading-tight text-surface-900 dark:text-surface-100">
             
            {task.title} 
          </h4> 
          <div className="flex flex-wrap items-center gap-3 text-xs text-surface-500 dark:text-surface-400">
             
            {task.assignee && (
              <div className="flex items-center gap-1.5">
                 
                <div className="flex h-5 w-5 items-center justify-center rounded-full bg-primary-100 dark:bg-primary-900/40 text-primary-700 dark:text-primary-300 text-[10px] font-bold">
                   
                  {task.assignee.name?.charAt(0)?.toUpperCase()} 
                </div> 
                <span className="truncate">{task.assignee.name}</span> 
              </div>
            )} 
            {task.dueDate && (
              <span
                className={`rounded-md px-1.5 py-0.5 font-medium ${new Date(task.dueDate) < new Date() ? "bg-danger-100 dark:bg-danger-900/30 text-danger-700 dark:text-danger-400" : "bg-success-100 dark:bg-success-900/30 text-success-700 dark:text-success-400"}`}
              >
                 
                {new Date(task.dueDate).toLocaleDateString()} 
              </span>
            )} 
          </div> 
        </div> 
      </div> 
    </div>
  );
}
function DroppableColumn({ column, tasks, onEdit, onDelete, activeId }) {
  const { setNodeRef, isOver } = useSortable({
    id: `column-${column.id}`,
    data: { type: "column", column },
  });
  return (
    <div
      ref={setNodeRef}
      className={`flex min-h-[400px] flex-col rounded-2xl border transition-colors duration-200 ${isOver ? "border-primary-400 dark:border-primary-500 bg-primary-50/50 dark:bg-primary-900/10" : "border-surface-200 dark:border-surface-700 bg-surface-50/50 dark:bg-surface-900/50"}`}
    >
       
      <div className="sticky top-0 z-10 flex items-center justify-between border-b border-surface-200 dark:border-surface-700 bg-inherit p-4">
         
        <div className="flex items-center gap-3">
           
          <StatusBadge status={column.name} color={column.color} /> 
          <div>
             
            <h3 className="text-sm font-semibold text-surface-900 dark:text-surface-100">
              {column.name}
            </h3> 
            <span className="text-xs text-surface-500 dark:text-surface-400">
              {tasks.length} tasks
            </span> 
          </div> 
        </div> 
      </div> 
      <SortableContext
        items={tasks.map((t) => `task-${t.id}`)}
        strategy={verticalListSortingStrategy}
      >
         
        <div className="flex-1 space-y-2 overflow-y-auto p-3">
           
          {tasks.map((task) => (
            <KanbanTask
              key={task.id}
              task={task}
              onEdit={onEdit}
              onDelete={onDelete}
              isOverlay={String(activeId) === `task-${task.id}`}
            />
          ))} 
        </div> 
      </SortableContext> 
      {tasks.length === 0 && (
        <div className="flex flex-1 items-center justify-center p-8 text-center text-surface-400 dark:text-surface-500">
           
          <p className="text-sm">Drop tasks here</p> 
        </div>
      )} 
    </div>
  );
}
const TaskKanban = ({
  tasks = [],
  statuses = [],
  onEdit,
  onRefresh,
  onDelete,
}) => {
  const [activeId, setActiveId] = useState(null);
  const { canEditTask, canDeleteTask } = usePermission();
  const derivedStatuses = useMemo(() => {
    if (statuses.length > 0) return statuses;
    return [
      ...new Map(
        tasks.map((task) => [
          task.statusId || task.status,
          { id: task.statusId || task.status, name: task.status, color: null },
        ]),
      ).values(),
    ];
  }, [statuses, tasks]);
  const groupedTasks = useMemo(() => {
    return derivedStatuses.map((status) => ({
      ...status,
      tasks: tasks.filter((task) =>
        status.id != null
          ? String(task.statusId) === String(status.id)
          : task.status === status.name,
      ),
    }));
  }, [derivedStatuses, tasks]);
  const activeTask = useMemo(() => {
    if (!activeId) return null;
    const taskId = activeId.replace("task-", "");
    return tasks.find((t) => String(t.id) === taskId) || null;
  }, [activeId, tasks]);
  const sensors = useSensors(
    useSensor(MouseSensor, { activationConstraint: { distance: 8 } }),
    useSensor(TouchSensor, {
      activationConstraint: { delay: 250, tolerance: 5 },
    }),
    useSensor(PointerSensor, { activationConstraint: { distance: 8 } }),
  );
  const handleDragStart = useCallback((event) => {
    const { active } = event;
    setActiveId(active.id);
  }, []);
  const handleDragEnd = useCallback(
    async (event) => {
      const { active, over } = event;
      setActiveId(null);
      if (!over || !active.data.current?.task) return;
      const draggedTask = active.data.current.task;
      const overData = over.data.current;
      let targetColumnId = null;
      if (overData?.type === "column") {
        targetColumnId = overData.column.id;
      } else if (overData?.type === "task") {
        const overTask = overData.task;
        targetColumnId = overTask.statusId;
      }
      if (
        !targetColumnId ||
        String(draggedTask.statusId) === String(targetColumnId)
      ) {
        return;
      }
      const targetColumn = derivedStatuses.find(
        (s) => String(s.id) === String(targetColumnId),
      );
      try {
        await updateTaskStatus(draggedTask.id, targetColumnId);
        toast.success(`Moved to ${targetColumn?.name || "new status"}`);
        await onRefresh?.();
      } catch (error) {
        toast.error(error.response?.data?.message || "Failed to update status");
      }
    },
    [derivedStatuses, onRefresh],
  );
  const handleDragCancel = useCallback(() => {
    setActiveId(null);
  }, []);
  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCorners}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
      onDragCancel={handleDragCancel}
    >
       
      <div className="grid auto-rows-start grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4">
         
        {groupedTasks.map((column) => (
          <DroppableColumn
            key={column.id}
            column={column}
            tasks={column.tasks}
            onEdit={onEdit}
            onDelete={onDelete}
            activeId={activeId}
          />
        ))} 
      </div> 
      <DragOverlay>
         
        {activeTask ? <KanbanTask task={activeTask} isOverlay /> : null} 
      </DragOverlay> 
    </DndContext>
  );
};
export default TaskKanban;
