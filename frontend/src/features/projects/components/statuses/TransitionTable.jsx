import { useState } from "react";
import { Trash2, ArrowRight } from "lucide-react";
import ConfirmationDialog from "../members/ConfirmationDialog.jsx";
import StatusBadge from "../../../tasks/components/tasks/StatusBadge.jsx";
import RuleChip from "./RuleChip.jsx";
import { toast } from "sonner";

const TransitionTable = ({
  transitions = [],
  statuses = [],
  onDelete,
  loading = false,
}) => {
  const [showConfirm, setShowConfirm] = useState(false);
  const [deletingTransitionId, setDeletingTransitionId] = useState(null);

  const handleDelete = (transitionId) => {
    setDeletingTransitionId(transitionId);
    setShowConfirm(true);
  };

  const confirmDelete = async () => {
    try {
      onDelete?.(deletingTransitionId);
      toast.success("Transition removed");
    } catch {
      toast.error("Failed to delete transition");
    }
    setShowConfirm(false);
    setDeletingTransitionId(null);
  };

  if (loading) {
    return (
      <div className="card p-8">
        <div className="animate-pulse space-y-4">
          <div className="h-10 bg-surface-200 dark:bg-surface-700 rounded-xl w-3/4" />
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="flex items-center gap-4 p-4 bg-surface-100 dark:bg-surface-800 rounded-xl h-16"
              />
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="card p-0 overflow-hidden">
        <div className="px-6 py-4 border-b border-surface-200 dark:border-surface-700">
          <h3 className="text-lg font-semibold text-surface-900 dark:text-surface-100">
            Workflow Transitions ({transitions.length})
          </h3>
          <p className="text-sm text-surface-500 dark:text-surface-400 mt-0.5">
            Allowed task movements between statuses
          </p>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-surface-200 dark:border-surface-700 bg-surface-50 dark:bg-surface-900/50">
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  #
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  From
                </th>
                <th className="px-6 py-4 text-center text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  ΓåÆ
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  To
                </th>
                <th className="px-6 py-4 text-left text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Rules
                </th>
                <th className="px-6 py-4 text-right text-xs font-semibold uppercase tracking-wider text-surface-500 dark:text-surface-400">
                  Action
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-100 dark:divide-surface-800">
              {transitions.map((transition, index) => {
                const fromStatus = statuses.find(
                  (s) => s.id === transition.fromStatusId,
                );
                const toStatus = statuses.find(
                  (s) => s.id === transition.toStatusId,
                );
                return (
                  <tr
                    key={transition.id}
                    className="hover:bg-surface-50 dark:hover:bg-surface-800/50 transition-colors group"
                  >
                    <td className="px-6 py-4 font-mono text-sm text-surface-500 dark:text-surface-400 font-semibold">
                      {index + 1}
                    </td>
                    <td className="px-6 py-4">
                      {fromStatus ? (
                        <StatusBadge
                          status={fromStatus.name}
                          color={fromStatus.color}
                        />
                      ) : (
                        <span className="text-surface-400">Unknown</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <ArrowRight className="h-5 w-5 text-primary-500 dark:text-primary-400 mx-auto" />
                    </td>
                    <td className="px-6 py-4">
                      {toStatus ? (
                        <StatusBadge
                          status={toStatus.name}
                          color={toStatus.color}
                        />
                      ) : (
                        <span className="text-surface-400">Unknown</span>
                      )}
                    </td>
                    <td className="px-6 py-4 max-w-xs">
                      <div className="flex flex-wrap gap-1">
                        {transition.rules && transition.rules.length > 0 ? (
                          transition.rules.map((rule, ruleIndex) => (
                            <RuleChip
                              key={`${transition.id}-rule-${ruleIndex}`}
                              rule={rule}
                            />
                          ))
                        ) : (
                          <span className="px-2.5 py-1 text-xs font-medium text-surface-500 dark:text-surface-400 bg-surface-100 dark:bg-surface-800 rounded-full">
                            Everyone
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button
                        onClick={() => handleDelete(transition.id)}
                        className="p-2 text-danger-600 hover:bg-danger-100 dark:hover:bg-danger-900/30 rounded-xl transition-all md:opacity-0 md:group-hover:opacity-100"
                        title="Delete transition"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
        {transitions.length === 0 && !loading && (
          <div className="text-center py-12 text-surface-500 dark:text-surface-400 border-2 border-dashed border-surface-200 dark:border-surface-700 rounded-2xl mx-6 my-6">
            <ArrowRight className="h-12 w-12 mx-auto mb-3 text-surface-300 dark:text-surface-600" />
            <h3 className="text-base font-semibold mb-1">No transitions defined</h3>
            <p className="text-sm text-surface-400 dark:text-surface-500">
              Add transitions to define allowed task movements
            </p>
          </div>
        )}
      </div>

      <ConfirmationDialog
        isOpen={showConfirm}
        onClose={() => setShowConfirm(false)}
        onConfirm={confirmDelete}
        title="Delete Transition?"
        message="This will prevent tasks from moving between these statuses."
      />
    </>
  );
};

export default TransitionTable;
