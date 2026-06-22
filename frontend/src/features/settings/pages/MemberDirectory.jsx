import { useEffect, useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Users, Search, Loader2, Mail, Clock, Shield } from "lucide-react";
import { useWorkspace } from "../../../context/WorkspaceContext";
import { getWorkspaceMembers } from "../api/workspaceApi";
import Button from "../../../components/Button";
const ROLE_COLORS = {
  WORKSPACE_OWNER: "bg-amber-100 text-amber-700",
  WORKSPACE_ADMIN: "bg-blue-100 text-blue-700",
  WORKSPACE_MEMBER: "bg-slate-100 text-slate-600",
};
export default function MemberDirectory() {
  const { workspace } = useWorkspace();
  const navigate = useNavigate();
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  useEffect(() => {
    if (!workspace) return;
    loadMembers();
  }, [workspace?.id]);
  const loadMembers = async () => {
    try {
      setLoading(true);
      const res = await getWorkspaceMembers(workspace.id);
      setMembers(res.data.data || []);
    } catch {
    } finally {
      setLoading(false);
    }
  };
  const filteredMembers = useMemo(() => {
    return members.filter((m) => {
      const matchesSearch =
        !search.trim() ||
        m.userName?.toLowerCase().includes(search.toLowerCase()) ||
        m.userEmail?.toLowerCase().includes(search.toLowerCase());
      const matchesRole =
        roleFilter === "ALL" || m.workspaceRole === roleFilter;
      return matchesSearch && matchesRole;
    });
  }, [members, search, roleFilter]);
  const roles = useMemo(() => {
    return [...new Set(members.map((m) => m.workspaceRole))];
  }, [members]);
  if (loading) {
    return (
      <div className="flex justify-center py-20">
         
        <Loader2 className="w-8 h-8 animate-spin text-indigo-600" /> 
      </div>
    );
  }
  return (
    <div>
       
      <div className="mb-8">
         
        <h1 className="text-3xl font-bold text-slate-900">Members</h1> 
        <p className="text-slate-500 mt-1">
           
          {members.length} {members.length === 1 ? "member" : "members"} in 
          {workspace?.name} 
        </p> 
      </div> 
      <div className="flex flex-col sm:flex-row gap-3 mb-6">
         
        <div className="relative flex-1">
           
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" /> 
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search members..."
            className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-3 text-sm outline-none focus:border-indigo-400"
          /> 
        </div> 
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          className="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm outline-none focus:border-indigo-400"
        >
           
          <option value="ALL">All Roles</option> 
          {roles.map((r) => (
            <option key={r} value={r}>
              {r.replace("WORKSPACE_", "")}
            </option>
          ))} 
        </select> 
      </div> 
      {filteredMembers.length === 0 ? (
        <div className="text-center py-20">
           
          <Users className="w-16 h-16 text-slate-300 mx-auto mb-4" /> 
          <h3 className="text-lg font-semibold text-slate-600 mb-2">
            No members found
          </h3> 
          <p className="text-slate-400">
            Try adjusting your search or filters
          </p> 
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
           
          {filteredMembers.map((m) => (
            <div
              key={m.id}
              className="rounded-2xl border border-slate-200 bg-white p-5 shadow-lg hover:shadow-xl transition-all"
            >
               
              <div className="flex items-start justify-between mb-4">
                 
                <div className="flex h-12 w-12 items-center justify-center overflow-hidden rounded-2xl bg-gradient-to-br from-indigo-500 to-fuchsia-500 text-lg font-bold text-white">
                   
                  {m.userName?.charAt(0).toUpperCase() || "U"} 
                </div> 
                <span
                  className={`shrink-0 rounded-full px-3 py-1 text-xs font-medium ${ROLE_COLORS[m.workspaceRole] || "bg-slate-100 text-slate-600"}`}
                >
                   
                  {m.workspaceRole?.replace("WORKSPACE_", "")} 
                </span> 
              </div> 
              <h3 className="font-semibold text-slate-900 mb-1 truncate">
                {m.userName}
              </h3> 
              <p className="text-sm text-slate-400 truncate mb-3">
                {m.userEmail}
              </p> 
              <div className="flex items-center gap-3 text-xs text-slate-400">
                 
                <span className="flex items-center gap-1">
                   
                  <Clock className="w-3.5 h-3.5" /> Joined 
                  {m.joinedAt
                    ? new Date(m.joinedAt).toLocaleDateString()
                    : "ΓÇö"} 
                </span> 
              </div> 
            </div>
          ))} 
        </div>
      )} 
    </div>
  );
}
