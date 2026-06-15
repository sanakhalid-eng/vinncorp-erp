import { useEffect, useState, useMemo } from 'react';
import {
  BarChart3,
  Loader2,
  Search,
  Download,
  Filter,
  TrendingUp,
  Clock,
  AlertTriangle,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  getEmployeeUtilization,
  listEmployees,
  listDepartments,
  exportUtilizationReport,
} from '../api/hrApi';

const RATING_COLORS = {
  Excellent: 'bg-emerald-100 text-emerald-700',
  Good: 'bg-blue-100 text-blue-700',
  Average: 'bg-amber-100 text-amber-700',
  'Below Average': 'bg-orange-100 text-orange-700',
  Poor: 'bg-rose-100 text-rose-700',
  'N/A': 'bg-slate-100 text-slate-500',
};

function UtilizationBar({ percentage }) {
  const pct = Math.min(100, Math.max(0, Number(percentage) || 0));
  let barColor = 'bg-emerald-500';
  if (pct < 50) barColor = 'bg-rose-500';
  else if (pct < 75) barColor = 'bg-amber-500';

  return (
    <div className="flex items-center gap-2">
      <div className="flex-1 bg-slate-100 rounded-full h-2">
        <div className={`h-2 rounded-full ${barColor}`} style={{ width: `${pct}%` }} />
      </div>
      <span className="text-xs font-medium text-slate-700 w-10 text-right">{pct.toFixed(0)}%</span>
    </div>
  );
}

export default function UtilizationReportPage() {
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [deptFilter, setDeptFilter] = useState('');
  const [ratingFilter, setRatingFilter] = useState('');
  const [startDate, setStartDate] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`;
  });
  const [endDate, setEndDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [exporting, setExporting] = useState(false);

  useEffect(() => {
    loadData();
  }, [startDate, endDate]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [utilData, empData, deptData] = await Promise.allSettled([
        getEmployeeUtilization(startDate, endDate),
        listEmployees(),
        listDepartments(false),
      ]);
      if (utilData.status === 'fulfilled') setEmployees(utilData.value);
      if (empData.status === 'fulfilled') setDepartments([]); // Will be populated from dept data
      if (deptData.status === 'fulfilled') setDepartments(deptData.value);
    } catch (e) {
      toast.error(e.message || 'Failed to load utilization data');
    } finally {
      setLoading(false);
    }
  };

  const deptMap = useMemo(() => Object.fromEntries(departments.map((d) => [d.id, d])), [departments]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return employees.filter((e) => {
      if (deptFilter && e.department !== deptFilter) return false;
      if (ratingFilter && e.rating !== ratingFilter) return false;
      if (!q) return true;
      return (
        e.employeeName?.toLowerCase().includes(q) ||
        e.employeeCode?.toLowerCase().includes(q)
      );
    });
  }, [employees, search, deptFilter, ratingFilter]);

  const stats = useMemo(() => {
    if (filtered.length === 0) return null;
    const totalLogged = filtered.reduce((sum, e) => sum + (Number(e.loggedHours) || 0), 0);
    const totalExpected = filtered.reduce((sum, e) => sum + (Number(e.expectedHours) || 0), 0);
    const avgUtil = filtered.reduce((sum, e) => sum + (Number(e.utilizationPercentage) || 0), 0) / filtered.length;
    const totalOvertime = filtered.reduce((sum, e) => sum + (Number(e.overtimeHours) || 0), 0);
    return { totalLogged, totalExpected, avgUtil, totalOvertime };
  }, [filtered]);

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

  return (
    <div>
      <div className="mb-8 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
            <BarChart3 className="w-7 h-7 text-indigo-600" /> Utilization Report
          </h1>
          <p className="text-slate-500 mt-1">Employee utilization and productivity analysis</p>
        </div>
        <button
          onClick={handleExport}
          disabled={exporting}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
        >
          <Download className="w-4 h-4" />
          {exporting ? 'Exporting...' : 'Export CSV'}
        </button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <input
          type="date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm outline-none focus:border-indigo-400"
        />
        <span className="flex items-center text-slate-400">to</span>
        <input
          type="date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm outline-none focus:border-indigo-400"
        />
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by name or code..."
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-2.5 text-sm outline-none focus:border-indigo-400"
          />
        </div>
        <select
          value={deptFilter}
          onChange={(e) => setDeptFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-indigo-400"
        >
          <option value="">All Departments</option>
          {[...new Set(employees.map((e) => e.department).filter(Boolean))].map((dept) => (
            <option key={dept} value={dept}>{dept}</option>
          ))}
        </select>
        <select
          value={ratingFilter}
          onChange={(e) => setRatingFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-indigo-400"
        >
          <option value="">All Ratings</option>
          <option value="Excellent">Excellent</option>
          <option value="Good">Good</option>
          <option value="Average">Average</option>
          <option value="Below Average">Below Average</option>
          <option value="Poor">Poor</option>
        </select>
      </div>

      {stats && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <Clock className="w-6 h-6 text-indigo-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{stats.totalLogged.toFixed(1)}</p>
            <p className="text-xs text-slate-500">Total Logged Hours</p>
          </div>
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <TrendingUp className="w-6 h-6 text-emerald-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{stats.avgUtil.toFixed(1)}%</p>
            <p className="text-xs text-slate-500">Average Utilization</p>
          </div>
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <AlertTriangle className="w-6 h-6 text-amber-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{stats.totalOvertime.toFixed(1)}</p>
            <p className="text-xs text-slate-500">Overtime Hours</p>
          </div>
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <BarChart3 className="w-6 h-6 text-blue-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{filtered.length}</p>
            <p className="text-xs text-slate-500">Employees Shown</p>
          </div>
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <BarChart3 className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No data found</h3>
          <p className="text-slate-500 text-sm">No utilization data matches your filters.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Employee</th>
                <th className="text-left px-4 py-3">Department</th>
                <th className="text-left px-4 py-3">Logged / Expected</th>
                <th className="text-left px-4 py-3">Utilization</th>
                <th className="text-left px-4 py-3">Overtime</th>
                <th className="text-left px-4 py-3">Attendance</th>
                <th className="text-left px-4 py-3">Rating</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((emp) => (
                <tr key={emp.employeeId} className="hover:bg-slate-50">
                  <td className="px-4 py-3">
                    <div className="font-medium text-slate-900">{emp.employeeName}</div>
                    <div className="text-slate-500 text-xs">{emp.employeeCode}</div>
                  </td>
                  <td className="px-4 py-3 text-slate-700">{emp.department || '—'}</td>
                  <td className="px-4 py-3 text-slate-700">
                    {Number(emp.loggedHours).toFixed(1)}h / {Number(emp.expectedHours).toFixed(1)}h
                  </td>
                  <td className="px-4 py-3 min-w-[180px]">
                    <UtilizationBar percentage={emp.utilizationPercentage} />
                  </td>
                  <td className="px-4 py-3 text-slate-700">{Number(emp.overtimeHours).toFixed(1)}h</td>
                  <td className="px-4 py-3 text-slate-700">
                    {emp.attendanceDays}/{emp.workingDays} ({emp.attendanceRate}%)
                  </td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${RATING_COLORS[emp.rating] || 'bg-slate-100 text-slate-500'}`}>
                      {emp.rating}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
