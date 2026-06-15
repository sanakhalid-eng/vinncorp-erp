import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Plus,
  Search,
  Loader2,
  Users,
  Edit2,
  Trash2,
  Briefcase,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  listEmployees,
  createEmployee,
  updateEmployee,
  deleteEmployee,
  listDepartments,
  listDesignations,
} from '../api/hrApi';
import EmployeeFormModal from '../components/EmployeeFormModal';

const STATUS_BADGE = {
  ACTIVE: 'bg-emerald-100 text-emerald-700',
  ON_LEAVE: 'bg-amber-100 text-amber-700',
  SUSPENDED: 'bg-rose-100 text-rose-700',
  TERMINATED: 'bg-slate-200 text-slate-700',
  PROBATION: 'bg-indigo-100 text-indigo-700',
};

export default function EmployeesPage() {
  const [params, setParams] = useSearchParams();
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [designations, setDesignations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState(params.get('status') || '');
  const [deptFilter, setDeptFilter] = useState(params.get('departmentId') || '');
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    load();
  }, []);

  const load = async () => {
    setLoading(true);
    try {
      const [emps, depts, desigs] = await Promise.all([
        listEmployees(),
        listDepartments(false),
        listDesignations(false),
      ]);
      setEmployees(emps);
      setDepartments(depts);
      setDesignations(desigs);
    } catch (e) {
      toast.error(e.message || 'Failed to load HR data');
    } finally {
      setLoading(false);
    }
  };

  const deptMap = useMemo(() => Object.fromEntries(departments.map((d) => [d.id, d])), [departments]);
  const desigMap = useMemo(() => Object.fromEntries(designations.map((d) => [d.id, d])), [designations]);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    return employees.filter((e) => {
      if (statusFilter && e.status !== statusFilter) return false;
      if (deptFilter && String(e.departmentId ?? '') !== String(deptFilter)) return false;
      if (!q) return true;
      return (
        e.fullName?.toLowerCase().includes(q) ||
        e.employeeCode?.toLowerCase().includes(q) ||
        e.workEmail?.toLowerCase().includes(q) ||
        e.jobTitle?.toLowerCase().includes(q)
      );
    });
  }, [employees, search, statusFilter, deptFilter]);

  const openCreate = () => {
    setEditing(null);
    setShowModal(true);
  };
  const openEdit = (e) => {
    setEditing(e);
    setShowModal(true);
  };

  const handleSubmit = async (payload) => {
    setSubmitting(true);
    try {
      if (editing?.id) {
        await updateEmployee(editing.id, payload);
        toast.success('Employee updated');
      } else {
        await createEmployee(payload);
        toast.success('Employee created');
      }
      setShowModal(false);
      await load();
    } catch (e) {
      toast.error(e.message || 'Save failed');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (e) => {
    if (!window.confirm(`Delete employee ${e.fullName}?`)) return;
    try {
      await deleteEmployee(e.id);
      toast.success('Employee deleted');
      await load();
    } catch (err) {
      toast.error(err.message || 'Delete failed');
    }
  };

  const applyFilter = (key, value) => {
    setStatusFilter(key === 'status' ? value : statusFilter);
    setDeptFilter(key === 'departmentId' ? value : deptFilter);
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    setParams(next, { replace: true });
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
            <Users className="w-7 h-7 text-indigo-600" /> Employees
          </h1>
          <p className="text-slate-500 mt-1">
            {employees.length} {employees.length === 1 ? 'employee' : 'employees'}
          </p>
        </div>
        <button
          onClick={openCreate}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700"
        >
          <Plus className="w-4 h-4" /> New Employee
        </button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by name, code, email…"
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400"
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => applyFilter('status', e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-indigo-400"
        >
          <option value="">All Statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="ON_LEAVE">On Leave</option>
          <option value="PROBATION">Probation</option>
          <option value="SUSPENDED">Suspended</option>
          <option value="TERMINATED">Terminated</option>
        </select>
        <select
          value={deptFilter}
          onChange={(e) => applyFilter('departmentId', e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-indigo-400"
        >
          <option value="">All Departments</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name}
            </option>
          ))}
        </select>
      </div>

      {filtered.length === 0 ? (
        <div className="text-center py-20 bg-white rounded-2xl border border-slate-200">
          <Users className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-slate-600 mb-2">No employees found</h3>
          <p className="text-slate-500 text-sm">Add your first employee to get started.</p>
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-600 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Employee</th>
                <th className="text-left px-4 py-3">Code</th>
                <th className="text-left px-4 py-3">Department</th>
                <th className="text-left px-4 py-3">Designation</th>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">Status</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {filtered.map((e) => (
                <tr key={e.id} className="hover:bg-slate-50">
                  <td className="px-4 py-3">
                    <div className="font-medium text-slate-900">{e.fullName}</div>
                    <div className="text-slate-500 text-xs">{e.workEmail || '—'}</div>
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-700">{e.employeeCode}</td>
                  <td className="px-4 py-3 text-slate-700">
                    {e.departmentId ? deptMap[e.departmentId]?.name : '—'}
                  </td>
                  <td className="px-4 py-3 text-slate-700">
                    {e.designationId ? desigMap[e.designationId]?.title : '—'}
                  </td>
                  <td className="px-4 py-3 text-slate-600 text-xs">
                    {e.employmentType?.replace('_', ' ')}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${
                        STATUS_BADGE[e.status] || 'bg-slate-100 text-slate-600'
                      }`}
                    >
                      {e.status?.replace('_', ' ')}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <div className="inline-flex gap-1">
                      <button
                        onClick={() => openEdit(e)}
                        className="p-1.5 rounded hover:bg-slate-100"
                        title="Edit"
                      >
                        <Edit2 className="w-4 h-4 text-slate-600" />
                      </button>
                      <button
                        onClick={() => handleDelete(e)}
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

      <EmployeeFormModal
        open={showModal}
        onClose={() => setShowModal(false)}
        onSubmit={handleSubmit}
        initial={editing}
        departments={departments}
        designations={designations}
        submitting={submitting}
      />

      <p className="text-xs text-slate-400 mt-6 flex items-center gap-1">
        <Briefcase className="w-3 h-3" /> HR module · VinnCorp ERP
      </p>
    </div>
  );
}
