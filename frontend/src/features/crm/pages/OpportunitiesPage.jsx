import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Plus,
  Search,
  Edit2,
  Trash2,
  DollarSign,
  CheckCircle2,
  XCircle,
  ExternalLink,
} from "lucide-react";
import { toast } from "sonner";
import {
  listOpportunities,
  deleteOpportunity,
  markOpportunityWon,
  markOpportunityLost,
  listPipelines,
  listPipelineStages,
  getProjectByOpportunity,
} from "../api/crmApi";
import OpportunityFormModal from "../components/OpportunityFormModal";
import { TableRowSkeleton } from "../../../components/LoadingSkeleton";
import EmptyState from "../../../components/EmptyState";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";

const STAGE_COLORS = {
  New: "bg-blue-100 text-blue-700",
  Qualified: "bg-cyan-100 text-cyan-700",
  Proposal: "bg-amber-100 text-amber-700",
  Negotiation: "bg-orange-100 text-orange-700",
  Won: "bg-emerald-100 text-emerald-700",
  Lost: "bg-rose-100 text-rose-700",
};

export default function OpportunitiesPage() {
  const navigate = useNavigate();
  const [opportunities, setOpportunities] = useState([]);
  const [pipelines, setPipelines] = useState([]);
  const [stages, setStages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [projectLinks, setProjectLinks] = useState({});
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);
  const [confirmTitle, setConfirmTitle] = useState("");
  const [confirmMessage, setConfirmMessage] = useState("");

  // Extract workspaceSlug from URL
  const workspaceSlug = window.location.pathname.split("/")[2] || "workspace";

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const [opps, pipes] = await Promise.all([
        listOpportunities(0, 100),
        listPipelines(),
      ]);
      const oppsList = Array.isArray(opps) ? opps : opps?.content || [];
      setOpportunities(oppsList);
      setPipelines(pipes);
      if (pipes.length > 0) {
        const stgs = await listPipelineStages(pipes[0].id);
        setStages(stgs);
      }
      // Load project links for won opportunities
      const links = {};
      for (const opp of oppsList) {
        const isWon = opp.stage?.isWon || opp.stageName === "Won";
        if (isWon) {
          try {
            const projData = await getProjectByOpportunity(opp.id);
            links[opp.id] = projData;
          } catch {
            links[opp.id] = { linked: false };
          }
        }
      }
      setProjectLinks(links);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return opportunities.filter((o) => {
      if (!q) return true;
      return (
        o.title?.toLowerCase().includes(q) ||
        o.contactName?.toLowerCase().includes(q) ||
        o.customerName?.toLowerCase().includes(q)
      );
    });
  }, [opportunities, search]);

  const handleDelete = (id) => {
    setConfirmTitle("Delete Opportunity");
    setConfirmMessage("Are you sure you want to delete this opportunity?");
    setConfirmAction(() => async () => {
      try {
        await deleteOpportunity(id);
        toast.success("Opportunity deleted");
        loadData();
      } catch (e) {
        toast.error(e.message || "Failed to delete opportunity");
      }
    });
    setShowConfirmDialog(true);
  };

  const handleWon = (id) => {
    setConfirmTitle("Mark as Won");
    setConfirmMessage("Mark this deal as WON? This will create a project.");
    setConfirmAction(() => async () => {
      try {
        await markOpportunityWon(id);
        toast.success("Deal marked as Won! Project created.");
        loadData();
      } catch (e) {
        toast.error(e.message || "Failed to mark as won");
      }
    });
    setShowConfirmDialog(true);
  };

  const handleLost = (id) => {
    setConfirmTitle("Mark as Lost");
    setConfirmMessage("Mark this deal as LOST?");
    setConfirmAction(() => async () => {
      try {
        await markOpportunityLost(id, "Marked as lost");
        toast.success("Deal marked as Lost");
        loadData();
      } catch (e) {
        toast.error(e.message || "Failed to mark as lost");
      }
    });
    setShowConfirmDialog(true);
  };

  const getStageName = (stage) => {
    if (!stage) return "N/A";
    return stage.name || stages.find(s => s.id === stage.id)?.name || "Unknown";
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-800">Opportunities</h1>
          <p className="text-slate-500">{filtered.length} opportunities</p>
        </div>
        <button
          onClick={() => { setEditing(null); setShowModal(true); }}
          className="flex items-center gap-2 rounded-xl bg-indigo-600 px-4 py-2.5 text-sm font-medium text-white hover:bg-indigo-700"
        >
          <Plus className="h-4 w-4" /> New Opportunity
        </button>
      </div>

      <div className="mb-6">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search opportunities..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full rounded-xl border border-slate-200 bg-white py-2.5 pl-10 pr-4 text-sm focus:border-indigo-500 focus:outline-none"
          />
        </div>
      </div>

      {loading ? (
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                {["Title", "Value", "Stage", "Project", "Actions"].map((h) => (
                  <th key={h} className="px-4 py-3 font-medium text-slate-600">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {Array.from({ length: 5 }).map((_, i) => <TableRowSkeleton key={i} columns={5} />)}
            </tbody>
          </table>
        </div>
      ) : error ? (
        <ErrorState error={error} onRetry={loadData} />
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={DollarSign}
          title="No opportunities yet"
          description="Track deals through your pipeline from first contact to close."
          action={{ label: "New Opportunity", icon: Plus, onClick: () => { setEditing(null); setShowModal(true); } }}
        />
      ) : (
        <div className="overflow-x-auto overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                <th className="px-4 py-3 font-medium text-slate-600">Title</th>
                <th className="px-4 py-3 font-medium text-slate-600">Value</th>
                <th className="px-4 py-3 font-medium text-slate-600">Stage</th>
                <th className="px-4 py-3 font-medium text-slate-600">Project</th>
                <th className="px-4 py-3 font-medium text-slate-600">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((opp) => {
                const isWon = opp.stage?.isWon || getStageName(opp.stage) === "Won";
                const projInfo = projectLinks[opp.id];
                return (
                <tr key={opp.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-800">{opp.title}</td>
                  <td className="px-4 py-3 text-slate-600">
                    {opp.value != null ? `$${Number(opp.value).toLocaleString()}` : "-"}
                  </td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-1 text-xs font-medium ${STAGE_COLORS[getStageName(opp.stage)] || "bg-slate-100 text-slate-600"}`}>
                      {getStageName(opp.stage)}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {isWon && projInfo?.linked ? (
                      <button
                        onClick={() => navigate(`/w/${workspaceSlug}/projects/${projInfo.projectId}`)}
                        className="flex items-center gap-1.5 rounded-lg bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 hover:bg-emerald-100"
                      >
                        <CheckCircle2 className="h-3 w-3" />
                        {projInfo.projectName || "View Project"}
                        <ExternalLink className="h-3 w-3" />
                      </button>
                    ) : isWon ? (
                      <span className="text-xs text-slate-400 italic">No project</span>
                    ) : (
                      <span className="text-xs text-slate-300">-</span>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      {!isWon && opp.stageName !== "Lost" && (
                        <>
                          <button
                            onClick={() => handleWon(opp.id)}
                            title="Mark Won"
                            className="rounded-lg p-1.5 text-emerald-500 hover:bg-emerald-50"
                          >
                            <CheckCircle2 className="h-4 w-4" />
                          </button>
                          <button
                            onClick={() => handleLost(opp.id)}
                            title="Mark Lost"
                            className="rounded-lg p-1.5 text-rose-500 hover:bg-rose-50"
                          >
                            <XCircle className="h-4 w-4" />
                          </button>
                        </>
                      )}
                      <button
                        onClick={() => { setEditing(opp); setShowModal(true); }}
                        className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
                      >
                        <Edit2 className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(opp.id)}
                        className="rounded-lg p-1.5 text-slate-400 hover:bg-rose-50 hover:text-rose-600"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {showModal && (
        <OpportunityFormModal
          opportunity={editing}
          pipelines={pipelines}
          stages={stages}
          onClose={() => { setShowModal(false); setEditing(null); }}
          onSaved={() => { setShowModal(false); setEditing(null); loadData(); }}
        />
      )}

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => setShowConfirmDialog(false)}
        onConfirm={() => {
          if (confirmAction) confirmAction();
          setShowConfirmDialog(false);
        }}
        title={confirmTitle}
        message={confirmMessage}
        confirmText="Confirm"
      />
    </div>
  );
}
