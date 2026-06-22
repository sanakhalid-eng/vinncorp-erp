import { useEffect, useMemo, useState } from 'react';
import { Plus, Search, Network, Edit2, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import {
  listDepartments,
  createDepartment,
  updateDepartment,
  deleteDepartment,
} from '../api/hrApi';
import DepartmentFormModal from '../components/DepartmentFormModal';
import { PageSkeleton } from '../../../components/LoadingSkeleton';
import { EmptyState } from '../../../components/EmptyStates';
import ErrorState from '../../../components/ErrorState';
import ConfirmationDialog from '../../projects/components/members/ConfirmationDialog';

export default function DepartmentsPage() {
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');
  const [activeOnly, setActiveOnly] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmAction, setConfirmAction] = useState(null);

  useEffect(() => {
    load();
  }, [activeOnly]);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const rows = await listDepartments(activeOnly);
      setDepartments(rows);
    } catch (e) {
      setError(e.message || 'Failed to load departments');
      toast.error(e.message || 'Failed to load departments');
    } finally {
      setLoading(false);
    }
  };

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return departments;
    return departments.filter(
      (d) =>
        d.name?.toLowerCase().includes(q) ||
        d.code?.toLowerCase().includes(q) ||
        d.description?.toLowerCase().includes(q)
    );
  }, [departments, search]);

  const handleSubmit = async (payload) => {
    setSubmitting(true);
    try {
      if (editing?.id) {
        await updateDepartment(editing.id, payload);
        toast.success('Department updated');
      } else {
        await createDepartment(payload);
        toast.success('Department created');
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
    setConfirmAction(() => async () => {
      try {
        await deleteDepartment(d.id);
        toast.success('Department deleted');
        await load();
      } catch (e) {
        toast.error(e.message || 'Delete failed');
      }
    });
    setShowConfirmDialog(true);
  };

  if (loading) return <PageSkeleton />;

  if (error) return <ErrorState title="Failed to load departments" message={error} onRetry={load} />;

  return (
    <div className="p-4 sm:p-6">
      <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-2">
            <Network className="w-7 h-7 text-indigo-600" /> Departments
          </h1>
          <p className="text-slate-500 mt-1">
            {departments.length} {departments.length === 1 ? 'department' : 'departments'}
          </p>
        </div>
        <button
          onClick={() => {
            setEditing(null);
            setShowModal(true);
          }}
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-sm font-medium hover:bg-indigo-700"
        >
          <Plus className="w-4 h-4" /> New Department
        </button>
      </div>

      <div className="flex flex-col sm:flex-row gap-3 mb-6">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search departmentsΓÇª"
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
        <EmptyState title="No departments yet" message="Create departments to organise your workforce." />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((d) => (
            <div
              key={d.id}
              className="bg-white rounded-2xl border border-slate-200 p-5 hover:shadow-md transition-shadow"
            >
              <div className="flex items-start justify-between mb-2">
                <div>
                  <h3 className="font-semibold text-slate-900">{d.name}</h3>
                  {d.code && <span className="text-xs font-mono text-slate-500">{d.code}</span>}
                </div>
                <span
                  className={`inline-block px-2 py-0.5 rounded-full text-xs font-medium ${
                    d.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                  }`}
                >
                  {d.active ? 'Active' : 'Inactive'}
                </span>
              </div>
              {d.description && (
                <p className="text-sm text-slate-600 line-clamp-3 mb-3">{d.description}</p>
              )}
              <div className="flex justify-end gap-1 pt-2 border-t border-slate-100">
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
            </div>
          ))}
        </div>
      )}

      <DepartmentFormModal
        open={showModal}
        onClose={() => setShowModal(false)}
        onSubmit={handleSubmit}
        initial={editing}
        departments={departments}
        submitting={submitting}
      />

      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => setShowConfirmDialog(false)}
        onConfirm={() => { confirmAction(); setShowConfirmDialog(false); }}
        title="Confirm Delete"
        message="Are you sure you want to delete this department?"
        confirmText="Delete"
      />
    </div>
  );
}
