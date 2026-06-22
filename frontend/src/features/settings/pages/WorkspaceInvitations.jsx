import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Mail,
  ArrowLeft,
  Loader2,
  Trash2,
  Building2,
  Send,
} from "lucide-react";
import notify from "../../../lib/toast";
import {
  getWorkspace,
  getWorkspaceInvitations,
  createWorkspaceInvitation,
  revokeWorkspaceInvitation,
} from "../api/workspaceApi";
import Button from "../../../components/Button";
export default function WorkspaceInvitations() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [workspace, setWorkspace] = useState(null);
  const [invitations, setInvitations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showInvite, setShowInvite] = useState(false);
  const [email, setEmail] = useState("");
  const [sending, setSending] = useState(false);
  const [revoking, setRevoking] = useState(null);
  useEffect(() => {
    loadData();
  }, [id]);
  const loadData = async () => {
    try {
      setLoading(true);
      const [wsRes, invRes] = await Promise.all([
        getWorkspace(id),
        getWorkspaceInvitations(id),
      ]);
      setWorkspace(wsRes.data.data);
      setInvitations(invRes.data.data || []);
    } catch {
      notify.error("Failed to load invitations");
      navigate("/workspaces");
    } finally {
      setLoading(false);
    }
  };
  const handleInvite = async () => {
    if (!email.trim()) {
      notify.error("Email is required");
      return;
    }
    try {
      setSending(true);
      await createWorkspaceInvitation(id, {
        email: email.trim(),
        workspaceRole: "WORKSPACE_MEMBER",
      });
      notify.success("Invitation sent");
      setEmail("");
      setShowInvite(false);
      loadData();
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to send invitation");
    } finally {
      setSending(false);
    }
  };
  const handleRevoke = async (invitationId) => {
    try {
      setRevoking(invitationId);
      await revokeWorkspaceInvitation(invitationId);
      notify.success("Invitation revoked");
      loadData();
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to revoke invitation");
    } finally {
      setRevoking(null);
    }
  };
  const getStatusBadge = (status) => {
    const styles = {
      PENDING: "bg-amber-50 text-amber-700",
      ACCEPTED: "bg-green-50 text-green-700",
      EXPIRED: "bg-red-50 text-red-700",
      REVOKED: "bg-slate-50 text-slate-500",
    };
    return styles[status] || "bg-slate-50 text-slate-600";
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
            {workspace?.name} Invitations
          </span> 
        </div> 
      </header> 
      <main className="max-w-4xl mx-auto px-4 py-6 lg:px-6">
         
        <div className="flex items-center justify-between mb-8">
           
          <div>
             
            <h1 className="text-3xl font-bold text-slate-900">
              Invitations
            </h1> 
            <p className="text-slate-500 mt-1">
              Manage workspace invitations
            </p> 
          </div> 
          <Button
            onClick={() => setShowInvite(true)}
            className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
          >
             
            <Send className="w-4 h-4 mr-1.5 inline" /> Invite Member 
          </Button> 
        </div> 
        <div className="rounded-2xl border border-slate-200 bg-white shadow-lg overflow-hidden">
           
          {invitations.length === 0 ? (
            <div className="text-center py-12">
               
              <Mail className="w-12 h-12 text-slate-300 mx-auto mb-3" /> 
              <p className="text-slate-500">No invitations yet</p> 
            </div>
          ) : (
            <div className="divide-y divide-slate-100">
               
              {invitations.map((inv) => (
                <div
                  key={inv.id}
                  className="flex items-center justify-between p-4 hover:bg-slate-50 transition"
                >
                   
                  <div className="flex items-center gap-4">
                     
                    <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-cyan-400 to-indigo-500 text-white">
                       
                      <Mail className="h-5 w-5" /> 
                    </div> 
                    <div>
                       
                      <p className="font-semibold text-slate-900">
                        {inv.email}
                      </p> 
                      <p className="text-sm text-slate-500">
                         
                        Invited by {inv.invitedByName} &middot; Role: 
                        {inv.workspaceRole || "WORKSPACE_MEMBER"} 
                      </p> 
                    </div> 
                  </div> 
                  <div className="flex items-center gap-3">
                     
                    <span
                      className={`rounded-full px-3 py-1 text-xs font-medium ${getStatusBadge(inv.status)}`}
                    >
                       
                      {inv.status} 
                    </span> 
                    {inv.status === "PENDING" && (
                      <button
                        onClick={() => handleRevoke(inv.id)}
                        disabled={revoking === inv.id}
                        className="rounded-xl p-2 text-red-400 hover:bg-red-50 hover:text-red-600 transition"
                        title="Revoke invitation"
                      >
                         
                        {revoking === inv.id ? (
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
      {showInvite && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/45 backdrop-blur-sm p-4">
           
          <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl">
             
            <h3 className="text-xl font-bold text-slate-900 mb-4">
              Invite Member
            </h3> 
            <div>
               
              <label className="block text-sm font-medium text-slate-700 mb-1">
                Email Address
              </label> 
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="colleague@example.com"
                className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400"
              /> 
            </div> 
            <div className="flex justify-end gap-3 mt-6">
               
              <Button
                type="secondary"
                onClick={() => setShowInvite(false)}
                className="rounded-xl"
              >
                 
                Cancel 
              </Button> 
              <Button
                onClick={handleInvite}
                disabled={sending || !email.trim()}
                className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
              >
                 
                {sending ? "Sending..." : "Send Invitation"} 
              </Button> 
            </div> 
          </div> 
        </div>
      )} 
    </div>
  );
}
