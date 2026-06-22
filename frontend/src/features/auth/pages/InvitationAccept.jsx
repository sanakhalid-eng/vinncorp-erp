import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { getInvitation, acceptInvitation } from "../api/invitationApi";
import { useAuth } from "../../../context/useAuth";
import notify from "../../../lib/toast";
import { CheckCircle, XCircle, Clock, Loader2, ArrowRight } from "lucide-react";
import Logo from "../../../assets/Logo - PMT-SK.png";
export default function InvitationAccept() {
  const { token } = useParams();
  const navigate = useNavigate();
  const { token: authToken, user } = useAuth();
  const [invitation, setInvitation] = useState(null);
  const [loading, setLoading] = useState(true);
  const [accepting, setAccepting] = useState(false);
  const [error, setError] = useState(null);
  const [accepted, setAccepted] = useState(false);
  useEffect(() => {
    loadInvitation();
  }, [token]);
  const loadInvitation = async () => {
    try {
      const res = await getInvitation(token);
      setInvitation(res.data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Invalid or expired invitation");
    } finally {
      setLoading(false);
    }
  };
  const handleAccept = async () => {
    if (!authToken) {
      localStorage.setItem("pendingInviteToken", token);
      navigate("/login", { state: { redirectTo: `/invite/${token}` } });
      return;
    }
    setAccepting(true);
    try {
      await acceptInvitation(token);
      setAccepted(true);
      notify.success("Welcome to the project!");
      setTimeout(() => navigate("/projects"), 2000);
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to accept invitation");
      setError(err.response?.data?.message || "Failed to accept invitation");
    } finally {
      setAccepting(false);
    }
  };
  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
         
        <Loader2 className="w-8 h-8 text-indigo-600 animate-spin" /> 
      </div>
    );
  }
  if (error && !invitation) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
         
        <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">
           
          <XCircle className="w-16 h-16 text-red-400 mx-auto mb-4" /> 
          <h1 className="text-xl font-bold text-gray-900 mb-2">
            Invalid Invitation
          </h1> 
          <p className="text-gray-500 mb-6">{error}</p> 
          <Link
            to="/"
            className="inline-block px-6 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors"
          >
             
            Go to Home 
          </Link> 
        </div> 
      </div>
    );
  }
  if (accepted) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
         
        <div className="bg-white rounded-2xl shadow-lg p-8 max-w-md w-full text-center">
           
          <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" /> 
          <h1 className="text-xl font-bold text-gray-900 mb-2">
            Welcome Aboard!
          </h1> 
          <p className="text-gray-500 mb-2">
            You've joined the project successfully.
          </p> 
          <p className="text-sm text-gray-400">
            Redirecting to your projects...
          </p> 
        </div> 
      </div>
    );
  }
  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50 flex items-center justify-center p-4">
       
      <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full">
         
        <div className="text-center mb-6">
           
          <img
            src={Logo}
            alt="PMT-SK"
            className="w-20 h-20 mx-auto mb-4 object-contain"
          /> 
          <h1 className="text-2xl font-bold text-gray-900">
            Project Invitation
          </h1> 
        </div> 
        <div className="bg-gradient-to-r from-indigo-50 to-purple-50 rounded-xl p-6 mb-6">
           
          <div className="space-y-3">
             
            <div className="flex items-center justify-between">
               
              <span className="text-sm text-gray-500">Project</span> 
              <span className="text-sm font-semibold text-gray-900">
                {invitation?.projectName}
              </span> 
            </div> 
            <div className="flex items-center justify-between">
               
              <span className="text-sm text-gray-500">Invited by</span> 
              <span className="text-sm font-medium text-gray-900">
                {invitation?.invitedByName}
              </span> 
            </div> 
            <div className="flex items-center justify-between">
               
              <span className="text-sm text-gray-500">Role</span> 
              <span className="px-2.5 py-0.5 bg-indigo-100 text-indigo-700 rounded-full text-xs font-medium">
                 
                {invitation?.roleName} 
              </span> 
            </div> 
            <div className="flex items-center justify-between">
               
              <span className="text-sm text-gray-500">Status</span> 
              <span className="flex items-center gap-1 text-sm">
                 
                {invitation?.status === "PENDING" ? (
                  <>
                     
                    <Clock className="w-4 h-4 text-yellow-500" /> 
                    <span className="text-yellow-600">Pending</span> 
                  </>
                ) : (
                  <>
                     
                    <XCircle className="w-4 h-4 text-red-400" /> 
                    <span className="text-red-500 capitalize">
                      {invitation?.status?.toLowerCase()}
                    </span> 
                  </>
                )} 
              </span> 
            </div> 
          </div> 
        </div> 
        {error && (
          <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg mb-4">
             
            {error} 
          </div>
        )} 
        {invitation?.status === "PENDING" && (
          <button
            onClick={handleAccept}
            disabled={accepting}
            className="w-full py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-xl hover:from-indigo-700 hover:to-purple-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg hover:shadow-xl flex items-center justify-center gap-2"
          >
             
            {accepting ? (
              <Loader2 className="w-5 h-5 animate-spin" />
            ) : (
              <>
                 
                Accept Invitation <ArrowRight className="w-4 h-4" /> 
              </>
            )} 
          </button>
        )} 
        {invitation?.status !== "PENDING" && (
          <p className="text-center text-gray-500 text-sm">
             
            This invitation is no longer valid. 
          </p>
        )} 
        <p className="mt-6 text-center text-xs text-gray-400">
           
          By accepting, you agree to the PMT-SK terms of service 
        </p> 
      </div> 
    </div>
  );
}
