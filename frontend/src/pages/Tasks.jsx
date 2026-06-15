import { useCallback, useEffect, useMemo, useState } from "react";
import {
  CheckSquare,
  Columns3,
  Download,
  FileText,
  Plus,
  Search,
  Settings,
  Table,
  Users,
  X,
} from "lucide-react";
import { toast, Toaster } from "sonner";
import TaskTable from "../components/tasks/TaskTable";
import TaskKanban from "../components/tasks/TaskKanban";
import TaskDrawer from "../components/tasks/TaskDrawer";
import BulkActionBar from "../components/tasks/BulkActionBar";
import StatusBadge from "../components/tasks/StatusBadge";
import ConfirmationDialog from "../components/members/ConfirmationDialog";
import { getUserProjects } from "../api/projectMembersApi";
import { deleteTask, getTasksByProject, exportTasksPdf } from "../api/taskApi";
import { downloadTasksCsv } from "../utils/csvExport";
import { getProjectStatuses } from "../api/statusApi";
import { usePermission } from "../context/usePermission.js";
import { useProjectPermission } from "../context/ProjectPermissionContext.jsx";
import {
  TaskCardSkeleton,
  PageSkeleton,
} from "../components/LoadingSkeleton.jsx";
const Tasks = () => {
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [tasks, setTasks] = useState([]);
  const [statuses, setStatuses] = useState([]);
  const [viewMode, setViewMode] = useState("table");
  const [showTaskDrawer, setShowTaskDrawer] = useState(false);
  const [drawerTask, setDrawerTask] = useState(null);
  const [isCreating, setIsCreating] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedTaskIds, setSelectedTaskIds] = useState(new Set());
  const [kanbanSearch, setKanbanSearch] = useState("");
  const [kanbanPriorityFilter, setKanbanPriorityFilter] = useState("all");
  const {
    canCreateTask,
    canDeleteTask,
  } = usePermission();
  const { setProjectId, clearProjectId } = useProjectPermission();
  const loadProjects = useCallback(async () => {
    try {
      const userProjects = await getUserProjects();
      setProjects(userProjects);
      if (userProjects.length > 0) {
        setSelectedProjectId(String(userProjects[0].id));
      }
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load projects");
    }
  }, []);
  const loadProjectData = useCallback(async () => {
    setLoading(true);
    try {
      const [taskPage, projectStatuses] = await Promise.all([
        getTasksByProject(selectedProjectId, { size: 50 }),
        getProjectStatuses(selectedProjectId),
      ]);
      setTasks(taskPage.content || []);
      setStatuses(projectStatuses);
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to load project data",
      );
    } finally {
      setLoading(false);
    }
  }, [selectedProjectId]);
  useEffect(() => {
    loadProjects();
  }, [loadProjects]);
  useEffect(() => {
    if (selectedProjectId) {
      setProjectId(Number(selectedProjectId));
    } else {
      clearProjectId();
    }
    return () => clearProjectId();
  }, [selectedProjectId, setProjectId, clearProjectId]);
  useEffect(() => {
    if (selectedProjectId) {
      loadProjectData();
    }
  }, [loadProjectData, selectedProjectId]);
  useEffect(() => {
    setSelectedTaskIds(new Set());
  }, [selectedProjectId, viewMode]);
  const handleEditTask = (task) => {
    setDrawerTask(task);
    setIsCreating(false);
    setShowTaskDrawer(true);
  };
  const handleDeleteTask = (taskId) => {
    setConfirmAction(() => async () => {
      try {
        await deleteTask(taskId);
        toast.success("Task deleted");
        await loadProjectData();
      } catch (error) {
        toast.error(error.response?.data?.message || "Delete failed");
      }
      setShowConfirm(false);
    });
    setShowConfirm(true);
  };
  const handleTaskSaved = async () => {
    await loadProjectData();
    setShowTaskDrawer(false);
    setDrawerTask(null);
    setIsCreating(false);
  };
  const selectedProject = projects.find(
    (project) => String(project.id) === String(selectedProjectId),
  );
  const statusStats = statuses.map((status) => ({
    ...status,
    count: tasks.filter((task) => String(task.statusId) === String(status.id))
      .length,
  }));
  const filteredKanbanTasks = useMemo(() => {
    let filtered = tasks;
    if (kanbanSearch.trim()) {
      const q = kanbanSearch.toLowerCase();
      filtered = filtered.filter(
        (t) =>
          t.title.toLowerCase().includes(q) ||
          (t.description && t.description.toLowerCase().includes(q)),
      );
    }
    if (kanbanPriorityFilter !== "all") {
      filtered = filtered.filter((t) => t.priority === kanbanPriorityFilter);
    }
    return filtered;
  }, [tasks, kanbanSearch, kanbanPriorityFilter]);
  if (loading && !selectedProjectId) {
    return <PageSkeleton />;
  }
  return (
    <>
       
      <Toaster position="top-right" /> 
      <div className="p-4 md:p-6 space-y-6">
         
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
           
          <div>
             
            <div className="flex items-center gap-3">
               
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-r from-primary-500 to-primary-600 shadow-lg">
                 
                <CheckSquare className="h-6 w-6 text-white" /> 
              </div> 
              <div>
                 
                <h1 className="text-2xl font-bold text-surface-900 dark:text-surface-100 md:text-3xl">
                   
                  Tasks 
                </h1> 
                <p className="text-sm text-surface-500 dark:text-surface-400">
                   
                  Manage tasks for 
                  <span className="font-semibold text-surface-700 dark:text-surface-300">
                    {selectedProject?.name}
                  </span> 
                </p> 
              </div> 
            </div> 
          </div> 
          <div className="flex flex-wrap gap-2">
             
            {statusStats.map((status) => (
              <div
                key={status.id}
                className="flex items-center gap-2 rounded-xl bg-white dark:bg-surface-800 px-3 py-2 text-surface-700 dark:text-surface-300 shadow-sm ring-1 ring-surface-200 dark:ring-surface-700"
              >
                 
                <StatusBadge status={status.name} color={status.color} /> 
                <span className="font-semibold text-sm">
                  {status.count}
                </span> 
              </div>
            ))} 
            <div className="rounded-xl bg-surface-100 dark:bg-surface-800 px-3 py-2 font-semibold text-surface-700 dark:text-surface-300 text-sm ring-1 ring-surface-200 dark:ring-surface-700">
               
              {tasks.length} total 
            </div> 
          </div> 
        </div> 
        <div className="rounded-2xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-4 shadow-sm">
           
          <div className="flex flex-col items-start justify-between gap-4 lg:flex-row lg:items-center">
             
            <div className="flex min-w-[200px] sm:min-w-[280px] items-center gap-3 rounded-xl border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-900/50 px-4 py-2">
               
              <Users className="h-4 w-4 text-primary-600 dark:text-primary-400" /> 
              <select
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(e.target.value)}
                className="flex-1 bg-transparent text-sm font-semibold text-surface-900 dark:text-surface-100 focus:outline-none"
                disabled={loading}
              >
                 
                {projects.map((project) => (
                  <option key={project.id} value={project.id}>
                     
                    {project.name} 
                  </option>
                ))} 
              </select> 
            </div> 
            <div className="flex rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-1 shadow-sm">
               
              <button
                onClick={() => setViewMode("table")}
                className={`flex items-center gap-2 rounded-lg px-3 py-2 font-medium transition-all text-sm ${viewMode === "table" ? "bg-primary-600 text-white shadow-sm" : "text-surface-600 dark:text-surface-400 hover:bg-surface-50 dark:hover:bg-surface-700"}`}
              >
                 
                <Table className="h-4 w-4" /> 
                <span className="hidden sm:inline">Table</span> 
              </button> 
              <button
                onClick={() => setViewMode("kanban")}
                className={`flex items-center gap-2 rounded-lg px-3 py-2 font-medium transition-all text-sm ${viewMode === "kanban" ? "bg-primary-600 text-white shadow-sm" : "text-surface-600 dark:text-surface-400 hover:bg-surface-50 dark:hover:bg-surface-700"}`}
              >
                 
                <Columns3 className="h-4 w-4" /> 
                <span className="hidden sm:inline">Kanban</span> 
              </button> 
            </div> 
            <div className="flex gap-2">
               
              {tasks.length > 0 && (
                <>
                <button
                  onClick={() =>
                    downloadTasksCsv(
                      tasks,
                      `tasks-${selectedProject?.name || "export"}.csv`,
                    )
                  }
                  className="btn btn-secondary"
                  disabled={loading}
                  title="Download as CSV"
                >
                  <Download className="h-4 w-4" /> 
                  <span className="hidden sm:inline">Export</span> 
                </button>
                <button
                  onClick={() => exportTasksPdf(selectedProjectId)}
                  className="btn btn-secondary"
                  disabled={loading || !selectedProjectId}
                  title="Export as PDF"
                >
                  <FileText className="h-4 w-4" /> 
                  <span className="hidden sm:inline">PDF</span> 
                </button>
                </>
              )} 
              {canCreateTask() && (
                <button
                  onClick={() =>
                    window.open(
                      `/workflow-statuses?project=${selectedProjectId}`,
                      "_blank",
                    )
                  }
                  className="btn btn-secondary"
                  disabled={!selectedProjectId || loading}
                  title="Configure project workflow stages"
                >
                   
                  <Settings className="h-4 w-4" /> 
                  <span className="hidden sm:inline">Workflow</span> 
                </button>
              )} 
              {canCreateTask() && (
                <button
                  onClick={() => {
                    setDrawerTask(null);
                    setIsCreating(true);
                    setShowTaskDrawer(true);
                  }}
                  className="btn btn-primary"
                  disabled={!selectedProjectId || loading}
                >
                   
                  <Plus className="h-4 w-4" /> New Task 
                </button>
              )} 
            </div> 
          </div> 
        </div> 
        {loading ? (
          <div className="rounded-2xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-6 md:p-12 text-center shadow-sm">
             
            <div className="space-y-4">
               
              <TaskCardSkeleton /> <TaskCardSkeleton /> 
              <TaskCardSkeleton /> 
            </div> 
          </div>
        ) : (
          <div className="space-y-6">
             
            {viewMode === "table" && (
              <>
                 
                <BulkActionBar
                  selectedIds={[...selectedTaskIds]}
                  onClear={() => setSelectedTaskIds(new Set())}
                  onComplete={() => {
                    setSelectedTaskIds(new Set());
                    loadProjectData();
                  }}
                  statuses={statuses}
                  assignees={
                    projects.find(
                      (p) => String(p.id) === String(selectedProjectId),
                    )?.members || []
                  }
                /> 
                <TaskTable
                  projectId={selectedProjectId}
                  tasks={tasks}
                  statuses={statuses}
                  onEdit={handleEditTask}
                  onDelete={(taskId) => {
                    if (canDeleteTask()) handleDeleteTask(taskId);
                  }}
                  selectedIds={selectedTaskIds}
                  onSelectionChange={setSelectedTaskIds}
                /> 
              </>
            )} 
            {viewMode === "kanban" && (
              <div className="space-y-4">
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                  <div className="relative flex-1 max-w-md">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-surface-400" />
                    <input
                      type="text"
                      placeholder="Search tasks..."
                      value={kanbanSearch}
                      onChange={(e) => setKanbanSearch(e.target.value)}
                      className="input-field pl-9 pr-8"
                    />
                    {kanbanSearch && (
                      <button
                        onClick={() => setKanbanSearch("")}
                        className="absolute right-2 top-1/2 -translate-y-1/2 p-1 rounded text-surface-400 hover:text-surface-600"
                      >
                        <X className="h-3.5 w-3.5" />
                      </button>
                    )}
                  </div>
                  <select
                    value={kanbanPriorityFilter}
                    onChange={(e) => setKanbanPriorityFilter(e.target.value)}
                    className="input-field w-auto"
                  >
                    <option value="all">All Priorities</option>
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="CRITICAL">Critical</option>
                  </select>
                  <span className="text-sm text-surface-500 whitespace-nowrap">
                    {filteredKanbanTasks.length} of {tasks.length} tasks
                  </span>
                </div>
                <TaskKanban
                  tasks={filteredKanbanTasks}
                  statuses={statuses}
                  onEdit={handleEditTask}
                  onRefresh={loadProjectData}
                  onDelete={(taskId) => {
                    if (canDeleteTask()) handleDeleteTask(taskId);
                  }}
                />
              </div>
            )} 
          </div>
        )} 
      </div> 
      <TaskDrawer
        isOpen={showTaskDrawer}
        onClose={() => {
          setShowTaskDrawer(false);
          setDrawerTask(null);
          setIsCreating(false);
        }}
        task={isCreating ? null : drawerTask}
        projectId={selectedProjectId}
        projects={projects}
        onTaskSaved={handleTaskSaved}
      /> 
      <ConfirmationDialog
        isOpen={showConfirm}
        onClose={() => setShowConfirm(false)}
        onConfirm={confirmAction}
        title="Delete Task?"
        message="This action cannot be undone."
      /> 
    </>
  );
};
export default Tasks;
