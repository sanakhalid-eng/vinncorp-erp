import { useEffect, useState } from 'react';
import {
  Calendar,
  Loader2,
  Clock,
  CheckCircle2,
  XCircle,
  AlertTriangle,
  Coffee,
} from 'lucide-react';
import { toast } from 'sonner';
import { getMyAttendance, getMyAttendanceSummary } from '../api/hrApi';

const STATUS_ICON = {
  PRESENT: <CheckCircle2 className="w-4 h-4 text-emerald-600" />,
  ABSENT: <XCircle className="w-4 h-4 text-rose-600" />,
  LATE: <AlertTriangle className="w-4 h-4 text-amber-600" />,
  HALF_DAY: <Coffee className="w-4 h-4 text-orange-600" />,
  ON_LEAVE: <Clock className="w-4 h-4 text-indigo-600" />,
};

const STATUS_BADGE = {
  PRESENT: 'bg-emerald-100 text-emerald-700',
  ABSENT: 'bg-rose-100 text-rose-700',
  LATE: 'bg-amber-100 text-amber-700',
  HALF_DAY: 'bg-orange-100 text-orange-700',
  ON_LEAVE: 'bg-indigo-100 text-indigo-700',
};

export default function MyAttendancePage() {
  const [attendance, setAttendance] = useState([]);
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [year, setYear] = useState(new Date().getFullYear());
  const [month, setMonth] = useState(new Date().getMonth() + 1);

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [year, month]);

  const loadData = async () => {
    setLoading(true);
    try {
      const startDate = `${year}-${String(month).padStart(2, '0')}-01`;
      const endDate = new Date(year, month, 0).toISOString().split('T')[0];

      const [attData, summaryData] = await Promise.all([
        getMyAttendance(startDate, endDate),
        getMyAttendanceSummary(year, month),
      ]);
      setAttendance(attData);
      setSummary(summaryData);
    } catch (e) {
      toast.error(e.message || 'Failed to load attendance');
    } finally {
      setLoading(false);
    }
  };

  const months = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
  ];

  return (
    <div>
      <div className="mb-8 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
            <Calendar className="w-7 h-7 text-indigo-600" /> My Attendance
          </h1>
          <p className="text-slate-500 mt-1">Track your attendance and work hours</p>
        </div>
        <div className="flex gap-2">
          <select
            value={month}
            onChange={(e) => setMonth(Number(e.target.value))}
            className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-indigo-400"
          >
            {months.map((m, i) => (
              <option key={i} value={i + 1}>{m}</option>
            ))}
          </select>
          <select
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-indigo-400"
          >
            {[2024, 2025, 2026].map((y) => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </div>
      </div>

      {summary && (
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-4 mb-8">
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <CheckCircle2 className="w-6 h-6 text-emerald-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{summary.presentCount || 0}</p>
            <p className="text-xs text-slate-500">Present</p>
          </div>
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <XCircle className="w-6 h-6 text-rose-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{summary.absentCount || 0}</p>
            <p className="text-xs text-slate-500">Absent</p>
          </div>
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <AlertTriangle className="w-6 h-6 text-amber-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{summary.lateCount || 0}</p>
            <p className="text-xs text-slate-500">Late</p>
          </div>
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <Clock className="w-6 h-6 text-indigo-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{summary.onLeaveCount || 0}</p>
            <p className="text-xs text-slate-500">On Leave</p>
          </div>
          <div className="bg-white rounded-2xl border border-slate-200 p-4 text-center">
            <Coffee className="w-6 h-6 text-orange-600 mx-auto mb-2" />
            <p className="text-2xl font-bold text-slate-900">{summary.halfDayCount || 0}</p>
            <p className="text-xs text-slate-500">Half Day</p>
          </div>
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
        </div>
      ) : attendance.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <Calendar className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No attendance records</h3>
          <p className="text-slate-500 text-sm">No records found for this period.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Date</th>
                <th className="text-left px-4 py-3">Check In</th>
                <th className="text-left px-4 py-3">Check Out</th>
                <th className="text-left px-4 py-3">Hours</th>
                <th className="text-left px-4 py-3">Overtime</th>
                <th className="text-left px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {attendance.map((a) => (
                <tr key={a.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{a.attendanceDate}</td>
                  <td className="px-4 py-3 text-slate-700">
                    {a.checkInTime ? new Date(a.checkInTime).toLocaleTimeString() : '—'}
                  </td>
                  <td className="px-4 py-3 text-slate-700">
                    {a.checkOutTime ? new Date(a.checkOutTime).toLocaleTimeString() : '—'}
                  </td>
                  <td className="px-4 py-3 text-slate-700">{a.workHours || 0}h</td>
                  <td className="px-4 py-3 text-slate-700">{a.overtimeHours || 0}h</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[a.status] || 'bg-slate-100 text-slate-600'}`}>
                      {STATUS_ICON[a.status]}
                      {a.status?.replace('_', ' ')}
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
