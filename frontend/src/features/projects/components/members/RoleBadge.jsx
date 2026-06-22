import { Badge } from "../../../../components/ui/badge.jsx";
import { cn } from "../../../../utils/cn.js";
const RoleBadge = ({ role, className = "" }) => {
  const roleConfig = {
    ADMIN: { color: "bg-red-500 hover:bg-red-600 text-white", label: "Admin" },
    PROJECT_MANAGER: {
      color: "bg-indigo-500 hover:bg-indigo-600 text-white",
      label: "Manager",
    },
    TEAM_MEMBER: {
      color: "bg-emerald-500 hover:bg-emerald-600 text-white",
      label: "Member",
    },
    VIEWER: {
      color: "bg-gray-500 hover:bg-gray-600 text-white",
      label: "Viewer",
    },
  };
  const config = roleConfig[role] || {
    color: "bg-gray-400 hover:bg-gray-500 text-white",
    label: role,
  };
  return (
    <Badge className={cn("font-semibold px-3 py-1", config.color, className)}>
       
      {config.label} 
    </Badge>
  );
};
export default RoleBadge;
