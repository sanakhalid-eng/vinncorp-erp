import { useState, useCallback, useEffect } from "react";
import { GripVertical, Trash2, Plus } from "lucide-react";
import { reorderStatuses } from "../../api/statusApi.js";
import { toast } from "sonner";

const StatusKanbanPreview = ({
  statuses = [],
  projectId,
  onUpdateOrder,
  onDelete,
  onCreateStatus,
}) => {
  const [dragStatusId, setDragStatusId] = useState(null);
  const [editedStatuses, setEditedStatuses] = useState(statuses);

  useEffect(() => {
    setEditedStatuses(statuses);
  }, [statuses]);

  const handleDragStart = useCallback((e, statusId) => {
    setDragStatusId(statusId);
    e.dataTransfer.effectAllowed = "move";
  }, []);

  const handleDragOver = useCallback((e) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = "move";
  }, []);

  const handleDrop = useCallback(
    async (e, targetIndex) => {
      e.preventDefault();
      if (!dragStatusId) return;

      const draggedStatus = editedStatuses.find((s) => s.id === dragStatusId);
      if (!draggedStatus) return;

      const newStatuses = [...editedStatuses];
      newStatuses.splice(
        newStatuses.findIndex((s) => s.id === dragStatusId),
        1,
      );
      newStatuses.splice(targetIndex, 0, draggedStatus);

      const reordered = newStatuses.map((status, index) => ({
        ...status,
        orderIndex: index,
      }));

      setEditedStatuses(reordered);

      try {
        const savedStatuses = await reorderStatuses(projectId, reordered);
        onUpdateOrder?.(savedStatuses);
        toast.success("Workflow reordered");
      } catch {
        toast.error("Reorder failed");
        setEditedStatuses(statuses);
      }

      setDragStatusId(null);
    },
    [dragStatusId, editedStatuses, projectId, statuses, onUpdateOrder],
  );

  const handleDelete = (statusId) => {
    onDelete(statusId);
  };

  return (
    <div className="card p-6 min-h-[200px]">
      <div className="flex items-center justify-between mb-6 pb-4 border-b border-surface-200 dark:border-surface-700">
        <h3 className="text-lg font-semibold text-surface-900 dark:text-surface-100 flex items-center gap-3">
          <GripVertical className="h-5 w-5 text-primary-600 dark:text-primary-400" />
          Workflow Preview
        </h3>
        <div className="text-sm text-surface-500 dark:text-surface-400">
          Drag to reorder ΓÇó Live preview
        </div>
      </div>

      {editedStatuses.length === 0 ? (
        <div className="text-center py-12 border-2 border-dashed border-surface-200 dark:border-surface-700 rounded-2xl">
          <Plus className="h-12 w-12 mx-auto mb-3 text-surface-300 dark:text-surface-600" />
          <p className="text-base font-semibold text-surface-500 dark:text-surface-400 mb-1">
            No statuses yet
          </p>
          <p className="text-sm text-surface-400 dark:text-surface-500">
            Create statuses to see Kanban preview
          </p>
        </div>
      ) : (
        <div
          className="flex gap-4 overflow-x-auto pb-4 scrollbar-thin"
          style={{ scrollbarWidth: "thin" }}
        >
          {editedStatuses.map((status, index) => (
            <div
              key={status.id}
              className={`min-w-[200px] flex-none flex flex-col bg-white dark:bg-surface-800 rounded-xl p-4 shadow-sm border-2 transition-all cursor-grab group/status hover:shadow-md hover:border-primary-200 dark:hover:border-primary-700 active:cursor-grabbing ${
                dragStatusId === status.id
                  ? "ring-2 ring-primary-400/50 shadow-lg scale-105"
                  : "border-surface-200 dark:border-surface-700"
              }`}
              draggable
              onDragStart={(e) => handleDragStart(e, status.id)}
              onDragOver={(e) => handleDragOver(e, index)}
              onDrop={(e) => handleDrop(e, index)}
            >
              <div className="flex items-center justify-between mb-3">
                <div
                  className="w-4 h-4 rounded-full shadow-sm shrink-0"
                  style={{ backgroundColor: status.color }}
                  title={status.color}
                />
                <button
                  onClick={() => handleDelete(status.id)}
                  className="p-1 text-danger-500 hover:bg-danger-100 dark:hover:bg-danger-900/30 rounded-lg transition-all md:opacity-0 md:group-hover/status:opacity-100"
                  title="Delete"
                >
                  <Trash2 className="h-3 w-3" />
                </button>
              </div>

              <div
                className="font-semibold text-sm text-surface-900 dark:text-surface-100 mb-2 px-1 truncate"
                title={status.name}
              >
                {status.name}
              </div>

              <div className="flex items-center justify-between text-xs text-surface-500 dark:text-surface-400 mb-2">
                <span>0 tasks</span>
                <span className="font-mono px-1.5 py-0.5 bg-surface-100 dark:bg-surface-700 rounded-full text-surface-600 dark:text-surface-400">
                  {status.orderIndex}
                </span>
              </div>

              <div className="flex-1 min-h-[40px] border-2 border-dashed border-surface-200 dark:border-surface-700 rounded-xl opacity-30 group-hover/status:opacity-60 transition-all" />
            </div>
          ))}

          {onCreateStatus && (
          <button
            onClick={onCreateStatus}
            className="min-w-[200px] flex-none flex flex-col items-center justify-center border-2 border-dashed border-primary-300 dark:border-primary-700 rounded-xl p-8 transition-all hover:border-primary-400 dark:hover:border-primary-600 hover:bg-primary-50/50 dark:hover:bg-primary-900/10 opacity-60 hover:opacity-100"
          >
            <Plus className="h-8 w-8 text-primary-400 dark:text-primary-500 mb-2" />
            <span className="text-sm font-medium text-primary-600 dark:text-primary-400">
              Add Status
            </span>
          </button>
          )}
        </div>
      )}

      {editedStatuses.length > 0 && (
        <div className="flex flex-wrap gap-3 pt-4 mt-4 border-t border-surface-200 dark:border-surface-700 text-xs text-surface-500 dark:text-surface-400">
          <span>Drag columns to reorder workflow</span>
          <span>ΓÇó Colors apply to tasks & columns</span>
        </div>
      )}
    </div>
  );
};

export default StatusKanbanPreview;
