import { useNavigate, useLocation, useParams } from "react-router-dom";
import {
  FolderKanban,
  LayoutGrid,
  Settings,
  Mail,
  Info,
  ExternalLink,
  BarChart3,
} from "lucide-react";
import { cn } from "../utils/cn";
const tabs = [
  { path: "", label: "Overview", icon: Info },
  { path: "/board", label: "Board", icon: LayoutGrid },
  { path: "/gantt", label: "Gantt", icon: BarChart3 },
  { path: "/settings", label: "Settings", icon: Settings },
  { path: "/invitations", label: "Invitations", icon: Mail },
];
export default function ProjectNavBar({ projectName }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { workspaceSlug, id, projectId } = useParams();
  const currentProjectId = id || projectId;
  const getCurrentTab = () => {
    const path = location.pathname;
    if (path.endsWith("/board")) return "/board";
    if (path.endsWith("/settings")) return "/settings";
    if (path.endsWith("/invitations")) return "/invitations";
    return "";
  };
  const activeTab = getCurrentTab();
  return (
    <div className="flex flex-col gap-4 mb-6">
       
      <div className="flex items-center gap-2 text-sm text-surface-500 dark:text-surface-400">
         
        <button
          onClick={() => navigate(`/w/${workspaceSlug}/projects`)}
          className="hover:text-primary-600 dark:hover:text-primary-400 transition-colors font-medium"
        >
           
          Projects 
        </button> 
        <span>/</span> 
        <span className="text-surface-900 dark:text-surface-100 font-semibold truncate max-w-[200px]">
           
          {projectName || "Project"} 
        </span> 
      </div> 
      <div className="flex gap-1 rounded-2xl bg-white dark:bg-surface-800 border border-surface-200 dark:border-surface-700 p-1.5 shadow-sm overflow-x-auto">
         
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.path;
          const href = tab.path
            ? `/w/${workspaceSlug}/projects/${currentProjectId}${tab.path}`
            : `/w/${workspaceSlug}/projects/${currentProjectId}`;
          return (
            <button
              key={tab.path}
              onClick={() => navigate(href)}
              className={cn(
                "flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-medium transition-all whitespace-nowrap",
                isActive
                  ? "bg-primary-600 text-white shadow-sm"
                  : "text-surface-600 dark:text-surface-400 hover:bg-surface-100 dark:hover:bg-surface-700 hover:text-surface-900 dark:hover:text-surface-100",
              )}
            >
               
              <Icon className="w-4 h-4" /> {tab.label} 
            </button>
          );
        })} 
      </div> 
    </div>
  );
}
