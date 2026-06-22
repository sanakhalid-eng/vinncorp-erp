import { Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { AlertTriangle, X, CheckCircle } from "lucide-react";
import { clsx } from "clsx";
import { useIsMobile } from "../../../../hooks/useBreakpoint";

const ConfirmationDialog = ({
  isOpen,
  onClose,
  onConfirm,
  title = "Confirm Removal",
  message = "Are you sure you want to remove this member? This action cannot be undone.",
  confirmText = "Remove",
  cancelText = "Cancel",
}) => {
  const isMobile = useIsMobile();
  return (
    <Transition appear show={isOpen} as={Fragment}>
       
      <Dialog as="div" className="relative z-50" onClose={onClose}>
         
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
           
          <div className="fixed inset-0 bg-black/60 backdrop-blur-sm" /> 
        </Transition.Child> 
        <div className="fixed inset-0 overflow-y-auto">
           
          <div className={clsx(
            "flex min-h-full",
            isMobile ? "items-end p-0" : "items-center justify-center p-4",
          )}>
             
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95 translate-y-4"
              enterTo="opacity-100 scale-100 translate-y-0"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100 translate-y-0"
              leaveTo="opacity-0 scale-95 translate-y-4"
            >
               
              <Dialog.Panel className={clsx(
                "w-full transform bg-white shadow-2xl border border-gray-100",
                isMobile
                  ? "rounded-t-3xl max-h-[85vh]"
                  : "max-w-md rounded-3xl",
              )}>
                 
                <div className="relative p-8">
                   
                  <button
                    onClick={onClose}
                    className="absolute top-4 right-4 p-2 text-gray-400 hover:text-gray-600 hover:bg-gray-100 rounded-xl transition-all"
                  >
                     
                    <X className="w-5 h-5" /> 
                  </button> 
                  <div className="flex items-start gap-4 mb-6">
                     
                    <div className="w-14 h-14 bg-red-100 rounded-2xl flex items-center justify-center flex-shrink-0">
                       
                      <AlertTriangle className="w-7 h-7 text-red-600" /> 
                    </div> 
                    <div className="flex-1">
                       
                      <Dialog.Title className="text-xl font-bold text-gray-900">
                         
                        {title} 
                      </Dialog.Title> 
                      <p className="text-gray-600 mt-1">{message}</p> 
                    </div> 
                  </div> 
                  <div className="flex gap-3">
                     
                    <button
                      type="button"
                      onClick={onClose}
                      className="flex-1 px-6 py-3.5 border border-gray-200 text-gray-700 rounded-2xl hover:bg-gray-50 transition-colors font-medium"
                    >
                       
                      {cancelText} 
                    </button> 
                    <button
                      onClick={onConfirm}
                      className="flex-1 px-6 py-3.5 bg-gradient-to-r from-red-500 to-rose-600 text-white rounded-2xl hover:from-red-600 hover:to-rose-700 font-semibold shadow-lg hover:shadow-xl transition-all flex items-center justify-center gap-2"
                    >
                       
                      <CheckCircle className="w-5 h-5" /> {confirmText} 
                    </button> 
                  </div> 
                </div> 
              </Dialog.Panel> 
            </Transition.Child> 
          </div> 
        </div> 
      </Dialog> 
    </Transition>
  );
};
export default ConfirmationDialog;
