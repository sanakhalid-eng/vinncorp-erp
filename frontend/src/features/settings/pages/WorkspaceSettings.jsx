import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Settings2,
  ArrowLeft,
  Loader2,
  Building2,
  Save,
  Globe,
  Calendar,
  Clock,
} from "lucide-react";
import notify from "../../../lib/toast";
import { useWorkspace } from "../../../context/WorkspaceContext";
import {
  updateWorkspace,
  getWorkspacePreferences,
  updateWorkspacePreferences,
  getWorkspaceMembers,
} from "../api/workspaceApi";
import Button from "../../../components/Button";
const TIMEZONES = [
  "UTC",
  "America/New_York",
  "America/Chicago",
  "America/Denver",
  "America/Los_Angeles",
  "Europe/London",
  "Europe/Berlin",
  "Europe/Paris",
  "Asia/Tokyo",
  "Asia/Shanghai",
  "Asia/Kolkata",
  "Australia/Sydney",
  "Pacific/Auckland",
];
const DATE_FORMATS = ["YYYY-MM-DD", "DD/MM/YYYY", "MM/DD/YYYY", "DD.MM.YYYY"];
const WEEK_START_DAYS = ["MONDAY", "SUNDAY", "SATURDAY"];
const DASHBOARD_VIEWS = ["overview", "projects", "activity"];
const TABS = [
  { id: "general", label: "General" },
  { id: "members", label: "Members" },
  { id: "preferences", label: "Preferences" },
];
export default function WorkspaceSettings() {
  const { workspace } = useWorkspace();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState("general");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [preferences, setPreferences] = useState(null);
  const [members, setMembers] = useState([]);
  const [formData, setFormData] = useState({ name: "", description: "" });
  useEffect(() => {
    if (workspace) loadData();
  }, [workspace?.id]);
  const loadData = async () => {
    try {
      setLoading(true);
      const [prefRes, memRes] = await Promise.allSettled([
        getWorkspacePreferences(workspace.id),
        getWorkspaceMembers(workspace.id),
      ]);
      if (prefRes.status === "fulfilled") {
        setPreferences(prefRes.value.data.data);
      }
      if (memRes.status === "fulfilled") {
        setMembers(memRes.value.data.data || []);
      }
      setFormData({
        name: workspace.name || "",
        description: workspace.description || "",
      });
    } catch {
      notify.error("Failed to load settings");
    } finally {
      setLoading(false);
    }
  };
  const handleSaveGeneral = async () => {
    if (!formData.name.trim()) {
      notify.error("Name is required");
      return;
    }
    try {
      setSaving(true);
      await updateWorkspace(workspace.id, formData);
      notify.success("Workspace updated");
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to save");
    } finally {
      setSaving(false);
    }
  };
  const handleSavePreferences = async () => {
    try {
      setSaving(true);
      const res = await updateWorkspacePreferences(workspace.id, preferences);
      setPreferences(res.data.data);
      notify.success("Preferences saved");
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to save preferences");
    } finally {
      setSaving(false);
    }
  };
  const updatePref = (key, value) => {
    setPreferences((prev) => ({ ...prev, [key]: value }));
  };
  if (loading) {
    return (
      <div className="flex justify-center py-20">
         
        <Loader2 className="w-8 h-8 animate-spin text-indigo-600" /> 
      </div>
    );
  }
  return (
    <div>
       
      <div className="mb-8">
         
        <h1 className="text-3xl font-bold text-slate-900">Settings</h1> 
        <p className="text-slate-500 mt-1">
          Manage {workspace?.name} configuration
        </p> 
      </div> 
      <div className="flex gap-2 border-b border-slate-200 mb-6 overflow-x-auto">
         
        {TABS.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`px-4 py-3 text-sm font-medium border-b-2 transition whitespace-nowrap ${activeTab === tab.id ? "border-indigo-600 text-indigo-600" : "border-transparent text-slate-500 hover:text-slate-700"}`}
          >
             
            {tab.label} 
          </button>
        ))} 
      </div> 
      {activeTab === "general" && (
        <div className="max-w-2xl space-y-6">
           
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-lg">
             
            <h3 className="text-lg font-bold text-slate-900 mb-4">
              General Information
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
                  rows={3}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400 resize-none"
                /> 
              </div> 
              <div className="flex justify-end">
                 
                <Button
                  onClick={handleSaveGeneral}
                  disabled={saving}
                  className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
                >
                   
                  <Save className="w-4 h-4 mr-1.5 inline" />
                  {saving ? "Saving..." : "Save Changes"} 
                </Button> 
              </div> 
            </div> 
          </div> 
        </div>
      )} 
      {activeTab === "members" && (
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-lg">
           
          <h3 className="text-lg font-bold text-slate-900 mb-4">
            Members ({members.length})
          </h3> 
          <div className="space-y-3">
             
            {members.map((m) => (
              <div
                key={m.id}
                className="flex items-center gap-3 rounded-xl p-3 hover:bg-slate-50"
              >
                 
                <div className="flex h-10 w-10 items-center justify-center overflow-hidden rounded-xl bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-sm font-bold text-white">
                   
                  {m.userName?.charAt(0).toUpperCase() || "U"} 
                </div> 
                <div className="min-w-0 flex-1">
                   
                  <p className="font-medium text-slate-800 truncate">
                    {m.userName}
                  </p> 
                  <p className="text-xs text-slate-400 truncate">
                    {m.userEmail}
                  </p> 
                </div> 
                <span className="shrink-0 rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-500">
                   
                  {m.workspaceRole?.replace("WORKSPACE_", "")} 
                </span> 
              </div>
            ))} 
          </div> 
        </div>
      )} 
      {activeTab === "preferences" && (
        <div className="max-w-2xl space-y-6">
           
          <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-lg">
             
            <h3 className="text-lg font-bold text-slate-900 mb-4">
              Workspace Preferences
            </h3> 
            <div className="space-y-6">
               
              <div>
                 
                <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-2">
                   
                  <Globe className="w-4 h-4" /> Timezone 
                </label> 
                <select
                  value={preferences?.timezone || "UTC"}
                  onChange={(e) => updatePref("timezone", e.target.value)}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400"
                >
                   
                  {TIMEZONES.map((tz) => (
                    <option key={tz} value={tz}>
                      {tz}
                    </option>
                  ))} 
                </select> 
              </div> 
              <div>
                 
                <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-2">
                   
                  <Calendar className="w-4 h-4" /> Date Format 
                </label> 
                <select
                  value={preferences?.dateFormat || "YYYY-MM-DD"}
                  onChange={(e) => updatePref("dateFormat", e.target.value)}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400"
                >
                   
                  {DATE_FORMATS.map((fmt) => (
                    <option key={fmt} value={fmt}>
                      {fmt}
                    </option>
                  ))} 
                </select> 
              </div> 
              <div>
                 
                <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-2">
                   
                  <Clock className="w-4 h-4" /> Week Starts On 
                </label> 
                <select
                  value={preferences?.weekStartDay || "MONDAY"}
                  onChange={(e) => updatePref("weekStartDay", e.target.value)}
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400"
                >
                   
                  {WEEK_START_DAYS.map((day) => (
                    <option key={day} value={day}>
                      {day.charAt(0) + day.slice(1).toLowerCase()}
                    </option>
                  ))} 
                </select> 
              </div> 
              <div>
                 
                <label className="flex items-center gap-2 text-sm font-medium text-slate-700 mb-2">
                   
                  <Settings2 className="w-4 h-4" /> Default Dashboard View 
                </label> 
                <select
                  value={preferences?.defaultDashboardView || "overview"}
                  onChange={(e) =>
                    updatePref("defaultDashboardView", e.target.value)
                  }
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400"
                >
                   
                  {DASHBOARD_VIEWS.map((view) => (
                    <option key={view} value={view}>
                      {view.charAt(0).toUpperCase() + view.slice(1)}
                    </option>
                  ))} 
                </select> 
              </div> 
              <div className="flex justify-end pt-2">
                 
                <Button
                  onClick={handleSavePreferences}
                  disabled={saving}
                  className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
                >
                   
                  <Save className="w-4 h-4 mr-1.5 inline" />
                  {saving ? "Saving..." : "Save Preferences"} 
                </Button> 
              </div> 
            </div> 
          </div> 
        </div>
      )} 
    </div>
  );
}
