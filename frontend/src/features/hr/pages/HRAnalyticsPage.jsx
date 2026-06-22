import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  BarChart3,
  Users,
  Clock,
  TrendingUp,
  AlertTriangle,
  ArrowRight,
  Download,
} from 'lucide-react';
import { toast } from 'sonner';
import { useWorkspace } from '../../../context/WorkspaceContext';
import {
  getUtilizationSummary,
  listDepartments,
  exportUtilizationReport,
} from '../api/hrApi';
import { PageSkeleton } from '../../../components/LoadingSkeleton';
import { EmptyState } from '../../../components/EmptyStates';
import ErrorState from '../../../components/ErrorState';

const RATING_COLORS = {
  Excellent: 'bg-emerald-100 text-emerald-700',
  Good: 'bg-blue-100 text-blue-700',
  Average: 'bg-amber-100 text-amber-700',
  'Below Average': 'bg-orange-100 text-orange-700',
  Poor: 'bg-rose-100 text-rose-700',
  'N/A': 'bg-slate-100 text-slate-500',
};

function StatCard({ icon: Icon, label, value, color = 'blue', sub }) {
  const colors = {
    blue: 'from-blue-500 to-blue-600',
    emerald: 'from-emerald-500 to-emerald-600',
    purple: 'from-purple-500 to-purple-600',
    amber: 'from-amber-500 to-amber-600',
    rose: 'from-rose-500 to-rose-600',
  };
  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-lg">
      <div className={`flex h-12 w-12 items-center justify-center rounded-xl bg-gradient-to-br ${colors[color]} mb-4 shadow-sm`}>
        <Icon className="h-6 w-6 text-white" />
      </div>
      <p className="text-sm font-medium text-slate-500 mb-1">{label}</p>
      <p className="text-3xl font-bold text-slate-900">{value}</p>
      {sub && <p className="text-xs text-slate-400 mt-1">{sub}</p>}
    </div>
  );
}

function UtilizationBar({ percentage, label }) {
  const pct = Math.min(100, Math.max(0, Number(percentage) || 0));
  let barColor = 'bg-emerald-500';
  if (pct < 50) barColor = 'bg-rose-500';
  else if (pct < 75) barColor = 'bg-amber-500';

  return (
    <div className="flex items-center gap-3">
      <span className="text-sm text-slate-600 w-24 truncate">{label}</span>
      <div className="flex-1 bg-slate-100 rounded-full h-2.5">
        <div className={`h-2.5 rounded-full ${barColor}`} style={{ width: `${pct}%` }} />
      </div>
      <span className="text-sm font-medium text-slate-900 w-12 text-right">{pct.toFixed(0)}%</span>
    </div>
  );
}

export default function HRAnalyticsPage() {
  const { workspace } = useWorkspace();
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [startDate, setStartDate] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
  });
  const [endDate, setEndDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    loadData();
  }, [workspace?.id, startDate, endDate]);

  const loadData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [summaryData, deptData] = await Promise.allSettled([
        getUtilizationSummary(startDate, endDate),
        listDepartments(false),
      ]);
      if (summaryData.status === 'fulfilled') setSummary(summaryData.value);
      if (deptData.status === 'fulfilled') setDepartments(deptData.value);
      if (summaryData.status === 'rejected') setError(summaryData.reason?.message || 'Failed to load analytics');
    } catch (e) {
      setError(e.message || 'Failed to load analytics');
      toast.error(e.message || 'Failed to load analytics');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = async () => {
    setExporting(true);
    try {
      await exportUtilizationReport(startDate, endDate, 'csv');
      toast.success('Report exported successfully');
    } catch (e) {
      toast.error(e.message || 'Export failed');
    } finally {
      setExporting(false);
    }
  };

  if (loading) return <PageSkeleton />;

  if (error) return <ErrorState title="Failed to load analytics" message={error} onRetry={loadData} />;

  return (
    <div className="p-4 sm:p-6">
      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
            <BarChart3 className="w-7 h-7 text-indigo-600" /> HR Analytics
          </h1>
          <p className="text-slate-500 mt-1">Employee utilization and productivity insights</p>
        </div>
        <div className="flex gap-2 items-center">
          <input
            type="date"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-400"
          />
          <span className="text-slate-400">to</span>
          <input
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm outline-none focus:border-indigo-400"
          />
          <button
            onClick={handleExport}
            disabled={exporting}
            className="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-200 bg-white text-sm font-medium hover:bg-slate-50 disabled:opacity-50"
          >
            <Download className="w-4 h-4" />
            {exporting ? 'Exporting...' : 'Export'}
          </button>
        </div>
      </div>

      {summary ? (
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
            <StatCard
              icon={Users}
              label="Active Employees"
              value={summary.activeEmployees}
              color="blue"
              sub={`${summary.employeesWithData} with logged time`}
            />
            <StatCard
              icon={TrendingUp}
              label="Avg Utilization"
              value={`${summary.averageUtilization}%`}
              color="emerald"
              sub="Target: 75%"
            />
            <StatCard
              icon={Clock}
              label="Total Logged Hours"
              value={Number(summary.totalLoggedHours).toFixed(1)}
              color="purple"
              sub={`Overtime: ${Number(summary.totalOvertimeHours).toFixed(1)}h`}
            />
            <StatCard
              icon={AlertTriangle}
              label="Tasks Completed"
              value={summary.totalTasksCompleted}
              color="amber"
              sub={`of ${summary.totalTasksAssigned} assigned`}
            />
          </div>

          <div className="grid lg:grid-cols-2 gap-6 mb-8">
            <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-lg">
              <h3 className="text-lg font-bold text-slate-900 mb-4">Top Performers</h3>
              {summary.topPerformers.length === 0 ? (
                <p className="text-sm text-slate-500">No data available</p>
              ) : (
                <div className="space-y-3">
                  {summary.topPerformers.map((emp) => (
                    <div key={emp.employeeId} className="flex items-center justify-between p-3 rounded-xl bg-slate-50">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 text-xs font-bold">
                          {emp.employeeName?.charAt(0) || 'E'}
                        </div>
                        <div>
                          <p className="text-sm font-medium text-slate-900">{emp.employeeName}</p>
                          <p className="text-xs text-slate-500">{emp.loggedHours}h logged</p>
                        </div>
                      </div>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${RATING_COLORS[emp.rating] || 'bg-slate-100 text-slate-500'}`}>
                        {emp.rating}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-lg">
              <h3 className="text-lg font-bold text-slate-900 mb-4">Under-Utilized Employees</h3>
              {summary.underUtilized.length === 0 ? (
                <p className="text-sm text-slate-500">All employees are well-utilized</p>
              ) : (
                <div className="space-y-3">
                  {summary.underUtilized.map((emp) => (
                    <div key={emp.employeeId} className="flex items-center justify-between p-3 rounded-xl bg-slate-50">
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-amber-100 flex items-center justify-center text-amber-700 text-xs font-bold">
                          {emp.employeeName?.charAt(0) || 'E'}
                        </div>
                        <div>
                          <p className="text-sm font-medium text-slate-900">{emp.employeeName}</p>
                          <p className="text-xs text-slate-500">{emp.loggedHours}h / {emp.expectedHours}h expected</p>
                        </div>
                      </div>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${RATING_COLORS[emp.rating] || 'bg-slate-100 text-slate-500'}`}>
                        {emp.utilizationPercentage}%
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="bg-white rounded-2xl border border-slate-200 p-6 shadow-lg mb-8">
            <h3 className="text-lg font-bold text-slate-900 mb-4">Department Utilization</h3>
            {summary.byDepartment.length === 0 ? (
              <p className="text-sm text-slate-500">No department data available</p>
            ) : (
              <div className="space-y-4">
                {summary.byDepartment.map((dept) => (
                  <div key={dept.departmentName} className="p-4 rounded-xl bg-slate-50">
                    <div className="flex items-center justify-between mb-2">
                      <p className="font-medium text-slate-900">{dept.departmentName}</p>
                      <p className="text-sm text-slate-500">{dept.employeeCount} employees</p>
                    </div>
                    <UtilizationBar percentage={dept.averageUtilization} label="Utilization" />
                    <div className="flex gap-4 mt-2 text-xs text-slate-500">
                      <span>Hours: {Number(dept.totalLoggedHours).toFixed(1)}</span>
                      <span>Attendance: {dept.averageAttendanceRate}%</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="text-right">
            <button
              onClick={() => navigate(`/w/${workspace?.slug}/hr/utilization`)}
              className="inline-flex items-center gap-2 text-sm font-medium text-indigo-600 hover:text-indigo-700"
            >
              View detailed utilization report <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        </>
      ) : (
        <EmptyState title="No data available" message="Start logging time and attendance to see analytics." />
      )}
    </div>
  );
}
