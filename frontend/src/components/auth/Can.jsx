import { useAuth } from "../../context/useAuth.js";
import { usePermission } from "../../context/usePermission.js";
import { canAccess } from "../../utils/accessControl.js";

/**
 * Declarative RBAC wrapper ΓÇö renders children only when access is granted.
 *
 * @example
 * <Can permission={LEAD_CREATE}><CreateButton /></Can>
 * <Can permissions={[PROJECT_VIEW, PROJECT_VIEW_ALL]}><ProjectsLink /></Can>
 * <Can requiresAdmin fallback={<ReadOnlyView />}>...</Can>
 * <Can globalRole="SUPER_ADMIN">...</Can>
 */
export default function Can({
  children,
  permission,
  permissions,
  role,
  roles,
  globalRole,
  minRole,
  requiresAdmin = false,
  requiresOwner = false,
  fallback = null,
}) {
  const { user, globalRoles } = useAuth();
  const {
    hasPermission,
    hasAnyPermission,
    hasRole,
    hasAnyRole,
    hasMinRoleLevel,
    isAdmin,
    isSuperAdmin,
  } = usePermission();

  const allowed = canAccess({
    permission,
    permissions,
    role,
    roles,
    globalRole,
    minRole,
    requiresAdmin,
    requiresOwner,
    hasPermission,
    hasAnyPermission,
    hasRole,
    hasAnyRole,
    hasMinRoleLevel,
    isAdmin,
    isSuperAdmin,
    globalRoles,
    user,
  });

  return allowed ? children : fallback;
}
