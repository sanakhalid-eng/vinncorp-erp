import { clsx } from "clsx";
export default function Table({
  columns,
  data,
  renderRow,
  emptyMessage = "No data available",
  className = "",
  mobileFields,
  mobileRenderHeader,
  mobileRenderActions,
  mobileRenderFooter,
}) {
  const isEmpty = !data || data.length === 0;
  return (
    <>
       
      <div className={clsx("hidden md:block table-container", className)}>
         
        <table className="table">
           
          <thead>
             
            <tr>
               
              {columns.map((col, index) => (
                <th
                  key={index}
                  className={clsx(col.className)}
                  style={col.width ? { width: col.width } : undefined}
                >
                   
                  {col.header} 
                </th>
              ))} 
            </tr> 
          </thead> 
          <tbody>
             
            {isEmpty ? (
              <tr>
                 
                <td
                  colSpan={columns.length}
                  className="text-center py-12 text-surface-500 dark:text-surface-400"
                >
                   
                  {emptyMessage} 
                </td> 
              </tr>
            ) : (
              data.map((row, index) => renderRow(row, index))
            )} 
          </tbody> 
        </table> 
      </div> 
      {mobileFields && (
        <div className="md:hidden">
           
          {isEmpty ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
               
              <p className="text-surface-500 dark:text-surface-400">
                {emptyMessage}
              </p> 
            </div>
          ) : (
            <div className="space-y-3">
               
              {data.map((item, index) => (
                <div
                  key={item.id || index}
                  className="bg-white dark:bg-surface-900 rounded-xl border border-surface-200 dark:border-surface-800 p-4 shadow-sm"
                >
                   
                  {mobileRenderHeader && (
                    <div className="flex items-center justify-between mb-3 pb-3 border-b border-surface-100 dark:border-surface-800">
                       
                      {mobileRenderHeader(item, index)} 
                    </div>
                  )} 
                  <div className="space-y-2">
                     
                    {mobileFields.map((field, fieldIndex) => {
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
                  {mobileRenderActions && (
                    <div className="flex items-center gap-2 mt-4 pt-3 border-t border-surface-100 dark:border-surface-800">
                       
                      {mobileRenderActions(item, index)} 
                    </div>
                  )} 
                  {mobileRenderFooter && (
                    <div className="mt-3 pt-3 border-t border-surface-100 dark:border-surface-800">
                       
                      {mobileRenderFooter(item, index)} 
                    </div>
                  )} 
                </div>
              ))} 
            </div>
          )} 
        </div>
      )} 
    </>
  );
}
