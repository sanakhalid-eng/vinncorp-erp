import { useState, useEffect } from "react";
import { useAuth } from "../context/useAuth.js";
import StatusKanbanPreview from "../components/statuses/StatusKanbanPreview";
import StatusFormModal from "../components/statuses/StatusFormModal";
import TransitionFormModal from "../components/statuses/TransitionFormModal";
import TransitionTable from "../components/statuses/TransitionTable";
import WorkflowGraph from "../components/statuses/WorkflowGraph";
import ConfirmationDialog from "../components/members/ConfirmationDialog";
import {
  Settings,
  Plus,
  Columns3,
  Workflow,
  ArrowRight,
  Palette,
} from "lucide-react";
import { getUserProjects } from "../api/projectMembersApi";
import {
  getProjectStatuses,
  getProjectTransitions,
  deleteStatus,
  deleteTransition,
} from "../api/statusApi";
import { toast, Toaster } from "sonner";
import { usePermission } from "../context/usePermission.js";
import { useProjectPermission } from "../context/ProjectPermissionContext.jsx";

export default function WorkflowStatuses() {
  useAuth();
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState("");
  const [statuses, setStatuses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewMode, setViewMode] = useState("statuses");

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showTransitionModal, setShowTransitionModal] = useState(false);
  const [editingStatus, setEditingStatus] = useState(null);
  const [showConfirm, setShowConfirm] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [transitions, setTransitions] = useState([]);
  const [transitionsLoading, setTransitionsLoading] = useState(false);
  const { canEditProject } = usePermission();
  const { setProjectId, clearProjectId } = useProjectPermission();

  useEffect(() => {
    loadProjects();
  }, []);

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
      loadStatuses();
      loadTransitions();
    }
  }, [selectedProjectId]);

  const loadProjects = async () => {
    try {
      const userProjects = await getUserProjects();
      setProjects(userProjects);
      if (userProjects.length) {
        setSelectedProjectId(String(userProjects[0].id));
      }
    } catch {
      toast.error("Failed to load projects");
    }
  };

  const loadStatuses = async () => {
    setLoading(true);
    try {
      const projectStatuses = await getProjectStatuses(selectedProjectId);
      setStatuses(projectStatuses);
    } catch {
      toast.error("Failed to load workflow statuses");
      setStatuses([]);
    } finally {
      setLoading(false);
    }
  };

  const loadTransitions = async () => {
    setTransitionsLoading(true);
    try {
      const projectTransitions = await getProjectTransitions(selectedProjectId);
      setTransitions(projectTransitions);
    } catch {
      toast.error("Failed to load transitions");
      setTransitions([]);
    } finally {
      setTransitionsLoading(false);
    }
  };

  const handleCreateTransition = () => {
    setShowTransitionModal(true);
  };

  const handleTransitionSaved = () => {
    loadTransitions();
    loadStatuses();
    setShowTransitionModal(false);
  };

  const handleTransitionDeleted = (transitionId) => {
    setConfirmAction(() => async () => {
      try {
        await deleteTransition(selectedProjectId, transitionId);
        setTransitions(transitions.filter((t) => t.id !== transitionId));
        toast.success("Transition deleted");
      } catch {
        toast.error("Failed to delete transition");
      }
      setShowConfirm(false);
    });
    setShowConfirm(true);
  };

  const handleCreateStatus = () => {
    setEditingStatus(null);
    setShowCreateModal(true);
  };

  const handleDeleteStatus = (statusId) => {
    setConfirmAction(() => async () => {
      try {
        await deleteStatus(selectedProjectId, statusId);
        setStatuses(statuses.filter((s) => s.id !== statusId));
        toast.success("Status deleted");
      } catch {
        toast.error("Failed to delete status");
      }
      setShowConfirm(false);
    });
    setShowConfirm(true);
  };

  const handleStatusSaved = () => {
    loadStatuses();
    setEditingStatus(null);
    setShowCreateModal(false);
  };

  const handleStatusesReordered = (reorderedStatuses) => {
    setStatuses(reorderedStatuses);
  };

  const selectedProject = projects.find(
    (p) => String(p.id) === String(selectedProjectId),
  );
  const statusCount = statuses.length;

  return (
    <>
      <div className="p-6 space-y-8">
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
          <div>
            <div className="flex items-center gap-4 mb-4">
              <div className="h-14 w-14 rounded-2xl bg-gradient-to-br from-primary-500 to-primary-700 shadow-lg flex items-center justify-center">
                <Settings className="h-7 w-7 text-white" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-surface-900 dark:text-surface-100 md:text-3xl">
                  Workflow Statuses
                </h1>
                <p className="text-sm text-surface-500 dark:text-surface-400">
                  Configure stages for{" "}
                  <span className="font-semibold text-surface-700 dark:text-surface-300">
                    {selectedProject?.name}
                  </span>
                </p>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap gap-3">
            <div className="flex items-center gap-3 px-5 py-3 rounded-xl bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-300 shadow-sm ring-1 ring-primary-200 dark:ring-primary-800/50">
              <Palette className="h-5 w-5" />
              <span className="text-xl font-bold">{statusCount}</span>
              <span className="text-sm font-medium">Stages</span>
            </div>
            <div className="flex items-center gap-3 px-5 py-3 rounded-xl bg-surface-100 dark:bg-surface-800 text-surface-700 dark:text-surface-300 shadow-sm ring-1 ring-surface-200 dark:ring-surface-700">
              <ArrowRight className="h-5 w-5" />
              <span className="text-sm font-medium">Drag to reorder</span>
            </div>
          </div>
        </div>

        <div className="card p-4 md:p-6">
          <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
            <div className="flex items-center gap-3 px-4 py-2.5 rounded-xl border border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-900/50 min-w-[240px]">
              <Settings className="h-4 w-4 text-primary-600 dark:text-primary-400" />
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
                onClick={() => setViewMode("statuses")}
                className={`flex items-center gap-2 rounded-lg px-4 py-2 font-medium transition-all text-sm ${
                  viewMode === "statuses"
                    ? "bg-primary-600 text-white shadow-sm"
                    : "text-surface-600 dark:text-surface-400 hover:bg-surface-50 dark:hover:bg-surface-700"
                }`}
              >
                <Columns3 className="h-4 w-4" />
                <span className="hidden sm:inline">Statuses</span>
              </button>
              <button
                onClick={() => setViewMode("transitions")}
                className={`flex items-center gap-2 rounded-lg px-4 py-2 font-medium transition-all text-sm ${
                  viewMode === "transitions"
                    ? "bg-primary-600 text-white shadow-sm"
                    : "text-surface-600 dark:text-surface-400 hover:bg-surface-50 dark:hover:bg-surface-700"
                }`}
              >
                <ArrowRight className="h-4 w-4" />
                <span className="hidden sm:inline">Transitions</span>
              </button>
              <button
                onClick={() => setViewMode("graph")}
                className={`flex items-center gap-2 rounded-lg px-4 py-2 font-medium transition-all text-sm ${
                  viewMode === "graph"
                    ? "bg-primary-600 text-white shadow-sm"
                    : "text-surface-600 dark:text-surface-400 hover:bg-surface-50 dark:hover:bg-surface-700"
                }`}
              >
                <Workflow className="h-4 w-4" />
                <span className="hidden sm:inline">Graph</span>
              </button>
            </div>

            {viewMode === "statuses" ? (
              <button
                onClick={handleCreateStatus}
                className="btn btn-primary"
                disabled={!selectedProjectId || loading || !canEditProject()}
              >
                <Plus className="h-4 w-4" />
                New Status
              </button>
            ) : (
              <button
                onClick={handleCreateTransition}
                className="btn btn-primary"
                disabled={!selectedProjectId || transitionsLoading || !canEditProject()}
              >
                <Plus className="h-4 w-4" />
                {viewMode === "transitions"
                  ? "New Transition"
                  : "New Connection"}
              </button>
            )}
          </div>
        </div>

        {viewMode === "statuses" ? (
          <StatusKanbanPreview
            statuses={statuses}
            projectId={selectedProjectId}
            onUpdateOrder={handleStatusesReordered}
            onDelete={handleDeleteStatus}
            onCreateStatus={handleCreateStatus}
          />
        ) : viewMode === "transitions" ? (
          <TransitionTable
            transitions={transitions}
            statuses={statuses}
            onDelete={handleTransitionDeleted}
            loading={transitionsLoading}
          />
        ) : (
          <WorkflowGraph
            projectId={selectedProjectId}
            statuses={statuses}
            transitions={transitions}
          />
        )}
      </div>

      <Toaster position="top-right" richColors />

      <StatusFormModal
        isOpen={showCreateModal}
        onClose={() => {
          setShowCreateModal(false);
          setEditingStatus(null);
        }}
        status={editingStatus}
        projectId={selectedProjectId}
        onSuccess={handleStatusSaved}
      />

      <TransitionFormModal
        isOpen={showTransitionModal}
        onClose={() => setShowTransitionModal(false)}
        statuses={statuses}
        projectId={selectedProjectId}
        onSuccess={handleTransitionSaved}
      />

      <ConfirmationDialog
        isOpen={showConfirm}
        onClose={() => setShowConfirm(false)}
        onConfirm={confirmAction}
        title="Delete Workflow Status?"
        message="Tasks assigned to this status may lose their status. Consider reassigning first."
      />
    </>
  );
}
