import { useEffect, useState } from "react";
import {
  Users,
  UserCheck,
  Phone,
  TrendingUp,
  DollarSign,
  BarChart3,
  Loader2,
  ArrowRight,
} from "lucide-react";
import { Link } from "react-router-dom";
import API from "../../../api/axios";

const StatCard = ({ icon: Icon, label, value, color, sub }) => (
  <div className="flex items-center gap-4 rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
    <div className={`flex h-12 w-12 items-center justify-center rounded-xl ${color}`}>
      <Icon className="h-6 w-6 text-white" />
    </div>
    <div>
      <p className="text-sm text-slate-500">{label}</p>
      <p className="text-2xl font-bold text-slate-800">{value}</p>
      {sub && <p className="text-xs text-slate-400">{sub}</p>}
    </div>
  </div>
);

const PRIORITY_COLORS = {
  NEW: "bg-blue-100 text-blue-700",
  CONTACTED: "bg-amber-100 text-amber-700",
  QUALIFIED: "bg-emerald-100 text-emerald-700",
  UNQUALIFIED: "bg-slate-100 text-slate-600",
  CONVERTED: "bg-purple-100 text-purple-700",
  LOST: "bg-rose-100 text-rose-700",
};

export default function CrmDashboard() {
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
      const res = await API.get("/crm/dashboard");
      setData(res.data?.data || res.data);
    } catch (err) {
      console.error("Failed to load CRM dashboard:", err);
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
        <h1 className="text-2xl font-bold text-slate-800">CRM Dashboard</h1>
        <p className="text-slate-500">Overview of your customer relationships</p>
      </div>

      {/* Stats Grid */}
      <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard
          icon={Phone}
          label="Total Leads"
          value={data?.totalLeads ?? 0}
          color="bg-gradient-to-br from-blue-500 to-indigo-600"
        />
        <StatCard
          icon={Users}
          label="Total Customers"
          value={data?.totalCustomers ?? 0}
          color="bg-gradient-to-br from-emerald-500 to-teal-600"
        />
        <StatCard
          icon={TrendingUp}
          label="Open Deals"
          value={data?.openDeals ?? 0}
          sub={`Pipeline: $${(data?.pipelineValue ?? 0).toLocaleString()}`}
          color="bg-gradient-to-br from-amber-500 to-orange-600"
        />
        <StatCard
          icon={DollarSign}
          label="Won Revenue"
          value={`$${(data?.wonRevenue ?? 0).toLocaleString()}`}
          sub={`${data?.wonDeals ?? 0} deals`}
          color="bg-gradient-to-br from-purple-500 to-fuchsia-600"
        />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Leads by Status */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Leads by Status</h2>
          </div>
          <div className="space-y-3">
            {data?.leadsByStatus && Object.entries(data.leadsByStatus).length > 0 ? (
              Object.entries(data.leadsByStatus).map(([status, count]) => (
                <div
                  key={status}
                  className="flex items-center justify-between rounded-xl border border-slate-100 p-3"
                >
                  <span
                    className={`rounded-full px-3 py-1 text-xs font-medium ${PRIORITY_COLORS[status] || "bg-slate-100 text-slate-600"}`}
                  >
                    {status}
                  </span>
                  <span className="font-semibold text-slate-800">{count}</span>
                </div>
              ))
            ) : (
              <p className="py-4 text-center text-slate-400">No leads yet</p>
            )}
          </div>
        </div>

        {/* Top Customers */}
        <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-lg font-semibold text-slate-800">Top Customers</h2>
          </div>
          <div className="space-y-3">
            {data?.topCustomers?.length > 0 ? (
              data.topCustomers.map((c) => (
                <div
                  key={c.id}
                  className="flex items-center justify-between rounded-xl border border-slate-100 p-3"
                >
                  <div>
                    <p className="font-medium text-slate-800">{c.name}</p>
                    {c.email && <p className="text-xs text-slate-400">{c.email}</p>}
                  </div>
                  <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-600">
                    Customer
                  </span>
                </div>
              ))
            ) : (
              <p className="py-4 text-center text-slate-400">No customers yet</p>
            )}
          </div>
        </div>
      </div>

      {/* Quick Links */}
      <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Link
          to="leads"
          className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:bg-slate-50"
        >
          <div>
            <p className="font-semibold text-slate-800">Manage Leads</p>
            <p className="text-sm text-slate-400">Track and convert leads</p>
          </div>
          <ArrowRight className="h-5 w-5 text-slate-400" />
        </Link>
        <Link
          to="customers"
          className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:bg-slate-50"
        >
          <div>
            <p className="font-semibold text-slate-800">Manage Customers</p>
            <p className="text-sm text-slate-400">View customer directory</p>
          </div>
          <ArrowRight className="h-5 w-5 text-slate-400" />
        </Link>
        <Link
          to="pipeline"
          className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:bg-slate-50"
        >
          <div>
            <p className="font-semibold text-slate-800">Sales Pipeline</p>
            <p className="text-sm text-slate-400">Manage deals and stages</p>
          </div>
          <ArrowRight className="h-5 w-5 text-slate-400" />
        </Link>
      </div>
    </div>
  );
}
