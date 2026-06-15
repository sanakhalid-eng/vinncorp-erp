import { useEffect, useState } from 'react';
import {
  TreePalm,
  Loader2,
  Plus,
  Clock,
  CheckCircle2,
  XCircle,
  Ban,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  getMyLeaves,
  getMyLeaveBalances,
  applyLeave,
  cancelLeave,
  listActiveLeaveTypes,
} from '../api/hrApi';
import SelfServiceLeaveModal from '../components/SelfServiceLeaveModal';

const STATUS_ICON = {
  PENDING: <Clock className="w-4 h-4 text-amber-600" />,
  APPROVED: <CheckCircle2 className="w-4 h-4 text-emerald-600" />,
  REJECTED: <XCircle className="w-4 h-4 text-rose-600" />,
  CANCELLED: <Ban className="w-4 h-4 text-slate-500" />,
};

const STATUS_BADGE = {
  PENDING: 'bg-amber-100 text-amber-700',
  APPROVED: 'bg-emerald-100 text-emerald-700',
  REJECTED: 'bg-rose-100 text-rose-700',
  CANCELLED: 'bg-slate-200 text-slate-600',
};

export default function MyLeavesPage() {
  const [leaves, setLeaves] = useState([]);
  const [balances, setBalances] = useState([]);
  const [leaveTypes, setLeaveTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [year, setYear] = useState(new Date().getFullYear());

  useEffect(() => {
    loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [year]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [leavesData, balancesData, typesData] = await Promise.all([
        getMyLeaves(),
        getMyLeaveBalances(year),
        listActiveLeaveTypes(),
      ]);
      setLeaves(leavesData);
      setBalances(balancesData);
      setLeaveTypes(typesData);
    } catch (e) {
      toast.error(e.message || 'Failed to load leave data');
    } finally {
      setLoading(false);
    }
  };

  const handleApply = async (payload) => {
    setSubmitting(true);
    try {
      await applyLeave(payload);
      toast.success('Leave request submitted');
      setShowModal(false);
      await loadData();
    } catch (e) {
      toast.error(e.message || 'Failed to apply for leave');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm('Are you sure you want to cancel this leave request?')) return;
    try {
      await cancelLeave(id);
      toast.success('Leave request cancelled');
      await loadData();
    } catch (e) {
      toast.error(e.message || 'Failed to cancel leave');
    }
  };

  return (
    <div>
      <div className="mb-8 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
            <TreePalm className="w-7 h-7 text-indigo-600" /> My Leaves
          </h1>
          <p className="text-slate-500 mt-1">Manage your leave requests and view balances</p>
        </div>
        <div className="flex gap-2">
          <select
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-indigo-400"
          >
            {[2024, 2025, 2026].map((y) => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
          <button
            onClick={() => setShowModal(true)}
            className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700"
          >
            <Plus className="w-4 h-4" /> Apply Leave
          </button>
        </div>
      </div>

      {balances.length > 0 && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
          {balances.map((b) => (
            <div key={b.id} className="bg-white rounded-2xl border border-slate-200 p-4">
              <p className="text-sm font-medium text-slate-500 mb-1">{b.leaveTypeName}</p>
              <div className="flex items-baseline gap-2">
                <span className="text-2xl font-bold text-slate-900">{b.availableDays}</span>
                <span className="text-sm text-slate-500">days available</span>
              </div>
              <div className="mt-2 flex gap-3 text-xs text-slate-500">
                <span>Used: {b.usedDays}</span>
                <span>Pending: {b.pendingDays}</span>
                <span>Total: {b.totalDays}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
        </div>
      ) : leaves.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <TreePalm className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No leave requests</h3>
          <p className="text-slate-500 text-sm">You haven't submitted any leave requests yet.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">Start Date</th>
                <th className="text-left px-4 py-3">End Date</th>
                <th className="text-left px-4 py-3">Days</th>
                <th className="text-left px-4 py-3">Reason</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {leaves.map((l) => (
                <tr key={l.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{l.leaveTypeName}</td>
                  <td className="px-4 py-3 text-slate-700">{l.startDate}</td>
                  <td className="px-4 py-3 text-slate-700">{l.endDate}</td>
                  <td className="px-4 py-3 text-slate-700">{l.totalDays}</td>
                  <td className="px-4 py-3 text-slate-700 max-w-[200px] truncate">{l.reason || '—'}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[l.status] || 'bg-slate-100 text-slate-600'}`}>
                      {STATUS_ICON[l.status]}
                      {l.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    {l.status === 'PENDING' && (
                      <button
                        onClick={() => handleCancel(l.id)}
                        className="text-xs text-rose-600 hover:text-rose-700 font-medium"
                      >
                        Cancel
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <SelfServiceLeaveModal
        open={showModal}
        onClose={() => setShowModal(false)}
        onSubmit={handleApply}
        leaveTypes={leaveTypes}
        submitting={submitting}
      />
    </div>
  );
}
