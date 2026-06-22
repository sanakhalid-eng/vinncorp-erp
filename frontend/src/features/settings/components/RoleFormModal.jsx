import { useState, useEffect, Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { X, Save, Shield, Globe, Folder, CheckCircle } from "lucide-react";
import { createRole, updateRole } from "../api/roleApi";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import { toast } from "sonner";
const RoleFormModal = ({
  isOpen,
  onClose,
  role: initialRole = null,
  onSuccess,
}) => {
  const [form, setForm] = useState({
    name: "",
    description: "",
    scope: "PROJECT",
  });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  useEffect(() => {
    if (initialRole) {
      setForm({
        name: initialRole.name || "",
        description: initialRole.description || "",
        scope: initialRole.scope || "PROJECT",
      });
    } else {
      setForm({ name: "", description: "", scope: "PROJECT" });
    }
    setErrors({});
  }, [initialRole, isOpen]);
  const validate = () => {
    const newErrors = {};
    if (!form.name.trim()) {
      newErrors.name = "Role name is required";
    } else if (form.name.length < 2 || form.name.length > 50) {
      newErrors.name = "Role name must be 2-50 characters";
    }
    if (form.name.trim().toUpperCase() === "ADMIN") {
      newErrors.name = 'Cannot modify system role "ADMIN"';
    }
    if (!form.scope) {
      newErrors.scope = "Scope is required";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const submitData = {
        name: form.name.trim().toUpperCase().replace(" ", "_"),
        description: form.description.trim(),
        scope: form.scope,
      };
      if (initialRole) {
        await updateRole(initialRole.id, submitData);
        toast.success("Role updated successfully");
      } else {
        await createRole(submitData);
        toast.success("Role created successfully");
      }
      onSuccess?.();
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to save role");
    } finally {
      setLoading(false);
    }
  };
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
           
          <div className="flex min-h-full items-center justify-center p-4">
             
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 scale-95 translate-y-4"
              enterTo="opacity-100 scale-100 translate-y-0"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 scale-100 translate-y-0"
              leaveTo="opacity-0 scale-95 translate-y-4"
            >
               
              <Dialog.Panel className="w-full max-w-md transform rounded-3xl bg-white shadow-2xl border border-gray-100">
                 
                <div className="relative">
                   
                  <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-t-3xl" /> 
                  <div className="p-8 pb-6 border-b border-gray-100 bg-gradient-to-r from-indigo-50/80 to-purple-50/80 rounded-t-3xl">
                     
                    <div className="flex items-center gap-4">
                       
                      <div className="w-14 h-14 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg">
                         
                        <Shield className="w-8 h-8 text-white" /> 
                      </div> 
                      <div className="flex-1">
                         
                        <Dialog.Title className="text-2xl font-bold text-gray-900">
                           
                          {initialRole ? "Edit Role" : "Create New Role"} 
                        </Dialog.Title> 
                        <p className="text-gray-600">
                           
                          {initialRole
                            ? "Update role details"
                            : "Define a new role for your system"} 
                        </p> 
                      </div> 
                      <button
                        onClick={onClose}
                        className="p-2.5 text-gray-400 hover:text-gray-600 hover:bg-white/60 rounded-xl transition-all"
                      >
                         
                        <X className="w-5 h-5" /> 
                      </button> 
                    </div> 
                  </div> 
                  <form onSubmit={handleSubmit} className="p-8 space-y-6">
                     
                    <div>
                       
                      <label className="block text-sm font-semibold text-gray-900 mb-2.5">
                         
                        Role Name <span className="text-red-500">*</span> 
                      </label> 
                      <Input
                        value={form.name}
                        onChange={(e) =>
                          setForm({ ...form, name: e.target.value })
                        }
                        placeholder="e.g. PROJECT_MANAGER"
                        className="w-full border-gray-200 focus:border-indigo-500 focus:ring-indigo-200 uppercase"
                        error={errors.name}
                      /> 
                      {errors.name && (
                        <p className="mt-2 text-sm text-red-600 flex items-center gap-1">
                           
                          <X className="w-3 h-3" /> {errors.name} 
                        </p>
                      )} 
                    </div> 
                    <div>
                       
                      <label className="block text-sm font-semibold text-gray-900 mb-3">
                         
                        Role Scope <span className="text-red-500">*</span> 
                      </label> 
                      <div className="grid grid-cols-2 gap-3">
                         
                        <label
                          className={`flex items-center gap-3 p-4 border-2 rounded-2xl cursor-pointer transition-all ${form.scope === "SYSTEM" ? "border-indigo-500 bg-indigo-50 shadow-md" : "border-gray-200 hover:border-gray-300 hover:bg-gray-50"}`}
                        >
                           
                          <input
                            type="radio"
                            name="scope"
                            value="SYSTEM"
                            checked={form.scope === "SYSTEM"}
                            onChange={(e) =>
                              setForm({ ...form, scope: e.target.value })
                            }
                            className="sr-only"
                          /> 
                          <Globe
                            className={`w-5 h-5 ${form.scope === "SYSTEM" ? "text-indigo-600" : "text-gray-400"}`}
                          /> 
                          <div>
                             
                            <p className="font-semibold text-gray-900">
                              System
                            </p> 
                            <p className="text-xs text-gray-500">
                              System access
                            </p> 
                          </div> 
                        </label> 
                        <label
                          className={`flex items-center gap-3 p-4 border-2 rounded-2xl cursor-pointer transition-all ${form.scope === "PROJECT" ? "border-indigo-500 bg-indigo-50 shadow-md" : "border-gray-200 hover:border-gray-300 hover:bg-gray-50"}`}
                        >
                           
                          <input
                            type="radio"
                            name="scope"
                            value="PROJECT"
                            checked={form.scope === "PROJECT"}
                            onChange={(e) =>
                              setForm({ ...form, scope: e.target.value })
                            }
                            className="sr-only"
                          /> 
                          <Folder
                            className={`w-5 h-5 ${form.scope === "PROJECT" ? "text-indigo-600" : "text-gray-400"}`}
                          /> 
                          <div>
                             
                            <p className="font-semibold text-gray-900">
                              Project
                            </p> 
                            <p className="text-xs text-gray-500">
                              Per-project
                            </p> 
                          </div> 
                        </label> 
                      </div> 
                      {errors.scope && (
                        <p className="mt-2 text-sm text-red-600 flex items-center gap-1">
                           
                          <X className="w-3 h-3" /> {errors.scope} 
                        </p>
                      )} 
                    </div> 
                    <div>
                       
                      <label className="block text-sm font-semibold text-gray-900 mb-2.5">
                         
                        Description 
                      </label> 
                      <textarea
                        rows={3}
                        value={form.description}
                        onChange={(e) =>
                          setForm({ ...form, description: e.target.value })
                        }
                        placeholder="Optional description of role responsibilities..."
                        className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-200 focus:border-indigo-500 resize-none transition-all"
                      /> 
                    </div> 
                    <div className="flex gap-3 pt-4">
                       
                      <Button
                        type="submit"
                        disabled={loading}
                        className="flex-1 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-white shadow-lg hover:shadow-xl transition-all h-12 text-base"
                      >
                         
                        {loading ? (
                          <span className="flex items-center gap-2">
                             
                            <svg
                              className="animate-spin w-4 h-4"
                              viewBox="0 0 24 24"
                            >
                               
                              <circle
                                className="opacity-25"
                                cx="12"
                                cy="12"
                                r="10"
                                stroke="currentColor"
                                strokeWidth="4"
                                fill="none"
                              /> 
                              <path
                                className="opacity-75"
                                fill="currentColor"
                                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                              /> 
                            </svg> 
                            Saving... 
                          </span>
                        ) : (
                          <span className="flex items-center gap-2">
                             
                            <CheckCircle className="w-5 h-5" /> Save Role 
                          </span>
                        )} 
                      </Button> 
                      <Button
                        type="button"
                        variant="outline"
                        onClick={onClose}
                        disabled={loading}
                        className="flex-1 border-gray-200 hover:bg-gray-50 h-12 text-base"
                      >
                         
                        Cancel 
                      </Button> 
                    </div> 
                  </form> 
                </div> 
              </Dialog.Panel> 
            </Transition.Child> 
          </div> 
        </div> 
      </Dialog> 
    </Transition>
  );
};
export default RoleFormModal;
