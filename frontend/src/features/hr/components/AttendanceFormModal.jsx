import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { updateAttendance, AttendanceStatuses } from '../api/hrApi';
import { toast } from 'sonner';

const empty = {
  employeeId: '',
  attendanceDate: '',
  shiftId: '',
  status: '',
  checkInTime: '',
  checkOutTime: '',
  workHours: '',
  overtimeHours: '',
  lateMinutes: '',
  earlyLeaveMinutes: '',
  notes: '',
};

export default function AttendanceFormModal({
  open,
  onClose,
  initial,
  employees = [],
  shifts = [],
  onSuccess,
}) {
  const [form, setForm] = useState(empty);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      if (initial?.id) {
        setForm({
          employeeId: initial.employeeId ?? '',
          attendanceDate: initial.attendanceDate ?? '',
          shiftId: initial.shiftId ?? '',
          status: initial.status ?? '',
          checkInTime: initial.checkInTime ? initial.checkInTime.replace('T', '').slice(0, 16) : '',
          checkOutTime: initial.checkOutTime ? initial.checkOutTime.replace('T', '').slice(0, 16) : '',
          workHours: initial.workHours ?? '',
          overtimeHours: initial.overtimeHours ?? '',
          lateMinutes: initial.lateMinutes ?? '',
          earlyLeaveMinutes: initial.earlyLeaveMinutes ?? '',
          notes: initial.notes ?? '',
        });
      } else {
        setForm(empty);
      }
      setError('');
    }
  }, [open, initial]);

  if (!open) return null;

  const set = (k, v) => setForm((p) => ({ ...p, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!initial?.id) return setError('Edit mode only');
    setSubmitting(true);
    try {
      const payload = {};
      if (form.status) payload.status = form.status;
      if (form.checkInTime) payload.checkInTime = form.checkInTime;
      if (form.checkOutTime) payload.checkOutTime = form.checkOutTime;
      if (form.workHours) payload.workHours = parseFloat(form.workHours);
      if (form.overtimeHours) payload.overtimeHours = parseFloat(form.overtimeHours);
      if (form.lateMinutes !== '') payload.lateMinutes = parseInt(form.lateMinutes) || 0;
      if (form.earlyLeaveMinutes !== '') payload.earlyLeaveMinutes = parseInt(form.earlyLeaveMinutes) || 0;
      if (form.notes) payload.notes = form.notes;
      if (form.shiftId) payload.shiftId = parseInt(form.shiftId) || null;

      await updateAttendance(initial.id, payload);
      toast.success('Attendance updated');
      onSuccess?.();
    } catch (e2) {
      setError(e2.message || 'Update failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl w-full max-w-xl max-h-[90vh] overflow-y-auto shadow-xl">
        <div className="flex items-center justify-between p-5 border-b border-slate-200">
          <h2 className="text-xl font-semibold text-slate-900">Edit Attendance</h2>
          <button type="button" onClick={onClose} className="p-1.5 rounded-lg hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {error && (
            <div className="rounded-lg bg-rose-50 text-rose-700 px-3 py-2 text-sm">{error}</div>
          )}

          <div className="text-sm text-slate-600">
            <strong>Employee:</strong> {initial?.employeeName || 'ΓÇö'}
          </div>
          <div className="text-sm text-slate-600">
            <strong>Date:</strong> {initial?.attendanceDate || 'ΓÇö'}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-600 mb-1">Status</label>
              <select
                value={form.status}
                onChange={(e) => set('status', e.target.value)}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
              >
                <option value="">ΓÇö Select ΓÇö</option>
                {AttendanceStatuses.map((s) => (
                  <option key={s} value={s}>{s.replace('_', ' ')}</option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-600 mb-1">Shift</label>
              <select
                value={form.shiftId}
                onChange={(e) => set('shiftId', e.target.value)}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
              >
                <option value="">ΓÇö None ΓÇö</option>
                {shifts.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
            <Field label="Work Hours" type="number" value={form.workHours} onChange={(v) => set('workHours', v)} />
            <Field label="Overtime Hours" type="number" value={form.overtimeHours} onChange={(v) => set('overtimeHours', v)} />
            <Field label="Late Minutes" type="number" value={form.lateMinutes} onChange={(v) => set('lateMinutes', v)} />
            <Field label="Early Leave Minutes" type="number" value={form.earlyLeaveMinutes} onChange={(v) => set('earlyLeaveMinutes', v)} />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-600 mb-1">Notes</label>
            <textarea
              value={form.notes}
              onChange={(e) => set('notes', e.target.value)}
              rows={2}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
            />
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
              {submitting ? 'Saving...' : 'Update'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function Field({ label, value, onChange, type = 'text' }) {
  return (
    <div>
      <label className="block text-xs font-medium text-slate-600 mb-1">{label}</label>
      <input
        type={type}
        value={value ?? ''}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
      />
    </div>
  );
}
