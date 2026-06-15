import { Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { X } from "lucide-react";
import { clsx } from "clsx";
export default function Modal({
  open,
  onClose,
  title,
  description,
  children,
  size = "md",
  footer,
  closeOnOverlay = true,
  className = "",
}) {
  const sizeClasses = {
    sm: "max-w-sm",
    md: "max-w-lg",
    lg: "max-w-2xl",
    xl: "max-w-4xl",
    "2xl": "max-w-6xl",
    full: "max-w-[95vw]",
  };
  return (
    <Transition.Root show={open} as={Fragment}>
       
      <Dialog
        as="div"
        className="relative z-50"
        onClose={closeOnOverlay ? onClose : () => {}}
        aria-modal="true"
        role="dialog"
      >
         
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-250"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
           
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity" /> 
        </Transition.Child> 
        <div className="fixed inset-0 z-50 overflow-y-auto">
           
          <div className="flex min-h-full items-center justify-center p-4 text-center sm:p-6">
             
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-250"
              enterFrom="opacity-0 scale-95 translate-y-4"
              enterTo="opacity-100 scale-100 translate-y-0"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100 translate-y-0"
              leaveTo="opacity-0 scale-95 translate-y-4"
            >
               
              <Dialog.Panel
                className={clsx(
                  "w-full transform overflow-hidden rounded-2xl bg-white dark:bg-surface-900 text-left shadow-soft-lg border border-surface-200/50 dark:border-surface-800/50 transition-all",
                  sizeClasses[size],
                  className,
                )}
              >
                 
                {title && (
                  <div className="flex items-center justify-between px-6 py-4 border-b border-surface-200 dark:border-surface-800">
                     
                    <div>
                       
                      <Dialog.Title className="text-lg font-semibold text-surface-900 dark:text-surface-100">
                         
                        {title} 
                      </Dialog.Title> 
                      {description && (
                        <Dialog.Description className="mt-1 text-sm text-surface-500 dark:text-surface-400">
                           
                          {description} 
                        </Dialog.Description>
                      )} 
                    </div> 
                    <button
                      type="button"
                      className="rounded-lg p-1.5 text-surface-400 hover:text-surface-600 hover:bg-surface-100 dark:hover:text-surface-200 dark:hover:bg-surface-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary-500"
                      onClick={onClose}
                      aria-label="Close modal"
                      autoFocus
                    >
                       
                      <X className="h-5 w-5" /> 
                    </button> 
                  </div>
                )} 
                <div className="px-6 py-4 overflow-y-auto max-h-[calc(90vh-8rem)]">
                   
                  {children} 
                </div> 
                {footer && (
                  <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-surface-200 dark:border-surface-800 bg-surface-50/50 dark:bg-surface-900/50">
                     
                    {footer} 
                  </div>
                )} 
              </Dialog.Panel> 
            </Transition.Child> 
          </div> 
        </div> 
      </Dialog> 
    </Transition.Root>
  );
}
