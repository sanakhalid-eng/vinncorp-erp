import { useState } from "react";
import RoleBadge from "./RoleBadge.jsx";
import { Trash2 } from "lucide-react";
import { toast } from "sonner";
import ConfirmationDialog from "./ConfirmationDialog.jsx";
import {
  updateProjectMemberRole,
  removeProjectMember,
} from "../../api/projectMembersApi";
import { usePermission } from "../../../../context/usePermission.js";
const MemberCardView = ({ projectId, members, onMembersChange }) => {
  const [loading, setLoading] = useState({});
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmMemberId, setConfirmMemberId] = useState(null);
  const { canRemoveMember, canUpdateMemberRole } = usePermission();
  const handleRoleChange = async (memberId, memberUserId, newRole) => {
    setLoading((prev) => ({ ...prev, [memberId]: true }));
    try {
      await updateProjectMemberRole(projectId, memberUserId, newRole);
      onMembersChange?.(
        members.map((m) => (m.id === memberId ? { ...m, role: newRole } : m)),
      );
      toast.success("Role updated");
    } catch (error) {
      console.error(error);
      toast.error("Failed to update role");
    } finally {
      setLoading((prev) => ({ ...prev, [memberId]: false }));
    }
  };
  const handleRemoveClick = (member) => {
    setConfirmMemberId(member);
    setShowConfirmDialog(true);
  };
  const handleRemoveConfirm = async () => {
    if (!confirmMemberId) return;
    setLoading((prev) => ({ ...prev, [confirmMemberId.id]: true }));
    try {
      await removeProjectMember(projectId, confirmMemberId.userId);
      onMembersChange?.(members.filter((m) => m.id !== confirmMemberId.id));
      toast.success("Member removed");
    } catch (error) {
      console.error(error);
      toast.error("Failed to remove member");
    } finally {
      setLoading((prev) => ({ ...prev, [confirmMemberId.id]: false }));
      setShowConfirmDialog(false);
      setConfirmMemberId(null);
    }
  };
  const handleRemoveCancel = () => {
    setShowConfirmDialog(false);
    setConfirmMemberId(null);
  };
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
       
      {members.map((member) => (
        <div
          key={member.id}
          className="group bg-white/70 backdrop-blur-xl rounded-3xl p-6 shadow-xl border border-white/50 hover:shadow-2xl hover:-translate-y-1 transition-all hover:bg-white/90"
        >
          <div className="flex items-start justify-between mb-4">
             
            <div className="w-12 h-12 bg-gradient-to-r from-indigo-400 to-purple-500 rounded-2xl flex items-center justify-center text-white font-semibold text-lg flex-shrink-0">
               
              {member.name?.charAt(0)?.toUpperCase()} 
            </div> 
            {(canUpdateMemberRole() || canRemoveMember()) && (
              <div className="flex items-center gap-2 opacity-100 md:opacity-0 md:group-hover:opacity-100 transition-opacity">
                 
                {canUpdateMemberRole() && (
                  <select
                    value={member.role}
                    onChange={(e) =>
                      handleRoleChange(member.id, member.userId, e.target.value)
                    }
                    disabled={loading[member.id]}
                    className="px-2 py-1 text-xs border border-gray-200 rounded-xl focus:ring-1 focus:ring-indigo-500 bg-white"
                  >
                     
                    <option value="PROJECT_MANAGER">Manager</option> 
                    <option value="TEAM_MEMBER">Member</option> 
                  </select>
                )} 
                {canRemoveMember() && (
                  <button
                    onClick={() => handleRemoveClick(member)}
                    disabled={loading[member.id]}
                    className="p-1.5 text-red-500 hover:bg-red-50 rounded-xl transition-colors hover:scale-110"
                  >
                     
                    <Trash2 className="w-4 h-4" /> 
                  </button>
                )} 
              </div>
            )} 
          </div> 
          <h3 className="font-semibold text-gray-900 mb-1 truncate">
            {member.name}
          </h3> 
          <p className="text-sm text-gray-600 mb-3 truncate">{member.email}</p> 
          <RoleBadge role={member.role} className="mb-4" /> 
          {loading[member.id] && (
            <div className="absolute inset-0 bg-white/80 flex items-center justify-center rounded-3xl backdrop-blur-sm">
               
              <div className="w-6 h-6 border-2 border-indigo-500 border-t-transparent rounded-full animate-spin" /> 
            </div>
          )} 
        </div>
      ))} 
      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={handleRemoveCancel}
        onConfirm={handleRemoveConfirm}
        title="Remove Member?"
        message="This member will be removed from the project. This action cannot be undone."
      /> 
    </div>
  );
};
export default MemberCardView;
