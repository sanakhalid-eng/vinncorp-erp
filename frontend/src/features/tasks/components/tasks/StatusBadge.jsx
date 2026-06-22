import { Badge } from "../../../../components/ui/badge.jsx";
import { cn } from "../../../../utils/cn.js";
const statusVariants = {
  PENDING:
    "bg-orange-100 text-orange-800 border-orange-200 hover:bg-orange-200",
  IN_PROGRESS: "bg-blue-100 text-blue-800 border-blue-200 hover:bg-blue-200",
  COMPLETED:
    "bg-emerald-100 text-emerald-800 border-emerald-200 hover:bg-emerald-200",
  CANCELLED: "bg-gray-100 text-gray-800 border-gray-200 hover:bg-gray-200",
};
const StatusBadge = ({ status, color, className, ...props }) => {
  const variantClass =
    statusVariants[status?.toUpperCase()] ||
    "bg-gray-100 text-gray-800 border-gray-200 hover:bg-gray-200";
  const dynamicStyle = color
    ? {
        "--badge-bg": color,
        "--badge-border": color + "40",
        backgroundColor: "var(--badge-bg)",
        borderColor: "var(--badge-border)",
        color: "#fff",
      }
    : {};
  return (
    <Badge
      className={cn(
        "capitalize font-semibold border px-2.5 py-1",
        color ? "text-white shadow-lg" : variantClass,
        className,
      )}
      style={dynamicStyle}
      {...props}
    >
       
      {status?.replace("_", " ") || "Unknown"} 
    </Badge>
  );
};
export default StatusBadge;
