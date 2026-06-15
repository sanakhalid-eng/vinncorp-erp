import { FolderOpen, LayoutGrid, Edit3, Trash2 } from "lucide-react";
import { useNavigate } from "react-router-dom";
export default function ProjectCardActions({
  projectId,
  workspaceSlug,
  canEdit,
  canDelete,
  onEdit,
  onDelete,
}) {
  const navigate = useNavigate();
  return (
    <div className="flex items-center gap-2 border-t border-slate-200/60 dark:border-slate-800/60 pt-5">
       
      <button
        onClick={(e) => {
          e.stopPropagation();
          navigate(`/w/${workspaceSlug}/projects/${projectId}`);
        }}
        className="flex-1 flex items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-primary-500 to-indigo-600 px-4 py-3 text-sm font-semibold text-white shadow-lg hover:opacity-90 transition"
      >
         
        <FolderOpen className="w-4 h-4" /> Open Project 
      </button> 
      <button
        onClick={(e) => {
          e.stopPropagation();
          navigate(`/w/${workspaceSlug}/projects/${projectId}/board`);
        }}
        className="rounded-2xl border border-slate-200 dark:border-slate-700 p-3 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
        title="Board View"
      >
         
        <LayoutGrid className="w-4 h-4 text-slate-600 dark:text-slate-300" /> 
      </button> 
      {canEdit && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            onEdit();
          }}
          className="rounded-2xl border border-slate-200 dark:border-slate-700 p-3 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          title="Edit Project"
        >
           
          <Edit3 className="w-4 h-4 text-slate-600 dark:text-slate-300" /> 
        </button>
      )} 
      {canDelete && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            onDelete();
          }}
          className="rounded-2xl border border-red-200 dark:border-red-900 p-3 hover:bg-red-50 dark:hover:bg-red-950/40 transition"
          title="Delete Project"
        >
           
          <Trash2 className="w-4 h-4 text-red-600 dark:text-red-400" /> 
        </button>
      )} 
    </div>
  );
}
