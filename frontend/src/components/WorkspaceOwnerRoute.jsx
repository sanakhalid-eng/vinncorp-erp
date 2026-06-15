import { Navigate } from "react-router-dom";
import { useAuth } from "../context/useAuth.js";
function WorkspaceOwnerRoute({ children }) {
  const { token, user } = useAuth();
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  if (token && !user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
         
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600" /> 
      </div>
    );
  }
  if (!user?.workspaceOwner) {
    return <Navigate to="/user-home" replace />;
  }
  return children;
}
export default WorkspaceOwnerRoute;
