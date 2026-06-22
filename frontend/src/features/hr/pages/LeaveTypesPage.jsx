import { useEffect, useState } from 'react';
import { Tag, Plus, Search, Edit2, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { listLeaveTypes, deleteLeaveType } from '../api/hrApi';
import LeaveTypeFormModal from '../components/LeaveTypeFormModal';
import { TableRowSkeleton } from '../../../components/LoadingSkeleton';
import EmptyState from '../../../components/EmptyState';
import ErrorState from '../../../components/ErrorState';
import ConfirmationDialog from '../../projects/components/members/ConfirmationDialog';

export default function LeaveTypesPage() {
  const [types, setTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  useEffect(() => { load(); }, []);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      setTypes(await listLeaveTypes());
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = types.filter((t) => {
    const q = search.trim().toLowerCase();
    if (!q) return true;
    return t.name?.toLowerCase().includes(q) || t.code?.toLowerCase().includes(q);
  });

  const openCreate = () => { setEditing(null); setShowModal(true); };
  const openEdit = (t) => { setEditing(t); setShowModal(true); };

  const handleDelete = (t) => {
    setItemToDelete(t);
    setShowConfirmDialog(true);
  };

  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await deleteLeaveType(itemToDelete.id);
      toast.success('Leave type deleted');
      await load();
    } catch (e) {
      toast.error(e.message || 'Delete failed');
    } finally {
      setShowConfirmDialog(false);
      setItemToDelete(null);
    }
  };

  if (loading) {
    return (
      <div className="p-4 sm:p-6">
        <div className="mb-8 h-10 w-1/3 animate-pulse rounded bg-slate-200" />
        <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white">
          <table className="w-full text-sm">
            <tbody>
              {[1, 2, 3, 4, 5].map((i) => <TableRowSkeleton key={i} columns={6} />)}
            </tbody>
          </table>
        </div>
      </div>
    );
  }

  if (error) return <ErrorState error={error} onRetry={load} />;

  return (
    <div>
      <div className="mb-8 flex items-start justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
            <Tag className="w-7 h-7 text-indigo-600" /> Leave Types
          </h1>
          <p className="text-slate-500 mt-1">{types.length} {types.length === 1 ? 'type' : 'types'}</p>
        </div>
        <button onClick={openCreate} className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700">
          <Plus className="w-4 h-4" /> New Leave Type
        </button>
      </div>

      <div className="flex gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input type="text" value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search leave types..."
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400" />
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          icon={Tag}
          title="No leave types yet"
          description="Define leave types like annual, sick, and casual leave."
          action={{ label: 'New Leave Type', icon: Plus, onClick: openCreate }}
        />
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Name</th>
                <th className="text-left px-4 py-3">Code</th>
                <th className="text-left px-4 py-3">Default Days</th>
                <th className="text-left px-4 py-3">Paid</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((t) => (
                <tr key={t.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{t.name}</td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-700">{t.code}</td>
                  <td className="px-4 py-3 text-slate-700">{t.defaultDays}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${t.isPaid ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                      {t.isPaid ? 'Paid' : 'Unpaid'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${t.isActive ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                      {t.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex gap-1">
                      <button onClick={() => openEdit(t)} className="p-1.5 rounded hover:bg-slate-100" title="Edit">
                        <Edit2 className="w-4 h-4 text-slate-600" />
                      </button>
                      <button onClick={() => handleDelete(t)} className="p-1.5 rounded hover:bg-rose-50" title="Delete">
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

      <LeaveTypeFormModal open={showModal} onClose={() => setShowModal(false)} initial={editing} onSuccess={async () => { setShowModal(false); await load(); }} />
      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setItemToDelete(null); }}
        onConfirm={confirmDelete}
        title="Delete leave type?"
        message={itemToDelete ? `Delete "${itemToDelete.name}"? This cannot be undone.` : ''}
        confirmText="Delete"
      />
    </div>
  );
}
