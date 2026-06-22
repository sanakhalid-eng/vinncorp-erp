import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { Building2, CheckCircle2, XCircle, Loader2 } from "lucide-react";
import { useAuth } from "../../../context/useAuth";
import {
  getWorkspaceInvitationByToken,
  acceptWorkspaceInvitation,
} from "../../settings/api/workspaceApi";
import Button from "../../../components/Button";
export default function WorkspaceInvitationAccept() {
  const { token } = useParams();
  const navigate = useNavigate();
  const { token: authToken, user } = useAuth();
  const [invitation, setInvitation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [accepting, setAccepting] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  useEffect(() => {
    loadInvitation();
  }, [token]);
  const loadInvitation = async () => {
    try {
      setLoading(true);
      const res = await getWorkspaceInvitationByToken(token);
      setInvitation(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Invalid or expired invitation");
    } finally {
      setLoading(false);
    }
  };
  const handleAccept = async () => {
    try {
      setAccepting(true);
      await acceptWorkspaceInvitation(token);
      setSuccess(true);
      setTimeout(() => navigate("/workspaces"), 2000);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to accept invitation");
    } finally {
      setAccepting(false);
    }
  };
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
         
        <Loader2 className="w-8 h-8 animate-spin text-indigo-600" /> 
      </div>
    );
  }
  if (!authToken) {
    return (
      <div className="min-h-screen flex items-center justify-center p-4">
         
        <div className="max-w-md w-full rounded-2xl border border-slate-200 bg-white p-8 shadow-lg text-center">
           
          <Building2 className="w-12 h-12 text-indigo-600 mx-auto mb-4" /> 
          <h1 className="text-2xl font-bold text-slate-900 mb-2">
            Workspace Invitation
          </h1> 
          {error ? (
            <>
               
              <XCircle className="w-10 h-10 text-red-500 mx-auto mb-3" /> 
              <p className="text-slate-600 mb-6">{error}</p> 
            </>
          ) : (
            <>
               
              <p className="text-slate-600 mb-2">
                 
                You've been invited to join 
                <strong>{invitation?.workspaceName}</strong> 
              </p> 
              <p className="text-slate-500 mb-6">
                Please log in or create an account to accept.
              </p> 
            </>
          )} 
          <div className="flex gap-3 justify-center">
             
            <Link to={`/login?redirect=/workspace-invite/${token}`}>
               
              <Button className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700">
                 
                Log In 
              </Button> 
            </Link> 
            <Link to="/register">
               
              <Button type="secondary" className="rounded-xl">
                 
                Register 
              </Button> 
            </Link> 
          </div> 
        </div> 
      </div>
    );
  }
  return (
    <div className="min-h-screen flex items-center justify-center p-4">
       
      <div className="max-w-md w-full rounded-2xl border border-slate-200 bg-white p-8 shadow-lg text-center">
         
        <Building2 className="w-12 h-12 text-indigo-600 mx-auto mb-4" /> 
        <h1 className="text-2xl font-bold text-slate-900 mb-2">
          Workspace Invitation
        </h1> 
        {success ? (
          <>
             
            <CheckCircle2 className="w-10 h-10 text-green-500 mx-auto mb-3" /> 
            <p className="text-green-600 font-medium mb-2">
              Invitation Accepted!
            </p> 
            <p className="text-slate-500">
              Redirecting to your workspaces...
            </p> 
          </>
        ) : error ? (
          <>
             
            <XCircle className="w-10 h-10 text-red-500 mx-auto mb-3" /> 
            <p className="text-red-600 mb-6">{error}</p> 
            <Button
              onClick={() => navigate("/workspaces")}
              className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
            >
               
              Go to Workspaces 
            </Button> 
          </>
        ) : (
          <>
             
            <p className="text-slate-600 mb-2">
               
              You've been invited to join workspace 
            </p> 
            <p className="text-xl font-bold text-slate-900 mb-1">
              {invitation?.workspaceName}
            </p> 
            <p className="text-sm text-slate-500 mb-6">
               
              Invited by {invitation?.invitedByName} &middot; Role: 
              {invitation?.workspaceRole || "MEMBER"} 
            </p> 
            <Button
              onClick={handleAccept}
              disabled={accepting}
              className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700 w-full"
            >
               
              {accepting ? "Accepting..." : "Accept Invitation"} 
            </Button> 
          </>
        )} 
      </div> 
    </div>
  );
}
