import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import {
  createInvitation,
  getProjectInvitations,
  revokeInvitation,
} from "../api/invitationApi";
import { getUserProjects } from "../api/projectMembersApi";
import { getProjectRoles } from "../api/roleApi";
import { useAuth } from "../context/useAuth";
import { usePermission } from "../context/usePermission";
import { useProjectPermission } from "../context/ProjectPermissionContext.jsx";
import { toast, Toaster } from "sonner";
import {
  Mail,
  X,
  Clock,
  CheckCircle,
  AlertCircle,
  UserPlus,
  Send,
  Trash2,
  Users,
  ChevronLeft,
} from "lucide-react";
import ProjectNavBar from "../components/ProjectNavBar.jsx";
const statusConfig = {
  PENDING: {
    icon: Clock,
    color: "text-yellow-600 dark:text-yellow-400",
    bg: "bg-yellow-100 dark:bg-yellow-900/20",
    label: "Pending",
  },
  ACCEPTED: {
    icon: CheckCircle,
    color: "text-success-600 dark:text-success-400",
    bg: "bg-success-100 dark:bg-success-900/20",
    label: "Accepted",
  },
  EXPIRED: {
    icon: AlertCircle,
    color: "text-surface-500 dark:text-surface-400",
    bg: "bg-surface-100 dark:bg-surface-800",
    label: "Expired",
  },
  REVOKED: {
    icon: X,
    color: "text-danger-500 dark:text-danger-400",
    bg: "bg-danger-100 dark:bg-danger-900/20",
    label: "Revoked",
  },
};
export default function ProjectInvitations() {
  const { projectId, workspaceSlug } = useParams();
  const { user } = useAuth();
  const { canAddMember } = usePermission();
  const { setProjectId, clearProjectId } = useProjectPermission();
  const [invitations, setInvitations] = useState([]);
  const [roles, setRoles] = useState([]);
  const [projects, setProjects] = useState([]);
  const [selectedProjectId, setSelectedProjectId] = useState(projectId || "");
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [email, setEmail] = useState("");
  const [selectedRole, setSelectedRole] = useState("");
  const [sending, setSending] = useState(false);
  useEffect(() => {
    if (selectedProjectId) {
      setProjectId(Number(selectedProjectId));
    } else {
      clearProjectId();
    }
    return () => clearProjectId();
  }, [selectedProjectId, setProjectId, clearProjectId]);
  useEffect(() => {
    const init = async () => {
      try {
        const userProjects = await getUserProjects();
        setProjects(userProjects);
        if (!selectedProjectId && userProjects.length > 0) {
          setSelectedProjectId(String(userProjects[0].id));
        }
      } catch {
        toast.error("Failed to load projects");
      }
    };
    init();
  }, []);
  useEffect(() => {
    if (!selectedProjectId) return;
    loadData();
  }, [selectedProjectId]);
  const loadData = async () => {
    setLoading(true);
    try {
      const [invRes, rolesRes] = await Promise.all([
        getProjectInvitations(selectedProjectId),
        getProjectRoles(),
      ]);
      setInvitations(invRes.data?.data || invRes.data || []);
      setRoles(rolesRes.data?.data || rolesRes.data || []);
    } catch {
      toast.error("Failed to load invitations");
      setInvitations([]);
    } finally {
      setLoading(false);
    }
  };
  const handleSend = async () => {
    if (!email || !selectedRole) return;
    setSending(true);
    try {
      await createInvitation(selectedProjectId, {
        email,
        roleId: parseInt(selectedRole),
      });
      toast.success("Invitation sent!");
      setShowModal(false);
      setEmail("");
      setSelectedRole("");
      loadData();
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to send invitation");
    } finally {
      setSending(false);
    }
  };
  const handleRevoke = async (id) => {
    try {
      await revokeInvitation(id);
      toast.success("Invitation revoked");
      loadData();
    } catch {
      toast.error("Failed to revoke invitation");
    }
  };
  const selectedProject = projects.find(
    (p) => String(p.id) === String(selectedProjectId),
  );
  const pendingCount = invitations.filter((i) => i.status === "PENDING").length;
  return (
    <>
       
      <Toaster position="top-right" richColors /> 
      <div className="p-6 space-y-8">
         
        <ProjectNavBar projectName={selectedProject?.name} /> 
        <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
           
          <div>
             
            <div className="flex items-center gap-3 mb-2">
               
              <div className="w-12 h-12 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-3xl flex items-center justify-center shadow-lg">
                 
                <Mail className="w-6 h-6 text-white" /> 
              </div> 
              <div>
                 
                <h1 className="text-3xl font-bold bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent">
                   
                  Project Invitations 
                </h1> 
                <p className="text-gray-600">
                   
                  Manage invites for 
                  <span className="font-semibold">
                    {selectedProject?.name || "..."}
                  </span> 
                </p> 
              </div> 
            </div> 
          </div> 
          <div className="flex flex-wrap gap-3">
             
            <div className="flex items-center gap-2 px-4 py-2 bg-yellow-100 text-yellow-800 rounded-2xl">
               
              <Clock className="w-4 h-4" /> 
              <span className="font-semibold">{pendingCount}</span> 
              <span className="text-sm">pending</span> 
            </div> 
            <div className="flex items-center gap-2 px-4 py-2 bg-surface-100 text-surface-700 rounded-2xl">
               
              <Send className="w-4 h-4" /> 
              <span className="font-semibold">{invitations.length}</span> 
              <span className="text-sm">total</span> 
            </div> 
          </div> 
        </div> 
        <div className="bg-white/70 backdrop-blur-xl rounded-3xl p-6 shadow-xl border border-white/50">
           
          <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
             
            <div className="flex items-center gap-3 px-4 py-2 bg-gradient-to-r from-indigo-50 to-purple-50 rounded-2xl border border-indigo-100">
               
              <ChevronLeft className="w-4 h-4 text-indigo-600" /> 
              <select
                value={selectedProjectId}
                onChange={(e) => setSelectedProjectId(e.target.value)}
                className="bg-transparent font-semibold text-gray-900 focus:outline-none"
              >
                 
                <option value="">Select project</option> 
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))} 
              </select> 
            </div> 
            <div className="flex gap-3">
               
              {canAddMember() && (
                <button
                  onClick={() => setShowModal(true)}
                  disabled={!selectedProjectId}
                  className="flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-2xl font-semibold shadow-lg hover:shadow-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                   
                  <UserPlus className="w-5 h-5" /> Invite Member 
                </button>
              )} 
            </div> 
          </div> 
        </div> 
        {loading ? (
          <div className="bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-2xl border border-white/50">
             
            <div className="animate-pulse space-y-4">
               
              {[...Array(4)].map((_, i) => (
                <div key={i} className="h-16 bg-gray-200 rounded-2xl" />
              ))} 
            </div> 
          </div>
        ) : invitations.length === 0 ? (
          <div className="bg-white/70 backdrop-blur-xl rounded-3xl p-12 shadow-2xl border border-white/50 text-center">
             
            <Send className="w-16 h-16 text-surface-300 mx-auto mb-4" /> 
            <h3 className="text-xl font-bold text-surface-700 mb-2">
              No invitations yet
            </h3> 
            <p className="text-surface-500 mb-6">
              Invite team members to collaborate on this project
            </p> 
            {canAddMember() && (
              <button
                onClick={() => setShowModal(true)}
                className="inline-flex items-center gap-2 px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-2xl font-semibold shadow-lg hover:shadow-xl transition-all"
              >
                 
                <UserPlus className="w-5 h-5" /> Send First Invitation 
              </button>
            )} 
          </div>
        ) : (
          <div className="bg-white/70 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/50 overflow-hidden">
             
            <div className="overflow-x-auto">
               
              <table className="w-full text-sm">
                 
                <thead>
                   
                  <tr className="border-b border-surface-200 bg-surface-50/50">
                     
                    <th className="text-left py-4 px-6 text-surface-600 font-semibold">
                      Email
                    </th> 
                    <th className="text-left py-4 px-6 text-surface-600 font-semibold">
                      Role
                    </th> 
                    <th className="text-left py-4 px-6 text-surface-600 font-semibold">
                      Invited By
                    </th> 
                    <th className="text-left py-4 px-6 text-surface-600 font-semibold">
                      Status
                    </th> 
                    <th className="text-left py-4 px-6 text-surface-600 font-semibold">
                      Expires
                    </th> 
                    <th className="text-right py-4 px-6 text-surface-600 font-semibold">
                      Actions
                    </th> 
                  </tr> 
                </thead> 
                <tbody>
                   
                  {invitations.map((inv) => {
                    const status =
                      statusConfig[inv.status] || statusConfig.PENDING;
                    const StatusIcon = status.icon;
                    return (
                      <tr
                        key={inv.id}
                        className="border-b border-surface-100 hover:bg-surface-50/50 transition-colors"
                      >
                         
                        <td className="py-4 px-6">
                           
                          <span className="font-medium text-surface-900">
                            {inv.email}
                          </span> 
                        </td> 
                        <td className="py-4 px-6">
                           
                          <span className="inline-flex px-3 py-1 bg-indigo-50 text-indigo-700 rounded-xl text-xs font-semibold">
                             
                            {inv.roleName} 
                          </span> 
                        </td> 
                        <td className="py-4 px-6 text-surface-600">
                          {inv.invitedByName}
                        </td> 
                        <td className="py-4 px-6">
                           
                          <span
                            className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-xl text-xs font-semibold ${status.bg} ${status.color}`}
                          >
                             
                            <StatusIcon className="w-3.5 h-3.5" /> 
                            {status.label} 
                          </span> 
                        </td> 
                        <td className="py-4 px-6 text-surface-500 text-xs">
                           
                          {inv.status === "PENDING"
                            ? new Date(inv.expiresAt).toLocaleDateString()
                            : inv.status === "ACCEPTED"
                              ? new Date(inv.acceptedAt).toLocaleDateString()
                              : "-"} 
                        </td> 
                        <td className="py-4 px-6 text-right">
                           
                          {inv.status === "PENDING" && (
                            <button
                              onClick={() => handleRevoke(inv.id)}
                              className="p-2 text-surface-400 hover:text-danger-500 hover:bg-danger-50 rounded-xl transition-all"
                              title="Revoke invitation"
                            >
                               
                              <Trash2 className="w-4 h-4" /> 
                            </button>
                          )} 
                        </td> 
                      </tr>
                    );
                  })} 
                </tbody> 
              </table> 
            </div> 
          </div>
        )} 
      </div> 
      {showModal && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-50 p-4">
           
          <div className="bg-white rounded-2xl p-6 w-full max-w-md mx-4 shadow-2xl border border-white/50">
             
            <div className="flex items-center justify-between mb-6">
               
              <div className="flex items-center gap-3">
                 
                <div className="w-10 h-10 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center">
                   
                  <UserPlus className="w-5 h-5 text-white" /> 
                </div> 
                <h3 className="text-lg font-bold text-gray-900">
                  Invite Member
                </h3> 
              </div> 
              <button
                onClick={() => setShowModal(false)}
                className="p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-xl transition-all"
              >
                 
                <X className="w-5 h-5" /> 
              </button> 
            </div> 
            <div className="space-y-4">
               
              <div>
                 
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Email Address
                </label> 
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="colleague@example.com"
                  className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-sm"
                /> 
              </div> 
              <div>
                 
                <label className="block text-sm font-semibold text-gray-700 mb-1">
                  Project Role
                </label> 
                <select
                  value={selectedRole}
                  onChange={(e) => setSelectedRole(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent text-sm bg-white"
                >
                   
                  <option value="">Select a role...</option> 
                  {roles.map((role) => (
                    <option key={role.id} value={role.id}>
                       
                      {role.name} 
                    </option>
                  ))} 
                </select> 
              </div> 
              <button
                onClick={handleSend}
                disabled={sending || !email || !selectedRole}
                className="w-full py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-2xl font-semibold hover:from-indigo-700 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg text-sm"
              >
                 
                {sending ? "Sending..." : "Send Invitation"} 
              </button> 
            </div> 
          </div> 
        </div>
      )} 
    </>
  );
}
