import { useEffect, useState } from 'react';
import { Loader2, FileText, Plus, Search, Edit2, Trash2, XCircle, Eye } from 'lucide-react';
import { toast } from 'sonner';
import { listLeaveRequests, cancelLeave, listActiveLeaveTypes, listEmployees } from '../api/hrApi';
import LeaveRequestFormModal from '../components/LeaveRequestFormModal';

const STATUS_BADGE = {
  PENDING: 'bg-amber-100 text-amber-700',
  APPROVED: 'bg-emerald-100 text-emerald-700',
  REJECTED: 'bg-rose-100 text-rose-700',
  CANCELLED: 'bg-slate-200 text-slate-600',
};

export default function LeaveRequestsPage() {
  const [requests, setRequests] = useState([]);
  const [leaveTypes, setLeaveTypes] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [viewing, setViewing] = useState(null);

  useEffect(() => { load(); }, []);

  const load = async () => {
    setLoading(true);
    try {
      const [reqs, types, emps] = await Promise.all([
        listLeaveRequests(),
        listActiveLeaveTypes(),
        listEmployees(),
      ]);
      setRequests(reqs);
      setLeaveTypes(types);
      setEmployees(emps);
    } catch (e) {
      toast.error(e.message || 'Failed to load leave requests');
    } finally {
      setLoading(false);
    }
  };

  const typeMap = Object.fromEntries(leaveTypes.map((t) => [t.id, t]));
  const empMap = Object.fromEntries(employees.map((e) => [e.id, e]));

  const filtered = requests.filter((r) => {
    const q = search.trim().toLowerCase();
    if (statusFilter && r.status !== statusFilter) return false;
    if (!q) return true;
    return r.employeeName?.toLowerCase().includes(q) || r.leaveTypeName?.toLowerCase().includes(q);
  });

  const handleCancel = async (r) => {
    if (!window.confirm('Cancel this leave request?')) return;
    try {
      await cancelLeave(r.id);
      toast.success('Leave request cancelled');
      await load();
    } catch (e) {
      toast.error(e.message || 'Cancel failed');
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
            <FileText className="w-7 h-7 text-indigo-600" /> My Leave Requests
          </h1>
          <p className="text-slate-500 mt-1">Apply for leave and track your requests</p>
        </div>
        <button onClick={() => setShowModal(true)} className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700">
          <Plus className="w-4 h-4" /> Apply for Leave
        </button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input type="text" value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search..."
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400" />
        </div>
        <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-indigo-400">
          <option value="">All Statuses</option>
          <option value="PENDING">Pending</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <FileText className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No leave requests</h3>
          <p className="text-slate-500 text-sm">Apply for leave to get started.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Leave Type</th>
                <th className="text-left px-4 py-3">Period</th>
                <th className="text-left px-4 py-3">Days</th>
                <th className="text-left px-4 py-3">Reason</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((r) => (
                <tr key={r.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{r.leaveTypeName}</td>
                  <td className="px-4 py-3 text-slate-700 text-xs">
                    {r.startDate} to {r.endDate}
                  </td>
                  <td className="px-4 py-3 text-slate-700">{r.totalDays}</td>
                  <td className="px-4 py-3 text-slate-700 truncate max-w-xs">{r.reason || '—'}</td>
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
                        <button onClick={() => handleCancel(r)} className="p-1.5 rounded hover:bg-rose-50" title="Cancel">
                          <XCircle className="w-4 h-4 text-rose-600" />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <LeaveRequestFormModal open={showModal} onClose={() => setShowModal(false)} leaveTypes={leaveTypes} onSuccess={async () => { setShowModal(false); await load(); }} />

      {viewing && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4" onClick={() => setViewing(null)}>
          <div className="bg-white rounded-2xl w-full max-w-md p-6 shadow-xl" onClick={(e) => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-semibold">Leave Request Details</h3>
              <button onClick={() => setViewing(null)} className="p-1 rounded hover:bg-slate-100"><XCircle className="w-5 h-5" /></button>
            </div>
            <div className="space-y-3 text-sm">
              <div><span className="font-medium text-slate-600">Type:</span> {viewing.leaveTypeName}</div>
              <div><span className="font-medium text-slate-600">Period:</span> {viewing.startDate} to {viewing.endDate}</div>
              <div><span className="font-medium text-slate-600">Days:</span> {viewing.totalDays}</div>
              <div><span className="font-medium text-slate-600">Reason:</span> {viewing.reason || '—'}</div>
              <div><span className="font-medium text-slate-600">Status:</span> <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[viewing.status]}`}>{viewing.status}</span></div>
              {viewing.rejectionReason && <div><span className="font-medium text-slate-600">Rejection Reason:</span> {viewing.rejectionReason}</div>}
              <div><span className="font-medium text-slate-600">Applied:</span> {viewing.createdAt ? new Date(viewing.createdAt).toLocaleDateString() : '—'}</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
