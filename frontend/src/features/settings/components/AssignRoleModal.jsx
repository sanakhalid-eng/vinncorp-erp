import { useState, useEffect, Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import {
  X,
  UserPlus,
  Users,
  Shield,
  Search,
  CheckCircle,
  ChevronRight,
} from "lucide-react";
import { toast } from "sonner";
import RoleBadge from "../../projects/components/members/RoleBadge.jsx";
import { getAllRoles, updateProjectMemberRole } from "../api/roleApi";
import { getAllUsers } from "../../../api/userApi";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import { cn } from "../../../utils/cn.js";
const AssignRoleModal = ({ isOpen, onClose, projectId, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [roles, setRoles] = useState([]);
  const [form, setForm] = useState({ userEmail: "", role: "", userId: "" });
  const [userSearch, setUserSearch] = useState("");
  const [selectedUser, setSelectedUser] = useState(null);
  const [rolesLoading, setRolesLoading] = useState(false);
  const [users, setUsers] = useState([]);
  useEffect(() => {
    if (isOpen) {
      loadRoles();
      loadUsers();
      setForm({ userEmail: "", role: "", userId: "" });
      setUserSearch("");
      setSelectedUser(null);
    }
  }, [isOpen]);
  const loadRoles = async () => {
    setRolesLoading(true);
    try {
      const roleList = (await getAllRoles()) || [];
      setRoles(Array.isArray(roleList) ? roleList : []);
    } catch (error) {
      console.error("Load roles error:", error);
      toast.error("Failed to load roles");
      setRoles([]);
    } finally {
      setRolesLoading(false);
    }
  };
  const loadUsers = async () => {
    try {
      const userList = await getAllUsers();
      setUsers(Array.isArray(userList) ? userList : []);
    } catch (error) {
      console.error("Load users error:", error);
      toast.error("Failed to load users");
      setUsers([]);
    }
  };
  const handleUserSearch = (email) => {
    const user = users.find((u) =>
      u.email.toLowerCase().includes(email.toLowerCase()),
    );
    setSelectedUser(user || null);
    if (user) {
      setForm({ ...form, userId: user.id, userEmail: user.email });
    } else {
      setForm({ ...form, userId: "", userEmail: email });
    }
  };
  const handleAssign = async () => {
    if (!form.userId || !form.role || !projectId) {
      toast.error("Please select user, role, and project");
      return;
    }
    setLoading(true);
    try {
      await updateProjectMemberRole(projectId, form.userId, form.role);
      toast.success("Role assigned successfully");
      onSuccess?.();
      onClose();
    } catch (error) {
      toast.error(error.response?.data?.message || "Failed to assign role");
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
               
              <Dialog.Panel className="w-full max-w-lg transform rounded-3xl bg-white shadow-2xl border border-gray-100">
                 
                <div className="relative">
                   
                  <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-emerald-500 to-teal-600 rounded-t-3xl" /> 
                  <div className="p-8 pb-6 border-b border-gray-100 bg-gradient-to-r from-emerald-50/80 to-teal-50/80 rounded-t-3xl">
                     
                    <div className="flex items-center gap-4">
                       
                      <div className="w-14 h-14 bg-gradient-to-r from-emerald-500 to-teal-600 rounded-2xl flex items-center justify-center shadow-lg">
                         
                        <UserPlus className="w-8 h-8 text-white" /> 
                      </div> 
                      <div className="flex-1">
                         
                        <Dialog.Title className="text-2xl font-bold text-gray-900">
                           
                          Assign Role to User 
                        </Dialog.Title> 
                        <p className="text-gray-600">
                           
                          Add user to project with specific role 
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
                  <div className="p-8 space-y-6">
                     
                    <div>
                       
                      <label className="block text-sm font-semibold text-gray-900 mb-3">
                         
                        Search User by Email 
                      </label> 
                      <div className="relative">
                         
                        <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" /> 
                        <Input
                          type="text"
                          placeholder="Search by email..."
                          value={userSearch}
                          onChange={(e) => {
                            setUserSearch(e.target.value);
                            handleUserSearch(e.target.value);
                          }}
                          className="w-full pl-12 border-gray-200 focus:border-emerald-500 focus:ring-emerald-200"
                        /> 
                      </div> 
                      {userSearch && !selectedUser && users.length > 0 && (
                        <div className="mt-3 rounded-2xl border border-amber-200 bg-amber-50 p-3 text-sm text-amber-700 flex items-center gap-2">
                           
                          <X className="w-4 h-4" /> No matching user found 
                        </div>
                      )} 
                      {selectedUser && (
                        <div className="mt-3 p-4 bg-emerald-50 rounded-2xl border border-emerald-200 flex items-center justify-between">
                           
                          <div className="flex items-center gap-3">
                             
                            <div className="w-10 h-10 bg-emerald-200 rounded-full flex items-center justify-center">
                               
                              <Users className="w-5 h-5 text-emerald-600" /> 
                            </div> 
                            <div>
                               
                              <p className="font-semibold text-gray-900">
                                {selectedUser.name}
                              </p> 
                              <p className="text-sm text-gray-600">
                                {selectedUser.email}
                              </p> 
                            </div> 
                          </div> 
                          <CheckCircle className="w-5 h-5 text-emerald-600" /> 
                        </div>
                      )} 
                    </div> 
                    <div>
                       
                      <label className="block text-sm font-semibold text-gray-900 mb-3">
                         
                        Select Role 
                      </label> 
                      <div className="space-y-2">
                         
                        {rolesLoading ? (
                          <div className="p-8 text-center">
                             
                            <div className="inline-block w-8 h-8 border-3 border-gray-200 border-t-emerald-600 rounded-full animate-spin mx-auto mb-3"></div> 
                            <p className="text-gray-500">
                              Loading roles...
                            </p> 
                          </div>
                        ) : Array.isArray(roles) && roles.length > 0 ? (
                          roles.map((role) => (
                            <button
                              key={role.id}
                              type="button"
                              onClick={() =>
                                setForm({ ...form, role: role.name })
                              }
                              className={cn(
                                "flex items-center gap-3 w-full p-4 border-2 rounded-2xl transition-all hover:shadow-md",
                                form.role === role.name
                                  ? "border-emerald-300 bg-emerald-50 shadow-md"
                                  : "border-gray-200 hover:border-emerald-200 hover:bg-gray-50",
                              )}
                            >
                               
                              <RoleBadge role={role.name} /> 
                              <div className="flex-1 text-left">
                                 
                                <p className="font-semibold text-gray-900">
                                  {role.name}
                                </p> 
                                <p className="text-sm text-gray-500">
                                  {role.description || "No description"}
                                </p> 
                              </div> 
                              {form.role === role.name && (
                                <CheckCircle className="w-5 h-5 text-emerald-600" />
                              )} 
                            </button>
                          ))
                        ) : (
                          <div className="p-6 text-center text-gray-500 border-2 border-dashed border-gray-200 rounded-xl">
                             
                            No roles available <br /> 
                            <button
                              type="button"
                              onClick={loadRoles}
                              className="mt-2 px-4 py-2 bg-emerald-100 text-emerald-700 rounded-lg hover:bg-emerald-200 text-sm font-medium"
                            >
                               
                              Reload 
                            </button> 
                          </div>
                        )} 
                      </div> 
                    </div> 
                    <div className="flex gap-3 pt-2">
                       
                      <Button
                        type="button"
                        onClick={handleAssign}
                        disabled={
                          loading || rolesLoading || !form.userId || !form.role
                        }
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
                            Assigning... 
                          </span>
                        ) : (
                          <span className="flex items-center gap-2">
                             
                            <Shield className="w-5 h-5" /> Assign Role 
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
export default AssignRoleModal;
