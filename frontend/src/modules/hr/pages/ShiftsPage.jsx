import { useEffect, useState } from 'react';
import {
  Loader2,
  Clock,
  Plus,
  Search,
  Edit2,
  Trash2,
} from 'lucide-react';
import { toast } from 'sonner';
import { listShifts, deleteShift } from '../api/hrApi';
import ShiftFormModal from '../components/ShiftFormModal';

export default function ShiftsPage() {
  const [shifts, setShifts] = useState([]);
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
      const data = await listShifts();
      setShifts(data);
    } catch (e) {
      toast.error(e.message || 'Failed to load shifts');
    } finally {
      setLoading(false);
    }
  };

  const filtered = shifts.filter((s) => {
    const q = search.trim().toLowerCase();
    if (!q) return true;
    return s.name?.toLowerCase().includes(q);
  });

  const openCreate = () => { setEditing(null); setShowModal(true); };
  const openEdit = (s) => { setEditing(s); setShowModal(true); };

  const handleDelete = async (s) => {
    if (!window.confirm(`Delete shift "${s.name}"?`)) return;
    try {
      await deleteShift(s.id);
      toast.success('Shift deleted');
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
            <Clock className="w-7 h-7 text-indigo-600" /> Shifts
          </h1>
          <p className="text-slate-500 mt-1">
            {shifts.length} {shifts.length === 1 ? 'shift' : 'shifts'}
          </p>
        </div>
        <button
          onClick={openCreate}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700"
        >
          <Plus className="w-4 h-4" /> New Shift
        </button>
      </div>

      <div className="flex gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search shifts..."
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400"
          />
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <Clock className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No shifts found</h3>
          <p className="text-slate-500 text-sm">Create your first shift to get started.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Name</th>
                <th className="text-left px-4 py-3">Start Time</th>
                <th className="text-left px-4 py-3">End Time</th>
                <th className="text-left px-4 py-3">Break (min)</th>
                <th className="text-left px-4 py-3">Grace (min)</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((s) => (
                <tr key={s.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{s.name}</td>
                  <td className="px-4 py-3 text-slate-700">{s.startTime}</td>
                  <td className="px-4 py-3 text-slate-700">{s.endTime}</td>
                  <td className="px-4 py-3 text-slate-700">{s.breakMinutes}</td>
                  <td className="px-4 py-3 text-slate-700">{s.gracePeriodMinutes}</td>
                  <td className="px-4 py-3">
                    <span className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${s.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                      {s.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex gap-1">
                      <button onClick={() => openEdit(s)} className="p-1.5 rounded hover:bg-slate-100" title="Edit">
                        <Edit2 className="w-4 h-4 text-slate-600" />
                      </button>
                      <button onClick={() => handleDelete(s)} className="p-1.5 rounded hover:bg-rose-50" title="Delete">
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

      <ShiftFormModal
        open={showModal}
        onClose={() => setShowModal(false)}
        initial={editing}
        onSuccess={async () => { setShowModal(false); await load(); }}
      />

      <p className="text-xs text-slate-400 mt-6 flex items-center gap-1">
        <Clock className="w-3 h-3" /> HR module · VinnCorp ERP
      </p>
    </div>
  );
}
