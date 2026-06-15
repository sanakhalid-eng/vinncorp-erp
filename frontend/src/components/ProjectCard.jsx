import { useNavigate } from "react-router-dom";
import {
  EyeOff,
  BarChart3,
  Star,
  Users,
  DollarSign,
  AlertTriangle,
  FolderOpen,
} from "lucide-react";
import { cn } from "../utils/cn";
import ProjectPriorityBadge from "./projects/ProjectPriorityBadge";
import ProjectStatusBadge from "./projects/ProjectStatusBadge";
import ProjectProgressBar from "./projects/ProjectProgressBar";
import ProjectStatsGrid from "./projects/ProjectStatsGrid";
import ProjectDateRange from "./projects/ProjectDateRange";
import ProjectTags from "./projects/ProjectTags";
import ProjectOwnerAvatar from "./projects/ProjectOwnerAvatar";
import ProjectCardActions from "./projects/ProjectCardActions";
const healthColors = {
  healthy: "bg-emerald-500",
  risk: "bg-yellow-500",
  delayed: "bg-red-500",
};
const priorityGradients = {
  LOW: "from-emerald-500 to-green-600",
  MEDIUM: "from-amber-500 to-yellow-600",
  HIGH: "from-orange-500 to-red-500",
  CRITICAL: "from-red-500 to-red-700",
};
const ProjectCard = ({
  project,
  onEdit,
  onDelete,
  canEdit,
  canDelete,
  workspaceSlug,
  showStats = true,
  showTags = true,
  showProgress = true,
  clickable = false,
}) => {
  const navigate = useNavigate();
  const priority = project.priority || "MEDIUM";
  const progress = project.progress || 0;
  const totalTasks = project.totalTasks || 0;
  const completedTasks = project.completedTasks || 0;
  const pendingTasks = totalTasks - completedTasks;
  const health = project.health || "healthy";
  const isOverdue = project.endDate && new Date(project.endDate) < new Date();
  const handleCardClick = () => {
    if (clickable) {
      navigate(`/w/${workspaceSlug}/projects/${project.id}`);
    }
  };
  const handleEdit = () => onEdit(project);
  const handleDelete = () => onDelete(project);
  return (
    <div
      onClick={handleCardClick}
      className={cn(
        "group relative overflow-hidden rounded-3xl border border-slate-200/70 dark:border-slate-800/80 bg-white/90 dark:bg-slate-900/90 backdrop-blur-xl shadow-md transition-all duration-300",
        clickable
          ? "hover:shadow-2xl hover:-translate-y-1 hover:border-primary-400/40 cursor-pointer"
          : "hover:shadow-2xl hover:-translate-y-1 hover:border-primary-400/40",
        !project.isActive && "opacity-90",
      )}
    >
       
      {/* Top Gradient */} 
      <div
        className={cn(
          "h-2 w-full bg-gradient-to-r",
          priorityGradients[priority] || priorityGradients.MEDIUM,
        )}
      /> 
      {/* Floating Actions */} 
      <div className="absolute right-4 top-4 flex items-center gap-2 opacity-0 translate-y-1 group-hover:opacity-100 group-hover:translate-y-0 transition-all duration-300 z-10">
         
        <button
          onClick={(e) => {
            e.stopPropagation();
            navigate(`/w/${workspaceSlug}/projects/${project.id}/board`);
          }}
          className="p-2 rounded-xl bg-white dark:bg-slate-800 shadow hover:scale-105 transition"
          aria-label="Open board view"
          title="Board View"
        >
           
          <BarChart3 className="w-4 h-4 text-slate-600 dark:text-slate-300" /> 
        </button> 
        <button
          onClick={(e) => {
            e.stopPropagation();
            navigate(`/w/${workspaceSlug}/projects/${project.id}/settings`);
          }}
          className="p-2 rounded-xl bg-white dark:bg-slate-800 shadow hover:scale-105 transition"
          aria-label="Open project settings"
          title="Project Settings"
        >
           
          <Star className="w-4 h-4 text-yellow-500" /> 
        </button> 
      </div> 
      <div className="p-6">
         
        {/* Header */} 
        <div className="flex items-start justify-between gap-4">
           
          <div className="flex-1 min-w-0">
             
            <div className="flex items-center gap-2">
               
              <h3 className="truncate text-xl font-bold text-slate-900 dark:text-slate-100 group-hover:text-primary-600 dark:group-hover:text-primary-400 transition-colors">
                 
                {project.name} 
              </h3> 
              {!project.isPublic && (
                <EyeOff className="w-4 h-4 text-slate-400 shrink-0" />
              )} 
            </div> 
            {project.category && (
              <div className="mt-1 flex items-center gap-1 text-sm text-slate-500">
                 
                <FolderOpen className="w-4 h-4" /> {project.category} 
              </div>
            )} 
          </div> 
          {/* Health + Owner */} 
          <div className="flex items-center gap-3 shrink-0">
             
            <div
              className={cn(
                "h-3 w-3 rounded-full",
                healthColors[health] || healthColors.healthy,
              )}
              title={`Health: ${health}`}
            /> 
            <ProjectOwnerAvatar ownerName={project.ownerName} /> 
          </div> 
        </div> 
        {/* Description */} 
        {project.description && (
          <p className="mt-4 text-sm leading-6 text-slate-600 dark:text-slate-400 line-clamp-2">
             
            {project.description} 
          </p>
        )} 
        {/* Status + Priority */} 
        <div className="mt-4 flex flex-wrap items-center gap-2">
           
          <ProjectStatusBadge status={project.status} /> 
          <ProjectPriorityBadge priority={priority} /> 
          {isOverdue && (
            <span className="inline-flex items-center gap-1 rounded-full bg-red-100 dark:bg-red-900/30 px-3 py-1 text-xs font-semibold text-red-600 dark:text-red-400">
               
              <AlertTriangle className="w-3 h-3" /> Overdue 
            </span>
          )} 
        </div> 
        {/* Tags */} 
        {showTags && (
          <div className="mt-4">
            <ProjectTags tags={project.tags} />
          </div>
        )} 
        {/* Progress */} 
        {showProgress && (
          <div className="mt-5">
             
            <ProjectProgressBar percent={progress} /> 
          </div>
        )} 
        {/* Stats */} 
        {showStats && (
          <div className="mt-5">
             
            <ProjectStatsGrid
              totalTasks={totalTasks}
              completedTasks={completedTasks}
              pendingTasks={pendingTasks}
            /> 
          </div>
        )} 
        {/* Details Row */} 
        <div className="mt-5 space-y-3 text-sm">
           
          <ProjectDateRange
            startDate={project.startDate}
            endDate={project.endDate}
          /> 
          <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
             
            <Users className="w-4 h-4" /> 
            <span>
               
              {project.memberCount || 1} member
              {project.memberCount !== 1 ? "s" : ""} 
            </span> 
          </div> 
          {project.budget && (
            <div className="flex items-center gap-2 text-slate-500 dark:text-slate-400">
               
              <DollarSign className="w-4 h-4" /> 
              <span>
                 
                {project.currency || "$"} {project.budget.toLocaleString()} 
              </span> 
            </div>
          )} 
        </div> 
        {/* Footer Actions */} 
        <div onClick={(e) => e.stopPropagation()}>
           
          <ProjectCardActions
            projectId={project.id}
            workspaceSlug={workspaceSlug}
            canEdit={canEdit}
            canDelete={canDelete}
            onEdit={handleEdit}
            onDelete={handleDelete}
          /> 
        </div> 
      </div> 
    </div>
  );
};
export default ProjectCard;
