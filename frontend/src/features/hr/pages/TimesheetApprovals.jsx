import { useEffect, useState } from "react";
import { toast, Toaster } from "sonner";
import {
  getPendingTimesheets,
  approveTimesheet,
  rejectTimesheet,
} from "../../../api/timeTrackingApi";
import { CheckCircle, XCircle, Clock, Calendar } from "lucide-react";

const TimesheetApprovals = () => {
  const [pendingTimesheets, setPendingTimesheets] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    loadPendingTimesheets();
  }, []);

  const loadPendingTimesheets = async () => {
    try {
      const data = await getPendingTimesheets();
      setPendingTimesheets(data || []);
    } catch (error) {
      toast.error("Failed to load pending timesheets");
    }
  };

  const handleApprove = async (approvalId) => {
    if (!confirm("Approve this timesheet?")) return;
    try {
      setLoading(true);
      const approverId = localStorage.getItem("userId");
      // Get from auth context ideally

      await approveTimesheet(approvalId, approverId);
      toast.success("Timesheet approved successfully");
      loadPendingTimesheets();
    } catch (error) {
      toast.error("Failed to approve timesheet");
    } finally {
      setLoading(false);
    }
  };

  const handleReject = async (approvalId) => {
    const reason = prompt("Reason for rejection (optional):");
    try {
      setLoading(true);
      const approverId = localStorage.getItem("userId");
      await rejectTimesheet(approvalId, approverId, reason);
      toast.success("Timesheet rejected");
      loadPendingTimesheets();
    } catch (error) {
      toast.error("Failed to reject timesheet");
    } finally {
      setLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const styles = {
      PENDING: "bg-yellow-100 text-yellow-800",
      APPROVED: "bg-green-100 text-green-800",
      REJECTED: "bg-red-100 text-red-800",
    };
    return styles[status] || "bg-gray-100 text-gray-800";
  };

  return (
    <>
      <Toaster position="top-right" />
      <div className="p-6 max-w-7xl mx-auto">
        <h1 className="text-2xl font-bold text-gray-900 mb-6 flex items-center gap-2">
          <CheckCircle size={28} />
          Timesheet Approvals
        </h1>

        {pendingTimesheets.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-xl shadow-sm border border-gray-100">
            <CheckCircle size={48} className="mx-auto text-gray-300 mb-4" />
            <p className="text-gray-500">No pending timesheets to approve</p>
          </div>
        ) : (
          <div className="space-y-4">
            {pendingTimesheets.map((approval) => (
              <div
                key={approval.id}
                className="bg-white p-5 rounded-xl shadow-sm border border-gray-100"
              >
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h3 className="font-semibold text-gray-900">
                      User ID: {approval.user?.id || "Unknown"}
                    </h3>
                    <p className="text-sm text-gray-500">
                      Week: {approval.weekStart} to {approval.weekEnd}
                    </p>
                  </div>
                  <span
                    className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusBadge(approval.status)}`}
                  >
                    {approval.status}
                  </span>
                </div>

                <div className="flex gap-3">
                  <button
                    onClick={() => handleApprove(approval.id)}
                    disabled={loading}
                    className="flex-1 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50 flex items-center justify-center gap-2"
                  >
                    <CheckCircle size={16} />
                    Approve
                  </button>
                  <button
                    onClick={() => handleReject(approval.id)}
                    disabled={loading}
                    className="flex-1 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 flex items-center justify-center gap-2"
                  >
                    <XCircle size={16} />
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </>
  );
};

export default TimesheetApprovals;
