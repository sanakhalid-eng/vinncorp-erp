import { useMemo } from "react";
import { useAuth } from "../context/useAuth.js";
import { usePermission } from "../context/usePermission.js";
const RoleGuard = ({
  children,
  roles,
  minRole,
  permission,
  fallback = null,
  inverse = false,
}) => {
  const { user } = useAuth();
  const { hasRole, hasMinRoleLevel, hasPermission, isAdmin, userRole } =
    usePermission();
  const hasAccess = () => {
    if (!user) return false;
    if (permission) {
      const match = hasPermission(permission);
      return inverse ? !match : match;
    }
    if (roles) {
      const allowedRoles = Array.isArray(roles) ? roles : [roles];
      const match = allowedRoles.includes(userRole);
      return inverse ? !match : match;
    }
    if (minRole) {
      return hasMinRoleLevel(minRole);
    }
    return true;
  };
  if (!hasAccess()) {
    return fallback;
  }
  return children;
};
export const withRoleGuard = (Component, guardProps) => {
  return (props) => (
    <RoleGuard {...guardProps}>
       
      <Component {...props} /> 
    </RoleGuard>
  );
};
export const useRole = () => {
  const { user } = useAuth();
  const {
    userRole,
    userRoleLevel,
    hasRole,
    hasMinRoleLevel,
    hasPermission,
    isAdmin,
    isProjectManager,
    isTeamMember,
  } = usePermission();
  return useMemo(
    () => ({
      role: userRole,
      level: userRoleLevel,
      hasRole,
      hasMinRole: hasMinRoleLevel,
      isAdmin: isAdmin(),
      isManager: isProjectManager(),
      isTeamLead: isProjectManager() || isAdmin(),
      isDeveloper: isTeamMember() || isProjectManager(),
      hasPermission,
    }),
    [
      userRole,
      userRoleLevel,
      hasRole,
      hasMinRoleLevel,
      hasPermission,
      isAdmin,
      isProjectManager,
      isTeamMember,
    ],
  );
};
export default RoleGuard;
