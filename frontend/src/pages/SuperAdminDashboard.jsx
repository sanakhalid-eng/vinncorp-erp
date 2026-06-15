import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Briefcase,
  Users,
  UserCheck,
  FolderKanban,
  Activity,
  ArrowRight,
  Loader2,
} from "lucide-react";
import API from "../api/axios";

const StatCard = ({ icon: Icon, label, value, color, onClick }) => (
  <button
    onClick={onClick}
    className={`flex items-center gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md ${onClick ? "cursor-pointer" : "cursor-default"}`}
  >
    <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${color}`}>
      <Icon className="h-6 w-6 text-white" />
    </div>
    <div className="text-left">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="text-2xl font-bold text-slate-800">{value}</p>
    </div>
  </button>
);

export default function SuperAdminDashboard() {
  const navigate = useNavigate();
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
      const res = await API.get("/admin/dashboard/summary");
      setData(res.data.data);
    } catch (err) {
      console.error("Failed to load admin dashboard:", err);
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
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-slate-800">Platform Dashboard</h1>
        <p className="text-slate-500">Overview of all workspaces and users</p>
      </div>

      {/* Stats Grid */}
      <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          icon={Briefcase}
          label="Total Workspaces"
          value={data?.totalWorkspaces ?? 0}
          color="bg-gradient-to-br from-cyan-500 to-blue-600"
          onClick={() => navigate("/admin/workspaces")}
        />
        <StatCard
          icon={Users}
          label="Total Users"
          value={data?.totalUsers ?? 0}
          color="bg-gradient-to-br from-violet-500 to-purple-600"
          onClick={() => navigate("/admin/users")}
        />
        <StatCard
          icon={UserCheck}
          label="Total Employees"
          value={data?.totalEmployees ?? 0}
          color="bg-gradient-to-br from-emerald-500 to-teal-600"
        />
        <StatCard
          icon={FolderKanban}
          label="Total Projects"
          value={data?.totalProjects ?? 0}
          color="bg-gradient-to-br from-amber-500 to-orange-600"
        />
      </div>

      {/* Recent Workspaces */}
      <div className="mb-8 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-800">Recent Workspaces</h2>
          <button
            onClick={() => navigate("/admin/workspaces")}
            className="flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-700"
          >
            View all <ArrowRight className="h-4 w-4" />
          </button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-500">
                <th className="pb-3 pr-4 font-medium">Name</th>
                <th className="pb-3 pr-4 font-medium">Slug</th>
                <th className="pb-3 pr-4 font-medium">Members</th>
                <th className="pb-3 pr-4 font-medium">Status</th>
                <th className="pb-3 font-medium">Created</th>
              </tr>
            </thead>
            <tbody>
              {(data?.recentWorkspaces ?? []).map((ws) => (
                <tr key={ws.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-3 pr-4 font-medium text-slate-800">{ws.name}</td>
                  <td className="py-3 pr-4 text-slate-500">{ws.slug}</td>
                  <td className="py-3 pr-4 text-slate-500">{ws.memberCount}</td>
                  <td className="py-3 pr-4">
                    <span
                      className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${
                        ws.active
                          ? "bg-emerald-100 text-emerald-700"
                          : "bg-slate-100 text-slate-600"
                      }`}
                    >
                      {ws.active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="py-3 text-slate-500">
                    {ws.createdAt ? new Date(ws.createdAt).toLocaleDateString() : "-"}
                  </td>
                </tr>
              ))}
              {(!data?.recentWorkspaces || data.recentWorkspaces.length === 0) && (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-slate-400">
                    No workspaces found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Recent Users */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-800">Recent Users</h2>
          <button
            onClick={() => navigate("/admin/users")}
            className="flex items-center gap-1 text-sm text-indigo-600 hover:text-indigo-700"
          >
            View all <ArrowRight className="h-4 w-4" />
          </button>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-500">
                <th className="pb-3 pr-4 font-medium">User</th>
                <th className="pb-3 pr-4 font-medium">Email</th>
                <th className="pb-3 pr-4 font-medium">Status</th>
                <th className="pb-3 font-medium">Joined</th>
              </tr>
            </thead>
            <tbody>
              {(data?.recentUsers ?? []).map((u) => (
                <tr key={u.id} className="border-b border-slate-100 last:border-0">
                  <td className="py-3 pr-4">
                    <div className="flex items-center gap-3">
                      <div className="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-xs font-bold text-white">
                        {u.name?.charAt(0)?.toUpperCase() || "U"}
                      </div>
                      <span className="font-medium text-slate-800">{u.name}</span>
                    </div>
                  </td>
                  <td className="py-3 pr-4 text-slate-500">{u.email}</td>
                  <td className="py-3 pr-4">
                    <span
                      className={`inline-flex rounded-full px-2 py-1 text-xs font-medium ${
                        u.isActive
                          ? "bg-emerald-100 text-emerald-700"
                          : "bg-slate-100 text-slate-600"
                      }`}
                    >
                      {u.isActive ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="py-3 text-slate-500">
                    {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : "-"}
                  </td>
                </tr>
              ))}
              {(!data?.recentUsers || data.recentUsers.length === 0) && (
                <tr>
                  <td colSpan={4} className="py-8 text-center text-slate-400">
                    No users found
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
