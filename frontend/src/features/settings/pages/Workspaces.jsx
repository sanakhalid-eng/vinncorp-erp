import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Building2,
  Plus,
  Settings,
  Users,
  Mail,
  Trash2,
  Loader2,
  ArrowLeft,
} from "lucide-react";
import notify from "../../../lib/toast";
import {
  getWorkspaces,
  createWorkspace,
  deleteWorkspace,
} from "../api/workspaceApi";
import Button from "../../../components/Button";
export default function Workspaces() {
  const navigate = useNavigate();
  const [workspaces, setWorkspaces] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [formData, setFormData] = useState({ name: "", description: "" });
  const [creating, setCreating] = useState(false);
  const [deleting, setDeleting] = useState(null);
  useEffect(() => {
    loadWorkspaces();
  }, []);
  const loadWorkspaces = async () => {
    try {
      setLoading(true);
      const res = await getWorkspaces();
      setWorkspaces(res.data.data || []);
    } catch {
      notify.error("Failed to load workspaces");
    } finally {
      setLoading(false);
    }
  };
  const handleCreate = async () => {
    if (!formData.name.trim()) {
      notify.error("Workspace name is required");
      return;
    }
    try {
      setCreating(true);
      await createWorkspace(formData);
      notify.success("Workspace created");
      setShowCreate(false);
      setFormData({ name: "", description: "" });
      loadWorkspaces();
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to create workspace");
    } finally {
      setCreating(false);
    }
  };
  const handleDelete = async (id) => {
    if (!confirm("Are you sure you want to delete this workspace?")) return;
    try {
      setDeleting(id);
      await deleteWorkspace(id);
      notify.success("Workspace deleted");
      loadWorkspaces();
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to delete workspace");
    } finally {
      setDeleting(null);
    }
  };
  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(56,189,248,0.12),_transparent_28%),linear-gradient(180deg,_#f8fafc_0%,_#eef2ff_100%)]">
       
      <header className="sticky top-0 z-30 flex items-center justify-between border-b border-slate-200/70 bg-white/80 px-4 py-3 backdrop-blur lg:px-6">
         
        <button
          onClick={() => navigate("/user-home")}
          className="flex items-center gap-2 text-slate-500 hover:text-indigo-600 transition-colors group"
        >
           
          <ArrowLeft className="w-5 h-5 group-hover:-translate-x-1 transition-transform" /> 
          <span className="font-medium hidden sm:inline">
            Back to Home
          </span> 
        </button> 
        <div className="flex items-center gap-3">
           
          <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-400 to-indigo-500 shadow-sm">
             
            <Building2 className="h-4 w-4 text-white" /> 
          </div> 
          <span className="font-bold text-slate-800">Workspaces</span> 
        </div> 
      </header> 
      <main className="max-w-5xl mx-auto px-4 py-6 lg:px-6">
         
        <div className="flex items-center justify-between mb-8">
           
          <div>
             
            <h1 className="text-3xl font-bold text-slate-900">
              Your Workspaces
            </h1> 
            <p className="text-slate-500 mt-1">
              Manage all your workspaces in one place
            </p> 
          </div> 
          <Button
            onClick={() => setShowCreate(true)}
            className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
          >
             
            <Plus className="w-4 h-4 mr-1.5 inline" /> New Workspace 
          </Button> 
        </div> 
        {loading ? (
          <div className="flex justify-center py-20">
             
            <Loader2 className="w-8 h-8 animate-spin text-indigo-600" /> 
          </div>
        ) : workspaces.length === 0 ? (
          <div className="text-center py-20">
             
            <Building2 className="w-16 h-16 text-slate-300 mx-auto mb-4" /> 
            <h3 className="text-xl font-semibold text-slate-600 mb-2">
              No workspaces yet
            </h3> 
            <p className="text-slate-400 mb-6">
              Create your first workspace to get started
            </p> 
            <Button
              onClick={() => setShowCreate(true)}
              className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
            >
               
              <Plus className="w-4 h-4 mr-1.5 inline" /> Create Workspace 
            </Button> 
          </div>
        ) : (
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
             
            {workspaces.map((ws) => (
              <div
                key={ws.id}
                className="rounded-2xl border border-slate-200 bg-white p-6 shadow-lg hover:shadow-xl transition-all group"
              >
                 
                <div className="flex items-start justify-between mb-4">
                   
                  <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-gradient-to-br from-cyan-400 to-indigo-500 shadow-md">
                     
                    <Building2 className="h-6 w-6 text-white" /> 
                  </div> 
                  <div className="flex gap-1">
                     
                    <button
                      onClick={() => {
                        localStorage.setItem("activeWorkspaceId", ws.id);
                        localStorage.setItem("activeWorkspaceSlug", ws.slug);
                        navigate(`/w/${ws.slug}/settings`);
                      }}
                      className="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
                      title="Settings"
                    >
                       
                      <Settings className="w-4 h-4" /> 
                    </button> 
                    <button
                      onClick={() => {
                        localStorage.setItem("activeWorkspaceId", ws.id);
                        localStorage.setItem("activeWorkspaceSlug", ws.slug);
                        navigate(`/w/${ws.slug}/members`);
                      }}
                      className="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
                      title="Members"
                    >
                       
                      <Users className="w-4 h-4" /> 
                    </button> 
                    <button
                      onClick={() =>
                        navigate(`/workspaces/${ws.id}/invitations`)
                      }
                      className="rounded-xl p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600 transition"
                      title="Invitations"
                    >
                       
                      <Mail className="w-4 h-4" /> 
                    </button> 
                    {ws.role === "WORKSPACE_OWNER" && (
                      <button
                        onClick={() => handleDelete(ws.id)}
                        disabled={deleting === ws.id}
                        className="rounded-xl p-2 text-red-400 hover:bg-red-50 hover:text-red-600 transition"
                        title="Delete"
                      >
                         
                        {deleting === ws.id ? (
                          <Loader2 className="w-4 h-4 animate-spin" />
                        ) : (
                          <Trash2 className="w-4 h-4" />
                        )} 
                      </button>
                    )} 
                  </div> 
                </div> 
                <h3 className="text-lg font-bold text-slate-900 mb-1">
                  {ws.name}
                </h3> 
                {ws.description && (
                  <p className="text-sm text-slate-500 mb-3 line-clamp-2">
                    {ws.description}
                  </p>
                )} 
                <div className="flex items-center gap-4 text-xs text-slate-400">
                   
                  <span className="flex items-center gap-1">
                     
                    <Users className="w-3.5 h-3.5" /> {ws.memberCount || 0} 
                    members 
                  </span> 
                  <span className="rounded-full bg-indigo-50 text-indigo-600 px-2.5 py-0.5 font-medium">
                     
                    {ws.role?.replace("WORKSPACE_", "") || "MEMBER"} 
                  </span> 
                </div> 
              </div>
            ))} 
          </div>
        )} 
      </main> 
      {showCreate && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/45 backdrop-blur-sm p-4">
           
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl">
             
            <h3 className="text-xl font-bold text-slate-900 mb-4">
              Create Workspace
            </h3> 
            <div className="space-y-4">
               
              <div>
                 
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Name
                </label> 
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) =>
                    setFormData((s) => ({ ...s, name: e.target.value }))
                  }
                  placeholder="My Workspace"
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400"
                /> 
              </div> 
              <div>
                 
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Description
                </label> 
                <textarea
                  value={formData.description}
                  onChange={(e) =>
                    setFormData((s) => ({ ...s, description: e.target.value }))
                  }
                  placeholder="Optional description"
                  rows={3}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400 resize-none"
                /> 
              </div> 
            </div> 
            <div className="flex justify-end gap-3 mt-6">
               
              <Button
                type="secondary"
                onClick={() => setShowCreate(false)}
                className="rounded-xl"
              >
                 
                Cancel 
              </Button> 
              <Button
                onClick={handleCreate}
                disabled={creating || !formData.name.trim()}
                className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
              >
                 
                {creating ? "Creating..." : "Create"} 
              </Button> 
            </div> 
          </div> 
        </div>
      )} 
    </div>
  );
}
