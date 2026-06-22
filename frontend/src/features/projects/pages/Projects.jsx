import { useState, useEffect, useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import ProjectFormModal from "../components/ProjectFormModal";
import ProjectCard from "../components/ProjectCard";
import {
  getUserProjects,
  getProjectById,
  deleteProject,
} from "../api/projectApi";
import { createProjectFromTemplate } from "../api/projectTemplateApi";
import TemplateSelector from "../components/TemplateSelector.jsx";
import Button from "../../../components/Button.jsx";
import { Badge } from "../../../components/ui/badge";
import { CardSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";
import EmptyState from "../../../components/EmptyState";
import ConfirmationDialog from "../components/members/ConfirmationDialog";
import { parseApiError } from "../../../utils/apiError";
import { toast } from "sonner";
import {
  Plus,
  Search,
  LayoutGrid,
  List,
  FolderOpen,
  SlidersHorizontal,
  FileText,
} from "lucide-react";
import { usePermission } from "../../../context/usePermission";
const Projects = () => {
  const [showTemplateSelector, setShowTemplateSelector] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState(null);
  const navigate = useNavigate();
  const { workspaceSlug } = useParams();
  const { canCreateProject, canEditProject, canDeleteProject } =
    usePermission();
  const [projects, setProjects] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editProject, setEditProject] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [projectToDelete, setProjectToDelete] = useState(null);
  const [viewMode, setViewMode] = useState("grid");
  const [search, setSearch] = useState("");
  const [sortBy, setSortBy] = useState("name");
  const [sortOrder, setSortOrder] = useState("asc");
  const [filterPriority, setFilterPriority] = useState("all");
  const [filterStatus, setFilterStatus] = useState("all");
  const [showFilters, setShowFilters] = useState(false);
  useEffect(() => {
    loadProjects();
  }, []);
  const loadProjects = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await getUserProjects();
      const projectsArray = Array.isArray(data) ? data : data?.content || [];
      setProjects(projectsArray);
    } catch (err) {
      setError(err);
      setProjects([]);
    } finally {
      setLoading(false);
    }
  };
  const handleTemplateSelect = async (template) => {
    setShowTemplateSelector(false);
    if (!template) {
      setShowModal(true);
      return;
    }
    try {
      await createProjectFromTemplate(template.id, {});
      toast.success("Project created from template!");
      loadProjects();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to create project from template");
    }
  };

  const handleDelete = (project) => {
    setProjectToDelete(project);
    setShowDeleteConfirm(true);
  };

  const confirmDelete = async () => {
    if (!projectToDelete) return;
    try {
      await deleteProject(projectToDelete.id);
      toast.success("Project deleted");
      loadProjects();
    } catch (err) {
      toast.error(parseApiError(err).message);
    } finally {
      setShowDeleteConfirm(false);
      setProjectToDelete(null);
    }
  };
  const processedProjects = useMemo(() => {
    let filtered = [...projects];
    if (search) {
      const searchLower = search.toLowerCase();
      filtered = filtered.filter(
        (p) =>
          p.name?.toLowerCase().includes(searchLower) ||
          p.description?.toLowerCase().includes(searchLower) ||
          p.category?.toLowerCase().includes(searchLower),
      );
    }
    if (filterPriority !== "all") {
      filtered = filtered.filter((p) => p.priority === filterPriority);
    }
    if (filterStatus !== "all") {
      filtered = filtered.filter((p) =>
        filterStatus === "active" ? !p.isActive : p.isActive,
      );
    }
    filtered.sort((a, b) => {
      let aVal, bVal;
      switch (sortBy) {
        case "name":
          aVal = a.name || "";
          bVal = b.name || "";
          break;
        case "createdAt":
          aVal = new Date(a.createdAt || 0).getTime();
          bVal = new Date(b.createdAt || 0).getTime();
          break;
        case "priority":
          const priorityOrder = { CRITICAL: 4, HIGH: 3, MEDIUM: 2, LOW: 1 };
          aVal = priorityOrder[a.priority] || 0;
          bVal = priorityOrder[b.priority] || 0;
          break;
        default:
          return 0;
      }
      return sortOrder === "asc"
        ? aVal > bVal
          ? 1
          : -1
        : aVal < bVal
          ? 1
          : -1;
    });
    return filtered;
  }, [projects, search, filterPriority, filterStatus, sortBy, sortOrder]);
  const priorityCounts = useMemo(() => {
    return {
      all: projects.length,
      CRITICAL: projects.filter((p) => p.priority === "CRITICAL").length,
      HIGH: projects.filter((p) => p.priority === "HIGH").length,
      MEDIUM: projects.filter((p) => p.priority === "MEDIUM").length,
      LOW: projects.filter((p) => p.priority === "LOW").length,
    };
  }, [projects]);
  return (
    <>
       
      <div className="p-4 md:p-6 space-y-6">
         
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
           
          <div>
             
            <h1 className="text-2xl font-bold text-surface-900 dark:text-surface-100 md:text-3xl">
               
              Projects 
            </h1> 
            <p className="text-sm text-surface-500 dark:text-surface-400 mt-1">
               
              Manage your projects and teams. ({projects.length} total) 
            </p> 
          </div> 
          <div className="flex items-center gap-3">
             
            <div className="flex items-center rounded-xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-1 shadow-sm">
               
              <button
                onClick={() => setViewMode("grid")}
                className={`p-2 rounded-lg transition-all ${viewMode === "grid" ? "bg-primary-600 text-white shadow-sm" : "text-surface-500 dark:text-surface-400 hover:text-surface-700 dark:hover:text-surface-200"}`}
              >
                 
                <LayoutGrid className="w-4 h-4" /> 
              </button> 
              <button
                onClick={() => setViewMode("list")}
                className={`p-2 rounded-lg transition-all ${viewMode === "list" ? "bg-primary-600 text-white shadow-sm" : "text-surface-500 dark:text-surface-400 hover:text-surface-700 dark:hover:text-surface-200"}`}
              >
                 
                <List className="w-4 h-4" /> 
              </button> 
            </div> 
            {canCreateProject() && (
              <>
                <Button
                  onClick={() => {
                    setEditProject(null);
                    setShowModal(true);
                  }}
                  className="shadow-sm"
                >
                  <Plus className="w-4 h-4 mr-2" /> 
                  <span className="hidden sm:inline">New Project</span> 
                  <span className="sm:hidden">New</span> 
                </Button>
                <Button
                  onClick={() => {
                    setSelectedTemplate(null);
                    setShowTemplateSelector(true);
                  }}
                  variant="outline"
                  className="shadow-sm"
                >
                  <FileText className="w-4 h-4 mr-2" /> 
                  <span className="hidden sm:inline">From Template</span> 
                  <span className="sm:hidden">Template</span> 
                </Button>
              </>
            )} 
          </div> 
        </div> 
        <div className="rounded-2xl border border-surface-200 dark:border-surface-700 bg-white dark:bg-surface-800 p-4 shadow-sm space-y-4">
           
          <div className="flex flex-col lg:flex-row gap-3 items-start lg:items-center">
             
            <div className="relative flex-1">
               
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-surface-400" /> 
              <input
                type="text"
                placeholder="Search projects..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full pl-10 pr-4 py-2.5 border border-surface-200 dark:border-surface-700 rounded-xl focus:ring-2 focus:ring-primary-500/20 focus:border-primary-500 bg-white dark:bg-surface-900 text-sm"
              /> 
            </div> 
            <button
              onClick={() => setShowFilters(!showFilters)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl border transition-all text-sm ${showFilters ? "border-primary-500 bg-primary-50 dark:bg-primary-900/20 text-primary-700 dark:text-primary-400" : "border-surface-200 dark:border-surface-700 hover:bg-surface-50 dark:hover:bg-surface-700 text-surface-600 dark:text-surface-400"}`}
            >
               
              <SlidersHorizontal className="w-4 h-4" /> Filters 
            </button> 
          </div> 
          {showFilters && (
            <div className="flex flex-wrap gap-2 pt-3 border-t border-surface-200 dark:border-surface-700">
               
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="px-3 py-2 border border-surface-200 dark:border-surface-700 rounded-lg text-sm bg-white dark:bg-surface-900 text-surface-700 dark:text-surface-300"
              >
                 
                <option value="name">Sort by Name</option> 
                <option value="createdAt">Sort by Date</option> 
                <option value="priority">Sort by Priority</option> 
              </select> 
              <button
                onClick={() =>
                  setSortOrder(sortOrder === "asc" ? "desc" : "asc")
                }
                className="px-3 py-2 border border-surface-200 dark:border-surface-700 rounded-lg text-sm hover:bg-surface-50 dark:hover:bg-surface-700 bg-white dark:bg-surface-900 text-surface-700 dark:text-surface-300"
              >
                 
                {sortOrder === "asc" ? "Γåæ Asc" : "Γåô Desc"} 
              </button> 
              {["all", "CRITICAL", "HIGH", "MEDIUM", "LOW"].map((p) => (
                <button
                  key={p}
                  onClick={() => setFilterPriority(p)}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-all ${filterPriority === p ? "bg-primary-600 text-white" : "border border-surface-200 dark:border-surface-700 hover:bg-surface-50 dark:hover:bg-surface-700 bg-white dark:bg-surface-900 text-surface-700 dark:text-surface-300"}`}
                >
                   
                  {p === "all"
                    ? `All (${priorityCounts.all})`
                    : `${p} (${priorityCounts[p] || 0})`} 
                </button>
              ))} 
              {["all", "active", "inactive"].map((s) => (
                <button
                  key={s}
                  onClick={() => setFilterStatus(s)}
                  className={`px-3 py-2 rounded-lg text-sm font-medium transition-all ${filterStatus === s ? "bg-primary-600 text-white" : "border border-surface-200 dark:border-surface-700 hover:bg-surface-50 dark:hover:bg-surface-700 bg-white dark:bg-surface-900 text-surface-700 dark:text-surface-300"}`}
                >
                   
                  {s.charAt(0).toUpperCase() + s.slice(1)} 
                </button>
              ))} 
            </div>
          )} 
        </div> 
        {loading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {[...Array(6)].map((_, i) => (
              <CardSkeleton key={i} />
            ))}
          </div>
        ) : error ? (
          <ErrorState error={error} onRetry={loadProjects} />
        ) : processedProjects.length > 0 ? (
          <div
            className={
              viewMode === "grid"
                ? "grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3"
                : "space-y-3"
            }
          >
             
            {processedProjects.map((project) =>
              viewMode === "grid" ? (
                <ProjectCard
                  key={project.id}
                  project={project}
                  onEdit={(p) => {
                    setEditProject(p);
                    setShowModal(true);
                  }}
                  onDelete={handleDelete}
                  canEdit={canEditProject()}
                  canDelete={canDeleteProject()}
                  workspaceSlug={workspaceSlug}
                />
              ) : (
                <div
                  key={project.id}
                  className="bg-white dark:bg-surface-800 rounded-xl p-4 shadow-sm ring-1 ring-surface-200 dark:ring-surface-700 hover:shadow-md transition-all flex items-center justify-between"
                >
                   
                  <div className="flex-1 min-w-0">
                     
                    <div className="flex items-center gap-2 mb-1">
                       
                      <h3 className="font-semibold text-surface-900 dark:text-surface-100 truncate">
                        {project.name}
                      </h3> 
                      {project.priority && (
                        <Badge
                          className={`text-xs ${project.priority === "CRITICAL" ? "bg-danger-100 text-danger-700 dark:bg-danger-900/30 dark:text-danger-400" : project.priority === "HIGH" ? "bg-warning-100 text-warning-700 dark:bg-warning-900/30 dark:text-warning-400" : project.priority === "MEDIUM" ? "bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400" : "bg-success-100 text-success-700 dark:bg-success-900/30 dark:text-success-400"}`}
                        >
                           
                          {project.priority} 
                        </Badge>
                      )} 
                    </div> 
                    <p className="text-sm text-surface-500 dark:text-surface-400 truncate">
                      {project.description || "No description"}
                    </p> 
                  </div> 
                  <div className="flex items-center gap-1 ml-4">
                     
                    {canEditProject() && (
                      <button
                        onClick={() => {
                          setEditProject(project);
                          setShowModal(true);
                        }}
                        className="p-2 text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-900/20 rounded-lg"
                      >
                         
                        <FolderOpen className="w-4 h-4" /> 
                      </button>
                    )} 
                    {canDeleteProject() && (
                      <button
                        onClick={() => handleDelete(project)}
                        className="p-2 text-danger-600 hover:bg-danger-50 dark:hover:bg-danger-900/20 rounded-lg"
                      >
                         
                        <FolderOpen className="w-4 h-4" /> 
                      </button>
                    )} 
                  </div> 
                </div>
              ),
            )} 
          </div>
        ) : (
          <EmptyState
            icon={FolderOpen}
            title="No projects found"
            description={
              search || filterPriority !== "all" || filterStatus !== "all"
                ? "Try adjusting your search or filters."
                : "Create your first project to get started."
            }
            action={
              canCreateProject() &&
              !search &&
              filterPriority === "all" &&
              filterStatus === "all"
                ? {
                    label: "Create Project",
                    icon: Plus,
                    onClick: () => {
                      setEditProject(null);
                      setSelectedTemplate(null);
                      setShowTemplateSelector(true);
                    },
                  }
                : undefined
            }
          />
        )}
      </div> 
      {showTemplateSelector && (
        <TemplateSelector
          onSelect={handleTemplateSelect}
          onClose={() => setShowTemplateSelector(false)}
        />
      )}
      <ProjectFormModal
        isOpen={showModal}
        onClose={() => {
          setShowModal(false);
          setEditProject(null);
        }}
        onSuccess={() => {
          setShowModal(false);
          setEditProject(null);
          loadProjects();
        }}
        projectId={editProject?.id || null}
        initialData={editProject || {}}
      />
      <ConfirmationDialog
        isOpen={showDeleteConfirm}
        onClose={() => {
          setShowDeleteConfirm(false);
          setProjectToDelete(null);
        }}
        onConfirm={confirmDelete}
        title="Delete project?"
        message={
          projectToDelete
            ? `Delete "${projectToDelete.name}"? This action cannot be undone.`
            : "This action cannot be undone."
        }
        confirmText="Delete"
      />
    </>
  );
};
export default Projects;
