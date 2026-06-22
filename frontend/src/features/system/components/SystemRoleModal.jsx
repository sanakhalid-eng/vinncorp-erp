import { useState, useEffect, Fragment } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { X, Shield, CheckCircle } from "lucide-react";
import { toast } from "sonner";
import {
  getAllRoles,
  assignSystemRole,
  removeSystemRole,
  getUserSystemRoles,
} from "../../settings/api/roleApi.js";
import Button from "../../../components/Button.jsx";
import { cn } from "../../../utils/cn.js";

const SystemRoleModal = ({ isOpen, onClose, userId, userName, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [roles, setRoles] = useState([]);
  const [systemRoles, setSystemRoles] = useState([]);
  const [selectedRoleId, setSelectedRoleId] = useState("");
  const [currentSystemRoles, setCurrentSystemRoles] = useState([]);
  const [rolesLoading, setRolesLoading] = useState(false);

  useEffect(() => {
    if (isOpen && userId) {
      loadRoles();
      loadUserSystemRoles();
      setSelectedRoleId("");
    }
  }, [isOpen, userId]);

  const loadRoles = async () => {
    setRolesLoading(true);
    try {
      const roleList = (await getAllRoles()) || [];

      // Filter only SYSTEM scope roles (backend enum: SYSTEM, PROJECT)

      const systemOnly = roleList.filter((r) =>
        ["ADMIN", "USER"].includes(r.name),
      );
      setRoles(systemOnly);
    } catch (error) {
      console.error("Load roles error:", error);
      toast.error("Failed to load roles");
      setRoles([]);
    } finally {
      setRolesLoading(false);
    }
  };

  const loadUserSystemRoles = async () => {
    try {
      const userRoles = await getUserSystemRoles(userId);
      setCurrentSystemRoles(userRoles || []);
    } catch (error) {
      console.error("Load user system roles error:", error);
      setCurrentSystemRoles([]);
    }
  };

  const handleAssign = async () => {
    if (!selectedRoleId) {
      toast.error("Please select a role");
      return;
    }

    setLoading(true);
    try {
      await assignSystemRole(userId, selectedRoleId);
      toast.success("System role assigned successfully");
      onSuccess?.();
      onClose();
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to assign system role",
      );
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (roleId) => {
    setLoading(true);
    try {
      await removeSystemRole(userId, roleId);
      toast.success("System role removed successfully");
      loadUserSystemRoles();
      onSuccess?.();
    } catch (error) {
      toast.error(
        error.response?.data?.message || "Failed to remove system role",
      );
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
                  <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-blue-500 to-purple-600 rounded-t-3xl" />

                  <div className="p-8 pb-6 border-b border-gray-100 bg-gradient-to-r from-blue-50/80 to-purple-50/80 rounded-t-3xl">
                    <div className="flex items-center gap-4">
                      <div className="w-14 h-14 bg-gradient-to-r from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg">
                        <Shield className="w-8 h-8 text-white" />
                      </div>
                      <div className="flex-1">
                        <Dialog.Title className="text-2xl font-bold text-gray-900">
                          Manage System Role
                        </Dialog.Title>
                        <p className="text-gray-600">
                          {userName || "User"}'s system role (1 per user)
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
                    {/* Current System Roles */}
                    {currentSystemRoles.length > 0 && (
                      <div>
                        <label className="block text-sm font-semibold text-gray-900 mb-3">
                          Current System Role
                        </label>
                        <div className="space-y-2">
                          {currentSystemRoles.map((ur) => (
                            <div
                              key={ur.id}
                              className="flex items-center justify-between p-4 bg-blue-50 rounded-2xl border border-blue-200"
                            >
                              <div className="flex items-center gap-3">
                                <Shield className="w-5 h-5 text-blue-600" />
                                <div>
                                  <p className="font-semibold text-gray-900">
                                    {ur.role?.name}
                                  </p>
                                  <p className="text-sm text-gray-600">
                                    {ur.role?.description}
                                  </p>
                                </div>
                              </div>
                              <button
                                onClick={() => handleRemove(ur.role?.id)}
                                disabled={loading}
                                className="px-4 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-all"
                              >
                                Remove
                              </button>
                            </div>
                          ))}
                        </div>
                      </div>
                    )}

                    {/* Assign New Role */}
                    <div>
                      <label className="block text-sm font-semibold text-gray-900 mb-3">
                        Assign New System Role
                      </label>
                      <p className="text-sm text-gray-600 mb-3">
                        Note: User can only have 1 system role. Assigning a new
                        one will require removing the current one first.
                      </p>
                      <div className="space-y-2">
                        {rolesLoading ? (
                          <div className="p-8 text-center">
                            <div className="inline-block w-8 h-8 border-3 border-gray-200 border-t-blue-600 rounded-full animate-spin mx-auto mb-3"></div>
                            <p className="text-gray-500">Loading roles...</p>
                          </div>
                        ) : Array.isArray(roles) && roles.length > 0 ? (
                          roles.map((role) => (
                            <button
                              key={role.id}
                              type="button"
                              onClick={() => setSelectedRoleId(role.id)}
                              className={cn(
                                "flex items-center gap-3 w-full p-4 border-2 rounded-2xl transition-all hover:shadow-md",
                                String(selectedRoleId) === String(role.id)
                                  ? "border-blue-300 bg-blue-50 shadow-md"
                                  : "border-gray-200 hover:border-blue-200 hover:bg-gray-50",
                              )}
                            >
                              <Shield className="w-5 h-5 text-blue-600" />
                              <div className="flex-1 text-left">
                                <p className="font-semibold text-gray-900">
                                  {role.name}
                                </p>
                                <p className="text-sm text-gray-500">
                                  {role.description || "No description"}
                                </p>
                              </div>
                              {String(selectedRoleId) === String(role.id) && (
                                <CheckCircle className="w-5 h-5 text-blue-600" />
                              )}
                            </button>
                          ))
                        ) : (
                          <div className="p-6 text-center text-gray-500 border-2 border-dashed border-gray-200 rounded-xl">
                            No system roles available
                          </div>
                        )}
                      </div>
                    </div>

                    <div className="flex gap-3 pt-2">
                      <Button
                        type="button"
                        onClick={handleAssign}
                        disabled={
                          loading ||
                          rolesLoading ||
                          !selectedRoleId ||
                          currentSystemRoles.length > 0
                        }
                        className="flex-1 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white shadow-lg hover:shadow-xl transition-all h-12 text-base"
                      >
                        {loading ? "Assigning..." : "Assign Role"}
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

export default SystemRoleModal;
