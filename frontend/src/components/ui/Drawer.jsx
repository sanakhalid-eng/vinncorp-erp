import { Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { X } from "lucide-react";
import { clsx } from "clsx";
export default function Drawer({
  open,
  onClose,
  title,
  description,
  children,
  size = "md",
  footer,
  position = "right",
  closeOnOverlay = true,
  className = "",
}) {
  const sizeClasses = {
    sm: "max-w-sm",
    md: "max-w-md",
    lg: "max-w-lg",
    xl: "max-w-2xl",
    full: "max-w-[95vw]",
  };
  const positionClasses = { right: "origin-right", left: "origin-left" };
  const panelPositionClasses = {
    right: "right-0 border-l",
    left: "left-0 border-r",
  };
  const slideDirection = {
    right: {
      enterFrom: "translate-x-full",
      enterTo: "translate-x-0",
      leaveFrom: "translate-x-0",
      leaveTo: "translate-x-full",
    },
    left: {
      enterFrom: "-translate-x-full",
      enterTo: "translate-x-0",
      leaveFrom: "translate-x-0",
      leaveTo: "-translate-x-full",
    },
  };
  const direction = slideDirection[position];
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
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
           
          <div className="fixed inset-0 bg-black/50 backdrop-blur-sm transition-opacity" /> 
        </Transition.Child> 
        <div
          className={`fixed inset-0 overflow-hidden ${position === "left" ? "justify-start" : "justify-end"} flex`}
        >
           
          <div className="absolute inset-0 overflow-hidden">
             
            <div
              className={`pointer-events-none fixed inset-y-0 ${position} flex max-w-full ${position === "left" ? "pl-4" : "pr-4"}`}
            >
               
              <Transition.Child
                as={Fragment}
                enter="transform transition ease-in-out duration-300"
                enterFrom={direction.enterFrom}
                enterTo={direction.enterTo}
                leave="transform transition ease-in-out duration-200"
                leaveFrom={direction.leaveFrom}
                leaveTo={direction.leaveTo}
              >
                 
                <Dialog.Panel
                  className={clsx(
                    "pointer-events-auto w-full transform overflow-y-auto bg-white dark:bg-surface-900 shadow-soft-lg border-surface-200/50 dark:border-surface-800/50 transition-all flex flex-col",
                    sizeClasses[size],
                    panelPositionClasses[position],
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
                        aria-label="Close drawer"
                        autoFocus
                      >
                         
                        <X className="h-5 w-5" /> 
                      </button> 
                    </div>
                  )} 
                  <div className="flex-1 overflow-y-auto px-6 py-4">
                     
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
        </div> 
      </Dialog> 
    </Transition.Root>
  );
}
