import { useState } from "react";
import { Dialog, Transition } from "@headlessui/react";
import RoleBadge from "./RoleBadge.jsx";
import { Users, X, Plus } from "lucide-react";
import { bulkAddProjectMembers } from "../../api/projectMembersApi";
import { getAllUsers } from "../../../../api/userApi";
import { toast } from "sonner";
const BulkAddModal = ({ isOpen, onClose, projectId, onMembersAdded }) => {
  const [loading, setLoading] = useState(false);
  const [bulkMembers, setBulkMembers] = useState([
    { email: "", role: "TEAM_MEMBER" },
  ]);
  const addRow = () => {
    setBulkMembers([...bulkMembers, { email: "", role: "TEAM_MEMBER" }]);
  };
  const removeRow = (index) => {
    if (bulkMembers.length > 1) {
      setBulkMembers(bulkMembers.filter((_, i) => i !== index));
    }
  };
  const updateRow = (index, field, value) => {
    const newMembers = [...bulkMembers];
    newMembers[index][field] = value;
    setBulkMembers(newMembers);
  };
  const handleSubmit = async (e) => {
    e.preventDefault();
    const validMembers = bulkMembers.filter((m) => m.email.trim());
    if (validMembers.length === 0) return;
    setLoading(true);
    try {
      const users = await getAllUsers();
      const membersWithIds = validMembers.map((member) => {
        const matchedUser = users.find(
          (user) =>
            user.email?.toLowerCase() === member.email.trim().toLowerCase(),
        );
        if (!matchedUser) {
          throw new Error(`No user found for ${member.email}`);
        }
        return { userId: matchedUser.id, role: member.role };
      });
      await bulkAddProjectMembers(projectId, membersWithIds);
      toast.success(`${validMembers.length} members added!`);
      onMembersAdded?.();
      onClose();
      setBulkMembers([{ email: "", role: "TEAM_MEMBER" }]);
    } catch (error) {
      toast.error("Bulk add failed");
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
             
            <Dialog.Panel className="w-full max-w-2xl transform rounded-3xl bg-white/90 backdrop-blur-xl shadow-2xl border border-white/50 p-8">
               
              <div className="flex items-center justify-between mb-8">
                 
                <Dialog.Title className="text-2xl font-bold text-gray-900 flex items-center gap-3">
                   
                  <Users className="w-8 h-8 text-indigo-600" /> Bulk Add
                  Members 
                </Dialog.Title> 
                <button
                  onClick={onClose}
                  className="p-2 hover:bg-gray-100 rounded-2xl"
                >
                   
                  <X className="w-6 h-6 text-gray-500" /> 
                </button> 
              </div> 
              <form onSubmit={handleSubmit} className="space-y-6">
                 
                <div className="space-y-3 max-h-96 overflow-y-auto pr-2">
                   
                  {bulkMembers.map((member, index) => (
                    <div
                      key={index}
                      className="flex items-end gap-3 p-4 border border-gray-200 rounded-2xl bg-gray-50/50"
                    >
                       
                      <div className="flex-1 relative">
                         
                        <input
                          type="email"
                          placeholder="user@company.com"
                          className="w-full px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 pl-12"
                          value={member.email}
                          onChange={(e) =>
                            updateRow(index, "email", e.target.value)
                          }
                        /> 
                      </div> 
                      <select
                        value={member.role}
                        onChange={(e) =>
                          updateRow(index, "role", e.target.value)
                        }
                        className="px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 w-44"
                      >
                         
                        <option value="PROJECT_MANAGER">Manager</option> 
                        <option value="TEAM_MEMBER">Member</option> 
                      </select> 
                      <button
                        type="button"
                        onClick={() => removeRow(index)}
                        className="p-3 text-gray-400 hover:text-red-500 hover:bg-red-50 rounded-2xl transition-colors"
                        disabled={bulkMembers.length === 1}
                      >
                         
                        <X className="w-5 h-5" /> 
                      </button> 
                    </div>
                  ))} 
                </div> 
                <div className="flex items-center gap-3 pt-2 mb-6">
                   
                  <button
                    type="button"
                    onClick={addRow}
                    className="flex items-center gap-2 px-4 py-2 border border-dashed border-gray-300 rounded-2xl hover:border-indigo-400 hover:text-indigo-600 transition-colors font-medium"
                  >
                     
                    <Plus className="w-4 h-4" /> Add Another 
                  </button> 
                  <span className="text-sm text-gray-500">
                     
                    {bulkMembers.filter((m) => m.email.trim()).length} valid
                    emails 
                  </span> 
                </div> 
                <div className="flex gap-3">
                   
                  <button
                    type="button"
                    onClick={onClose}
                    className="flex-1 px-6 py-3 border border-gray-200 text-gray-700 rounded-2xl hover:bg-gray-50 font-medium"
                  >
                     
                    Cancel 
                  </button> 
                  <button
                    type="submit"
                    disabled={
                      loading ||
                      bulkMembers.filter((m) => m.email.trim()).length === 0
                    }
                    className="flex-1 px-6 py-3 bg-emerald-600 text-white rounded-2xl hover:bg-emerald-700 font-semibold shadow-lg transition-all disabled:opacity-50"
                  >
                     
                    {loading
                      ? "Adding..."
                      : `Add ${bulkMembers.filter((m) => m.email.trim()).length} Members`} 
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
export default BulkAddModal;
