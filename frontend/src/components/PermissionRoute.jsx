import ProtectedRoute from "./ProtectedRoute";

/**
 * Backwards-compatible alias for ProtectedRoute with permission props.
 * @deprecated Use ProtectedRoute with permission/permissions props directly.
 */
const PermissionRoute = ({
  children,
  permission,
  permissions,
  roles,
  minRole,
  fallbackPath = "/forbidden",
}) => {
  return (
    <ProtectedRoute
      permission={permission}
      permissions={permissions}
      roles={roles}
      minRole={minRole}
      fallbackPath={fallbackPath}
    >
      {children}
    </ProtectedRoute>
  );
};

export default PermissionRoute;
