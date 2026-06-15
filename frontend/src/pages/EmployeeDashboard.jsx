import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  User,
  FolderKanban,
  CheckCircle2,
  Clock,
  AlertTriangle,
  Loader2,
  ArrowRight,
} from "lucide-react";
import API from "../api/axios";

const StatCard = ({ icon: Icon, label, value, color }) => (
  <div className="flex items-center gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
    <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${color}`}>
      <Icon className="h-6 w-6 text-white" />
    </div>
    <div>
      <p className="text-sm text-slate-500">{label}</p>
      <p className="text-2xl font-bold text-slate-800">{value}</p>
    </div>
  </div>
);

const PRIORITY_COLORS = {
  CRITICAL: "bg-red-100 text-red-700",
  HIGH: "bg-orange-100 text-orange-700",
  MEDIUM: "bg-amber-100 text-amber-700",
  LOW: "bg-emerald-100 text-emerald-700",
};

export default function EmployeeDashboard() {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchDashboard();
  }, []);

  const fetchDashboard = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await API.get("/dashboard/employee-summary");
      setData(res.data.data);
    } catch (err) {
      console.error("Failed to load employee dashboard:", err);
      setError(err.response?.data?.message || "Failed to load dashboard");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="py-20 text-center">
        <p className="text-red-500">{error}</p>
        <button
          onClick={fetchDashboard}
          className="mt-4 rounded-lg bg-indigo-600 px-4 py-2 text-white hover:bg-indigo-700"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      {/* Profile Header */}
      <div className="mb-8 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex items-center gap-6">
          <div className="flex h-20 w-20 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-2xl font-bold text-white">
            {data?.fullName?.charAt(0)?.toUpperCase() || "E"}
          </div>
          <div>
            <h1 className="text-2xl font-bold text-slate-800">{data?.fullName || "Employee"}</h1>
            <p className="text-slate-500">{data?.jobTitle || "No job title"}</p>
            <div className="mt-1 flex items-center gap-4 text-sm text-slate-400">
              {data?.employeeCode && <span>Code: {data.employeeCode}</span>}
              {data?.departmentName && <span>Dept: {data.departmentName}</span>}
              {data?.designationName && <span>Level: {data.designationName}</span>}
            </div>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          icon={FolderKanban}
          label="My Projects"
          value={data?.myProjectsCount ?? 0}
          color="bg-gradient-to-br from-cyan-500 to-blue-600"
        />
        <StatCard
          icon={CheckCircle2}
          label="Completed Tasks"
          value={data?.completedTasks ?? 0}
          color="bg-gradient-to-br from-emerald-500 to-teal-600"
        />
        <StatCard
          icon={Clock}
          label="Pending Tasks"
          value={data?.pendingTasks ?? 0}
          color="bg-gradient-to-br from-amber-500 to-orange-600"
        />
        <StatCard
          icon={AlertTriangle}
          label="Overdue Tasks"
          value={data?.overdueTasks ?? 0}
          color="bg-gradient-to-br from-rose-500 to-red-600"
        />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Recent Projects */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">My Projects</h2>
          </div>
          <div className="space-y-3">
            {(data?.recentProjects ?? []).map((p) => (
              <div
                key={p.id}
                className="flex items-center justify-between rounded-xl border border-slate-100 p-3 transition hover:bg-slate-50"
              >
                <div>
                  <p className="font-medium text-slate-800">{p.name}</p>
                  <p className="text-xs text-slate-400">
                    {p.updatedAt ? new Date(p.updatedAt).toLocaleDateString() : ""}
                  </p>
                </div>
                <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
                  {p.status || "Unknown"}
                </span>
              </div>
            ))}
            {(!data?.recentProjects || data.recentProjects.length === 0) && (
              <p className="py-4 text-center text-slate-400">No projects assigned</p>
            )}
          </div>
        </div>

        {/* Recent Tasks */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Recent Tasks</h2>
          </div>
          <div className="space-y-3">
            {(data?.recentTasks ?? []).map((t) => (
              <div
                key={t.id}
                className="flex items-center justify-between rounded-xl border border-slate-100 p-3 transition hover:bg-slate-50"
              >
                <div className="min-w-0 flex-1">
                  <p className="truncate font-medium text-slate-800">{t.title}</p>
                  <p className="text-xs text-slate-400">
                    {t.projectName && <span>{t.projectName}</span>}
                    {t.dueDate && (
                      <span className={t.dueDate < new Date().toISOString() ? "text-red-500" : ""}>
                        {" "}
                        - Due: {new Date(t.dueDate).toLocaleDateString()}
                      </span>
                    )}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
                    {t.status || "Unknown"}
                  </span>
                  {t.priority && (
                    <span
                      className={`rounded-full px-2 py-1 text-xs font-medium ${
                        PRIORITY_COLORS[t.priority] || "bg-slate-100 text-slate-600"
                      }`}
                    >
                      {t.priority}
                    </span>
                  )}
                </div>
              </div>
            ))}
            {(!data?.recentTasks || data.recentTasks.length === 0) && (
              <p className="py-4 text-center text-slate-400">No tasks assigned</p>
            )}
          </div>
        </div>
      </div>

      {/* Tasks by Status */}
      {data?.tasksByStatus && Object.keys(data.tasksByStatus).length > 0 && (
        <div className="mt-6 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <h2 className="mb-4 text-lg font-semibold text-slate-800">Tasks by Status</h2>
          <div className="flex flex-wrap gap-3">
            {Object.entries(data.tasksByStatus).map(([status, count]) => (
              <div
                key={status}
                className="flex items-center gap-2 rounded-xl border border-slate-200 px-4 py-2"
              >
                <span className="text-sm text-slate-600">{status}</span>
                <span className="font-semibold text-slate-800">{count}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
