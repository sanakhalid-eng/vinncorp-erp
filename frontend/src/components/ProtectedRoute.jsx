import { Navigate } from "react-router-dom";
import { useAuth } from "../context/useAuth.js";
import { usePermission } from "../context/usePermission.js";

/**
 * Unified route guard that handles authentication and optional permission checks.
 *
 * Props:
 *   children      - Route content
 *   permission    - Single permission string required
 *   permissions   - Array of permission strings (any-of match)
 *   roles         - Array of role strings (any-of match)
 *   globalRole    - Single global role string required (e.g., "SUPER_ADMIN")
 *   minRole       - Minimum role level required
 *   fallbackPath  - Redirect path on permission denial (default: /forbidden)
 *
 * Usage:
 *   <ProtectedRoute><Dashboard /></ProtectedRoute>
 *   <ProtectedRoute permission="CREATE_PROJECT"><CreateProject /></ProtectedRoute>
 *   <ProtectedRoute permissions={["VIEW_USERS", "VIEW_ROLES"]}><Admin /></ProtectedRoute>
 *   <ProtectedRoute globalRole="SUPER_ADMIN"><AdminDashboard /></ProtectedRoute>
 *   <ProtectedRoute minRole="ADMIN"><Settings /></ProtectedRoute>
 */
function ProtectedRoute({
  children,
  permission,
  permissions,
  roles,
  globalRole,
  minRole,
  fallbackPath = "/forbidden",
}) {
  const { token, user, globalRoles } = useAuth();
  const {
    hasPermission,
    hasAnyPermission,
    hasRole,
    hasMinRoleLevel,
    isCheckingProjectPermissions,
    permissionsLoading,
  } = usePermission();

  // Not authenticated
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // Token exists but user not loaded yet
  if (token && !user) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600" />
      </div>
    );
  }

  // Global role check (before other permission checks)
  if (globalRole) {
    const allowedGlobalRoles = Array.isArray(globalRole) ? globalRole : [globalRole];
    const userGlobalRoles = globalRoles || [];
    const hasGlobalRole = allowedGlobalRoles.some((r) => userGlobalRoles.includes(r));
    if (!hasGlobalRole) {
      return <Navigate to={fallbackPath} replace />;
    }
  }

  // Permission checks (only when permission props are provided)
  const hasPermissionProps = permission || permissions || roles || minRole;
  const isLoading = permissionsLoading || isCheckingProjectPermissions;

  if (hasPermissionProps && !isLoading) {
    // Single permission check
    if (permission && !hasPermission(permission)) {
      return <Navigate to={fallbackPath} replace />;
    }

    // Multiple permissions (any-of)
    if (permissions) {
      const perms = Array.isArray(permissions) ? permissions : [permissions];
      if (!hasAnyPermission(...perms)) {
        return <Navigate to={fallbackPath} replace />;
      }
    }

    // Role check
    if (roles) {
      const allowedRoles = Array.isArray(roles) ? roles : [roles];
      if (!hasRole || !allowedRoles.some((r) => hasRole(r))) {
        return <Navigate to={fallbackPath} replace />;
      }
    }

    // Minimum role level check
    if (minRole && hasMinRoleLevel && !hasMinRoleLevel(minRole)) {
      return <Navigate to={fallbackPath} replace />;
    }
  }

  return (
    <div className="relative">
      {children}
      {hasPermissionProps && isLoading && (
        <div className="absolute inset-0 z-50 flex items-center justify-center bg-white/70 dark:bg-surface-900/70">
          <div className="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-b-2 border-indigo-600" />
        </div>
      )}
    </div>
  );
}

export default ProtectedRoute;
