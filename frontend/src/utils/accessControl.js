/**
 * Shared access-control helpers for navigation, routes, and UI visibility.
 * Delegates to usePermission() values ΓÇö keep logic in one place.
 */

export function canAccess({
  permission,
  permissions,
  role,
  roles,
  globalRole,
  minRole,
  requiresAdmin = false,
  requiresOwner = false,
  hasPermission,
  hasAnyPermission,
  hasRole,
  hasAnyRole,
  hasMinRoleLevel,
  isAdmin,
  isSuperAdmin,
  globalRoles = [],
  user,
}) {
  if (isSuperAdmin?.()) return true;

  if (requiresOwner && !user?.workspaceOwner) return false;

  if (requiresAdmin && !(isAdmin?.() || user?.workspaceOwner)) return false;

  if (globalRole) {
    const allowed = Array.isArray(globalRole) ? globalRole : [globalRole];
    if (!allowed.some((r) => globalRoles.includes(r))) return false;
  }

  if (permission && !hasPermission?.(permission)) return false;

  if (permissions) {
    const perms = Array.isArray(permissions) ? permissions : [permissions];
    if (!hasAnyPermission?.(...perms)) return false;
  }

  if (role && !hasRole?.(role)) return false;

  if (roles) {
    const allowedRoles = Array.isArray(roles) ? roles : [roles];
    if (!hasAnyRole?.(allowedRoles)) return false;
  }

  if (minRole && !hasMinRoleLevel?.(minRole)) return false;

  return true;
}

export function filterByAccess(items, accessContext) {
  return items.filter((item) =>
    canAccess({
      permission: item.requiresPermission ?? item.permission,
      permissions: item.requiresAnyPermission ?? item.permissions,
      role: item.requiresRole ?? item.role,
      roles: item.requiresAnyRole ?? item.roles,
      globalRole: item.globalRole,
      minRole: item.minRole,
      requiresAdmin: item.requiresAdmin,
      requiresOwner: item.requiresOwner,
      ...accessContext,
    }),
  );
}
