import { useState } from "react";
import { X, CheckSquare, ArrowRight, User, Flag, Trash2, List } from "lucide-react";
import { toast } from "sonner";
import { bulkUpdateTasks, bulkDeleteTasks } from "../../api/taskApi";
import ConfirmationDialog from "../../../projects/components/members/ConfirmationDialog";
export default function BulkActionBar({
  selectedIds,
  onClear,
  onComplete,
  statuses,
  assignees,
  sprints,
}) {
  const [action, setAction] = useState(null);
  const [value, setValue] = useState("");
  const [loading, setLoading] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const count = selectedIds.length;
  const handleDelete = async () => {
    if (loading) return;
    setLoading(true);
    try {
      const result = await bulkDeleteTasks({ taskIds: selectedIds });
      const data = result?.data?.data ?? result?.data ?? result;
      const successCount = data?.updatedCount ?? 0;
      const failCount = data?.failedTaskIds?.length ?? 0;
      if (successCount > 0) {
        toast.success(`Deleted ${successCount} task${successCount !== 1 ? "s" : ""}`);
      }
      if (failCount > 0) {
        toast.error(`${failCount} task${failCount !== 1 ? "s" : ""} failed to delete`);
      }
      onComplete();
    } catch {
      toast.error("Delete operation failed");
    } finally {
      setLoading(false);
      setShowConfirm(false);
    }
  };
  const handleApply = async () => {
    if (!value || loading) return;
    setLoading(true);
    try {
      const payload = {};
      if (action === "status") payload.statusId = parseInt(value);
      if (action === "assignee") payload.assigneeId = parseInt(value);
      if (action === "priority") payload.priority = value;
      if (action === "sprint") payload.sprintId = parseInt(value);
      const result = await bulkUpdateTasks({ taskIds: selectedIds, ...payload });
      const data = result?.data?.data ?? result?.data ?? result;
      toast.success(
        `Updated ${data.updatedCount || data.updateCount || 0} task${data.updatedCount !== 1 ? "s" : ""}`,
      );
      if (data.failedTaskIds?.length > 0) {
        toast.error(
          `${data.failedTaskIds.length} task${data.failedTaskIds.length !== 1 ? "s" : ""} failed`,
        );
      }
      onComplete();
    } catch (error) {
      toast.error(error.response?.data?.message || "Bulk update failed");
    } finally {
      setLoading(false);
      setAction(null);
      setValue("");
    }
  };
  if (count === 0) return null;
  return (
    <>
    <div className="sticky top-0 z-20 -mx-4 -mt-4 mb-4 rounded-2xl border border-primary-200 bg-primary-50 dark:border-primary-800 dark:bg-primary-950/50 px-4 py-3 shadow-lg backdrop-blur">
       
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
         
        <div className="flex items-center gap-3">
           
          <button
            onClick={onClear}
            className="rounded-lg p-1.5 text-primary-600 hover:bg-primary-100 dark:hover:bg-primary-900/30 transition-colors"
            aria-label="Clear selection"
          >
             
            <X className="h-4 w-4" /> 
          </button> 
          <span className="flex items-center gap-2 text-sm font-semibold text-primary-800 dark:text-primary-300">
             
            <CheckSquare className="h-4 w-4" /> {count} selected 
          </span> 
        </div> 
        <div className="flex flex-wrap items-center gap-2">
           
          {!action ? (
            <>
               
              <button
                onClick={() => setAction("status")}
                className="flex items-center gap-1.5 rounded-xl bg-white dark:bg-primary-900 px-3 py-2 text-xs font-semibold text-primary-700 dark:text-primary-300 shadow-sm hover:bg-primary-100 dark:hover:bg-primary-800 transition-colors"
                aria-label="Change status"
              >
                   
                <ArrowRight className="h-3.5 w-3.5" /> Change Status 
              </button> 
              <button
                onClick={() => setAction("assignee")}
                className="flex items-center gap-1.5 rounded-xl bg-white dark:bg-primary-900 px-3 py-2 text-xs font-semibold text-primary-700 dark:text-primary-300 shadow-sm hover:bg-primary-100 dark:hover:bg-primary-800 transition-colors"
                aria-label="Assign to"
              >
                   
                <User className="h-3.5 w-3.5" /> Assign To 
              </button> 
              <button
                onClick={() => setAction("priority")}
                className="flex items-center gap-1.5 rounded-xl bg-white dark:bg-primary-900 px-3 py-2 text-xs font-semibold text-primary-700 dark:text-primary-300 shadow-sm hover:bg-primary-100 dark:hover:bg-primary-800 transition-colors"
                aria-label="Set priority"
              >
                   
                <Flag className="h-3.5 w-3.5" /> Set Priority 
              </button>
              {sprints && sprints.length > 0 && (
                <button
                  onClick={() => setAction("sprint")}
                  className="flex items-center gap-1.5 rounded-xl bg-white dark:bg-primary-900 px-3 py-2 text-xs font-semibold text-primary-700 dark:text-primary-300 shadow-sm hover:bg-primary-100 dark:hover:bg-primary-800 transition-colors"
                  aria-label="Assign to sprint"
                >
                  <List className="h-3.5 w-3.5" /> Assign Sprint
                </button>
              )} 
              <button
                onClick={() => setShowConfirm(true)}
                disabled={loading}
                className="flex items-center gap-1.5 rounded-xl bg-white dark:bg-primary-900 px-3 py-2 text-xs font-semibold text-red-600 dark:text-red-400 shadow-sm hover:bg-red-50 dark:hover:bg-red-950/30 transition-colors"
                aria-label="Delete selected tasks"
              >
                 
                <Trash2 className="h-3.5 w-3.5" /> Delete 
              </button> 
            </>
          ) : (
            <div className="flex items-center gap-2">
               
              {action === "status" && (
                  <select
                    value={value}
                    onChange={(e) => setValue(e.target.value)}
                    className="rounded-xl border border-primary-200 bg-white px-3 py-2 text-xs font-medium focus:outline-none focus:ring-2 focus:ring-primary-500"
                    aria-label="Select status"
                  >
                    
                    <option value="">Select status...</option> 
                    {(statuses || []).map((s) => (
                      <option key={s.id} value={s.id}>
                        {s.name}
                      </option>
                    ))} 
                  </select>
                )} 
                {action === "assignee" && (
                  <select
                    value={value}
                    onChange={(e) => setValue(e.target.value)}
                    className="rounded-xl border border-primary-200 bg-white px-3 py-2 text-xs font-medium focus:outline-none focus:ring-2 focus:ring-primary-500"
                    aria-label="Select assignee"
                  >
                    
                    <option value="">Select assignee...</option> 
                    {(assignees || []).map((a) => (
                      <option key={a.id} value={a.id}>
                        {a.name || a.email}
                      </option>
                    ))} 
                  </select>
                )} 
                {action === "priority" && (
                  <select
                    value={value}
                    onChange={(e) => setValue(e.target.value)}
                    className="rounded-xl border border-primary-200 bg-white px-3 py-2 text-xs font-medium focus:outline-none focus:ring-2 focus:ring-primary-500"
                    aria-label="Select priority"
                  >
                    
                  <option value="">Select priority...</option> 
                  <option value="LOW">Low</option> 
                  <option value="MEDIUM">Medium</option> 
                  <option value="HIGH">High</option> 
                  <option value="CRITICAL">Critical</option> 
                </select>
              )}
              {action === "sprint" && (
                <select
                  value={value}
                  onChange={(e) => setValue(e.target.value)}
                  className="rounded-xl border border-primary-200 bg-white px-3 py-2 text-xs font-medium focus:outline-none focus:ring-2 focus:ring-primary-500"
                  aria-label="Select sprint"
                >
                  <option value="">Select sprint...</option>
                  {(sprints || []).map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name}
                    </option>
                  ))}
                </select>
              )} 
              <button
                onClick={handleApply}
                disabled={!value || loading}
                className="rounded-xl bg-primary-600 px-4 py-2 text-xs font-semibold text-white hover:bg-primary-700 disabled:opacity-50 transition-colors"
                aria-label="Apply action"
              >
                  
                {loading ? "Applying..." : "Apply"} 
              </button> 
              <button
                onClick={() => {
                  setAction(null);
                  setValue("");
                }}
                className="rounded-xl bg-white px-3 py-2 text-xs font-semibold text-surface-600 hover:bg-surface-100 transition-colors"
                aria-label="Cancel action"
              >
                  
                Cancel 
              </button> 
            </div>
          )} 
        </div> 
      </div> 
    </div>
    <ConfirmationDialog
      isOpen={showConfirm}
      onClose={() => setShowConfirm(false)}
      onConfirm={handleDelete}
      title={`Delete ${count} task${count !== 1 ? "s" : ""}?`}
      message="This action cannot be undone."
      confirmText="Delete"
      cancelText="Cancel"
    />
    </>
  );
}
