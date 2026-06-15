import { useEffect } from "react";
import { useParams, Outlet, Navigate } from "react-router-dom";
import { useWorkspace } from "../context/WorkspaceContext";
import Sidebar from "../components/Sidebar";
import NotificationDropdown from "../components/notifications/NotificationDropdown";
import NotificationToastHandler from "../components/notifications/NotificationToastHandler";
import CommandPalette from "../components/CommandPalette";
import QuickCreate from "../components/QuickCreate";
import LoadingSkeleton from "../components/LoadingSkeleton";
export default function WorkspaceLayout() {
  const { workspaceSlug } = useParams();
  const { workspace, loading, error, refreshWorkspace } = useWorkspace();
  useEffect(() => {
    if (workspaceSlug) {
      refreshWorkspace(workspaceSlug);
    }
  }, [workspaceSlug, refreshWorkspace]);
  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-surface-50 dark:bg-surface-950">
         
        <div className="text-center">
           
          <div className="animate-spin h-8 w-8 border-4 border-primary-600 border-t-transparent rounded-full mx-auto mb-4" /> 
          <p className="text-surface-500 dark:text-surface-400">
            Loading workspace...
          </p> 
        </div> 
      </div>
    );
  }
  if (error) {
    return <Navigate to="/workspaces" replace />;
  }
  if (!workspace) {
    return <Navigate to="/workspaces" replace />;
  }
  return (
    <div className="flex min-h-screen bg-surface-50 dark:bg-surface-950">
       
      <Sidebar /> 
      <div className="min-w-0 flex-1 overflow-auto lg:max-h-screen">
         
        <header className="sticky top-0 z-30 flex items-center justify-end border-b border-surface-200/70 dark:border-surface-800/70 bg-white/80 dark:bg-surface-900/80 px-4 py-3 backdrop-blur lg:px-6">
           
          <NotificationDropdown /> 
        </header> 
        <div className="page-container">
           
          <Outlet /> 
        </div> 
      </div> 
      <CommandPalette /> <QuickCreate /> 
      <NotificationToastHandler workspaceSlug={workspaceSlug} /> 
    </div>
  );
}
