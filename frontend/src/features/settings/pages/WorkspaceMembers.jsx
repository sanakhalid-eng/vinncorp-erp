import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Users,
  ArrowLeft,
  Loader2,
  Trash2,
  Building2,
  UserCircle2,
} from "lucide-react";
import notify from "../../../lib/toast";
import {
  getWorkspace,
  getWorkspaceMembers,
  removeWorkspaceMember,
} from "../api/workspaceApi";
import Button from "../../../components/Button";
export default function WorkspaceMembers() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [workspace, setWorkspace] = useState(null);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [removing, setRemoving] = useState(null);
  useEffect(() => {
    loadData();
  }, [id]);
  const loadData = async () => {
    try {
      setLoading(true);
      const [wsRes, membersRes] = await Promise.all([
        getWorkspace(id),
        getWorkspaceMembers(id),
      ]);
      setWorkspace(wsRes.data.data);
      setMembers(membersRes.data.data || []);
    } catch {
      notify.error("Failed to load workspace members");
      navigate("/workspaces");
    } finally {
      setLoading(false);
    }
  };
  const handleRemove = async (userId) => {
    if (!confirm("Remove this member from the workspace?")) return;
    try {
      setRemoving(userId);
      await removeWorkspaceMember(id, userId);
      notify.success("Member removed");
      loadData();
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to remove member");
    } finally {
      setRemoving(null);
    }
  };
  if (loading) {
    return (
      <div className="flex justify-center py-20">
         
        <Loader2 className="w-8 h-8 animate-spin text-indigo-600" /> 
      </div>
    );
  }
  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top,_rgba(56,189,248,0.12),_transparent_28%),linear-gradient(180deg,_#f8fafc_0%,_#eef2ff_100%)]">
       
      <header className="sticky top-0 z-30 flex items-center justify-between border-b border-slate-200/70 bg-white/80 px-4 py-3 backdrop-blur lg:px-6">
         
        <button
          onClick={() => navigate("/workspaces")}
          className="flex items-center gap-2 text-slate-500 hover:text-indigo-600 transition-colors group"
        >
           
          <ArrowLeft className="w-5 h-5 group-hover:-translate-x-1 transition-transform" /> 
          <span className="font-medium hidden sm:inline">Back</span> 
        </button> 
        <div className="flex items-center gap-3">
           
          <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-400 to-indigo-500 shadow-sm">
             
            <Building2 className="h-4 w-4 text-white" /> 
          </div> 
          <span className="font-bold text-slate-800">
            {workspace?.name} Members
          </span> 
        </div> 
      </header> 
      <main className="max-w-4xl mx-auto px-4 py-6 lg:px-6">
         
        <div className="mb-8">
           
          <h1 className="text-3xl font-bold text-slate-900">
            Workspace Members
          </h1> 
          <p className="text-slate-500 mt-1">{members.length} member(s)</p> 
        </div> 
        <div className="rounded-2xl border border-slate-200 bg-white shadow-lg overflow-hidden">
           
          {members.length === 0 ? (
            <div className="text-center py-12">
               
              <Users className="w-12 h-12 text-slate-300 mx-auto mb-3" /> 
              <p className="text-slate-500">No members found</p> 
            </div>
          ) : (
            <div className="divide-y divide-slate-100">
               
              {members.map((member) => (
                <div
                  key={member.id}
                  className="flex items-center justify-between p-4 hover:bg-slate-50 transition"
                >
                   
                  <div className="flex items-center gap-4">
                     
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-white font-bold">
                       
                      {member.avatarUrl ? (
                        <img
                          src={member.avatarUrl}
                          alt={member.userName}
                          className="h-full w-full object-cover rounded-xl"
                        />
                      ) : (
                        <UserCircle2 className="h-6 w-6" />
                      )} 
                    </div> 
                    <div>
                       
                      <p className="font-semibold text-slate-900">
                         
                        {member.userName} 
                      </p> 
                      <p className="text-sm text-slate-500">
                        {member.userEmail}
                      </p> 
                    </div> 
                  </div> 
                  <div className="flex items-center gap-3">
                     
                    <span
                      className={`rounded-full px-3 py-1 text-xs font-medium ${member.workspaceRole === "WORKSPACE_OWNER" ? "bg-amber-50 text-amber-700" : member.workspaceRole === "WORKSPACE_ADMIN" ? "bg-indigo-50 text-indigo-700" : "bg-slate-50 text-slate-600"}`}
                    >
                       
                      {member.workspaceRole?.replace("WORKSPACE_", "") ||
                        "MEMBER"} 
                    </span> 
                    {workspace?.role === "WORKSPACE_OWNER" &&
                      member.workspaceRole !== "WORKSPACE_OWNER" && (
                        <button
                          onClick={() => handleRemove(member.userId)}
                          disabled={removing === member.userId}
                          className="rounded-xl p-2 text-red-400 hover:bg-red-50 hover:text-red-600 transition"
                          title="Remove member"
                        >
                           
                          {removing === member.userId ? (
                            <Loader2 className="w-4 h-4 animate-spin" />
                          ) : (
                            <Trash2 className="w-4 h-4" />
                          )} 
                        </button>
                      )} 
                  </div> 
                </div>
              ))} 
            </div>
          )} 
        </div> 
      </main> 
    </div>
  );
}
