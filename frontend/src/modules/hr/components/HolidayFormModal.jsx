import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { createHoliday, updateHoliday } from '../api/hrApi';
import { toast } from 'sonner';

const empty = {
  name: '',
  holidayDate: '',
  holidayType: 'PUBLIC',
  description: '',
  isRecurring: false,
};

export default function HolidayFormModal({ open, onClose, initial, onSuccess }) {
  const [form, setForm] = useState(empty);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      setForm({ ...empty, ...(initial || {}) });
      setError('');
    }
  }, [open, initial]);

  if (!open) return null;

  const set = (k, v) => setForm((p) => ({ ...p, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim()) return setError('Holiday name is required');
    if (!form.holidayDate) return setError('Holiday date is required');

    setSubmitting(true);
    try {
      const payload = {
        name: form.name,
        holidayDate: form.holidayDate,
        holidayType: form.holidayType || 'PUBLIC',
        description: form.description || null,
        isRecurring: !!form.isRecurring,
      };
      if (initial?.id) {
        await updateHoliday(initial.id, payload);
        toast.success('Holiday updated');
      } else {
        await createHoliday(payload);
        toast.success('Holiday created');
      }
      onSuccess?.();
    } catch (e2) {
      setError(e2.message || 'Save failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl w-full max-w-md max-h-[90vh] overflow-y-auto shadow-xl">
        <div className="flex items-center justify-between p-5 border-b border-slate-200">
          <h2 className="text-xl font-semibold text-slate-900">
            {initial?.id ? 'Edit Holiday' : 'New Holiday'}
          </h2>
          <button type="button" onClick={onClose} className="p-1.5 rounded-lg hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {error && (
            <div className="rounded-lg bg-rose-50 text-rose-700 px-3 py-2 text-sm">{error}</div>
          )}

          <Field label="Holiday Name *" value={form.name} onChange={(v) => set('name', v)} placeholder="e.g. Independence Day" />
          <Field label="Holiday Date *" type="date" value={form.holidayDate} onChange={(v) => set('holidayDate', v)} />
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Type</label>
            <select
              value={form.holidayType}
              onChange={(e) => set('holidayType', e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
            >
              <option value="PUBLIC">Public</option>
              <option value="NATIONAL">National</option>
              <option value="RELIGIOUS">Religious</option>
              <option value="COMPANY">Company</option>
            </select>
          </div>
          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Description</label>
            <textarea
              value={form.description}
              onChange={(e) => set('description', e.target.value)}
              rows={3}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
            />
          </div>
          <div className="flex items-center gap-2">
            <input
              type="checkbox"
              id="isRecurring"
              checked={!!form.isRecurring}
              onChange={(e) => set('isRecurring', e.target.checked)}
              className="rounded border-slate-300"
            />
            <label htmlFor="isRecurring" className="text-sm text-slate-600">Recurring annually</label>
          </div>

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
              {submitting ? 'Saving...' : initial?.id ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Field({ label, value, onChange, type = 'text', placeholder = '' }) {
  return (
    <div>
      <label className="block text-xs font-medium text-slate-600 mb-1">{label}</label>
      <input
        type={type}
        value={value ?? ''}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
      />
    </div>
  );
}
