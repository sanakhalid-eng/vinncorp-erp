import React from "react";
import { X } from "lucide-react";
import { cn } from "../../../../utils/cn.js";
const RuleChip = ({ rule, onRemove, className = "" }) => {
  const getFieldLabel = (field) => {
    switch (field) {
      case "priority":
        return "Priority";
      default:
        return field;
    }
  };
  const getColorClass = (type) => {
    switch (type) {
      case "role":
        return "bg-blue-100 hover:bg-blue-200 text-blue-800 border-blue-200";
      case "assignee":
        return "bg-emerald-100 hover:bg-emerald-200 text-emerald-800 border-emerald-200";
      case "field":
        return "bg-orange-100 hover:bg-orange-200 text-orange-800 border-orange-200";
      default:
        return "bg-gray-100 hover:bg-gray-200 text-gray-800 border-gray-200";
    }
  };
  const getDisplayText = () => {
    if (rule.type === "assignee") {
      return "Only Assignee";
    }
    if (rule.type === "role") {
      return `Role: ${rule.value}`;
    }
    if (rule.type === "field") {
      return `${getFieldLabel(rule.field)} = ${rule.value}`;
    }
    return "Custom Rule";
  };
  return (
    <div
      className={cn(
        "inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium shadow-sm border transition-all cursor-pointer hover:scale-[1.02] group",
        getColorClass(rule.type),
        className,
      )}
    >
       
      <span>{getDisplayText()}</span> 
      {onRemove && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            onRemove(rule);
          }}
          className="ml-1 p-0.5 rounded-full opacity-100 lg:opacity-0 lg:group-hover:opacity-100 hover:bg-white dark:hover:bg-surface-700 transition-all"
          title="Remove"
        >
           
          <X className="w-3 h-3" /> 
        </button>
      )} 
    </div>
  );
};
export default RuleChip;
