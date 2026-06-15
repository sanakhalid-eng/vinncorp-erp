import { useState } from "react";
import { Dialog, Transition } from "@headlessui/react";
// Note: install required

import RoleBadge from "./RoleBadge.jsx";
import { X, UserPlus, Mail, Users } from "lucide-react";
import { addProjectMember } from "../../api/projectMembersApi.js";
import { getAllUsers } from "../../api/userApi.js";
import { toast } from "sonner";
// Note: install required

const AddMemberModal = ({ isOpen, onClose, projectId, onMemberAdded }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ email: "", role: "TEAM_MEMBER" });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.email) return;

    setLoading(true);
    try {
      const users = await getAllUsers();
      const matchedUser = users.find(
        (user) => user.email?.toLowerCase() === form.email.trim().toLowerCase(),
      );
      if (!matchedUser) {
        toast.error("No user found with that email");
        return;
      }
      await addProjectMember(projectId, {
        userId: matchedUser.id,
        role: form.role,
      });
      toast.success("Member added successfully!");
      onMemberAdded?.();
      onClose();
      setForm({ email: "", role: "TEAM_MEMBER" });
    } catch (error) {
      toast.error("Failed to add member");
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={isOpen} onClose={onClose} className="relative z-50">
      <Transition
        show={isOpen}
        as="div"
        className="fixed inset-0 z-50 overflow-y-auto"
      >
        <div className="flex min-h-screen items-center justify-center p-4">
          <Transition.Child
            enter="ease-out duration-300"
            enterFrom="opacity-0"
            enterTo="opacity-100"
            leave="ease-in duration-200"
            leaveFrom="opacity-100"
            leaveTo="opacity-0"
          >
            <div className="fixed inset-0 bg-black/30 backdrop-blur-sm" />
          </Transition.Child>

          <Transition.Child
            enter="ease-out duration-300"
            enterFrom="opacity-0 scale-95"
            enterTo="opacity-100 scale-100"
            leave="ease-in duration-200"
            leaveFrom="opacity-100 scale-100"
            leaveTo="opacity-0 scale-95"
          >
            <Dialog.Panel className="w-full max-w-md transform rounded-3xl bg-white/90 backdrop-blur-xl shadow-2xl border border-white/50 p-8">
              <div className="flex items-center justify-between mb-6">
                <Dialog.Title className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                  <UserPlus className="w-7 h-7 text-indigo-600" />
                  Add Team Member
                </Dialog.Title>
                <button
                  onClick={onClose}
                  className="p-2 hover:bg-gray-100 rounded-2xl transition-colors"
                >
                  <X className="w-5 h-5 text-gray-500" />
                </button>
              </div>

              <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Search User by Email
                  </label>
                  <div className="relative">
                    <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                    <input
                      type="email"
                      placeholder="user@company.com"
                      className="w-full pl-11 pr-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                      value={form.email}
                      onChange={(e) =>
                        setForm({ ...form, email: e.target.value })
                      }
                      required
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Role
                  </label>
                  <div className="space-y-2">
                    <label className="flex items-center gap-3 p-3 border border-gray-200 rounded-2xl hover:bg-gray-50 cursor-pointer">
                      <input
                        type="radio"
                        name="role"
                        value="PROJECT_MANAGER"
                        checked={form.role === "PROJECT_MANAGER"}
                        onChange={(e) =>
                          setForm({ ...form, role: e.target.value })
                        }
                        className="w-4 h-4 text-indigo-600 border-gray-300"
                      />
                      <RoleBadge role="PROJECT_MANAGER" className="shrink-0" />
                      <span>Project Manager (full access)</span>
                    </label>
                    <label className="flex items-center gap-3 p-3 border border-gray-200 rounded-2xl hover:bg-gray-50 cursor-pointer">
                      <input
                        type="radio"
                        name="role"
                        value="TEAM_MEMBER"
                        checked={form.role === "TEAM_MEMBER"}
                        onChange={(e) =>
                          setForm({ ...form, role: e.target.value })
                        }
                        className="w-4 h-4 text-emerald-600 border-gray-300"
                      />
                      <RoleBadge role="TEAM_MEMBER" className="shrink-0" />
                      <span>Team Member (task access)</span>
                    </label>
                  </div>
                </div>

                <div className="flex gap-3 pt-4">
                  <button
                    type="button"
                    onClick={onClose}
                    className="flex-1 px-6 py-3 border border-gray-200 text-gray-700 rounded-2xl hover:bg-gray-50 transition-colors font-medium"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={loading}
                    className="flex-1 px-6 py-3 bg-gradient-to-r from-indigo-600 to-purple-600 text-white rounded-2xl hover:from-indigo-700 hover:to-purple-700 font-semibold shadow-lg transition-all disabled:opacity-50"
                  >
                    {loading ? "Adding..." : "Add Member"}
                  </button>
                </div>
              </form>
            </Dialog.Panel>
          </Transition.Child>
        </div>
      </Transition>
    </Dialog>
  );
};

export default AddMemberModal;
