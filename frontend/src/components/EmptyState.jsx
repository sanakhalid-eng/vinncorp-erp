import { Inbox } from "lucide-react";
export default function EmptyState({
  icon: Icon = Inbox,
  title = "No data",
  description,
  action,
  secondaryAction,
}) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4">
       
      <div className="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mb-4">
         
        <Icon className="w-8 h-8 text-gray-400" /> 
      </div> 
      <h3 className="text-lg font-semibold text-gray-900 mb-1">{title}</h3> 
      {description && (
        <p className="text-sm text-gray-500 text-center max-w-sm mb-6">
          {description}
        </p>
      )} 
      {(action || secondaryAction) && (
        <div className="flex flex-col sm:flex-row gap-3">
           
          {action && (
            <button
              onClick={action.onClick}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium"
            >
               
              {action.icon && <action.icon className="w-4 h-4" />} 
              {action.label} 
            </button>
          )} 
          {secondaryAction && (
            <button
              onClick={secondaryAction.onClick}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-gray-50 text-gray-700 rounded-lg hover:bg-gray-100 transition-colors text-sm font-medium border border-gray-200"
            >
               
              {secondaryAction.icon && (
                <secondaryAction.icon className="w-4 h-4" />
              )} 
              {secondaryAction.label} 
            </button>
          )} 
        </div>
      )} 
    </div>
  );
}
