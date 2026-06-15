import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Save,
  Settings,
  Workflow,
  Webhook,
  MessageSquare,
  Users,
  LayoutGrid,
  ArrowLeft,
  DollarSign,
  Calendar,
  Flag,
  Tag,
  Globe,
  FileText,
} from "lucide-react";
import { toast } from "sonner";
import { getProjectById, updateProject } from "../api/projectApi";
import { getBoardByProject } from "../api/boardApi";
import Button from "../components/Button";
import { useProjectPermission } from "../context/ProjectPermissionContext.jsx";
import ProjectNavBar from "../components/ProjectNavBar.jsx";
const priorityOptions = [
  {
    value: "LOW",
    label: "Low",
    color: "text-emerald-600",
    bg: "bg-emerald-50",
  },
  {
    value: "MEDIUM",
    label: "Medium",
    color: "text-amber-600",
    bg: "bg-amber-50",
  },
  {
    value: "HIGH",
    label: "High",
    color: "text-orange-600",
    bg: "bg-orange-50",
  },
  {
    value: "CRITICAL",
    label: "Critical",
    color: "text-red-600",
    bg: "bg-red-50",
  },
];
const categoryOptions = [
  "Web Development",
  "Mobile App",
  "Desktop Application",
  "Data Science",
  "Machine Learning",
  "DevOps",
  "UI/UX Design",
  "Marketing",
  "Research",
  "Other",
];
export default function ProjectSettings() {
  const { projectId, workspaceSlug } = useParams();
  const navigate = useNavigate();
  const { setProjectId, clearProjectId } = useProjectPermission();
  const [project, setProject] = useState(null);
  const [board, setBoard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({});
  useEffect(() => {
    if (projectId) {
      setProjectId(Number(projectId));
    } else {
      clearProjectId();
    }
    return () => clearProjectId();
  }, [projectId, setProjectId, clearProjectId]);
  useEffect(() => {
    const load = async () => {
      try {
        const projectData = await getProjectById(projectId);
        setProject(projectData);
        setForm({
          name: projectData.name || "",
          description: projectData.description || "",
          priority: projectData.priority || "MEDIUM",
          category: projectData.category || "",
          tags: projectData.tags || "",
          startDate: projectData.startDate
            ? new Date(projectData.startDate).toISOString().split("T")[0]
            : "",
          endDate: projectData.endDate
            ? new Date(projectData.endDate).toISOString().split("T")[0]
            : "",
          budget: projectData.budget || "",
          currency: projectData.currency || "USD",
          isActive: projectData.isActive ?? true,
          isPublic: projectData.isPublic ?? false,
        });
      } catch {
        toast.error("Failed to load project");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [projectId]);
  useEffect(() => {
    const loadBoard = async () => {
      try {
        const res = await getBoardByProject(projectId);
        setBoard(res?.data ?? res ?? null);
      } catch {
        setBoard(null);
      }
    };
    loadBoard();
  }, [projectId]);
  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        budget: form.budget ? parseFloat(form.budget) : null,
        startDate: form.startDate || null,
        endDate: form.endDate || null,
      };
      const updated = await updateProject(projectId, payload);
      setProject(updated);
      toast.success("Project settings saved");
    } catch {
      toast.error("Failed to save project settings");
    } finally {
      setSaving(false);
    }
  };
  const linkCards = [
    {
      label: "Workflow Statuses",
      path: `/w/${workspaceSlug}/workflow-statuses`,
      icon: Workflow,
      desc: "Manage statuses & transitions",
      color: "from-purple-500 to-pink-500",
    },
    {
      label: "Board Columns",
      path: `/w/${workspaceSlug}/projects/${projectId}/board`,
      icon: LayoutGrid,
      desc: `${board?.columns?.length ?? 0} columns configured`,
      color: "from-emerald-500 to-teal-500",
    },
    {
      label: "Team Members",
      path: `/w/${workspaceSlug}/project-members`,
      icon: Users,
      desc: "Add or remove members",
      color: "from-blue-500 to-indigo-500",
    },
    {
      label: "Webhooks",
      path: `/w/${workspaceSlug}/projects/${projectId}/webhooks`,
      icon: Webhook,
      desc: "Configure event webhooks",
      color: "from-orange-500 to-amber-500",
    },
    {
      label: "Slack Integration",
      path: `/w/${workspaceSlug}/projects/${projectId}/slack`,
      icon: MessageSquare,
      desc: "Connect Slack workspace",
      color: "from-cyan-500 to-blue-500",
    },
  ];
  if (loading) {
    return (
      <div className="flex min-h-[70vh] items-center justify-center">
         
        <div className="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-b-2 border-indigo-600" /> 
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
          onClick={() => navigate("/projects")}
          className="mt-4 rounded-xl bg-indigo-600 px-5 py-3 font-semibold text-white hover:bg-indigo-700"
        >
           
          Back to Projects 
        </button> 
      </div>
    );
  }
  return (
    <div className="mx-auto max-w-5xl space-y-8">
       
      <ProjectNavBar projectName={project.name} /> 
      <form onSubmit={handleSubmit} className="space-y-8">
         
        <section className="rounded-[2rem] border border-slate-200 bg-white/80 p-8 shadow-xl backdrop-blur">
           
          <div className="mb-6 flex items-center gap-3">
             
            <Settings className="h-6 w-6 text-indigo-600" /> 
            <h2 className="text-xl font-bold text-slate-900">
              Project Information
            </h2> 
          </div> 
          <div className="space-y-6">
             
            <div>
               
              <label className="block text-sm font-semibold text-slate-700 mb-1">
                Project Name *
              </label> 
              <input
                value={form.name}
                onChange={(e) => handleChange("name", e.target.value)}
                className="w-full rounded-2xl border border-slate-200 px-4 py-3 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-200"
                placeholder="Enter project name"
                required
              /> 
            </div> 
            <div>
               
              <label className="block text-sm font-semibold text-slate-700 mb-1">
                 
                <span className="inline-flex items-center gap-2">
                  <FileText className="w-4 h-4" /> Description
                </span> 
              </label> 
              <textarea
                value={form.description}
                onChange={(e) => handleChange("description", e.target.value)}
                rows={3}
                className="w-full rounded-2xl border border-slate-200 px-4 py-3 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-200 resize-vertical"
                placeholder="Describe the project..."
              /> 
            </div> 
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
               
              <div>
                 
                <label className="block text-sm font-semibold text-slate-700 mb-1">
                   
                  <span className="inline-flex items-center gap-2">
                    <Flag className="w-4 h-4" /> Priority
                  </span> 
                </label> 
                <div className="grid grid-cols-4 gap-2">
                   
                  {priorityOptions.map((p) => (
                    <button
                      key={p.value}
                      type="button"
                      onClick={() => handleChange("priority", p.value)}
                      className={`p-3 rounded-xl border-2 transition-all text-sm font-medium ${form.priority === p.value ? `border-indigo-500 ${p.bg} ${p.color}` : "border-slate-200 hover:border-slate-300 text-slate-600"}`}
                    >
                       
                      {p.label} 
                    </button>
                  ))} 
                </div> 
              </div> 
              <div>
                 
                <label className="block text-sm font-semibold text-slate-700 mb-1">
                   
                  <span className="inline-flex items-center gap-2">
                    <Tag className="w-4 h-4" /> Category
                  </span> 
                </label> 
                <select
                  value={form.category}
                  onChange={(e) => handleChange("category", e.target.value)}
                  className="w-full rounded-2xl border border-slate-200 px-4 py-3 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-200 bg-white"
                >
                   
                  <option value="">Select category...</option> 
                  {categoryOptions.map((cat) => (
                    <option key={cat} value={cat}>
                      {cat}
                    </option>
                  ))} 
                </select> 
              </div> 
            </div> 
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
               
              <div>
                 
                <label className="block text-sm font-semibold text-slate-700 mb-1">
                   
                  <span className="inline-flex items-center gap-2">
                    <Calendar className="w-4 h-4" /> Start Date
                  </span> 
                </label> 
                <input
                  type="date"
                  value={form.startDate}
                  onChange={(e) => handleChange("startDate", e.target.value)}
                  className="w-full rounded-2xl border border-slate-200 px-4 py-3 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-200"
                /> 
              </div> 
              <div>
                 
                <label className="block text-sm font-semibold text-slate-700 mb-1">
                  End Date
                </label> 
                <input
                  type="date"
                  value={form.endDate}
                  onChange={(e) => handleChange("endDate", e.target.value)}
                  className="w-full rounded-2xl border border-slate-200 px-4 py-3 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-200"
                /> 
              </div> 
            </div> 
            <div>
               
              <label className="block text-sm font-semibold text-slate-700 mb-1">
                 
                <span className="inline-flex items-center gap-2">
                  <DollarSign className="w-4 h-4" /> Budget
                </span> 
              </label> 
              <div className="flex gap-2">
                 
                <select
                  value={form.currency}
                  onChange={(e) => handleChange("currency", e.target.value)}
                  className="rounded-2xl border border-slate-200 px-4 py-3 focus:border-indigo-500 bg-white"
                >
                   
                  <option value="USD">USD ($)</option> 
                  <option value="EUR">EUR (€)</option> 
                  <option value="GBP">GBP (£)</option> 
                  <option value="PKR">PKR (₨)</option> 
                </select> 
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={form.budget}
                  onChange={(e) => handleChange("budget", e.target.value)}
                  className="flex-1 rounded-2xl border border-slate-200 px-4 py-3 focus:border-indigo-500 focus:outline-none focus:ring-4 focus:ring-indigo-200"
                  placeholder="0.00"
                /> 
              </div> 
            </div> 
            <div className="space-y-3 pt-4 border-t border-slate-100">
               
              <label className="flex items-center gap-3 cursor-pointer">
                 
                <input
                  type="checkbox"
                  checked={form.isActive}
                  onChange={(e) => handleChange("isActive", e.target.checked)}
                  className="w-5 h-5 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
                /> 
                <span className="text-sm font-medium text-slate-700">
                  Project is active
                </span> 
              </label> 
              <label className="flex items-center gap-3 cursor-pointer">
                 
                <input
                  type="checkbox"
                  checked={form.isPublic}
                  onChange={(e) => handleChange("isPublic", e.target.checked)}
                  className="w-5 h-5 rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
                /> 
                <span className="inline-flex items-center gap-2 text-sm font-medium text-slate-700">
                  <Globe className="w-4 h-4" /> Public project
                </span> 
              </label> 
            </div> 
            <div className="flex justify-end pt-4 border-t border-slate-100">
               
              <Button
                type="submit"
                disabled={saving}
                className="bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 px-6 py-3"
              >
                 
                <Save className="mr-2 h-4 w-4" /> 
                {saving ? "Saving..." : "Save Changes"} 
              </Button> 
            </div> 
          </div> 
        </section> 
      </form> 
      <section className="rounded-[2rem] border border-slate-200 bg-white/80 p-8 shadow-xl backdrop-blur">
         
        <div className="mb-6 flex items-center gap-3">
           
          <Settings className="h-6 w-6 text-slate-600" /> 
          <h2 className="text-xl font-bold text-slate-900">
            Configuration
          </h2> 
        </div> 
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
           
          {linkCards.map((card) => {
            const Icon = card.icon;
            return (
              <button
                key={card.path}
                type="button"
                onClick={() => navigate(card.path)}
                className="group relative rounded-2xl border-2 border-slate-200 bg-white p-5 text-left transition-all hover:-translate-y-1 hover:border-indigo-500 hover:shadow-xl hover:shadow-indigo-500/10"
              >
                 
                <div
                  className={`mb-3 inline-flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br ${card.color} shadow-lg`}
                >
                   
                  <Icon className="h-5 w-5 text-white" /> 
                </div> 
                <h3 className="font-bold text-slate-900">{card.label}</h3> 
                <p className="mt-1 text-sm text-slate-500">{card.desc}</p> 
              </button>
            );
          })} 
        </div> 
      </section> 
    </div>
  );
}
