import { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  Mail,
  Phone,
  Building2,
  Briefcase,
  User,
  Tag,
  Edit2,
  Trash2,
  PhoneCall,
  MailCheck,
  CalendarDays,
  CheckSquare,
  StickyNote,
  ArrowUpRight,
  Clock,
  AlertTriangle,
  History,
  Sparkles,
  FileText,
  Plus,
  X,
  Save,
  Phone as PhoneIcon,
  MessageSquare,
  ExternalLink,
  Copy,
  RefreshCw,
  ChevronRight,
  CircleDot,
  CheckCircle2,
  XCircle,
  ArrowRight,
} from "lucide-react";
import { toast } from "sonner";
import {
  getLead,
  updateLead,
  deleteLead,
  convertLead,
  listActivitiesByLead,
  createActivity,
  deleteActivity,
  getEntityAuditHistory,
  getOpportunitiesByLead,
  getProjectByOpportunity,
} from "../api/crmApi";
import { usePermission } from "../../../context/usePermission";
import { PageSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";

const TABS = [
  { id: "profile", label: "Profile", icon: User },
  { id: "activities", label: "Activities", icon: Clock },
  { id: "notes", label: "Notes", icon: StickyNote },
  { id: "opportunities", label: "Opportunities", icon: Briefcase },
  { id: "conversion", label: "Conversion History", icon: History },
];

const STATUS_BADGE = {
  NEW: "bg-blue-100 text-blue-700",
  CONTACTED: "bg-amber-100 text-amber-700",
  QUALIFIED: "bg-emerald-100 text-emerald-700",
  UNQUALIFIED: "bg-slate-200 text-slate-700",
  CONVERTED: "bg-purple-100 text-purple-700",
  LOST: "bg-rose-100 text-rose-700",
};

const SOURCE_BADGE = {
  WEBSITE: "bg-cyan-100 text-cyan-700",
  REFERRAL: "bg-green-100 text-green-700",
  SOCIAL_MEDIA: "bg-violet-100 text-violet-700",
  EMAIL_CAMPAIGN: "bg-blue-100 text-blue-700",
  PHONE: "bg-amber-100 text-amber-700",
  EVENT: "bg-pink-100 text-pink-700",
  ADVERTISING: "bg-orange-100 text-orange-700",
  COLD_CALL: "bg-rose-100 text-rose-700",
  OTHER: "bg-slate-100 text-slate-600",
};

const ACTIVITY_ICONS = {
  CALL: PhoneCall,
  EMAIL: MailCheck,
  MEETING: CalendarDays,
  TASK: CheckSquare,
  NOTE: StickyNote,
};

const ACTIVITY_COLORS = {
  CALL: "from-blue-500 to-indigo-600",
  EMAIL: "from-amber-500 to-orange-600",
  MEETING: "from-emerald-500 to-teal-600",
  TASK: "from-violet-500 to-purple-600",
  NOTE: "from-slate-500 to-gray-600",
};

const STATUSES = ["NEW", "CONTACTED", "QUALIFIED", "UNQUALIFIED"];
const SOURCES = [
  "WEBSITE", "REFERRAL", "SOCIAL_MEDIA", "EMAIL_CAMPAIGN",
  "PHONE", "EVENT", "ADVERTISING", "COLD_CALL", "OTHER",
];
const ACTIVITY_TYPES = ["CALL", "EMAIL", "MEETING", "TASK", "NOTE"];

const STAGE_COLORS = {
  New: "bg-blue-100 text-blue-700",
  Qualified: "bg-cyan-100 text-cyan-700",
  Proposal: "bg-amber-100 text-amber-700",
  Negotiation: "bg-orange-100 text-orange-700",
  Won: "bg-emerald-100 text-emerald-700",
  Lost: "bg-rose-100 text-rose-700",
};

function formatDate(d) {
  if (!d) return "-";
  return new Date(d).toLocaleDateString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}

function formatDateTime(d) {
  if (!d) return "-";
  return new Date(d).toLocaleString("en-US", {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function timeAgo(d) {
  if (!d) return "";
  const diff = Date.now() - new Date(d).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days < 7) return `${days}d ago`;
  return formatDate(d);
}

export default function LeadDetailPage() {
  const { leadId, workspaceSlug } = useParams();
  const navigate = useNavigate();
  const { hasPermission } = usePermission();

  const [lead, setLead] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState("profile");
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [converting, setConverting] = useState(false);

  // Activities
  const [activities, setActivities] = useState([]);
  const [activitiesLoading, setActivitiesLoading] = useState(false);
  const [showActivityForm, setShowActivityForm] = useState(false);
  const [activityForm, setActivityForm] = useState({ type: "CALL", subject: "", description: "" });
  const [activitySaving, setActivitySaving] = useState(false);

  // Notes
  const [notesText, setNotesText] = useState("");
  const [notesSaving, setNotesSaving] = useState(false);

  // Opportunities
  const [opportunities, setOpportunities] = useState([]);
  const [oppsLoading, setOppsLoading] = useState(false);
  const [oppProjects, setOppProjects] = useState({});

  // Conversion History
  const [auditEvents, setAuditEvents] = useState([]);
  const [auditLoading, setAuditLoading] = useState(false);

  const loadLead = useCallback(async () => {
    if (!leadId) return;
    setLoading(true);
    try {
      const data = await getLead(leadId);
      setLead(data);
      setEditForm({
        firstName: data.firstName || "",
        lastName: data.lastName || "",
        email: data.email || "",
        phone: data.phone || "",
        company: data.company || "",
        jobTitle: data.jobTitle || "",
        source: data.source || "OTHER",
        status: data.status || "NEW",
      });
      setNotesText(data.notes || "");
    } catch {
      toast.error("Failed to load lead");
      navigate(`/w/${workspaceSlug}/crm/leads`, { replace: true });
    } finally {
      setLoading(false);
    }
  }, [leadId, workspaceSlug, navigate]);

  useEffect(() => {
    if (leadId) loadLead();
  }, [leadId, loadLead]);

  // Load tab-specific data
  useEffect(() => {
    if (!leadId) return;
    if (activeTab === "activities") loadActivities();
    if (activeTab === "opportunities") loadOpportunities();
    if (activeTab === "conversion") loadAuditHistory();
  }, [activeTab, leadId]);

  const loadActivities = async () => {
    setActivitiesLoading(true);
    try {
      const data = await listActivitiesByLead(leadId);
      setActivities(Array.isArray(data) ? data : []);
    } catch {
      toast.error("Failed to load activities");
    } finally {
      setActivitiesLoading(false);
    }
  };

  const loadOpportunities = async () => {
    setOppsLoading(true);
    try {
      const opps = await getOpportunitiesByLead(leadId);
      setOpportunities(opps);
      // Load project links for each opportunity
      const projectMap = {};
      for (const opp of opps) {
        try {
          const projData = await getProjectByOpportunity(opp.id);
          projectMap[opp.id] = projData;
        } catch {
          projectMap[opp.id] = { linked: false };
        }
      }
      setOppProjects(projectMap);
    } catch {
      toast.error("Failed to load opportunities");
    } finally {
      setOppsLoading(false);
    }
  };

  const loadAuditHistory = async () => {
    setAuditLoading(true);
    try {
      const data = await getEntityAuditHistory("LEAD", leadId);
      setAuditEvents(Array.isArray(data) ? data : []);
    } catch {
      toast.error("Failed to load conversion history");
    } finally {
      setAuditLoading(false);
    }
  };

  // ΓöÇΓöÇΓöÇ Profile Edit ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  const setEdit = (field) => (e) => setEditForm({ ...editForm, [field]: e.target.value });

  const handleSaveProfile = async () => {
    if (!editForm.firstName.trim()) {
      toast.error("First name is required");
      return;
    }
    setSaving(true);
    try {
      await updateLead(leadId, editForm);
      toast.success("Lead updated");
      setEditing(false);
      loadLead();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to update lead");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm("Delete this lead? This cannot be undone.")) return;
    try {
      await deleteLead(leadId);
      toast.success("Lead deleted");
      navigate(`/w/${workspaceSlug}/crm/leads`, { replace: true });
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to delete lead");
    }
  };

  const handleConvert = async () => {
    if (!window.confirm("Convert this lead to a customer? This will create a Customer and Contact.")) return;
    setConverting(true);
    try {
      await convertLead(leadId);
      toast.success("Lead converted! Customer and contact created.");
      loadLead();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to convert lead");
    } finally {
      setConverting(false);
    }
  };

  // ΓöÇΓöÇΓöÇ Activity ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  const handleCreateActivity = async (e) => {
    e.preventDefault();
    if (!activityForm.subject.trim()) {
      toast.error("Subject is required");
      return;
    }
    setActivitySaving(true);
    try {
      await createActivity({ ...activityForm, leadId: Number(leadId) });
      toast.success("Activity logged");
      setShowActivityForm(false);
      setActivityForm({ type: "CALL", subject: "", description: "" });
      loadActivities();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to create activity");
    } finally {
      setActivitySaving(false);
    }
  };

  const handleDeleteActivity = async (id) => {
    if (!window.confirm("Delete this activity?")) return;
    try {
      await deleteActivity(id);
      toast.success("Activity deleted");
      loadActivities();
    } catch (err) {
      toast.error(err.message || "Failed to delete activity");
    }
  };

  // ΓöÇΓöÇΓöÇ Notes ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  const handleSaveNotes = async () => {
    setNotesSaving(true);
    try {
      await updateLead(leadId, { notes: notesText });
      toast.success("Notes saved");
      loadLead();
    } catch (err) {
      toast.error(err.message || "Failed to save notes");
    } finally {
      setNotesSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
      </div>
    );
  }

  if (!lead) return null;

  const isConverted = lead.status === "CONVERTED";

  return (
    <div className="mx-auto max-w-6xl px-4 py-8">
      {/* Header */}
      <div className="mb-6">
        <button
          onClick={() => navigate(`/w/${workspaceSlug}/crm/leads`)}
          className="mb-4 flex items-center gap-1.5 text-sm text-slate-500 hover:text-slate-700"
        >
          <ArrowLeft className="h-4 w-4" /> Back to Leads
        </button>

        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div className="flex items-center gap-4">
            <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-600 text-xl font-bold text-white shadow-lg shadow-blue-500/20">
              {lead.firstName?.charAt(0)?.toUpperCase() || "L"}
            </div>
            <div>
              <h1 className="text-2xl font-bold text-slate-800">
                {lead.firstName} {lead.lastName}
              </h1>
              <div className="mt-1 flex items-center gap-3">
                <span className={`rounded-full px-2.5 py-0.5 text-xs font-medium ${STATUS_BADGE[lead.status] || "bg-slate-100 text-slate-600"}`}>
                  {lead.status?.replace("_", " ")}
                </span>
                {lead.company && (
                  <span className="text-sm text-slate-500">{lead.company}</span>
                )}
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            {!isConverted && hasPermission?.("LEAD_CONVERT") && (
              <button
                onClick={handleConvert}
                disabled={converting}
                className="flex items-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
              >
                {converting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Sparkles className="h-4 w-4" />}
                Convert to Customer
              </button>
            )}
            {!isConverted && hasPermission?.("LEAD_UPDATE") && (
              <button
                onClick={() => setEditing(!editing)}
                className="flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 hover:bg-slate-50"
              >
                <Edit2 className="h-4 w-4" /> Edit
              </button>
            )}
            {hasPermission?.("LEAD_DELETE") && (
              <button
                onClick={handleDelete}
                className="flex items-center gap-2 rounded-xl border border-rose-200 bg-white px-4 py-2.5 text-sm font-medium text-rose-600 hover:bg-rose-50"
              >
                <Trash2 className="h-4 w-4" /> Delete
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="mb-6 flex gap-1 overflow-x-auto border-b border-slate-200">
        {TABS.map((tab) => {
          const Icon = tab.icon;
          return (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 whitespace-nowrap border-b-2 px-4 py-3 text-sm font-medium transition ${
                activeTab === tab.id
                  ? "border-indigo-600 text-indigo-600"
                  : "border-transparent text-slate-500 hover:text-slate-700"
              }`}
            >
              <Icon className="h-4 w-4" />
              {tab.label}
            </button>
          );
        })}
      </div>

      {/* ΓöÇΓöÇΓöÇ Profile Tab ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ */}
      {activeTab === "profile" && (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          {/* Main Info */}
          <div className="lg:col-span-2 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="mb-4 flex items-center justify-between">
              <h2 className="text-lg font-semibold text-slate-800">Lead Information</h2>
              {!editing && !isConverted && hasPermission?.("LEAD_UPDATE") && (
                <button onClick={() => setEditing(true)} className="text-sm text-indigo-600 hover:text-indigo-700">
                  Edit
                </button>
              )}
            </div>

            {editing ? (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">First Name *</label>
                    <input value={editForm.firstName} onChange={setEdit("firstName")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Last Name</label>
                    <input value={editForm.lastName} onChange={setEdit("lastName")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
                    <input type="email" value={editForm.email} onChange={setEdit("email")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Phone</label>
                    <input value={editForm.phone} onChange={setEdit("phone")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Company</label>
                    <input value={editForm.company} onChange={setEdit("company")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Job Title</label>
                    <input value={editForm.jobTitle} onChange={setEdit("jobTitle")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Source</label>
                    <select value={editForm.source} onChange={setEdit("source")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none">
                      {SOURCES.map((s) => <option key={s} value={s}>{s.replace("_", " ")}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Status</label>
                    <select value={editForm.status} onChange={setEdit("status")} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none">
                      {STATUSES.map((s) => <option key={s} value={s}>{s.replace("_", " ")}</option>)}
                    </select>
                  </div>
                </div>
                <div className="flex justify-end gap-3 border-t border-slate-200 pt-4">
                  <button onClick={() => { setEditing(false); setEditForm({ firstName: lead.firstName || "", lastName: lead.lastName || "", email: lead.email || "", phone: lead.phone || "", company: lead.company || "", jobTitle: lead.jobTitle || "", source: lead.source || "OTHER", status: lead.status || "NEW" }); }} className="rounded-xl px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100">
                    Cancel
                  </button>
                  <button onClick={handleSaveProfile} disabled={saving} className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50">
                    {saving && <Loader2 className="h-4 w-4 animate-spin" />}
                    Save Changes
                  </button>
                </div>
              </div>
            ) : (
              <div className="space-y-4">
                <InfoRow icon={User} label="Full Name" value={`${lead.firstName} ${lead.lastName}`} />
                <InfoRow icon={Mail} label="Email" value={lead.email} copyable />
                <InfoRow icon={Phone} label="Phone" value={lead.phone} copyable />
                <InfoRow icon={Building2} label="Company" value={lead.company} />
                <InfoRow icon={Briefcase} label="Job Title" value={lead.jobTitle} />
                <InfoRow icon={Tag} label="Source" value={lead.source?.replace("_", " ")} badge={SOURCE_BADGE[lead.source]} />
              </div>
            )}
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Status Card */}
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <h3 className="mb-3 text-sm font-semibold text-slate-500 uppercase tracking-wider">Status</h3>
              <span className={`inline-flex rounded-full px-3 py-1.5 text-sm font-medium ${STATUS_BADGE[lead.status] || "bg-slate-100 text-slate-600"}`}>
                {lead.status?.replace("_", " ")}
              </span>
              {isConverted && (
                <div className="mt-3 rounded-xl bg-purple-50 p-3">
                  <p className="text-xs font-medium text-purple-700">Converted</p>
                  {lead.convertedAt && <p className="text-xs text-purple-500">{formatDate(lead.convertedAt)}</p>}
                  {lead.convertedCustomerId && (
                    <p className="mt-1 text-xs text-purple-600">
                      Customer ID: {lead.convertedCustomerId}
                    </p>
                  )}
                </div>
              )}
            </div>

            {/* Quick Stats */}
            <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
              <h3 className="mb-3 text-sm font-semibold text-slate-500 uppercase tracking-wider">Details</h3>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-slate-500">Created</span>
                  <span className="text-slate-700">{formatDate(lead.createdAt)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Last Updated</span>
                  <span className="text-slate-700">{formatDate(lead.updatedAt)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Activities</span>
                  <span className="text-slate-700">{activities.length || "-"}</span>
                </div>
              </div>
            </div>

            {/* Converted Customer Link */}
            {isConverted && lead.convertedCustomerId && (
              <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
                <h3 className="mb-3 text-sm font-semibold text-slate-500 uppercase tracking-wider">Converted To</h3>
                <button
                  onClick={() => navigate(`/w/${workspaceSlug}/crm/customers`)}
                  className="flex w-full items-center gap-3 rounded-xl border border-slate-100 p-3 text-left transition hover:bg-slate-50"
                >
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-gradient-to-br from-emerald-500 to-teal-600 text-sm font-bold text-white">
                    C
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-sm font-medium text-slate-800">Customer #{lead.convertedCustomerId}</p>
                    <p className="text-xs text-slate-400">View customer record</p>
                  </div>
                  <ExternalLink className="h-4 w-4 text-slate-400" />
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ΓöÇΓöÇΓöÇ Activities Tab ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ */}
      {activeTab === "activities" && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Activity Timeline</h2>
            {!isConverted && (
              <button
                onClick={() => setShowActivityForm(true)}
                className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
              >
                <Plus className="h-4 w-4" /> Log Activity
              </button>
            )}
          </div>

          {activitiesLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-indigo-600" />
            </div>
          ) : activities.length === 0 ? (
            <div className="rounded-2xl border border-slate-200 bg-white py-12 text-center">
              <Clock className="mx-auto mb-3 h-10 w-10 text-slate-300" />
              <p className="text-slate-500">No activities logged yet</p>
            </div>
          ) : (
            <div className="space-y-4">
              {activities.map((act) => {
                const Icon = ACTIVITY_ICONS[act.type] || Clock;
                return (
                  <div key={act.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                    <div className="flex items-start gap-4">
                      <div className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br ${ACTIVITY_COLORS[act.type] || "from-slate-500 to-gray-600"} text-white`}>
                        <Icon className="h-5 w-5" />
                      </div>
                      <div className="min-w-0 flex-1">
                        <div className="flex items-start justify-between">
                          <div>
                            <p className="font-medium text-slate-800">{act.subject}</p>
                            <p className="mt-0.5 text-xs text-slate-400">
                              {act.type} &middot; {formatDateTime(act.activityDate)}
                            </p>
                          </div>
                          <button onClick={() => handleDeleteActivity(act.id)} className="rounded-lg p-1 text-slate-400 hover:bg-rose-50 hover:text-rose-500">
                            <Trash2 className="h-3.5 w-3.5" />
                          </button>
                        </div>
                        {act.description && (
                          <p className="mt-2 text-sm text-slate-600">{act.description}</p>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {/* Activity Form Modal */}
          {showActivityForm && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
              <div className="w-full max-w-lg rounded-2xl bg-white shadow-xl">
                <div className="flex items-center justify-between border-b border-slate-200 px-6 py-4">
                  <h3 className="text-lg font-semibold text-slate-800">Log Activity</h3>
                  <button onClick={() => setShowActivityForm(false)} className="rounded-lg p-1 text-slate-400 hover:bg-slate-100">
                    <X className="h-5 w-5" />
                  </button>
                </div>
                <form onSubmit={handleCreateActivity} className="px-6 py-4 space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Type</label>
                    <select value={activityForm.type} onChange={(e) => setActivityForm({ ...activityForm, type: e.target.value })} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none">
                      {ACTIVITY_TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Subject *</label>
                    <input value={activityForm.subject} onChange={(e) => setActivityForm({ ...activityForm, subject: e.target.value })} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" placeholder="e.g. Follow-up call" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
                    <textarea value={activityForm.description} onChange={(e) => setActivityForm({ ...activityForm, description: e.target.value })} rows={3} className="w-full rounded-xl border border-slate-200 px-3 py-2.5 text-sm focus:border-indigo-500 focus:outline-none" placeholder="Details about this activity..." />
                  </div>
                  <div className="flex justify-end gap-3 border-t border-slate-200 pt-4">
                    <button type="button" onClick={() => setShowActivityForm(false)} className="rounded-xl px-4 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100">Cancel</button>
                    <button type="submit" disabled={activitySaving} className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50">
                      {activitySaving && <Loader2 className="h-4 w-4 animate-spin" />}
                      Save Activity
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
        </div>
      )}

      {/* ΓöÇΓöÇΓöÇ Notes Tab ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ */}
      {activeTab === "notes" && (
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Notes</h2>
            <button
              onClick={handleSaveNotes}
              disabled={notesSaving}
              className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-50"
            >
              {notesSaving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
              Save Notes
            </button>
          </div>
          <textarea
            value={notesText}
            onChange={(e) => setNotesText(e.target.value)}
            rows={12}
            className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm leading-relaxed focus:border-indigo-500 focus:outline-none"
            placeholder="Add notes about this lead... interactions, requirements, follow-up plans..."
          />
          <p className="mt-2 text-xs text-slate-400">
            Last updated: {lead.updatedAt ? formatDateTime(lead.updatedAt) : "Never"}
          </p>
        </div>
      )}

      {/* ΓöÇΓöÇΓöÇ Opportunities Tab ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ */}
      {activeTab === "opportunities" && (
        <div>
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Linked Opportunities</h2>
            <button
              onClick={() => navigate(`/w/${workspaceSlug}/crm/opportunities`)}
              className="flex items-center gap-1.5 text-sm text-indigo-600 hover:text-indigo-700"
            >
              View All <ExternalLink className="h-3.5 w-3.5" />
            </button>
          </div>

          {oppsLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-indigo-600" />
            </div>
          ) : opportunities.length === 0 ? (
            <div className="rounded-2xl border border-slate-200 bg-white py-12 text-center">
              <Briefcase className="mx-auto mb-3 h-10 w-10 text-slate-300" />
              <p className="text-slate-500">No opportunities linked to this lead</p>
            </div>
          ) : (
            <div className="space-y-4">
              {opportunities.map((opp) => {
                const projInfo = oppProjects[opp.id];
                const isWon = opp.stage?.isWon || opp.stageName === "Won";
                const isLost = opp.stage?.isLost || opp.stageName === "Lost";

                return (
                  <div key={opp.id} className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="font-semibold text-slate-800">{opp.title}</p>
                        <div className="mt-1 flex items-center gap-3">
                          {opp.value != null && (
                            <span className="text-sm font-semibold text-indigo-600">
                              ${Number(opp.value).toLocaleString()}
                            </span>
                          )}
                          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STAGE_COLORS[opp.stage?.name || opp.stageName] || "bg-slate-100 text-slate-600"}`}>
                            {opp.stage?.name || opp.stageName || "Unknown"}
                          </span>
                          {isWon && <CheckCircle2 className="h-4 w-4 text-emerald-500" />}
                          {isLost && <XCircle className="h-4 w-4 text-rose-500" />}
                        </div>
                        {opp.notes && <p className="mt-2 text-sm text-slate-500 line-clamp-2">{opp.notes}</p>}
                      </div>
                      <button
                        onClick={() => navigate(`/w/${workspaceSlug}/crm/opportunities`)}
                        className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                      >
                        <ExternalLink className="h-4 w-4" />
                      </button>
                    </div>

                    {/* Project Link */}
                    {isWon && projInfo && (
                      <div className={`mt-3 rounded-xl p-3 ${projInfo.linked ? "bg-emerald-50 border border-emerald-200" : "bg-slate-50 border border-slate-200"}`}>
                        <div className="flex items-center gap-2">
                          <ArrowRight className="h-4 w-4 text-emerald-600" />
                          <span className="text-xs font-semibold text-emerald-700 uppercase tracking-wider">Project</span>
                        </div>
                        {projInfo.linked ? (
                          <div className="mt-2 flex items-center justify-between">
                            <div>
                              <p className="text-sm font-medium text-slate-800">{projInfo.projectName}</p>
                              <div className="flex items-center gap-2 mt-0.5">
                                {projInfo.projectCategory && (
                                  <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700">
                                    {projInfo.projectCategory}
                                  </span>
                                )}
                                {projInfo.budget != null && (
                                  <span className="text-xs text-slate-500">
                                    Budget: ${Number(projInfo.budget).toLocaleString()}
                                  </span>
                                )}
                              </div>
                            </div>
                            <button
                              onClick={() => navigate(`/w/${workspaceSlug}/projects/${projInfo.projectId}`)}
                              className="flex items-center gap-1.5 rounded-lg bg-emerald-600 px-3 py-1.5 text-xs font-medium text-white hover:bg-emerald-700"
                            >
                              View Project <ExternalLink className="h-3 w-3" />
                            </button>
                          </div>
                        ) : (
                          <p className="mt-1 text-xs text-slate-500">No project linked yet</p>
                        )}
                      </div>
                    )}

                    {/* Opp -> Project arrow for non-won */}
                    {!isWon && !isLost && (
                      <div className="mt-3 flex items-center gap-2 text-xs text-slate-400">
                        <ArrowRight className="h-3 w-3" />
                        <span>Mark as Won to auto-create a project</span>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ΓöÇΓöÇΓöÇ Conversion History Tab ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ */}
      {activeTab === "conversion" && (
        <div>
          <h2 className="mb-4 text-lg font-semibold text-slate-800">Conversion History</h2>

          {auditLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-indigo-600" />
            </div>
          ) : auditEvents.length === 0 ? (
            <div className="rounded-2xl border border-slate-200 bg-white py-12 text-center">
              <History className="mx-auto mb-3 h-10 w-10 text-slate-300" />
              <p className="text-slate-500">No audit events recorded for this lead</p>
            </div>
          ) : (
            <div className="relative">
              <div className="absolute left-5 top-0 bottom-0 w-0.5 bg-slate-200" />
              <div className="space-y-6">
                {auditEvents.map((event, idx) => (
                  <div key={event.id || idx} className="relative flex gap-4">
                    <div className={`relative z-10 flex h-10 w-10 shrink-0 items-center justify-center rounded-full ${
                      event.action === "LEAD_CONVERTED"
                        ? "bg-gradient-to-br from-emerald-500 to-teal-600"
                        : event.action === "CREATED"
                        ? "bg-gradient-to-br from-blue-500 to-indigo-600"
                        : event.action === "DELETED"
                        ? "bg-gradient-to-br from-rose-500 to-red-600"
                        : "bg-gradient-to-br from-amber-500 to-orange-600"
                    } text-white shadow-lg`}>
                      {event.action === "LEAD_CONVERTED" ? (
                        <CheckCircle2 className="h-5 w-5" />
                      ) : event.action === "CREATED" ? (
                        <Plus className="h-5 w-5" />
                      ) : event.action === "DELETED" ? (
                        <XCircle className="h-5 w-5" />
                      ) : (
                        <Edit2 className="h-5 w-5" />
                      )}
                    </div>
                    <div className="flex-1 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                      <div className="flex items-start justify-between">
                        <div>
                          <p className="font-medium text-slate-800">
                            {event.action?.replace(/_/g, " ")}
                          </p>
                          <p className="mt-0.5 text-xs text-slate-400">
                            by {event.actorEmail || "System"} &middot; {formatDateTime(event.timestamp || event.createdAt)}
                          </p>
                        </div>
                      </div>
                      {event.newValue && (
                        <pre className="mt-2 rounded-lg bg-slate-50 p-2 text-xs text-slate-600 overflow-x-auto">
                          {typeof event.newValue === "string" ? event.newValue : JSON.stringify(event.newValue, null, 2)}
                        </pre>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function InfoRow({ icon: Icon, label, value, badge, copyable }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = () => {
    if (!value) return;
    navigator.clipboard.writeText(value);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="flex items-center gap-3">
      <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100">
        <Icon className="h-4 w-4 text-slate-500" />
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-xs text-slate-400">{label}</p>
        {badge ? (
          <span className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${badge}`}>
            {value || "-"}
          </span>
        ) : (
          <p className="text-sm font-medium text-slate-800">{value || "-"}</p>
        )}
      </div>
      {copyable && value && (
        <button onClick={handleCopy} className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600">
          {copied ? <CheckCircle2 className="h-4 w-4 text-emerald-500" /> : <Copy className="h-4 w-4" />}
        </button>
      )}
    </div>
  );
}
