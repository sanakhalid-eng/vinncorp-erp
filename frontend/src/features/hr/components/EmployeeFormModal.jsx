import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { EmploymentTypes, EmployeeStatuses } from '../api/hrApi';

const empty = {
  employeeCode: '',
  firstName: '',
  lastName: '',
  workEmail: '',
  personalEmail: '',
  phone: '',
  employmentType: 'FULL_TIME',
  status: 'ACTIVE',
  dateOfBirth: '',
  hireDate: '',
  terminationDate: '',
  jobTitle: '',
  timezone: '',
  locale: '',
  managerId: '',
  userId: '',
  departmentId: '',
  designationId: '',
};

export default function EmployeeFormModal({
  open,
  onClose,
  onSubmit,
  initial,
  departments = [],
  designations = [],
  submitting = false,
}) {
  const [form, setForm] = useState(empty);
  const [error, setError] = useState('');

  useEffect(() => {
    if (open) {
      setForm({ ...empty, ...(initial || {}) });
      setError('');
    }
  }, [open, initial]);

  if (!open) return null;

  const set = (k, v) => setForm((p) => ({ ...p, [k]: v }));

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.employeeCode.trim()) return setError('Employee code is required');
    if (!form.firstName.trim()) return setError('First name is required');
    if (!form.lastName.trim()) return setError('Last name is required');
    if (!form.hireDate) return setError('Hire date is required');

    const payload = { ...form };
    if (!payload.workEmail) delete payload.workEmail;
    if (!payload.personalEmail) delete payload.personalEmail;
    if (!payload.phone) delete payload.phone;
    if (!payload.dateOfBirth) delete payload.dateOfBirth;
    if (!payload.terminationDate) delete payload.terminationDate;
    if (!payload.jobTitle) delete payload.jobTitle;
    if (!payload.timezone) delete payload.timezone;
    if (!payload.locale) delete payload.locale;
    ['managerId', 'userId', 'departmentId', 'designationId'].forEach((k) => {
      if (payload[k] === '' || payload[k] == null) delete payload[k];
    });

    onSubmit(payload);
  };

  return (
    <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-xl">
        <div className="flex items-center justify-between p-5 border-b border-slate-200">
          <h2 className="text-xl font-semibold text-slate-900">
            {initial?.id ? 'Edit Employee' : 'New Employee'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-slate-100"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-5 space-y-4">
          {error && (
            <div className="rounded-lg bg-rose-50 text-rose-700 px-3 py-2 text-sm">
              {error}
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Field label="Employee Code *" value={form.employeeCode} onChange={(v) => set('employeeCode', v)} />
            <Field label="Job Title" value={form.jobTitle} onChange={(v) => set('jobTitle', v)} />
            <Field label="First Name *" value={form.firstName} onChange={(v) => set('firstName', v)} />
            <Field label="Last Name *" value={form.lastName} onChange={(v) => set('lastName', v)} />
            <Field label="Work Email" type="email" value={form.workEmail} onChange={(v) => set('workEmail', v)} />
            <Field label="Personal Email" type="email" value={form.personalEmail} onChange={(v) => set('personalEmail', v)} />
            <Field label="Phone" value={form.phone} onChange={(v) => set('phone', v)} />
            <Field label="Date of Birth" type="date" value={form.dateOfBirth} onChange={(v) => set('dateOfBirth', v)} />
            <Field label="Hire Date *" type="date" value={form.hireDate} onChange={(v) => set('hireDate', v)} />
            <Field label="Termination Date" type="date" value={form.terminationDate} onChange={(v) => set('terminationDate', v)} />
            <Field label="Timezone" value={form.timezone} onChange={(v) => set('timezone', v)} placeholder="e.g. Asia/Karachi" />
            <Field label="Locale" value={form.locale} onChange={(v) => set('locale', v)} placeholder="e.g. en-US" />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Select label="Employment Type" value={form.employmentType} onChange={(v) => set('employmentType', v)} options={EmploymentTypes} />
            <Select label="Status" value={form.status} onChange={(v) => set('status', v)} options={EmployeeStatuses} />
            <Select
              label="Department"
              value={form.departmentId}
              onChange={(v) => set('departmentId', v)}
              options={[{ value: '', label: 'ΓÇö None ΓÇö' }, ...departments.map((d) => ({ value: d.id, label: d.name }))]}
            />
            <Select
              label="Designation"
              value={form.designationId}
              onChange={(v) => set('designationId', v)}
              options={[{ value: '', label: 'ΓÇö None ΓÇö' }, ...designations.map((d) => ({ value: d.id, label: d.title }))]}
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
              {submitting ? 'SavingΓÇª' : initial?.id ? 'Update' : 'Create'}
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
        value={value || ''}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
      />
    </div>
  );
}

function Select({ label, value, onChange, options }) {
  return (
    <div>
      <label className="block text-xs font-medium text-slate-600 mb-1">{label}</label>
      <select
        value={value ?? ''}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm outline-none focus:border-indigo-400"
      >
        {options.map((o) =>
          typeof o === 'string' ? (
            <option key={o} value={o}>
              {o}
            </option>
          ) : (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          )
        )}
      </select>
    </div>
  );
}
