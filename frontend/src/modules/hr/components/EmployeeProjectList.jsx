import { useEffect, useState } from 'react';
import { FolderKanban, Loader2 } from 'lucide-react';
import { getEmployeeProjectAssignments } from '../api/hrApi';

const STATUS_BADGE = {
  ACTIVE: 'bg-emerald-100 text-emerald-700',
  INACTIVE: 'bg-slate-200 text-slate-600',
  COMPLETED: 'bg-blue-100 text-blue-700',
};

export default function EmployeeProjectList({ employeeId }) {
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (employeeId) loadAssignments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [employeeId]);

  const loadAssignments = async () => {
    setLoading(true);
    try {
      const data = await getEmployeeProjectAssignments(employeeId);
      setAssignments(data);
    } catch {
      // silently fail
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center py-6">
        <Loader2 className="w-5 h-5 animate-spin text-indigo-600" />
      </div>
    );
  }

  if (assignments.length === 0) {
    return (
      <p className="text-sm text-slate-500 text-center py-4">No project assignments found.</p>
    );
  }

  return (
    <div className="space-y-2">
      {assignments.map((a) => (
        <div key={a.id} className="flex items-center justify-between p-3 rounded-xl bg-slate-50 border border-slate-100">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-indigo-100 flex items-center justify-center">
              <FolderKanban className="w-4 h-4 text-indigo-600" />
            </div>
            <div>
              <p className="text-sm font-medium text-slate-900">{a.projectName || `Project #${a.projectId}`}</p>
              <p className="text-xs text-slate-500">{a.roleInProject || 'Team Member'} · {a.startDate}</p>
            </div>
          </div>
          <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${STATUS_BADGE[a.status] || 'bg-slate-100 text-slate-600'}`}>
            {a.status}
          </span>
        </div>
      ))}
    </div>
  );
}
