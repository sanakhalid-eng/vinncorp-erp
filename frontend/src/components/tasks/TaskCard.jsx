import { memo } from "react";
import PriorityBadge from "./PriorityBadge.jsx";
import StatusBadge from "./StatusBadge.jsx";
import DependencyBadge from "./DependencyBadge.jsx";
import { Edit3, Trash2, Copy, ExternalLink } from "lucide-react";
import { cn } from "../../utils/cn.js";
import { usePermission } from "../../context/usePermission.js";
import { useNavigate } from "react-router-dom";
import { cloneTask } from "../../api/taskApi";
import { toast } from "sonner";
const TaskCard = memo(
  ({ task, onEdit, onDelete, blocked = false, dependencyCount = 0, onUpdate }) => {
    const navigate = useNavigate();
    const { canEditTask, canDeleteTask } = usePermission();
    return (
      <div className="group bg-white/80 backdrop-blur-sm rounded-2xl p-6 shadow-lg hover:shadow-2xl border border-white/50 hover:border-indigo-200 transition-all hover:-translate-y-1 h-full">
         
        <div className="flex items-start justify-between mb-4">
           
          <PriorityBadge priority={task.priority} className="!text-xs" /> 
          <div className="flex items-center gap-2">
             
            <DependencyBadge
              blocked={blocked}
              dependencyCount={dependencyCount}
            /> 
            <StatusBadge status={task.status} /> 
          </div> 
        </div> 
        <h3 className="font-bold text-gray-900 mb-3 line-clamp-2 leading-tight text-lg">
           
          {task.title} 
        </h3> 
        {task.description && (
          <p className="text-gray-600 mb-4 text-sm line-clamp-3">
            {task.description}
          </p>
        )} 
        <div className="flex items-center justify-between pt-4 border-t border-gray-100">
           
          <div className="flex items-center gap-2 text-sm text-gray-500">
             
            {task.assignee ? (
              <>
                 
                <div className="w-6 h-6 bg-gradient-to-r from-indigo-400 to-purple-500 rounded-full flex items-center justify-center text-white text-xs font-bold">
                   
                  {task.assignee.name.charAt(0).toUpperCase()} 
                </div> 
                <span>{task.assignee.name}</span> 
              </>
            ) : (
              <span>Unassigned</span>
            )} 
          </div> 
          <div className="flex gap-1 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-opacity">
              
            {task.id && (
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  const slug = localStorage.getItem("activeWorkspaceSlug");
                  navigate(`/w/${slug}/tasks/${task.id}`);
                }}
                className="p-1.5 hover:bg-gray-100 rounded-xl text-gray-500 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400"
                aria-label="View task details"
              >
                <ExternalLink className="w-4 h-4" />
              </button>
            )}
            {canEditTask(task) && (
              <button
                onClick={() => onEdit(task)}
                className="p-1.5 hover:bg-indigo-100 rounded-xl text-indigo-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
                aria-label="Edit task"
              >
                <Edit3 className="w-4 h-4" />
              </button>
            )}
            {canEditTask(task) && (
              <button
                onClick={async () => {
                  try {
                    const cloned = await cloneTask(task.id);
                    toast.success("Task duplicated");
                    onUpdate?.();
                  } catch {
                    toast.error("Failed to clone");
                  }
                }}
                className="p-1.5 hover:bg-green-100 rounded-xl text-green-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-green-500"
                aria-label="Clone task"
              >
                <Copy className="w-4 h-4" />
              </button>
            )}
            {canDeleteTask() && (
              <button
                onClick={() => onDelete(task.id)}
                className="p-1.5 hover:bg-red-100 rounded-xl text-red-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-500"
                aria-label="Delete task"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            )}
          </div> 
        </div> 
        {task.dueDate && (
          <div className="absolute top-4 right-4 text-xs px-2 py-1 rounded-full font-semibold bg-white/90 shadow-sm">
             
            {new Date(task.dueDate).toLocaleDateString()} 
          </div>
        )} 
      </div>
    );
  },
);
export default TaskCard;
