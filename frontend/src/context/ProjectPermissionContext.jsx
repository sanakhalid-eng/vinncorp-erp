import { createContext, useContext, useState, useCallback } from "react";
import { getMyPermissions } from "../api/permissionApi";
const ProjectPermissionContext = createContext();
export const ProjectPermissionProvider = ({ children }) => {
  const [projectId, setProjectId] = useState(null);
  const clearProjectId = useCallback(() => {
    setProjectId(null);
  }, []);
  return (
    <ProjectPermissionContext.Provider
      value={{ projectId, setProjectId, clearProjectId }}
    >
       
      {children} 
    </ProjectPermissionContext.Provider>
  );
};
export const useProjectPermission = () => {
  const context = useContext(ProjectPermissionContext);
  if (!context) {
    throw new Error(
      "useProjectPermission must be used within ProjectPermissionProvider",
    );
  }
  return context;
};
