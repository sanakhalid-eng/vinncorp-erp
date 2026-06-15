import { useEffect, useMemo, useState } from 'react';
import { Plus, Search, Loader2, BadgeCheck, Edit2, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import {
  listDesignations,
  createDesignation,
  updateDesignation,
  deleteDesignation,
} from '../api/hrApi';
import DesignationFormModal from '../components/DesignationFormModal';

export default function DesignationsPage() {
  const [designations, setDesignations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [activeOnly, setActiveOnly] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    load();
  }, [activeOnly]);

  const load = async () => {
    setLoading(true);
    try {
      const rows = await listDesignations(activeOnly);
      setDesignations(rows);
    } catch (e) {
      toast.error(e.message || 'Failed to load designations');
    } finally {
      setLoading(false);
    }
  };

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    const list = q
      ? designations.filter(
          (d) =>
            d.title?.toLowerCase().includes(q) ||
            d.code?.toLowerCase().includes(q) ||
            d.description?.toLowerCase().includes(q)
        )
      : designations;
    return [...list].sort((a, b) => (a.level ?? 0) - (b.level ?? 0) || a.title.localeCompare(b.title));
  }, [designations, search]);

  const handleSubmit = async (payload) => {
    setSubmitting(true);
    try {
      if (editing?.id) {
        await updateDesignation(editing.id, payload);
        toast.success('Designation updated');
      } else {
        await createDesignation(payload);
        toast.success('Designation created');
      }
      setShowModal(false);
      await load();
    } catch (e) {
      toast.error(e.message || 'Save failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (d) => {
    if (!window.confirm(`Delete designation "${d.title}"?`)) return;
    try {
      await deleteDesignation(d.id);
      toast.success('Designation deleted');
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
            <BadgeCheck className="w-7 h-7 text-indigo-600" /> Designations
          </h1>
          <p className="text-slate-500 mt-1">
            {designations.length} {designations.length === 1 ? 'designation' : 'designations'}
          </p>
        </div>
        <button
          onClick={() => {
            setEditing(null);
            setShowModal(true);
          }}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700"
        >
          <Plus className="w-4 h-4" /> New Designation
        </button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search designations…"
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400"
          />
        </div>
        <label className="inline-flex items-center gap-2 px-4 py-3 rounded-xl border border-slate-200 bg-white text-sm text-slate-700">
          <input
            type="checkbox"
            checked={activeOnly}
            onChange={(e) => setActiveOnly(e.target.checked)}
            className="rounded"
          />
          Active only
        </label>
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <BadgeCheck className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No designations yet</h3>
          <p className="text-slate-500 text-sm">Create designations for job roles and levels.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Title</th>
                <th className="text-left px-4 py-3">Code</th>
                <th className="text-left px-4 py-3">Level</th>
                <th className="text-left px-4 py-3">Description</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((d) => (
                <tr key={d.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3 font-medium text-slate-900">{d.title}</td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-700">{d.code || '—'}</td>
                  <td className="px-4 py-3 text-slate-700">{d.level ?? 0}</td>
                  <td className="px-4 py-3 text-slate-600 max-w-xs truncate">{d.description || '—'}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${
                        d.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                      }`}
                    >
                      {d.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex gap-1">
                      <button
                        onClick={() => {
                          setEditing(d);
                          setShowModal(true);
                        }}
                        className="p-1.5 rounded hover:bg-slate-100"
                        title="Edit"
                      >
                        <Edit2 className="w-4 h-4 text-slate-600" />
                      </button>
                      <button
                        onClick={() => handleDelete(d)}
                        className="p-1.5 rounded hover:bg-rose-50"
                        title="Delete"
                      >
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

      <DesignationFormModal
        open={showModal}
        onClose={() => setShowModal(false)}
        onSubmit={handleSubmit}
        initial={editing}
        submitting={submitting}
      />
    </div>
  );
}
