import { useState, useEffect } from "react";
import RoleBadge from "./RoleBadge.jsx";
import ConfirmationDialog from "./ConfirmationDialog.jsx";
import { toast } from "sonner";
import { Search, Trash2 } from "lucide-react";
import {
  updateProjectMemberRole,
  removeProjectMember,
} from "../../api/projectMembersApi";
import { usePermission } from "../../context/usePermission.js";
const MembersTable = ({
  projectId,
  members: initialMembers = [],
  onMembersChange,
}) => {
  const [members, setMembers] = useState(initialMembers);
  const [loading, setLoading] = useState(false);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("all");
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [confirmMemberId, setConfirmMemberId] = useState(null);
  const { canRemoveMember, canUpdateMemberRole } = usePermission();
  useEffect(() => {
    setMembers(initialMembers);
  }, [initialMembers]);
  const filteredMembers = members.filter(
    (m) =>
      (m.name?.toLowerCase().includes(search.toLowerCase()) ||
        m.email?.toLowerCase().includes(search.toLowerCase())) &&
      (roleFilter === "all" || m.role === roleFilter),
  );
  const handleRoleChange = async (memberId, memberUserId, newRole) => {
    setLoading(true);
    try {
      await updateProjectMemberRole(projectId, memberUserId, newRole);
      const updated = members.map((m) =>
        m.id === memberId ? { ...m, role: newRole } : m,
      );
      setMembers(updated);
      onMembersChange?.(updated);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };
  const handleRemoveClick = (member) => {
    setConfirmMemberId(member);
    setShowConfirmDialog(true);
  };
  const handleRemoveConfirm = async () => {
    if (!confirmMemberId) return;
    setLoading(true);
    try {
      await removeProjectMember(projectId, confirmMemberId.userId);
      const updated = members.filter((m) => m.id !== confirmMemberId.id);
      setMembers(updated);
      onMembersChange?.(updated);
      toast.success("Member removed successfully");
    } catch (error) {
      console.error(error);
      toast.error("Failed to remove member");
    } finally {
      setLoading(false);
      setShowConfirmDialog(false);
      setConfirmMemberId(null);
    }
  };
  const handleRemoveCancel = () => {
    setShowConfirmDialog(false);
    setConfirmMemberId(null);
  };
  if (loading && members.length === 0) {
    return (
      <div className="animate-pulse space-y-4">
         
        {[...Array(5)].map((_, i) => (
          <div key={i} className="h-12 bg-gray-200 rounded-xl" />
        ))} 
      </div>
    );
  }
  return (
    <>
       
      <div className="space-y-6">
         
        {/* Search & Filter */} 
        <div className="flex flex-col sm:flex-row gap-4">
           
          <div className="relative flex-1">
             
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" /> 
            <input
              placeholder="Search members by name or email..."
              className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            /> 
          </div> 
          <select
            value={roleFilter}
            onChange={(e) => setRoleFilter(e.target.value)}
            className="px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500"
          >
             
            <option value="all">All Roles</option> 
            <option value="PROJECT_MANAGER">Managers</option> 
            <option value="TEAM_MEMBER">Members</option> 
          </select> 
        </div> 
        {/* Table */} 
        <div className="bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-2xl border border-white/50 overflow-hidden">
           
          <div className="overflow-x-auto">
             
            <table className="w-full">
               
              <thead>
                 
                <tr className="border-b border-gray-200">
                   
                  <th className="text-left py-4 font-semibold text-gray-900 w-12">
                     
                    <input
                      type="checkbox"
                      className="rounded border-gray-300"
                    /> 
                  </th> 
                  <th className="text-left py-4 font-semibold text-gray-900">
                    Member
                  </th> 
                  <th className="text-left py-4 font-semibold text-gray-900">
                    Email
                  </th> 
                  <th className="text-left py-4 font-semibold text-gray-900">
                    Role
                  </th> 
                  <th className="text-right py-4 font-semibold text-gray-900">
                    Actions
                  </th> 
                </tr> 
              </thead> 
              <tbody className="divide-y divide-gray-200">
                 
                {filteredMembers.map((member) => (
                  <tr
                    key={member.id}
                    className="hover:bg-gray-50 transition-colors"
                  >
                     
                    <td className="py-4">
                       
                      <input
                        type="checkbox"
                        className="rounded border-gray-300"
                      /> 
                    </td> 
                    <td className="py-4 font-medium text-gray-900 max-w-[200px] truncate">
                       
                      <div className="flex items-center gap-3">
                         
                        <div className="w-10 h-10 bg-gradient-to-r from-indigo-400 to-purple-500 rounded-full flex items-center justify-center text-white font-semibold">
                           
                          {member.name?.charAt(0)?.toUpperCase()} 
                        </div> 
                        <span>{member.name}</span> 
                      </div> 
                    </td> 
                    <td className="py-4 text-sm text-gray-900 max-w-[250px] truncate">
                      {member.email}
                    </td> 
                    <td className="py-4">
                       
                      <RoleBadge role={member.role} /> 
                    </td>
                    <td className="py-4 text-right space-x-2">
                       
                      {canUpdateMemberRole() && (
                        <select
                          value={member.role}
                          onChange={(e) =>
                            handleRoleChange(
                              member.id,
                              member.userId,
                              e.target.value,
                            )
                          }
                          className="px-3 py-1 border border-gray-200 rounded-xl text-sm focus:ring-2 focus:ring-indigo-500"
                        >
                           
                          <option value="PROJECT_MANAGER">
                            Project Manager
                          </option> 
                          <option value="TEAM_MEMBER">Team Member</option> 
                        </select>
                      )} 
                      {canRemoveMember() && (
                        <button
                          onClick={() => handleRemoveClick(member)}
                          className="p-2 text-red-500 hover:bg-red-50 rounded-xl transition-colors"
                          title="Remove member"
                        >
                           
                          <Trash2 className="w-4 h-4" /> 
                        </button>
                      )} 
                    </td> 
                  </tr>
                ))} 
                {filteredMembers.length === 0 && (
                  <tr>
                     
                    <td colSpan={5} className="py-12 text-center text-gray-500">
                       
                      {search || roleFilter !== "all"
                        ? "No matching members"
                        : "No members yet. Add your first team member!"} 
                    </td> 
                  </tr>
                )} 
              </tbody> 
            </table> 
          </div> 
        </div> 
      </div> 
      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={handleRemoveCancel}
        onConfirm={handleRemoveConfirm}
        title="Remove Member?"
        message="This member will be removed from the project. This action cannot be undone."
      /> 
    </>
  );
};
export default MembersTable;
