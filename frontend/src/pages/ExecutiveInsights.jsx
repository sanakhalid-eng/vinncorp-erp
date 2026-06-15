import { useEffect, useState } from "react";
import { BarChart3, Loader2, RefreshCw } from "lucide-react";
import { toast } from "sonner";
import { useWorkspace } from "../context/WorkspaceContext";
import {
  captureExecutiveSnapshot,
  getActivityIntelligence,
  getExecutiveDashboard,
  getExecutiveTrends,
  getPersonalProductivity,
} from "../api/notesApi";

export default function ExecutiveInsights() {
  const { workspace } = useWorkspace();
  const [loading, setLoading] = useState(true);
  const [dashboard, setDashboard] = useState(null);
  const [activity, setActivity] = useState(null);
  const [personal, setPersonal] = useState(null);
  const [trends, setTrends] = useState([]);

  const load = async () => {
    if (!workspace?.id) return;
    setLoading(true);
    try {
      const [dashRes, actRes, prodRes, trendRes] = await Promise.all([
        getExecutiveDashboard(),
        getActivityIntelligence({ days: 7 }),
        getPersonalProductivity(),
        getExecutiveTrends(0, 5),
      ]);
      setDashboard(dashRes.data.data);
      setActivity(actRes.data.data);
      setPersonal(prodRes.data.data);
      setTrends(trendRes.data.data?.content || []);
    } catch {
      toast.error("Failed to load insights");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [workspace?.id]);

  const handleSnapshot = async () => {
    try {
      await captureExecutiveSnapshot();
      toast.success("Snapshot saved");
      load();
    } catch {
      toast.error("Could not capture snapshot");
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
            <BarChart3 className="h-7 w-7 text-indigo-600" />
            Executive & Productivity Insights
          </h1>
          <p className="text-slate-500 text-sm mt-1">
            Workspace delivery and personal productivity
          </p>
        </div>
        <button
          type="button"
          onClick={handleSnapshot}
          className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700"
        >
          <RefreshCw className="h-4 w-4" />
          Capture snapshot
        </button>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label="Active projects" value={dashboard?.activeProjects ?? 0} />
        <Stat label="At-risk projects" value={dashboard?.atRiskProjects ?? 0} />
        <Stat
          label="Avg velocity"
          value={(dashboard?.averageVelocity ?? 0).toFixed(1)}
        />
        <Stat
          label="Predictability %"
          value={(dashboard?.deliveryPredictability ?? 0).toFixed(0)}
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card title="Activity intelligence (7d)">
          <ul className="text-sm text-slate-600 space-y-1">
            {(activity?.highlights || []).map((h) => (
              <li key={h}>• {h}</li>
            ))}
          </ul>
        </Card>
        <Card title="My productivity">
          <p className="text-sm text-slate-600">
            Completed this week: 
            <strong>{personal?.tasksCompletedThisWeek ?? 0}</strong>
          </p>
          <p className="text-sm text-slate-600 mt-1">
            Due this week: <strong>{personal?.tasksDueThisWeek ?? 0}</strong>
          </p>
          <p className="text-sm text-slate-600 mt-1">
            Overdue: <strong>{personal?.overdueTasks ?? 0}</strong>
          </p>
          <p className="text-sm text-slate-600 mt-1">
            Focus score: 
            <strong>{(personal?.focusScore ?? 0).toFixed(0)}%</strong>
          </p>
        </Card>
      </div>

      {trends.length > 0 && (
        <Card title="Recent executive snapshots">
          <ul className="text-sm text-slate-600 divide-y">
            {trends.map((t) => (
              <li key={t.id} className="py-2">
                {String(t.capturedAt)} — {t.metrics?.activeProjects ?? "—"} 
                active projects
              </li>
            ))}
          </ul>
        </Card>
      )}
    </div>
  );
}

function Stat({ label, value }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
      <p className="text-xs font-medium text-slate-500">{label}</p>
      <p className="text-2xl font-bold text-slate-900 mt-1">{value}</p>
    </div>
  );
}

function Card({ title, children }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <h2 className="font-semibold text-slate-900 mb-3">{title}</h2>
      {children}
    </div>
  );
}
