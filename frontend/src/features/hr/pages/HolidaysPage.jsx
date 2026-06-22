import { useEffect, useState } from 'react';
import {
  Calendar,
  Plus,
  Search,
  Edit2,
  Trash2,
} from 'lucide-react';
import { toast } from 'sonner';
import { listHolidays, deleteHoliday } from '../api/hrApi';
import HolidayFormModal from '../components/HolidayFormModal';
import { TableRowSkeleton } from '../../../components/LoadingSkeleton';
import EmptyState from '../../../components/EmptyState';
import ErrorState from '../../../components/ErrorState';
import ConfirmationDialog from '../../projects/components/members/ConfirmationDialog';

export default function HolidaysPage() {
  const [holidays, setHolidays] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [itemToDelete, setItemToDelete] = useState(null);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await listHolidays();
      setHolidays(data);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  };

  const filtered = holidays.filter((h) => {
    const q = search.trim().toLowerCase();
    if (!q) return true;
    return h.name?.toLowerCase().includes(q) || h.description?.toLowerCase().includes(q);
  });

  const openCreate = () => { setEditing(null); setShowModal(true); };
  const openEdit = (h) => { setEditing(h); setShowModal(true); };

  const handleDelete = (h) => {
    setItemToDelete(h);
    setShowConfirmDialog(true);
  };

  const confirmDelete = async () => {
    if (!itemToDelete) return;
    try {
      await deleteHoliday(itemToDelete.id);
      toast.success('Holiday deleted');
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
              {[1, 2, 3, 4, 5].map((i) => <TableRowSkeleton key={i} columns={4} />)}
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
            <Calendar className="w-7 h-7 text-indigo-600" /> Holidays
          </h1>
          <p className="text-slate-500 mt-1">
            {holidays.length} {holidays.length === 1 ? 'holiday' : 'holidays'}
          </p>
        </div>
        <button
          onClick={openCreate}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700"
        >
          <Plus className="w-4 h-4" /> New Holiday
        </button>
      </div>

      <div className="flex gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search holidays..."
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400"
          />
        </div>
      </div>

      {filtered.length === 0 ? (
        <EmptyState
          icon={Calendar}
          title="No holidays yet"
          description="Add public holidays and company days off for your team."
          action={{ label: 'New Holiday', icon: Plus, onClick: openCreate }}
        />
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Name</th>
                <th className="text-left px-4 py-3">Date</th>
                <th className="text-left px-4 py-3">Description</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((h) => (
                <tr key={h.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{h.name}</td>
                  <td className="px-4 py-3 text-slate-700">{h.holidayDate}</td>
                  <td className="px-4 py-3 text-slate-700 truncate max-w-xs">{h.description || 'ΓÇö'}</td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex gap-1">
                      <button onClick={() => openEdit(h)} className="p-1.5 rounded hover:bg-slate-100" title="Edit">
                        <Edit2 className="w-4 h-4 text-slate-600" />
                      </button>
                      <button onClick={() => handleDelete(h)} className="p-1.5 rounded hover:bg-rose-50" title="Delete">
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

      <HolidayFormModal
        open={showModal}
        onClose={() => setShowModal(false)}
        initial={editing}
        onSuccess={async () => { setShowModal(false); await load(); }}
      />

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setItemToDelete(null); }}
        onConfirm={confirmDelete}
        title="Delete holiday?"
        message={itemToDelete ? `Delete "${itemToDelete.name}"? This cannot be undone.` : ''}
        confirmText="Delete"
      />

      <p className="text-xs text-slate-400 mt-6 flex items-center gap-1">
        <Calendar className="w-3 h-3" /> HR module ┬╖ VinnCorp ERP
      </p>
    </div>
  );
}
