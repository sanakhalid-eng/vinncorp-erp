import { useState, useEffect, Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { X, Save, UserPlus, Mail, Shield, CheckCircle } from "lucide-react";
import { createUser, updateUser } from "../../../api/userApi";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import RoleBadge from "../../projects/components/members/RoleBadge.jsx";
import { toast } from "sonner";
const UserFormModal = ({
  isOpen,
  onClose,
  user: initialUser = null,
  onSuccess,
  availableRoles = [],
}) => {
  const [form, setForm] = useState({ name: "", email: "", role: "" });
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  useEffect(() => {
    if (initialUser) {
      setForm({
        name: initialUser.name || "",
        email: initialUser.email || "",
        role: initialUser.role || "",
      });
    } else {
      setForm({ name: "", email: "", role: "" });
    }
    setErrors({});
  }, [initialUser, isOpen]);
  const validate = () => {
    const newErrors = {};
    if (!form.name.trim()) {
      newErrors.name = "Name is required";
    }
    if (!form.email.trim()) {
      newErrors.email = "Email is required";
    } else if (!/\S+@\S+\.\S+/.test(form.email)) {
      newErrors.email = "Email is invalid";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;
    setLoading(true);
    try {
      const submitData = { name: form.name.trim(), email: form.email.trim() };
      if (initialUser?.id) {
        await updateUser(initialUser.id, submitData);
        toast.success("User updated successfully");
      } else {
        await createUser(submitData);
        toast.success(
          "User created successfully. Assign a role from the Users page.",
        );
      }
      onSuccess?.();
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to save user");
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
                   
                  <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-emerald-500 to-teal-600 rounded-t-3xl" /> 
                  <div className="p-8 pb-6 border-b border-gray-100 bg-gradient-to-r from-emerald-50/80 to-teal-50/80 rounded-t-3xl">
                     
                    <div className="flex items-center gap-4">
                       
                      <div className="w-14 h-14 bg-gradient-to-r from-emerald-500 to-teal-600 rounded-2xl flex items-center justify-center shadow-lg">
                         
                        <UserPlus className="w-8 h-8 text-white" /> 
                      </div> 
                      <div className="flex-1">
                         
                        <Dialog.Title className="text-2xl font-bold text-gray-900">
                           
                          {initialUser ? "Edit User" : "Create New User"} 
                        </Dialog.Title> 
                        <p className="text-gray-600">
                           
                          {initialUser
                            ? "Update user details"
                            : "Add a new team member"} 
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
                         
                        Full Name <span className="text-red-500">*</span> 
                      </label> 
                      <Input
                        value={form.name}
                        onChange={(e) =>
                          setForm({ ...form, name: e.target.value })
                        }
                        placeholder="John Doe"
                        className="w-full border-gray-200 focus:border-emerald-500 focus:ring-emerald-200"
                        error={errors.name}
                      /> 
                      {errors.name && (
                        <p className="mt-2 text-sm text-red-600 flex items-center gap-1">
                           
                          <X className="w-3 h-3" /> {errors.name} 
                        </p>
                      )} 
                    </div> 
                    <div>
                       
                      <label className="block text-sm font-semibold text-gray-900 mb-2.5">
                         
                        Email Address 
                        <span className="text-red-500">*</span> 
                      </label> 
                      <div className="relative">
                         
                        <Mail className="absolute left-3.5 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" /> 
                        <Input
                          type="email"
                          value={form.email}
                          onChange={(e) =>
                            setForm({ ...form, email: e.target.value })
                          }
                          placeholder="john@example.com"
                          className="w-full pl-11 border-gray-200 focus:border-emerald-500 focus:ring-emerald-200"
                          error={errors.email}
                        /> 
                      </div> 
                      {errors.email && (
                        <p className="mt-2 text-sm text-red-600 flex items-center gap-1">
                           
                          <X className="w-3 h-3" /> {errors.email} 
                        </p>
                      )} 
                    </div> 
                    <div className="flex gap-3 pt-4">
                       
                      <Button
                        type="submit"
                        disabled={loading}
                        className="flex-1 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white shadow-lg hover:shadow-xl transition-all h-12 text-base"
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
                             
                            <CheckCircle className="w-5 h-5" /> Save User 
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
export default UserFormModal;
