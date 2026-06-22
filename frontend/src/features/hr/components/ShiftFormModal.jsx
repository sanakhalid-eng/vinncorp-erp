import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { createShift, updateShift } from '../api/hrApi';
import { toast } from 'sonner';

const empty = {
  name: '',
  startTime: '',
  endTime: '',
  breakMinutes: 0,
  gracePeriodMinutes: 0,
};

export default function ShiftFormModal({ open, onClose, initial, onSuccess }) {
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
    if (!form.name.trim()) return setError('Shift name is required');
    if (!form.startTime) return setError('Start time is required');
    if (!form.endTime) return setError('End time is required');

    setSubmitting(true);
    try {
      const payload = {
        name: form.name,
        startTime: form.startTime,
        endTime: form.endTime,
        breakMinutes: parseInt(form.breakMinutes) || 0,
        gracePeriodMinutes: parseInt(form.gracePeriodMinutes) || 0,
      };
      if (initial?.id) {
        await updateShift(initial.id, payload);
        toast.success('Shift updated');
      } else {
        await createShift(payload);
        toast.success('Shift created');
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
            {initial?.id ? 'Edit Shift' : 'New Shift'}
          </h2>
          <button type="button" onClick={onClose} className="p-1.5 rounded-lg hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {error && (
            <div className="rounded-lg bg-rose-50 text-rose-700 px-3 py-2 text-sm">{error}</div>
          )}

          <Field label="Shift Name *" value={form.name} onChange={(v) => set('name', v)} placeholder="e.g. Morning Shift" />
          <Field label="Start Time *" type="time" value={form.startTime} onChange={(v) => set('startTime', v)} />
          <Field label="End Time *" type="time" value={form.endTime} onChange={(v) => set('endTime', v)} />
          <Field label="Break Minutes" type="number" value={form.breakMinutes} onChange={(v) => set('breakMinutes', v)} />
          <Field label="Grace Period Minutes" type="number" value={form.gracePeriodMinutes} onChange={(v) => set('gracePeriodMinutes', v)} />

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
