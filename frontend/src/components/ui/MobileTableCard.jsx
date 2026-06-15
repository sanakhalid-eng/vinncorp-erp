import { clsx } from "clsx";
export default function MobileTableCard({
  items,
  fields,
  renderActions,
  renderHeader,
  renderFooter,
  emptyMessage = "No items found",
  className = "",
  cardClassName = "",
}) {
  if (!items || items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
         
        <p className="text-surface-500 dark:text-surface-400">
          {emptyMessage}
        </p> 
      </div>
    );
  }
  return (
    <div className={clsx("space-y-3 md:hidden", className)}>
       
      {items.map((item, index) => (
        <div
          key={item.id || index}
          className={clsx(
            "bg-white dark:bg-surface-900 rounded-xl border border-surface-200 dark:border-surface-800 p-4 shadow-sm",
            cardClassName,
          )}
        >
           
          {renderHeader && (
            <div className="flex items-center justify-between mb-3 pb-3 border-b border-surface-100 dark:border-surface-800">
               
              {renderHeader(item, index)} 
            </div>
          )} 
          <div className="space-y-2">
             
            {fields.map((field, fieldIndex) => {
              if (field.hidden) return null;
              const value =
                typeof field.value === "function"
                  ? field.value(item)
                  : item[field.key];
              if (value === null || value === undefined) return null;
              return (
                <div
                  key={fieldIndex}
                  className="flex items-start justify-between gap-4"
                >
                   
                  {field.label && (
                    <span className="text-xs font-medium text-surface-500 dark:text-surface-400 uppercase tracking-wider shrink-0">
                       
                      {field.label} 
                    </span>
                  )} 
                  <div
                    className={clsx(
                      "text-sm text-surface-900 dark:text-surface-100",
                      field.label ? "text-right" : "w-full",
                    )}
                  >
                     
                    {value} 
                  </div> 
                </div>
              );
            })} 
          </div> 
          {renderActions && (
            <div className="flex items-center gap-2 mt-4 pt-3 border-t border-surface-100 dark:border-surface-800">
               
              {renderActions(item, index)} 
            </div>
          )} 
          {renderFooter && (
            <div className="mt-3 pt-3 border-t border-surface-100 dark:border-surface-800">
               
              {renderFooter(item, index)} 
            </div>
          )} 
        </div>
      ))} 
    </div>
  );
}
