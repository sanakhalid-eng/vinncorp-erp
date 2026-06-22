import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  CalendarDays,
  FolderKanban,
  ListTodo,
  Users,
  LayoutGrid,
  Clock,
  Settings,
  Save,
  UserPlus,
} from "lucide-react";
import { getProjectById } from "../api/projectApi";
import { getProjectMembers } from "../api/projectMembersApi";
import { getTasksByProject } from "../../tasks/api/taskApi";
import { saveProjectAsTemplate } from "../api/projectTemplateApi";
import RoleBadge from "../components/members/RoleBadge.jsx";
import PriorityBadge from "../../tasks/components/tasks/PriorityBadge.jsx";
import ActivityFeed from "../../analytics/components/ActivityFeed.jsx";
import { Badge } from "../../../components/ui/badge";
import { useProjectPermission } from "../../../context/ProjectPermissionContext.jsx";
import ProjectNavBar from "../components/ProjectNavBar.jsx";
import { toast } from "sonner";
const formatDate = (value) =>
  value ? new Date(value).toLocaleDateString() : "Not set";
export default function ProjectDetails() {
  const { id, workspaceSlug } = useParams();
  const navigate = useNavigate();
  const [project, setProject] = useState(null);
  const [members, setMembers] = useState([]);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showSaveTemplateModal, setShowSaveTemplateModal] = useState(false);
  const [templateName, setTemplateName] = useState("");
  const [templateDescription, setTemplateDescription] = useState("");
  const [savingTemplate, setSavingTemplate] = useState(false);
  const { setProjectId, clearProjectId } = useProjectPermission();
  useEffect(() => {
    if (id) {
      setProjectId(Number(id));
    } else {
      clearProjectId();
    }
    return () => clearProjectId();
  }, [id, setProjectId, clearProjectId]);
  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const [projectResult, memberResult, taskResult] =
          await Promise.allSettled([
            getProjectById(id),
            getProjectMembers(id),
            getTasksByProject(id, { size: 50 }),
          ]);
        if (projectResult.status === "fulfilled") {
          setProject(projectResult.value);
        } else {
          setProject(null);
        }
        if (memberResult.status === "fulfilled") {
          setMembers(memberResult.value);
        } else {
          setMembers([]);
        }
        if (taskResult.status === "fulfilled") {
          setTasks(
            Array.isArray(taskResult.value?.content)
              ? taskResult.value.content
              : [],
          );
        } else {
          setTasks([]);
        }
      } catch (error) {
        console.error("Project details load error:", error);
      } finally {
        setLoading(false);
      }
    };
    if (id) {
      load();
    }
  }, [id]);
  const handleSaveAsTemplate = async () => {
    if (!templateName.trim()) {
      toast.error("Template name is required");
      return;
    }
    try {
      setSavingTemplate(true);
      await saveProjectAsTemplate(id, {
        name: templateName,
        description: templateDescription,
      });
      toast.success("Project saved as template");
      setShowSaveTemplateModal(false);
      setTemplateName("");
      setTemplateDescription("");
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to save template");
    } finally {
      setSavingTemplate(false);
    }
  };

  const taskStats = useMemo(
    () => ({
      total: tasks.length,
      completed: tasks.filter(
        (task) =>
          (task.status || "").toUpperCase().includes("DONE") ||
          (task.status || "").toUpperCase().includes("COMPLETE"),
      ).length,
      open: tasks.filter(
        (task) =>
          !(
            (task.status || "").toUpperCase().includes("DONE") ||
            (task.status || "").toUpperCase().includes("COMPLETE")
          ),
      ).length,
    }),
    [tasks],
  );
  if (loading) {
    return (
      <div className="flex min-h-[70vh] items-center justify-center">
         
        <div className="text-center">
           
          <div className="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-b-2 border-indigo-600" /> 
          <p className="text-gray-600">Loading project details...</p> 
        </div> 
      </div>
    );
  }
  if (!project) {
    return (
      <div className="rounded-3xl border border-slate-200 bg-white/80 p-10 text-center shadow-xl">
         
        <h1 className="text-2xl font-bold text-slate-900">
          Project not found
        </h1> 
        <button
          onClick={() => navigate(`/w/${workspaceSlug}/projects`)}
          className="mt-4 rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white hover:bg-indigo-700"
        >
           
          Back to Projects 
        </button> 
      </div>
    );
  }
  return (
    <>
    <div className="space-y-8">
       
      <ProjectNavBar projectName={project.name} /> 
      <section className="rounded-[2rem] border border-white/60 bg-[radial-gradient(circle_at_top_right,_rgba(59,130,246,0.22),_transparent_28%),linear-gradient(135deg,_#0f172a_0%,_#1e293b_45%,_#312e81_100%)] p-8 text-white shadow-[0_30px_80px_rgba(15,23,42,0.18)]">
         
        <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
           
          <div className="max-w-3xl">
             
            <div className="flex items-center justify-between">
               
              <p className="mb-3 inline-flex rounded-full bg-white/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-cyan-100">
                 
                Project Detail 
              </p> 
              <button
                onClick={() => setShowSaveTemplateModal(true)}
                className="mb-3 inline-flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1.5 text-xs font-semibold text-white transition-colors hover:bg-white/20"
              >
                <Save className="h-3.5 w-3.5" /> Save as Template 
              </button> 
              <button
                onClick={() =>
                  navigate(`/w/${workspaceSlug}/projects/${id}/settings`)
                }
                className="mb-3 inline-flex items-center gap-1.5 rounded-full bg-white/10 px-3 py-1.5 text-xs font-semibold text-white transition-colors hover:bg-white/20"
              >
                <Settings className="h-3.5 w-3.5" /> Edit Project 
              </button> 
            </div> 
            <h1 className="text-4xl font-black">{project.name}</h1> 
            <p className="mt-4 text-sm leading-7 text-slate-200">
               
              {project.description ||
                "No description has been added to this project yet."} 
            </p> 
            <div className="mt-5 flex flex-wrap gap-3 text-sm text-slate-200">
               
              <span className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1.5">
                 
                <CalendarDays className="h-4 w-4" /> Created 
                {formatDate(project.createdAt)} 
              </span> 
              <span className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1.5">
                 
                <Users className="h-4 w-4" /> {members.length} members 
              </span> 
              <span className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3 py-1.5">
                 
                <ListTodo className="h-4 w-4" /> {tasks.length} tasks 
              </span> 
            </div> 
          </div> 
          <div className="grid grid-cols-3 gap-3 lg:w-[360px]">
             
            <div className="rounded-3xl border border-white/10 bg-white/10 p-4 text-center">
               
              <p className="text-xs uppercase tracking-[0.2em] text-slate-300">
                Total
              </p> 
              <p className="mt-3 text-3xl font-bold">{taskStats.total}</p> 
            </div> 
            <div className="rounded-3xl border border-white/10 bg-white/10 p-4 text-center">
               
              <p className="text-xs uppercase tracking-[0.2em] text-slate-300">
                Open
              </p> 
              <p className="mt-3 text-3xl font-bold">{taskStats.open}</p> 
            </div> 
            <div className="rounded-3xl border border-white/10 bg-white/10 p-4 text-center">
               
              <p className="text-xs uppercase tracking-[0.2em] text-slate-300">
                Done
              </p> 
              <p className="mt-3 text-3xl font-bold">
                {taskStats.completed}
              </p> 
            </div> 
          </div> 
        </div> 
      </section> 
      <section className="grid gap-8 xl:grid-cols-[0.95fr_1.05fr]">
         
        <div className="rounded-[2rem] border border-slate-200 bg-white/80 p-8 shadow-xl backdrop-blur">
           
          <div className="mb-6 flex items-center justify-between">
             
            <div className="flex items-center gap-3">
               
              <Users className="h-6 w-6 text-indigo-600" /> 
              <h2 className="text-2xl font-bold text-slate-900">
                Team Members
              </h2> 
            </div> 
            <button
              onClick={() => navigate(`/w/${workspaceSlug}/members`)}
              className="flex items-center gap-1.5 rounded-xl bg-indigo-100 px-3 py-1.5 text-sm font-semibold text-indigo-700 transition-colors hover:bg-indigo-200"
            >
               
              <UserPlus className="h-4 w-4" /> Manage Members 
            </button> 
          </div> 
          <div className="space-y-3">
             
            {members.length > 0 ? (
              members.map((member) => (
                <div
                  key={member.id}
                  className="flex items-center gap-3 rounded-2xl bg-slate-50 p-4"
                >
                   
                  <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-indigo-500 to-fuchsia-500 font-bold text-white">
                     
                    {member.name?.charAt(0)?.toUpperCase() || "U"} 
                  </div> 
                  <div className="min-w-0 flex-1">
                     
                    <p className="truncate font-semibold text-slate-900">
                      {member.name}
                    </p> 
                    <p className="truncate text-sm text-slate-500">
                      {member.email}
                    </p> 
                  </div> 
                  <RoleBadge role={member.role} /> 
                </div>
              ))
            ) : (
              <p className="text-sm text-slate-500">
                No members found for this project.
              </p>
            )} 
          </div> 
        </div> 
        <div className="rounded-[2rem] border border-slate-200 bg-white/80 p-8 shadow-xl backdrop-blur">
           
          <div className="mb-6 flex items-center justify-between">
             
            <div className="flex items-center gap-3">
               
              <FolderKanban className="h-6 w-6 text-emerald-600" /> 
              <h2 className="text-2xl font-bold text-slate-900">Tasks</h2> 
            </div> 
            <button
              onClick={() =>
                navigate(`/w/${workspaceSlug}/projects/${id}/board`)
              }
              className="flex items-center gap-2 rounded-xl bg-emerald-100 px-3 py-1.5 text-sm font-semibold text-emerald-700 transition-colors hover:bg-emerald-200"
            >
               
              <LayoutGrid className="h-4 w-4" /> Board View 
            </button> 
          </div> 
          <div className="space-y-3">
             
            {tasks.length > 0 ? (
              tasks.map((task) => (
                <div
                  key={task.id}
                  className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm"
                >
                   
                  <div className="flex flex-wrap items-start justify-between gap-3">
                     
                    <div>
                       
                      <h3 className="font-semibold text-slate-900">
                        {task.title}
                      </h3> 
                      <p className="mt-1 text-sm text-slate-500">
                        {task.description || "No description"}
                      </p> 
                    </div> 
                    <div className="flex flex-wrap gap-2">
                       
                      <Badge>{task.status || "Unknown"}</Badge> 
                      {task.priority && (
                        <PriorityBadge priority={task.priority} />
                      )} 
                    </div> 
                  </div> 
                </div>
              ))
            ) : (
              <p className="text-sm text-slate-500">
                No tasks found for this project.
              </p>
            )} 
          </div> 
        </div> 
      </section> 
      <section className="rounded-[2rem] border border-slate-200 bg-white/80 p-8 shadow-xl backdrop-blur">
         
        <div className="mb-6 flex items-center gap-3">
           
          <Clock className="h-6 w-6 text-amber-600" /> 
          <h2 className="text-2xl font-bold text-slate-900">
            Activity Log
          </h2> 
        </div> 
        <ActivityFeed projectId={Number(id)} limit={15} /> 
      </section> 
    </div>
      {showSaveTemplateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/45 backdrop-blur-sm p-4">
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-xl font-bold text-slate-900">Save as Template</h3>
              <button
                onClick={() => setShowSaveTemplateModal(false)}
                className="rounded-xl p-2 text-slate-400 hover:bg-slate-100"
              >
                Γ£ò
              </button>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1">Template Name *</label>
                <input
                  type="text"
                  value={templateName}
                  onChange={(e) => setTemplateName(e.target.value)}
                  className="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  placeholder="e.g., Sprint Template"
                />
              </div>
              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1">Description</label>
                <textarea
                  value={templateDescription}
                  onChange={(e) => setTemplateDescription(e.target.value)}
                  rows={3}
                  className="w-full px-4 py-3 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent resize-vertical"
                  placeholder="Optional description..."
                />
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button
                onClick={() => setShowSaveTemplateModal(false)}
                className="px-4 py-2.5 rounded-xl border border-slate-200 text-slate-600 font-medium hover:bg-slate-50"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveAsTemplate}
                disabled={savingTemplate}
                className="px-4 py-2.5 rounded-xl bg-indigo-600 text-white font-medium hover:bg-indigo-700 disabled:opacity-50"
              >
                {savingTemplate ? "Saving..." : "Save Template"}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
