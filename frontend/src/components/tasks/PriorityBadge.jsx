import { Badge } from "../ui/badge.jsx";
import { cn } from "../../utils/cn.js";
const priorityVariants = {
  LOW: "bg-green-100 text-green-800 border-green-200 hover:bg-green-200",
  MEDIUM: "bg-yellow-100 text-yellow-800 border-yellow-200 hover:bg-yellow-200",
  HIGH: "bg-red-100 text-red-800 border-red-200 hover:bg-red-200",
  CRITICAL: "bg-rose-100 text-rose-800 border-rose-200 hover:bg-rose-200",
};
const PriorityBadge = ({ priority, className, ...props }) => {
  const variantClass =
    priorityVariants[priority?.toUpperCase()] ||
    "bg-gray-100 text-gray-800 border-gray-200 hover:bg-gray-200";
  return (
    <Badge
      className={cn(
        "font-semibold border px-2.5 py-1 uppercase text-xs tracking-wide",
        variantClass,
        className,
      )}
      {...props}
    >
       
      {priority || "Medium"} 
    </Badge>
  );
};
export default PriorityBadge;
