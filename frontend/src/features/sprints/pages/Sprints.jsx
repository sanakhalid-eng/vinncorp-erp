import { useCallback, useEffect, useState, useMemo } from "react";
import {
  Plus,
  Play,
  CheckCircle,
  Trash2,
  Archive,
  Calendar,
  TrendingUp,
  AlertTriangle,
  ChevronDown,
  ChevronRight,
  ArrowRight,
  Trophy,
  BarChart3,
  X,
} from "lucide-react";
import { toast, Toaster } from "sonner";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";
import {
  createSprint,
  startSprint,
  completeSprint,
  getProjectSprints,
  getActiveSprint,
  getBacklogTasks,
  deleteSprint,
  getSprintTasks,
  assignTaskToSprint,
  removeTaskFromSprint,
  getSprintBurndown,
} from "../api/sprintApi";
import { getTasksByProject } from "../../tasks/api/taskApi";
import { getUserProjects } from "../../projects/api/projectMembersApi";
import { usePermission } from "../../../context/usePermission.js";
import { useProjectPermission } from "../../../context/ProjectPermissionContext.jsx";
import { useIsDesktop } from "../../../hooks/useBreakpoint";
import {
  DndContext,
  DragOverlay,
  PointerSensor,
  useSensor,
  useSensors,
  closestCenter,
} from "@dnd-kit/core";
import { useDraggable, useDroppable } from "@dnd-kit/core";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  ReferenceLine,
  Legend,
} from "recharts";
import SprintVelocityChart from "../components/sprints/SprintVelocityChart";
const DraggableTask = ({ task, onRemove }) => {
  const { attributes, listeners, setNodeRef, transform, isDragging } =
    useDraggable({ id: `task-${task.id}`, data: { type: "task", task } });
  const style = transform
    ? {
        transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
        opacity: isDragging ? 0.5 : 1,
      }
    : undefined;
  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      className="p-3 bg-white rounded-lg border border-surface-200 dark:border-surface-700 mb-2 cursor-grab hover:border-primary-400 dark:hover:border-primary-500 transition-colors"
    >
       
      <div className="flex justify-between items-start">
         
        <div className="flex-1 min-w-0">
           
          <p className="text-sm font-medium text-surface-900 dark:text-surface-100 truncate">
            {task.title}
          </p> 
          <div className="flex gap-2 mt-1 flex-wrap">
             
            {task.priority && (
              <span className="text-xs px-2 py-0.5 bg-surface-100 dark:bg-surface-800 text-surface-600 dark:text-surface-300 rounded">
                {task.priority}
              </span>
            )} 
            <span className="text-xs text-surface-500 dark:text-surface-400">
              {task.status}
            </span> 
          </div> 
        </div> 
        {onRemove && (
          <button
            onClick={(e) => {
              e.stopPropagation();
              onRemove(task.id);
            }}
            className="text-danger-500 hover:text-danger-700 ml-2 shrink-0 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500 rounded"
            aria-label="Remove task from sprint"
          >
            
            &times; 
          </button>
        )} 
      </div> 
    </div>
  );
};
const DroppableSprint = ({ sprint, children, isOver }) => {
  const { setNodeRef, isOver: dropOver } = useDroppable({
    id: `sprint-${sprint.id}`,
    data: { type: "sprint", sprint },
  });
  return (
    <div
      ref={setNodeRef}
      className={`transition-colors ${dropOver ? "bg-primary-50 dark:bg-primary-900/20 border-primary-400 dark:border-primary-600" : ""}`}
    >
       
      {children} 
    </div>
  );
};
const BurndownChart = ({ sprint }) => {
  const [burndownData, setBurndownData] = useState([]);
  const [loading, setLoading] = useState(false);
  useEffect(() => {
    if (!sprint || sprint.status === "PLANNED") {
      setBurndownData([]);
      return;
    }
    const fetchBurndown = async () => {
      setLoading(true);
      try {
        const data = await getSprintBurndown(sprint.id);
        setBurndownData(data || []);
      } catch (error) {
        console.error("Failed to load burndown data:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchBurndown();
  }, [sprint]);
  const chartData = useMemo(() => {
    if (!burndownData || burndownData.length === 0) return [];
    return burndownData.map((point) => ({
      date: point.date,
      ideal: point.idealRemaining,
      actual: point.remainingTasks,
      completed: point.completedTasks,
      blocked: point.blockedTasks,
      total: point.totalTasks,
    }));
  }, [burndownData]);
  if (!sprint || sprint.status === "PLANNED") {
    return (
      <p className="text-sm text-surface-500 dark:text-surface-400 text-center py-4">
        No burndown data available
      </p>
    );
  }
  if (loading) {
    return (
      <p className="text-sm text-surface-500 dark:text-surface-400 text-center py-4">
        Loading chart...
      </p>
    );
  }
  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const data = payload[0]?.payload;
      return (
        <div className="bg-white dark:bg-surface-800 p-3 border border-surface-200 dark:border-surface-700 rounded shadow-md">
           
          <p className="text-sm font-medium text-surface-900 dark:text-surface-100">
            {label}
          </p> 
          <p className="text-xs text-primary-600 dark:text-primary-400">
            Actual Remaining: {data?.actual ?? 0}
          </p> 
          <p className="text-xs text-surface-500 dark:text-surface-400">
            Completed: {data?.completed ?? 0}
          </p> 
          <p className="text-xs text-danger-500 dark:text-danger-400">
            Blocked: {data?.blocked ?? 0}
          </p> 
          <p className="text-xs text-surface-400 dark:text-surface-500">
            Ideal: {data?.ideal?.toFixed(1) ?? 0}
          </p> 
          {data?.actual > data?.ideal && (
            <p className="text-xs text-warning-500 dark:text-warning-400 mt-1">
              Behind schedule
            </p>
          )} 
          {data?.actual < data?.ideal && (
            <p className="text-xs text-success-500 dark:text-success-400 mt-1">
              Ahead of schedule
            </p>
          )} 
        </div>
      );
    }
    return null;
  };
  return (
    <div className="mt-4 p-3 md:p-4 bg-surface-50 dark:bg-surface-800/50 rounded-lg">
       
      <h4 className="text-sm font-medium text-surface-700 dark:text-surface-300 mb-3 flex items-center gap-2">
         
        <BarChart3 size={16} /> Sprint Burndown 
      </h4> 
      <ResponsiveContainer width="100%" height={200}>
         
        <LineChart data={chartData}>
           
          <CartesianGrid
            strokeDasharray="3 3"
            stroke="currentColor"
            className="text-surface-200 dark:text-surface-700"
          /> 
          <XAxis
            dataKey="date"
            tick={{ fontSize: 10 }}
            stroke="currentColor"
            className="text-surface-500 dark:text-surface-400"
          /> 
          <YAxis
            tick={{ fontSize: 10 }}
            allowDecimals={false}
            stroke="currentColor"
            className="text-surface-500 dark:text-surface-400"
          /> 
          <Tooltip content={<CustomTooltip />} /> <Legend /> 
          <Line
            type="linear"
            dataKey="ideal"
            stroke="#94a3b8"
            strokeDasharray="5 5"
            name="Ideal Line"
            dot={false}
          /> 
          <Line
            type="monotone"
            dataKey="actual"
            stroke="#3b82f6"
            strokeWidth={2}
            name="Actual Remaining"
            dot={{ r: 3 }}
          /> 
          <ReferenceLine y={0} stroke="#000" strokeWidth={1} /> 
        </LineChart> 
      </ResponsiveContainer> 
    </div>
  );
};
const Sprints = () => {
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [sprints, setSprints] = useState([]);
  const [activeSprint, setActiveSprint] = useState(null);
  const [backlogTasks, setBacklogTasks] = useState([]);
  const [allTasks, setAllTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [carryForward, setCarryForward] = useState(false);
  const [expandedSprint, setExpandedSprint] = useState(null);
  const [sprintTasks, setSprintTasks] = useState({});
  const [activeId, setActiveId] = useState(null);
  const [showBurndown, setShowBurndown] = useState({});
  const [sprintForm, setSprintForm] = useState({
    name: "",
    goal: "",
    startDate: "",
    endDate: "",
  });
  const { canCreateTask } = usePermission();
  const { setProjectId, clearProjectId } = useProjectPermission();
  const isDesktop = useIsDesktop();
  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
  );
  const loadProjects = useCallback(async () => {
    try {
      const userProjects = await getUserProjects();
      setProjects(userProjects);
      if (userProjects.length > 0) {
        setSelectedProjectId((prev) => prev || String(userProjects[0].id));
      }
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load projects");
    }
  }, []);
  const loadSprints = useCallback(async (projectId) => {
    if (!projectId) return;
    setLoading(true);
    try {
      const [sprintsData, activeSprintData, backlogData, tasksData] =
        await Promise.all([
          getProjectSprints(projectId),
          getActiveSprint(projectId),
          getBacklogTasks(projectId),
          getTasksByProject(projectId, { size: 100 }).then(
            (r) => r.content || [],
          ),
        ]);
      setSprints(sprintsData || []);
      setActiveSprint(activeSprintData);
      setBacklogTasks(backlogData || []);
      setAllTasks(tasksData || []);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to load sprints");
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    loadProjects();
  }, [loadProjects]);
  useEffect(() => {
    if (selectedProjectId) {
      setProjectId(Number(selectedProjectId));
      loadSprints(selectedProjectId);
    } else {
      clearProjectId();
    }
    return () => clearProjectId();
  }, [selectedProjectId, setProjectId, clearProjectId]);
  const handleDragStart = (event) => {
    setActiveId(event.active?.id);
  };
  const handleDragEnd = async (event) => {
    const { active, over } = event;
    setActiveId(null);
    if (!over || !active) return;
    const taskId = parseInt(String(active.id).replace("task-", ""));
    const sprintId = String(over.id).includes("sprint-")
      ? parseInt(String(over.id).replace("sprint-", ""))
      : null;
    if (!sprintId || isNaN(taskId)) return;
    try {
      await assignTaskToSprint(taskId, sprintId);
      toast.success("Task assigned to sprint");
      loadSprints(selectedProjectId);
    } catch (error) {
      const msg = error.response?.data?.message || "Failed to assign task";
      if (msg.includes("Warning")) toast.warning(msg);
      else toast.error(msg);
    }
  };
  const handleRemoveTask = async (taskId) => {
    try {
      await removeTaskFromSprint(taskId);
      toast.success("Task removed from sprint");
      loadSprints(selectedProjectId);
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to remove task");
    }
  };
  const handleCreateSprint = async (e) => {
    e.preventDefault();
    try {
      await createSprint({
        ...sprintForm,
        projectId: Number(selectedProjectId),
      });
      toast.success("Sprint created successfully");
      setShowCreateModal(false);
      setSprintForm({ name: "", goal: "", startDate: "", endDate: "" });
      loadSprints(selectedProjectId);
    } catch (error) {
      const msg = error.response?.data?.message || "Failed to create sprint";
      if (msg.includes("Warning")) toast.warning(msg);
      else toast.error(msg);
    }
  };
  const handleStartSprint = async (sprintId) => {
    try {
      await startSprint(sprintId);
      toast.success("Sprint started successfully");
      loadSprints(selectedProjectId);
    } catch (error) {
      const msg = error.response?.data?.message || "Failed to start sprint";
      if (msg.includes("Warning")) toast.warning(msg);
      else toast.error(msg);
    }
  };
  const handleCompleteSprint = async (sprintId, carryForward = false) => {
    try {
      await completeSprint(sprintId, carryForward);
      toast.success("Sprint completed successfully");
      setShowConfirm(false);
      setConfirmAction(null);
      loadSprints(selectedProjectId);
    } catch (error) {
      const msg = error.response?.data?.message || "Failed to complete sprint";
      if (msg.includes("Warning")) toast.warning(msg);
      else toast.error(msg);
    }
  };
  const handleDeleteSprint = async (sprintId) => {
    try {
      await deleteSprint(sprintId);
      toast.success("Sprint deleted");
      setShowConfirm(false);
      setConfirmAction(null);
      loadSprints(selectedProjectId);
    } catch (error) {
      const msg = error.response?.data?.message || "Failed to delete sprint";
      toast.error(msg);
    }
  };
  const loadSprintTasks = async (sprintId) => {
    if (sprintTasks[sprintId]) {
      setExpandedSprint(expandedSprint === sprintId ? null : sprintId);
      return;
    }
    try {
      const tasks = await getSprintTasks(sprintId);
      setSprintTasks((prev) => ({ ...prev, [sprintId]: tasks }));
      setExpandedSprint(sprintId);
    } catch (error) {
      toast.error("Failed to load sprint tasks");
    }
  };
  const toggleBurndown = (sprintId) => {
    setShowBurndown((prev) => ({ ...prev, [sprintId]: !prev[sprintId] }));
  };
  const getStatusBadge = (status) => {
    const styles = {
      PLANNED:
        "bg-primary-100 text-primary-700 dark:bg-primary-900/30 dark:text-primary-300",
      ACTIVE:
        "bg-success-100 text-success-700 dark:bg-success-900/30 dark:text-success-300",
      COMPLETED:
        "bg-surface-100 text-surface-700 dark:bg-surface-800 dark:text-surface-300",
    };
    return (
      <span
        className={`px-2 py-1 rounded-full text-xs font-medium ${styles[status] || styles["PLANNED"]}`}
      >
         
        {status} 
      </span>
    );
  };
  const getProgressColor = (percentage) => {
    if (percentage >= 75) return "bg-success-500";
    if (percentage >= 50) return "bg-primary-500";
    if (percentage >= 25) return "bg-warning-500";
    return "bg-surface-400";
  };
  const activeTask = activeId
    ? allTasks.find((t) => `task-${t.id}` === activeId)
    : null;
  return (
    <>
       
      <Toaster position="top-right" /> 
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
      >
         
        <div className="page-container max-w-8xl mx-auto">
           
          <div className="page-header">
             
            <h1 className="page-title flex items-center gap-2">
               
              <Trophy size={28} /> Sprints 
            </h1> 
            <div className="flex flex-col sm:flex-row gap-3">
               
              <select
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(e.target.value)}
                className="input-field sm:w-48"
              >
                 
                <option value="">Select Project</option> 
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))} 
              </select> 
              {selectedProjectId && (
                <button
                  onClick={() => setShowCreateModal(true)}
                  className="btn-primary whitespace-nowrap"
                >
                   
                  <Plus size={18} /> Create Sprint 
                </button>
              )} 
            </div> 
          </div> 
          {loading ? (
            <div className="flex flex-col items-center justify-center py-12">
               
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div> 
              <p className="mt-2 text-surface-600 dark:text-surface-400">
                Loading sprints...
              </p> 
            </div>
          ) : (
            <div className="space-y-6">
               
              {activeSprint && (
                <div className="bg-success-50 dark:bg-success-900/10 border border-success-200 dark:border-success-800/50 rounded-xl p-4 md:p-6">
                   
                  <div className="flex flex-col sm:flex-row sm:justify-between sm:items-start gap-4 mb-4">
                     
                    <div className="flex-1 min-w-0">
                       
                      <div className="flex flex-wrap items-center gap-3 mb-2">
                         
                        <h2 className="text-lg md:text-xl font-semibold text-surface-900 dark:text-surface-100">
                          {activeSprint.name}
                        </h2> 
                        {getStatusBadge(activeSprint.status)} 
                      </div> 
                      {activeSprint.goal && (
                        <p className="text-surface-600 dark:text-surface-400 mb-2 text-sm">
                          {activeSprint.goal}
                        </p>
                      )} 
                      <div className="flex flex-wrap gap-3 md:gap-4 text-xs md:text-sm text-surface-600 dark:text-surface-400">
                         
                        {activeSprint.startDate && (
                          <span className="flex items-center gap-1">
                             
                            <Calendar size={14} /> Started: 
                            {new Date(
                              activeSprint.startDate,
                            ).toLocaleDateString()} 
                          </span>
                        )} 
                        {activeSprint.endDate && (
                          <span className="flex items-center gap-1">
                             
                            <Calendar size={14} /> Ends: 
                            {new Date(
                              activeSprint.endDate,
                            ).toLocaleDateString()} 
                          </span>
                        )} 
                      </div> 
                    </div> 
                    <div className="flex flex-wrap gap-2 shrink-0">
                       
                      <button
                        onClick={() => toggleBurndown(activeSprint.id)}
                        className="btn-secondary btn-sm"
                      >
                         
                        <BarChart3 size={16} /> 
                        {showBurndown[activeSprint.id] ? "Hide" : "Show"} 
                        Chart 
                      </button> 
                      <button
                        onClick={() => {
                          setConfirmAction({
                            type: "complete",
                            sprintId: activeSprint.id,
                            sprintName: activeSprint.name,
                          });
                          setShowConfirm(true);
                        }}
                        className="btn-primary btn-sm bg-success-600 hover:bg-success-700"
                      >
                         
                        <CheckCircle size={18} /> Complete 
                      </button> 
                    </div> 
                  </div> 
                  <div className="mb-2">
                     
                    <div className="flex justify-between text-xs md:text-sm text-surface-600 dark:text-surface-400 mb-1">
                       
                      <span>Progress</span> 
                      <span>
                        {activeSprint.completedTasks}/{activeSprint.totalTasks} 
                        tasks ({activeSprint.progressPercentage}%)
                      </span> 
                    </div> 
                    <div className="w-full bg-surface-200 dark:bg-surface-700 rounded-full h-2.5">
                       
                      <div
                        className={`h-2.5 rounded-full ${getProgressColor(activeSprint.progressPercentage)}`}
                        style={{ width: `${activeSprint.progressPercentage}%` }}
                      ></div> 
                    </div> 
                  </div> 
                  {showBurndown[activeSprint.id] && (
                    <BurndownChart sprint={activeSprint} />
                  )} 
                </div>
              )} 
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 md:gap-6">
                 
                <div className="lg:col-span-1">
                   
                  <div className="bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 rounded-xl p-4">
                     
                    <h3 className="text-base md:text-lg font-semibold mb-3 flex items-center gap-2 text-surface-900 dark:text-surface-100">
                       
                      <Archive size={20} /> Backlog ({backlogTasks.length}) 
                    </h3> 
                    {isDesktop && (
                      <p className="text-xs text-surface-500 dark:text-surface-400 mb-3">
                        Drag tasks to a sprint below
                      </p>
                    )} 
                    <div className="space-y-2 max-h-80 lg:max-h-96 overflow-y-auto scrollbar-thin">
                       
                      {backlogTasks.length === 0 ? (
                        <p className="text-sm text-surface-500 dark:text-surface-400 text-center py-4">
                          No tasks in backlog
                        </p>
                      ) : (
                        backlogTasks.map((task) => (
                          <DraggableTask key={task.id} task={task} />
                        ))
                      )} 
                    </div> 
                  </div> 
                </div> 
                <div className="lg:col-span-2">
                   
                  <div className="bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 rounded-xl p-4 md:p-6">
                     
                    <h3 className="text-base md:text-lg font-semibold mb-4 text-surface-900 dark:text-surface-100">
                      All Sprints
                    </h3> 
                    {sprints.length === 0 ? (
                      <p className="text-surface-500 dark:text-surface-400 text-center py-8">
                        No sprints found. Create your first sprint!
                      </p>
                    ) : (
                      <div className="space-y-3">
                         
                        {sprints.map((sprint) => (
                          <DroppableSprint key={sprint.id} sprint={sprint}>
                             
                            <div className="border border-surface-200 dark:border-surface-700 rounded-xl overflow-hidden">
                               
                              <div
                                className="p-3 md:p-4 cursor-pointer hover:bg-surface-50 dark:hover:bg-surface-800/50 transition-colors"
                                onClick={() => loadSprintTasks(sprint.id)}
                              >
                                 
                                <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3">
                                   
                                  <div className="flex items-center gap-2 min-w-0 flex-1">
                                     
                                    {expandedSprint === sprint.id ? (
                                      <ChevronDown
                                        size={18}
                                        className="shrink-0"
                                      />
                                    ) : (
                                      <ChevronRight
                                        size={18}
                                        className="shrink-0"
                                      />
                                    )} 
                                    <div className="min-w-0">
                                       
                                      <div className="flex flex-wrap items-center gap-2">
                                         
                                        <span className="font-medium text-surface-900 dark:text-surface-100 truncate">
                                          {sprint.name}
                                        </span> 
                                        {getStatusBadge(sprint.status)} 
                                      </div> 
                                      {sprint.goal && (
                                        <p className="text-sm text-surface-600 dark:text-surface-400 mt-1 truncate">
                                          {sprint.goal}
                                        </p>
                                      )} 
                                    </div> 
                                  </div> 
                                  <div className="flex items-center gap-1 md:gap-2 shrink-0 sm:pl-4">
                                     
                                    {sprint.status === "PLANNED" && (
                                      <button
                                        onClick={(e) => {
                                          e.stopPropagation();
                                          handleStartSprint(sprint.id);
                                        }}
                                        className="p-2 text-success-600 dark:text-success-400 hover:bg-success-50 dark:hover:bg-success-900/20 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
                                        aria-label="Start sprint"
                                        title="Start"
                                      >
                                        
                                        <Play size={18} /> 
                                      </button>
                                    )} 
                                    {sprint.status === "ACTIVE" && (
                                      <>
                                         
                                        <button
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            toggleBurndown(sprint.id);
                                          }}
                                          className="p-2 text-primary-600 dark:text-primary-400 hover:bg-primary-50 dark:hover:bg-primary-900/20 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
                                          aria-label="Toggle burndown chart"
                                          title="Chart"
                                        >
                                          
                                          <BarChart3 size={18} /> 
                                        </button> 
                                        <button
                                          onClick={(e) => {
                                            e.stopPropagation();
                                            setConfirmAction({
                                              type: "complete",
                                              sprintId: sprint.id,
                                              sprintName: sprint.name,
                                            });
                                            setShowConfirm(true);
                                          }}
                                          className="p-2 text-success-600 dark:text-success-400 hover:bg-success-50 dark:hover:bg-success-900/20 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
                                          aria-label="Complete sprint"
                                          title="Complete"
                                        >
                                          
                                          <CheckCircle size={18} /> 
                                        </button> 
                                      </>
                                    )} 
                                    <button
                                      onClick={(e) => {
                                        e.stopPropagation();
                                        setConfirmAction({
                                          type: "delete",
                                          sprintId: sprint.id,
                                          sprintName: sprint.name,
                                        });
                                        setShowConfirm(true);
                                      }}
                                      className="p-2 text-danger-600 dark:text-danger-400 hover:bg-danger-50 dark:hover:bg-danger-900/20 rounded-lg transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
                                      aria-label="Delete sprint"
                                      title="Delete"
                                    >
                                      
                                      <Trash2 size={18} /> 
                                    </button> 
                                  </div> 
                                </div> 
                                {sprint.status === "COMPLETED" &&
                                  sprint.summaryTotalTasks != null && (
                                    <div className="mt-2 flex flex-wrap gap-3 md:gap-4 text-xs md:text-sm text-surface-600 dark:text-surface-400">
                                       
                                      <span className="flex items-center gap-1">
                                         
                                        <TrendingUp size={14} /> 
                                        {sprint.summaryCompletedTasks}/
                                        {sprint.summaryTotalTasks} 
                                        completed 
                                      </span> 
                                      {sprint.summaryCarriedForward > 0 && (
                                        <span>
                                          {sprint.summaryCarriedForward} carried
                                          forward
                                        </span>
                                      )} 
                                    </div>
                                  )} 
                                {sprint.status !== "COMPLETED" && (
                                  <div className="mt-2">
                                     
                                    <div className="flex justify-between text-xs text-surface-600 dark:text-surface-400 mb-1">
                                       
                                      <span>
                                        {sprint.completedTasks}/
                                        {sprint.totalTasks} tasks
                                      </span> 
                                      <span>
                                        {sprint.progressPercentage}%
                                      </span> 
                                    </div> 
                                    <div className="w-full bg-surface-200 dark:bg-surface-700 rounded-full h-1.5">
                                       
                                      <div
                                        className={`h-1.5 rounded-full ${getProgressColor(sprint.progressPercentage)}`}
                                        style={{
                                          width: `${sprint.progressPercentage}%`,
                                        }}
                                      ></div> 
                                    </div> 
                                  </div>
                                )} 
                              </div> 
                              {expandedSprint === sprint.id && (
                                <div className="border-t border-surface-200 dark:border-surface-700 p-3 md:p-4 bg-surface-50 dark:bg-surface-800/50">
                                   
                                  <h4 className="text-sm font-medium text-surface-700 dark:text-surface-300 mb-2">
                                    Tasks in sprint
                                  </h4> 
                                  {sprintTasks[sprint.id]?.length === 0 ? (
                                    <p className="text-sm text-surface-500 dark:text-surface-400">
                                      No tasks assigned.
                                    </p>
                                  ) : (
                                    <div className="space-y-2">
                                       
                                      {sprintTasks[sprint.id]?.map((task) => (
                                        <div
                                          key={task.id}
                                          className="p-2 md:p-3 bg-white dark:bg-surface-900 rounded-lg border border-surface-200 dark:border-surface-700 flex flex-col sm:flex-row sm:justify-between sm:items-center gap-2"
                                        >
                                           
                                          <div className="min-w-0">
                                             
                                            <span className="text-sm font-medium text-surface-900 dark:text-surface-100 truncate block">
                                              {task.title}
                                            </span> 
                                            <span className="text-xs text-surface-500 dark:text-surface-400">
                                              {task.status}
                                            </span> 
                                          </div> 
                                          <button
                                            onClick={() =>
                                              handleRemoveTask(task.id)
                                            }
                                            className="text-danger-500 dark:text-danger-400 hover:text-danger-700 dark:hover:text-danger-300 text-sm shrink-0"
                                          >
                                            Remove
                                          </button> 
                                        </div>
                                      ))} 
                                    </div>
                                  )} 
                                  {showBurndown[sprint.id] && (
                                    <BurndownChart sprint={sprint} />
                                  )} 
                                </div>
                              )} 
                            </div> 
                          </DroppableSprint>
                        ))} 
                      </div>
                    )} 
                  </div> 
                </div>
              </div>
              {selectedProjectId && (
                <div className="bg-white dark:bg-surface-900 border border-surface-200 dark:border-surface-800 rounded-xl p-4 md:p-6">
                  <h3 className="text-base md:text-lg font-semibold mb-4 text-surface-900 dark:text-surface-100 flex items-center gap-2">
                    <TrendingUp size={20} /> Velocity History
                  </h3>
                  <SprintVelocityChart projectId={selectedProjectId} />
                </div>
              )}
            </div>
          )} 
        </div> 
        <DragOverlay>
           
          {activeId && activeTask ? (
            <div className="p-3 bg-white dark:bg-surface-800 rounded-lg border-2 border-primary-400 shadow-lg opacity-90">
               
              <p className="text-sm font-medium text-surface-900 dark:text-surface-100">
                {activeTask.title}
              </p> 
            </div>
          ) : null} 
        </DragOverlay> 
      </DndContext> 
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
           
          <div className="bg-white dark:bg-surface-900 rounded-2xl p-4 md:p-6 w-full max-w-md shadow-soft-lg border border-surface-200/50 dark:border-surface-800/50">
             
            <h2 className="text-lg md:text-xl font-bold mb-4 text-surface-900 dark:text-surface-100">
              Create New Sprint
            </h2> 
            <form onSubmit={handleCreateSprint}>
               
              <div className="mb-4">
                 
                <label className="block text-sm font-medium mb-1 text-surface-700 dark:text-surface-300">
                  Sprint Name
                </label> 
                <input
                  type="text"
                  required
                  value={sprintForm.name}
                  onChange={(e) =>
                    setSprintForm({ ...sprintForm, name: e.target.value })
                  }
                  className="input-field"
                  placeholder="Sprint 1"
                /> 
              </div> 
              <div className="mb-4">
                 
                <label className="block text-sm font-medium mb-1 text-surface-700 dark:text-surface-300">
                  Goal (Optional)
                </label> 
                <textarea
                  value={sprintForm.goal}
                  onChange={(e) =>
                    setSprintForm({ ...sprintForm, goal: e.target.value })
                  }
                  className="input-field"
                  rows="2"
                  placeholder="What do you want to achieve?"
                /> 
              </div> 
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 md:gap-4 mb-4">
                 
                <div>
                   
                  <label className="block text-sm font-medium mb-1 text-surface-700 dark:text-surface-300">
                    Start Date
                  </label> 
                  <input
                    type="date"
                    value={sprintForm.startDate}
                    onChange={(e) =>
                      setSprintForm({
                        ...sprintForm,
                        startDate: e.target.value,
                      })
                    }
                    className="input-field"
                  /> 
                </div> 
                <div>
                   
                  <label className="block text-sm font-medium mb-1 text-surface-700 dark:text-surface-300">
                    End Date
                  </label> 
                  <input
                    type="date"
                    value={sprintForm.endDate}
                    onChange={(e) =>
                      setSprintForm({ ...sprintForm, endDate: e.target.value })
                    }
                    className="input-field"
                  /> 
                </div> 
              </div> 
              <div className="flex justify-end gap-3">
                 
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="btn-secondary"
                >
                   
                  Cancel 
                </button> 
                <button type="submit" className="btn-primary">
                   
                  Create Sprint 
                </button> 
              </div> 
            </form> 
          </div> 
        </div>
      )} 
      {confirmAction?.type === "delete" ? (
        <ConfirmationDialog
          isOpen={showConfirm}
          onClose={() => {
            setShowConfirm(false);
            setConfirmAction(null);
          }}
          onConfirm={() => handleDeleteSprint(confirmAction?.sprintId)}
          title="Delete Sprint"
          message={`Delete "${confirmAction?.sprintName}"? Tasks will move to backlog.`}
        />
      ) : (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
           
          <div
            className="fixed inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => {
              setShowConfirm(false);
              setConfirmAction(null);
            }}
          /> 
          <div className="relative w-full max-w-md rounded-2xl bg-white dark:bg-surface-900 p-6 md:p-8 shadow-soft-lg border border-surface-200/50 dark:border-surface-800/50">
             
            <button
              onClick={() => {
                setShowConfirm(false);
                setConfirmAction(null);
              }}
              className="absolute top-4 right-4 p-2 text-surface-400 hover:text-surface-600 dark:hover:text-surface-200 hover:bg-surface-100 dark:hover:bg-surface-800 rounded-xl transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
              aria-label="Close dialog"
            >
              
              <X className="w-5 h-5" /> 
            </button> 
            <div className="flex items-start gap-4 mb-6">
               
              <div className="w-12 h-12 md:w-14 md:h-14 bg-success-100 dark:bg-success-900/30 rounded-2xl flex items-center justify-center flex-shrink-0">
                 
                <CheckCircle className="w-6 h-6 md:w-7 md:h-7 text-success-600 dark:text-success-400" /> 
              </div> 
              <div>
                 
                <h3 className="text-lg md:text-xl font-bold text-surface-900 dark:text-surface-100">
                  Complete Sprint
                </h3> 
                <p className="text-surface-600 dark:text-surface-400 mt-1 text-sm">
                  Complete "{confirmAction?.sprintName}"?
                </p> 
              </div> 
            </div> 
            <label className="flex items-center gap-3 mb-6 p-4 bg-surface-50 dark:bg-surface-800/50 rounded-2xl cursor-pointer">
               
              <input
                type="checkbox"
                checked={carryForward}
                onChange={(e) => setCarryForward(e.target.checked)}
                className="w-5 h-5 rounded border-surface-300 dark:border-surface-600 text-primary-600 focus:ring-primary-500"
              /> 
              <div>
                 
                <p className="font-medium text-surface-900 dark:text-surface-100 text-sm">
                  Carry forward incomplete tasks
                </p> 
                <p className="text-xs text-surface-500 dark:text-surface-400">
                  Move unfinished tasks to the next sprint
                </p> 
              </div> 
            </label> 
            <div className="flex gap-3">
               
              <button
                onClick={() => {
                  setShowConfirm(false);
                  setConfirmAction(null);
                }}
                className="flex-1 px-4 py-3 border border-surface-200 dark:border-surface-700 text-surface-700 dark:text-surface-300 rounded-xl hover:bg-surface-50 dark:hover:bg-surface-800 transition-colors font-medium text-sm"
              >
                 
                Cancel 
              </button> 
              <button
                onClick={() =>
                  handleCompleteSprint(confirmAction?.sprintId, carryForward)
                }
                className="flex-1 px-4 py-3 bg-gradient-to-r from-success-500 to-emerald-600 text-white rounded-xl hover:from-success-600 hover:to-emerald-700 font-semibold shadow-lg hover:shadow-xl transition-all flex items-center justify-center gap-2 text-sm"
              >
                 
                <CheckCircle className="w-5 h-5" /> Complete Sprint 
              </button> 
            </div> 
          </div> 
        </div>
      )} 
    </>
  );
};
export default Sprints;
