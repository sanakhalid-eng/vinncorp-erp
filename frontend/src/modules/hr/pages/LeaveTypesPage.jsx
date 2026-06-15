import { useEffect, useState } from 'react';
import { Loader2, Tag, Plus, Search, Edit2, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { listLeaveTypes, deleteLeaveType } from '../api/hrApi';
import LeaveTypeFormModal from '../components/LeaveTypeFormModal';

export default function LeaveTypesPage() {
  const [types, setTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);

  useEffect(() => { load(); }, []);

  const load = async () => {
    setLoading(true);
    try {
      setTypes(await listLeaveTypes());
    } catch (e) {
      toast.error(e.message || 'Failed to load leave types');
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

  const handleDelete = async (t) => {
    if (!window.confirm(`Delete leave type "${t.name}"?`)) return;
    try {
      await deleteLeaveType(t.id);
      toast.success('Leave type deleted');
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
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <Tag className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No leave types found</h3>
          <p className="text-slate-500 text-sm">Create your first leave type to get started.</p>
        </div>
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
    </div>
  );
}
