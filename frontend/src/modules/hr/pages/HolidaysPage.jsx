import { useEffect, useState } from 'react';
import {
  Loader2,
  Calendar,
  Plus,
  Search,
  Edit2,
  Trash2,
} from 'lucide-react';
import { toast } from 'sonner';
import { listHolidays, deleteHoliday } from '../api/hrApi';
import HolidayFormModal from '../components/HolidayFormModal';

export default function HolidaysPage() {
  const [holidays, setHolidays] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    try {
      const data = await listHolidays();
      setHolidays(data);
    } catch (e) {
      toast.error(e.message || 'Failed to load holidays');
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

  const handleDelete = async (h) => {
    if (!window.confirm(`Delete holiday "${h.name}"?`)) return;
    try {
      await deleteHoliday(h.id);
      toast.success('Holiday deleted');
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
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <Calendar className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No holidays found</h3>
          <p className="text-slate-500 text-sm">Add your first holiday to get started.</p>
        </div>
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
                  <td className="px-4 py-3 text-slate-700 truncate max-w-xs">{h.description || '—'}</td>
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

      <p className="text-xs text-slate-400 mt-6 flex items-center gap-1">
        <Calendar className="w-3 h-3" /> HR module · VinnCorp ERP
      </p>
    </div>
  );
}
