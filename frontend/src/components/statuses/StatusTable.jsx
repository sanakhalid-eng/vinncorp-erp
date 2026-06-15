import { useState } from "react";
import { MoreHorizontal, Edit3, Trash2, GripVertical, Settings } from "lucide-react";
import ConfirmationDialog from "../members/ConfirmationDialog.jsx";

const StatusTable = ({
  statuses = [],
  loading = false,
  onEdit,
  onDelete,
}) => {
  const [showConfirm, setShowConfirm] = useState(false);
  const [deletingStatusId, setDeletingStatusId] = useState(null);

  const handleDelete = (statusId) => {
    setDeletingStatusId(statusId);
    setShowConfirm(true);
  };

  const confirmDelete = () => {
    onDelete(deletingStatusId);
    setShowConfirm(false);
    setDeletingStatusId(null);
  };

  if (loading) {
    return (
      <div className="card p-8">
        <div className="animate-pulse space-y-4">
          <div className="h-10 bg-surface-200 dark:bg-surface-700 rounded-xl w-64" />
          {[1, 2, 3].map((i) => (
            <div
              key={i}
              className="flex items-center gap-4 p-4 bg-surface-100 dark:bg-surface-800 rounded-xl"
            >
              <div className="w-10 h-10 bg-surface-200 dark:bg-surface-700 rounded-lg" />
              <div className="flex-1 space-y-2">
                <div className="h-5 bg-surface-200 dark:bg-surface-700 rounded-lg w-48" />
                <div className="h-4 bg-surface-200 dark:bg-surface-700 rounded-full w-32" />
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="card p-0 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-900/50">
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  #
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Status
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Color
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Order
                </th>
                <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-100 dark:divide-surface-800">
              {statuses.map((status, index) => (
                <tr
                  key={status.id}
                  className="hover:bg-surface-50 dark:hover:bg-surface-800/50 transition-colors group"
                >
                  <td className="px-6 py-4 font-mono text-sm text-surface-500 dark:text-surface-400 font-semibold">
                    {index + 1}
                  </td>
                  <td className="px-6 py-4">
                    <div className="font-medium text-surface-900 dark:text-surface-100 max-w-48 truncate">
                      {status.name}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div
                      className="w-10 h-7 rounded-lg shadow-sm border border-surface-200 dark:border-surface-700 flex items-center justify-center"
                      style={{ backgroundColor: status.color }}
                      title={status.color}
                    >
                      <span className="font-mono text-[10px] text-white/90 px-1">
                        {status.color.slice(1)}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2">
                      <GripVertical className="h-4 w-4 text-surface-400" />
                      <span className="font-mono font-semibold text-sm text-surface-900 dark:text-surface-100">
                        {status.orderIndex}
                      </span>
                    </div>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex items-center justify-end gap-1">
                      <button
                        onClick={() => onEdit(status)}
                        className="p-2 text-primary-600 hover:bg-primary-100 dark:hover:bg-primary-900/30 rounded-xl transition-all"
                        title="Edit"
                      >
                        <Edit3 className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(status.id)}
                        className="p-2 text-danger-600 hover:bg-danger-100 dark:hover:bg-danger-900/30 rounded-xl transition-all"
                        title="Delete"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {statuses.length === 0 && !loading && (
          <div className="text-center py-12 text-surface-500 dark:text-surface-400 border-2 border-dashed border-surface-200 dark:border-surface-700 rounded-2xl mx-6 my-6">
            <Settings className="h-12 w-12 mx-auto mb-3 text-surface-300 dark:text-surface-600" />
            <h3 className="text-base font-semibold mb-1">No workflow statuses</h3>
            <p className="text-sm text-surface-400 dark:text-surface-500">
              Create your first status to get started
            </p>
          </div>
        )}
      </div>

      <ConfirmationDialog
        isOpen={showConfirm}
        onClose={() => setShowConfirm(false)}
        onConfirm={confirmDelete}
        title="Delete Status?"
        message="Tasks using this status will need re-assignment. This cannot be undone."
      />
    </>
  );
};

export default StatusTable;
