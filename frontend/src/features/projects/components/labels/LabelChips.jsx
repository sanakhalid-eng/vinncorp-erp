import { useState } from "react";
import { X } from "lucide-react";
import { removeLabelFromTask } from "../../api/labelApi";
import notify from "../../../../lib/toast";
export default function LabelChips({
  labels,
  taskId,
  onRemove,
  editable = false,
  maxShow = 5,
}) {
  const [hoveredId, setHoveredId] = useState(null);
  if (!labels || labels.length === 0) return null;
  const visible = labels.slice(0, maxShow);
  const overflow = labels.length - maxShow;
  const handleRemove = async (e, labelId) => {
    e.stopPropagation();
    if (!editable) return;
    try {
      await removeLabelFromTask(taskId, labelId);
      onRemove?.(labelId);
      notify.success("Label removed");
    } catch {
      notify.error("Failed to remove label");
    }
  };
  return (
    <div className="flex flex-wrap items-center gap-1.5">
       
      {visible.map((label) => (
        <span
          key={label.id}
          className="group relative inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium text-white transition-all hover:opacity-80"
          style={{ backgroundColor: label.color }}
          onMouseEnter={() => setHoveredId(label.id)}
          onMouseLeave={() => setHoveredId(null)}
        >
           
          {label.name} 
          {editable && hoveredId === label.id && (
            <button
              onClick={(e) => handleRemove(e, label.id)}
              className="ml-0.5 rounded-full p-0.5 hover:bg-black/20"
            >
               
              <X className="h-3 w-3" /> 
            </button>
          )} 
        </span>
      ))} 
      {overflow > 0 && (
        <span className="rounded-full bg-gray-100 px-2 py-0.5 text-xs font-medium text-gray-500">
           
          +{overflow} 
        </span>
      )} 
    </div>
  );
}
