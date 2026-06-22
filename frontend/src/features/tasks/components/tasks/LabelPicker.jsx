import { useEffect, useState } from "react";
import { X, Plus, Tag } from "lucide-react";
import { toast } from "sonner";
import { getProjectLabels, assignLabelsToTask, removeLabelFromTask } from "../../../projects/api/labelApi";

const LABEL_COLORS = [
  "#6366f1", "#8b5cf6", "#ec4899", "#ef4444", "#f97316",
  "#eab308", "#22c55e", "#14b8a6", "#06b6d4", "#3b82f6",
];

export default function LabelPicker({ taskId, projectId, selectedLabels = [], onLabelsChanged }) {
  const [projectLabels, setProjectLabels] = useState([]);
  const [showPicker, setShowPicker] = useState(false);

  useEffect(() => {
    if (!projectId) return;
    getProjectLabels(projectId)
      .then((res) => {
        const data = res?.data ?? res ?? [];
        setProjectLabels(Array.isArray(data) ? data : []);
      })
      .catch(() => setProjectLabels([]));
  }, [projectId]);

  const selectedIds = new Set(
    (selectedLabels || []).map((l) => l.id ?? l.labelId)
  );

  const handleToggle = async (labelId) => {
    try {
      if (selectedIds.has(labelId)) {
        await removeLabelFromTask(taskId, labelId);
      } else {
        await assignLabelsToTask(taskId, [labelId]);
      }
      onLabelsChanged?.();
    } catch (err) {
      toast.error("Failed to update labels");
    }
  };

  return (
    <div className="relative">
      <label className="mb-2 flex items-center gap-2 text-sm font-medium text-surface-700 dark:text-surface-300">
        <Tag className="h-4 w-4" /> Labels
      </label>
      <div className="flex flex-wrap gap-1.5 min-h-[28px] mb-2">
        {(selectedLabels || []).map((label) => (
          <span
            key={label.id ?? label.labelId}
            className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium text-white"
            style={{ backgroundColor: label.color ?? "#6366f1" }}
          >
            {label.name}
            <button
              type="button"
              onClick={() => handleToggle(label.id ?? label.labelId)}
              className="hover:opacity-80"
            >
              <X className="h-3 w-3" />
            </button>
          </span>
        ))}
        <button
          type="button"
          onClick={() => setShowPicker(!showPicker)}
          className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium border border-dashed border-surface-300 text-surface-500 hover:border-surface-400 hover:text-surface-600"
        >
          <Plus className="h-3 w-3" /> Add label
        </button>
      </div>
      {showPicker && (
        <div className="absolute z-50 mt-1 p-3 rounded-xl border border-surface-200 bg-white shadow-lg w-64">
          <div className="flex flex-wrap gap-2">
            {projectLabels.map((label) => {
              const isSelected = selectedIds.has(label.id);
              return (
                <button
                  key={label.id}
                  type="button"
                  onClick={() => handleToggle(label.id)}
                  className={`px-2.5 py-1 rounded-full text-xs font-medium transition-all ${
                    isSelected
                      ? "ring-2 ring-offset-1 ring-primary-500"
                      : "opacity-60 hover:opacity-100"
                  } text-white`}
                  style={{ backgroundColor: label.color ?? "#6366f1" }}
                >
                  {label.name}
                </button>
              );
            })}
            {projectLabels.length === 0 && (
              <p className="text-xs text-surface-400">No labels in this project</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
