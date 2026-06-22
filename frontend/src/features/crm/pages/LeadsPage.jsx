import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Plus,
  Search,
  Phone,
  Mail,
  Edit2,
  Trash2,
  ArrowRight,
  ArrowUpRight,
  ExternalLink,
} from "lucide-react";
import { toast } from "sonner";
import { listLeads, deleteLead, convertLead } from "../api/crmApi";
import LeadFormModal from "../components/LeadFormModal";
import { TableRowSkeleton } from "../../../components/LoadingSkeleton";
import ErrorState from "../../../components/ErrorState";
import EmptyState from "../../../components/EmptyState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";
import { parseApiError } from "../../../utils/apiError";
import Can from "../../../components/auth/Can";
import { LEAD_CREATE, LEAD_UPDATE, LEAD_DELETE, LEAD_CONVERT } from "../../../constants/permissions";

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

export default function LeadsPage() {
  const navigate = useNavigate();
  const [leads, setLeads] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [converting, setConverting] = useState(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmDeleteId, setConfirmDeleteId] = useState(null);

  useEffect(() => {
    loadLeads();
  }, []);

  const loadLeads = async () => {
    setLoading(true);
    try {
      const res = await listLeads(0, 100);
      setLeads(Array.isArray(res) ? res : res?.content || []);
      setError(null);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return leads.filter((l) => {
      if (statusFilter && l.status !== statusFilter) return false;
      if (!q) return true;
      return (
        l.firstName?.toLowerCase().includes(q) ||
        l.lastName?.toLowerCase().includes(q) ||
        l.email?.toLowerCase().includes(q) ||
        l.company?.toLowerCase().includes(q)
      );
    });
  }, [leads, search, statusFilter]);

  const handleDelete = async (id) => {
    setConfirmDeleteId(id);
    setShowConfirmDialog(true);
  };

  const confirmDelete = async () => {
    if (!confirmDeleteId) return;
    try {
      await deleteLead(confirmDeleteId);
      toast.success("Lead deleted");
      loadLeads();
    } catch (e) {
      toast.error(e.message || "Failed to delete lead");
    } finally {
      setShowConfirmDialog(false);
      setConfirmDeleteId(null);
    }
  };

  const handleConvert = async (id) => {
    setConverting(id);
    try {
      await convertLead(id);
      toast.success("Lead converted to customer & contact");
      loadLeads();
    } catch (e) {
      toast.error(e.message || "Failed to convert lead");
    } finally {
      setConverting(null);
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Leads</h1>
          <p className="text-slate-500">{filtered.length} leads</p>
        </div>
        <Can permission={LEAD_CREATE}>
          <button
            onClick={() => { setEditing(null); setShowModal(true); }}
            className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700"
          >
            <Plus className="h-4 w-4" /> New Lead
          </button>
        </Can>
      </div>

      {/* Filters */}
      <div className="mb-6 flex flex-col gap-3 sm:flex-row">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search leads..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm focus:border-indigo-500 focus:outline-none"
        >
          <option value="">All Statuses</option>
          <option value="NEW">New</option>
          <option value="CONTACTED">Contacted</option>
          <option value="QUALIFIED">Qualified</option>
          <option value="UNQUALIFIED">Unqualified</option>
          <option value="CONVERTED">Converted</option>
          <option value="LOST">Lost</option>
        </select>
      </div>

      {loading ? (
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                {["Name", "Company", "Email", "Status", "Source", "Actions"].map((h) => (
                  <th key={h} className="px-4 py-3 font-medium text-slate-600">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {Array.from({ length: 5 }).map((_, i) => <TableRowSkeleton key={i} columns={6} />)}
            </tbody>
          </table>
        </div>
      ) : error ? (
        <ErrorState error={error} onRetry={loadLeads} />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={Phone}
          title="No leads found"
          description="Try adjusting your search or filters, or create a new lead."
          action={{ label: "New Lead", icon: Plus, onClick: () => { setEditing(null); setShowModal(true); } }}
        />
      ) : (
        <div className="overflow-x-auto overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                <th className="px-4 py-3 font-medium text-slate-600">Name</th>
                <th className="px-4 py-3 font-medium text-slate-600">Company</th>
                <th className="px-4 py-3 font-medium text-slate-600">Email</th>
                <th className="px-4 py-3 font-medium text-slate-600">Status</th>
                <th className="px-4 py-3 font-medium text-slate-600">Source</th>
                <th className="px-4 py-3 font-medium text-slate-600">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((lead) => (
                <tr key={lead.id} className="hover:bg-slate-50 cursor-pointer" onClick={() => navigate(`./${lead.id}`)}>
                  <td className="px-4 py-3 font-medium text-slate-800">
                    <div className="flex items-center gap-2">
                      {lead.firstName} {lead.lastName}
                      <ExternalLink className="h-3 w-3 text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity" />
                    </div>
                  </td>
                  <td className="px-4 py-3 text-slate-600">{lead.company || "-"}</td>
                  <td className="px-4 py-3 text-slate-600">{lead.email || "-"}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-1 text-xs font-medium ${STATUS_BADGE[lead.status] || "bg-slate-100 text-slate-600"}`}>
                      {lead.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-1 text-xs font-medium ${SOURCE_BADGE[lead.source] || "bg-slate-100 text-slate-600"}`}>
                      {lead.source || "N/A"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2" onClick={(e) => e.stopPropagation()}>
                      <Can permission={LEAD_CONVERT}>
                        {lead.status !== "CONVERTED" && lead.status !== "LOST" && (
                          <button
                            onClick={() => handleConvert(lead.id)}
                            disabled={converting === lead.id}
                            className="rounded-lg bg-emerald-50 px-2 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-100 disabled:opacity-50"
                          >
                            {converting === lead.id ? "Converting..." : "Convert"}
                          </button>
                        )}
                      </Can>
                      <Can permission={LEAD_UPDATE}>
                        <button
                          onClick={() => { setEditing(lead); setShowModal(true); }}
                          className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                        >
                          <Edit2 className="h-4 w-4" />
                        </button>
                      </Can>
                      <Can permission={LEAD_DELETE}>
                        <button
                          onClick={() => handleDelete(lead.id)}
                          className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                        >
                          <Trash2 className="h-4 w-4" />
                        </button>
                      </Can>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <LeadFormModal
          lead={editing}
          onClose={() => { setShowModal(false); setEditing(null); }}
          onSaved={() => { setShowModal(false); setEditing(null); loadLeads(); }}
        />
      )}

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setConfirmDeleteId(null); }}
        onConfirm={confirmDelete}
        title="Delete Lead"
        message="Are you sure you want to delete this lead?"
        confirmText="Delete"
      />
    </div>
  );
}
