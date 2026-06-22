import { useEffect, useState } from 'react';
import { CheckCircle, Search, Check, X, Eye } from 'lucide-react';
import { toast } from 'sonner';
import { listLeaveRequests, approveLeave, rejectLeave, listEmployees, listActiveLeaveTypes, getLeaveDashboard } from '../api/hrApi';
import { PageSkeleton } from '../../../components/LoadingSkeleton';
import { EmptyState } from '../../../components/EmptyStates';
import ErrorState from '../../../components/ErrorState';
import ConfirmationDialog from '../../projects/components/members/ConfirmationDialog';

const STATUS_BADGE = {
  PENDING: 'bg-amber-100 text-amber-700',
  APPROVED: 'bg-emerald-100 text-emerald-700',
  REJECTED: 'bg-rose-100 text-rose-700',
  CANCELLED: 'bg-slate-200 text-slate-600',
};

export default function LeaveApprovalsPage() {
  const [requests, setRequests] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [leaveTypes, setLeaveTypes] = useState([]);
  const [dashboard, setDashboard] = useState({ pendingCount: 0 });
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('PENDING');
  const [viewing, setViewing] = useState(null);
  const [rejectModal, setRejectModal] = useState(null);
  const [rejectReason, setRejectReason] = useState('');

  useEffect(() => { load(); }, []);

  const load = async () => {
    setLoading(true);
    try {
      const [reqs, emps, types, dash] = await Promise.all([
        listLeaveRequests(),
        listEmployees(),
        listActiveLeaveTypes(),
        getLeaveDashboard(),
      ]);
      setRequests(reqs);
      setEmployees(emps);
      setLeaveTypes(types);
      setDashboard(dash);
    } catch (e) {
      toast.error(e.message || 'Failed to load data');
    } finally {
      setLoading(false);
    }
  };

  const empMap = Object.fromEntries(employees.map((e) => [e.id, e]));
  const typeMap = Object.fromEntries(leaveTypes.map((t) => [t.id, t]));

  const filtered = requests.filter((r) => {
    const q = search.trim().toLowerCase();
    if (statusFilter && r.status !== statusFilter) return false;
    if (!q) return true;
    return r.employeeName?.toLowerCase().includes(q) || r.leaveTypeName?.toLowerCase().includes(q);
  });

  const handleApprove = async (r) => {
    if (!window.confirm(`Approve leave for ${r.employeeName}?`)) return;
    try {
      await approveLeave(r.id);
      toast.success('Leave approved');
      await load();
    } catch (e) {
      toast.error(e.message || 'Approve failed');
    }
  };

  const handleReject = async () => {
    if (!rejectModal) return;
    try {
      await rejectLeave(rejectModal.id, { rejectionReason: rejectReason || null });
      toast.success('Leave rejected');
      setRejectModal(null);
      setRejectReason('');
      await load();
    } catch (e) {
      toast.error(e.message || 'Reject failed');
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
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
          <CheckCircle className="w-7 h-7 text-indigo-600" /> Leave Approvals
        </h1>
        <p className="text-slate-500 mt-1">Review and manage employee leave requests</p>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mb-8">
        <StatCard label="Pending" value={dashboard.pendingCount || 0} color="bg-amber-100 text-amber-700" />
        <StatCard label="Total Requests" value={requests.length} color="bg-slate-100 text-slate-700" />
        <StatCard label="Approved" value={requests.filter((r) => r.status === 'APPROVED').length} color="bg-emerald-100 text-emerald-700" />
        <StatCard label="Rejected" value={requests.filter((r) => r.status === 'REJECTED').length} color="bg-rose-100 text-rose-700" />
      </div>

      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input type="text" value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search by employee or type..."
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400" />
        </div>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-indigo-400">
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="">All</option>
        </select>
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <CheckCircle className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No leave requests</h3>
          <p className="text-slate-500 text-sm">No requests matching current filters.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Employee</th>
                <th className="text-left px-4 py-3">Leave Type</th>
                <th className="text-left px-4 py-3">Period</th>
                <th className="text-left px-4 py-3">Days</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((r) => (
                <tr key={r.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3">
                    <div className="font-medium text-slate-900">{r.employeeName}</div>
                    <div className="text-slate-500 text-xs">{r.employeeEmail || 'ΓÇö'}</div>
                  </td>
                  <td className="px-4 py-3 text-slate-700">{r.leaveTypeName}</td>
                  <td className="px-4 py-3 text-slate-700 text-xs">{r.startDate} to {r.endDate}</td>
                  <td className="px-4 py-3 text-slate-700">{r.totalDays}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[r.status] || 'bg-slate-100 text-slate-600'}`}>
                      {r.status}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex gap-1">
                      <button onClick={() => setViewing(r)} className="p-1.5 rounded hover:bg-slate-100" title="View">
                        <Eye className="w-4 h-4 text-slate-600" />
                      </button>
                      {r.status === 'PENDING' && (
                        <>
                          <button onClick={() => handleApprove(r)} className="p-1.5 rounded hover:bg-emerald-50" title="Approve">
                            <Check className="w-4 h-4 text-emerald-600" />
                          </button>
                          <button onClick={() => { setRejectModal(r); setRejectReason(''); }} className="p-1.5 rounded hover:bg-rose-50" title="Reject">
                            <X className="w-4 h-4 text-rose-600" />
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {viewing && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={() => setViewing(null)}>
          <div className="bg-white rounded-2xl w-full max-w-md p-6 shadow-xl" onClick={(e) => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">Leave Request Details</h3>
              <button onClick={() => setViewing(null)} className="p-1 rounded hover:bg-slate-100"><X className="w-5 h-5" /></button>
            </div>
            <div className="space-y-3 text-sm">
              <div><span className="font-medium text-slate-600">Employee:</span> {viewing.employeeName}</div>
              <div><span className="font-medium text-slate-600">Type:</span> {viewing.leaveTypeName}</div>
              <div><span className="font-medium text-slate-600">Period:</span> {viewing.startDate} to {viewing.endDate}</div>
              <div><span className="font-medium text-slate-600">Days:</span> {viewing.totalDays}</div>
              <div><span className="font-medium text-slate-600">Reason:</span> {viewing.reason || 'ΓÇö'}</div>
              <div><span className="font-medium text-slate-600">Status:</span> <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[viewing.status]}`}>{viewing.status}</span></div>
              {viewing.rejectionReason && <div><span className="font-medium text-slate-600">Rejection Reason:</span> {viewing.rejectionReason}</div>}
            </div>
          </div>
        </div>
      )}

      {rejectModal && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={() => setRejectModal(null)}>
          <div className="bg-white rounded-2xl w-full max-w-md p-6 shadow-xl" onClick={(e) => e.stopPropagation()}>
            <h3 className="text-lg font-semibold mb-4">Reject Leave Request</h3>
            <p className="text-sm text-slate-600 mb-3">Rejecting leave for <strong>{rejectModal.employeeName}</strong></p>
            <textarea value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} rows={3} placeholder="Reason for rejection (optional)"
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400 mb-4" />
            <div className="flex justify-end gap-2">
              <button onClick={() => setRejectModal(null)} className="px-4 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-100">Cancel</button>
              <button onClick={handleReject} className="px-4 py-2 rounded-lg text-sm font-medium bg-rose-600 text-white hover:bg-rose-700">Reject</button>
            </div>
          </div>
        </div>
      )}
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
