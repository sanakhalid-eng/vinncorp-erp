import { useEffect, useState } from 'react';
import {
  Loader2,
  CalendarCheck,
  Search,
  Edit2,
  Trash2,
  Clock,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  getAttendanceDashboard,
  getAttendanceByDate,
  checkIn,
  checkOut,
  deleteAttendance,
  listEmployees,
  listActiveShifts,
  AttendanceStatuses,
} from '../api/hrApi';
import AttendanceFormModal from '../components/AttendanceFormModal';

const STATUS_BADGE = {
  PRESENT: 'bg-emerald-100 text-emerald-700',
  ABSENT: 'bg-rose-100 text-rose-700',
  LATE: 'bg-amber-100 text-amber-700',
  HALF_DAY: 'bg-orange-100 text-orange-700',
  ON_LEAVE: 'bg-indigo-100 text-indigo-700',
};

const today = () => new Date().toISOString().split('T')[0];

export default function AttendancePage() {
  const [date, setDate] = useState(today());
  const [dashboard, setDashboard] = useState(null);
  const [records, setRecords] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [shifts, setShifts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [showCheckIn, setShowCheckIn] = useState(false);
  const [showCheckOut, setShowCheckOut] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    load();
  }, [date]);

  const load = async () => {
    setLoading(true);
    try {
      const [dash, recs, emps, shs] = await Promise.all([
        getAttendanceDashboard(date),
        getAttendanceByDate(date),
        listEmployees(),
        listActiveShifts(),
      ]);
      setDashboard(dash);
      setRecords(recs);
      setEmployees(emps);
      setShifts(shs);
    } catch (e) {
      toast.error(e.message || 'Failed to load attendance');
    } finally {
      setLoading(false);
    }
  };

  const empMap = Object.fromEntries(employees.map((e) => [e.id, e]));
  const shiftMap = Object.fromEntries(shifts.map((s) => [s.id, s]));

  const filtered = records.filter((r) => {
    const q = search.trim().toLowerCase();
    if (statusFilter && r.status !== statusFilter) return false;
    if (!q) return true;
    return (
      r.employeeName?.toLowerCase().includes(q) ||
      r.employeeEmail?.toLowerCase().includes(q)
    );
  });

  const handleQuickCheckIn = async (employeeId) => {
    try {
      await checkIn({ employeeId, attendanceDate: date });
      toast.success('Checked in successfully');
      await load();
    } catch (e) {
      toast.error(e.message || 'Check-in failed');
    }
  };

  const handleQuickCheckOut = async (employeeId) => {
    try {
      await checkOut({ employeeId, attendanceDate: date });
      toast.success('Checked out successfully');
      await load();
    } catch (e) {
      toast.error(e.message || 'Check-out failed');
    }
  };

  const handleEdit = (record) => {
    setEditing(record);
    setShowEditModal(true);
  };

  const handleDelete = async (record) => {
    if (!window.confirm(`Delete attendance for ${record.employeeName}?`)) return;
    try {
      await deleteAttendance(record.id);
      toast.success('Attendance record deleted');
      await load();
    } catch (e) {
      toast.error(e.message || 'Delete failed');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
      </div>
    );
  }

  return (
    <div>
      <div className="mb-8 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
            <CalendarCheck className="w-7 h-7 text-indigo-600" /> Attendance
          </h1>
          <p className="text-slate-500 mt-1">
            Track daily attendance, check-ins and check-outs
          </p>
        </div>
      </div>

      {dashboard && (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4 mb-8">
          <StatCard label="Total Employees" value={dashboard.totalEmployees} color="bg-slate-100 text-slate-700" />
          <StatCard label="Present" value={dashboard.presentCount} color="bg-emerald-100 text-emerald-700" />
          <StatCard label="Absent" value={dashboard.absentCount} color="bg-rose-100 text-rose-700" />
          <StatCard label="Late" value={dashboard.lateCount} color="bg-amber-100 text-amber-700" />
          <StatCard label="Half Day" value={dashboard.halfDayCount} color="bg-orange-100 text-orange-700" />
          <StatCard label="On Leave" value={dashboard.onLeaveCount} color="bg-indigo-100 text-indigo-700" />
        </div>
      )}

      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <input
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-indigo-400"
        />
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by name or email..."
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400"
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-indigo-400"
        >
          <option value="">All Statuses</option>
          {AttendanceStatuses.map((s) => (
            <option key={s} value={s}>{s.replace('_', ' ')}</option>
          ))}
        </select>
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <Clock className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No attendance records</h3>
          <p className="text-slate-500 text-sm">No records found for {date}.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Employee</th>
                <th className="text-left px-4 py-3">Shift</th>
                <th className="text-left px-4 py-3">Check In</th>
                <th className="text-left px-4 py-3">Check Out</th>
                <th className="text-left px-4 py-3">Hours</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((r) => (
                <tr key={r.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3">
                    <div className="font-medium text-slate-900">{r.employeeName}</div>
                    <div className="text-slate-500 text-xs">{r.employeeEmail || '—'}</div>
                  </td>
                  <td className="px-4 py-3 text-slate-700">{r.shiftName || '—'}</td>
                  <td className="px-4 py-3 text-slate-700">
                    {r.checkInTime ? new Date(r.checkInTime).toLocaleTimeString() : '—'}
                  </td>
                  <td className="px-4 py-3 text-slate-700">
                    {r.checkOutTime ? new Date(r.checkOutTime).toLocaleTimeString() : '—'}
                  </td>
                  <td className="px-4 py-3 text-slate-700">{r.workHours ?? '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[r.status] || 'bg-slate-100 text-slate-600'}`}>
                      {r.status?.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex gap-1">
                      <button onClick={() => handleEdit(r)} className="p-1.5 rounded hover:bg-slate-100" title="Edit">
                        <Edit2 className="w-4 h-4 text-slate-600" />
                      </button>
                      <button onClick={() => handleDelete(r)} className="p-1.5 rounded hover:bg-rose-50" title="Delete">
                        <Trash2 className="w-4 h-4 text-rose-600" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <AttendanceFormModal
        open={showEditModal}
        onClose={() => setShowEditModal(false)}
        initial={editing}
        employees={employees}
        shifts={shifts}
        onSuccess={async () => { setShowEditModal(false); await load(); }}
      />

      <p className="text-xs text-slate-400 mt-6 flex items-center gap-1">
        <CalendarCheck className="w-3 h-3" /> HR module · VinnCorp ERP
      </p>
    </div>
  );
}

function StatCard({ label, value, color }) {
  return (
    <div className="bg-white rounded-2xl border border-slate-200 p-4">
      <div className={`text-2xl font-bold ${color} inline-block px-2 py-0.5 rounded-lg`}>{value}</div>
      <div className="text-xs text-slate-500 mt-1">{label}</div>
    </div>
  );
}
