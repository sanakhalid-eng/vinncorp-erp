import { useEffect, useState } from 'react';
import {
  UserPlus,
  Loader2,
  Trash2,
  Briefcase,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  listEmployees,
  getProjectAssignments,
  createProjectAssignment,
  unassignProject,
} from '../api/hrApi';
import ProjectAssignmentModal from './ProjectAssignmentModal';

export default function ProjectAssignmentPage({ projectId }) {
  const [assignments, setAssignments] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (projectId) loadData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [projectId]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [assignData, empData] = await Promise.all([
        getProjectAssignments(projectId),
        listEmployees(),
      ]);
      setAssignments(assignData);
      setEmployees(empData);
    } catch (e) {
      toast.error(e.message || 'Failed to load assignments');
    } finally {
      setLoading(false);
    }
  };

  const handleAssign = async (payload) => {
    setSubmitting(true);
    try {
      await createProjectAssignment({ ...payload, projectId });
      toast.success('Employee assigned to project');
      setShowModal(false);
      await loadData();
    } catch (e) {
      toast.error(e.message || 'Failed to assign');
    } finally {
      setSubmitting(false);
    }
  };

  const handleUnassign = async (id) => {
    if (!window.confirm('Remove this employee from the project?')) return;
    try {
      await unassignProject(id);
      toast.success('Employee unassigned');
      await loadData();
    } catch (e) {
      toast.error(e.message || 'Failed to unassign');
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <Loader2 className="w-6 h-6 animate-spin text-indigo-600" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-slate-900 flex items-center gap-2">
          <Briefcase className="w-5 h-5 text-indigo-600" />
          Assigned Employees
        </h3>
        <button
          onClick={() => setShowModal(true)}
          className="inline-flex items-center gap-2 px-3 py-1.5 rounded-lg bg-indigo-600 text-white text-xs font-medium hover:bg-indigo-700"
        >
          <UserPlus className="w-3 h-3" /> Assign Employee
        </button>
      </div>

      {assignments.length === 0 ? (
        <p className="text-sm text-slate-500 text-center py-6">No employees assigned to this project.</p>
      ) : (
        <div className="space-y-2">
          {assignments.map((a) => (
            <div key={a.id} className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-100">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-700 text-xs font-bold">
                  {a.employeeName?.charAt(0) || 'E'}
                </div>
                <div>
                  <p className="text-sm font-medium text-slate-900">{a.employeeName}</p>
                  <p className="text-xs text-slate-500">{a.roleInProject || 'Team Member'} · {a.allocationPercentage || 100}%</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${a.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'}`}>
                  {a.status}
                </span>
                <button
                  onClick={() => handleUnassign(a.id)}
                  className="p-1 rounded hover:bg-rose-50"
                  title="Unassign"
                >
                  <Trash2 className="w-3.5 h-3.5 text-rose-600" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <ProjectAssignmentModal
        open={showModal}
        onClose={() => setShowModal(false)}
        onSubmit={handleAssign}
        employees={employees}
        submitting={submitting}
      />
    </div>
  );
}
