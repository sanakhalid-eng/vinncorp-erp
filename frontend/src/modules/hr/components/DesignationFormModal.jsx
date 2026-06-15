import { useEffect, useState } from 'react';
import { X } from 'lucide-react';

const empty = { title: '', code: '', description: '', level: 0, active: true };

export default function DesignationFormModal({
  open,
  onClose,
  onSubmit,
  initial,
  submitting = false,
}) {
  const [form, setForm] = useState(empty);
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      setForm({ ...empty, ...(initial || {}), active: initial?.active ?? true });
      setError('');
    }
  }, [open, initial]);

  if (!open) return null;

  const set = (k, v) => setForm((p) => ({ ...p, [k]: v }));

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.title.trim()) return setError('Title is required');
    const payload = { ...form };
    if (!payload.code) delete payload.code;
    if (!payload.description) delete payload.description;
    if (payload.level === '' || payload.level == null) payload.level = 0;
    onSubmit(payload);
  };

  return (
    <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl w-full max-w-lg shadow-xl">
        <div className="flex items-center justify-between p-5 border-b border-slate-200">
          <h2 className="text-xl font-semibold text-slate-900">
            {initial?.id ? 'Edit Designation' : 'New Designation'}
          </h2>
          <button type="button" onClick={onClose} className="p-1.5 rounded-lg hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {error && (
            <div className="rounded-lg bg-rose-50 text-rose-700 px-3 py-2 text-sm">{error}</div>
          )}
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Title *</label>
            <input
              value={form.title || ''}
              onChange={(e) => set('title', e.target.value)}
              placeholder="e.g. Senior Software Engineer"
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Code</label>
            <input
              value={form.code || ''}
              onChange={(e) => set('code', e.target.value)}
              placeholder="e.g. SSE, MGR, DIR"
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Level</label>
            <input
              type="number"
              value={form.level ?? 0}
              onChange={(e) => set('level', parseInt(e.target.value, 10) || 0)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Description</label>
            <textarea
              value={form.description || ''}
              onChange={(e) => set('description', e.target.value)}
              rows={3}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
            />
          </div>
          <label className="flex items-center gap-2 text-sm text-slate-700">
            <input
              type="checkbox"
              checked={!!form.active}
              onChange={(e) => set('active', e.target.checked)}
              className="rounded"
            />
            Active
          </label>

          <div className="flex justify-end gap-2 pt-4 border-t border-slate-200">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg text-sm font-medium text-slate-700 hover:bg-slate-100"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="px-4 py-2 rounded-lg text-sm font-medium bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50"
            >
              {submitting ? 'Saving…' : initial?.id ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
