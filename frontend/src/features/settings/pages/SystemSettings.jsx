import { useEffect, useState } from "react";
import { getSystemSettings, transferOwnership } from "../../system/api/systemApi";
import { getAllUsers } from "../../../api/userApi";
import { useAuth } from "../../../context/useAuth";
import { usePermission } from "../../../context/usePermission.js";
import {
  Settings,
  Shield,
  ToggleLeft,
  Users,
  Activity,
  ArrowRight,
} from "lucide-react";
import { toast } from "sonner";
function ToggleSwitch({ enabled, onChange, label }) {
  return (
    <button
      type="button"
      onClick={onChange}
      className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 ${enabled ? "bg-indigo-600" : "bg-gray-200"}`}
      role="switch"
      aria-checked={enabled}
    >
       
      <span
        className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${enabled ? "translate-x-5" : "translate-x-0"}`}
      /> 
    </button>
  );
}
export default function SystemSettings() {
  const { user } = useAuth();
  const { canTransferOwnership } = usePermission();
  const [settings, setSettings] = useState(null);
  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState([]);
  const [selectedTarget, setSelectedTarget] = useState("");
  const [transferring, setTransferring] = useState(false);
  useEffect(() => {
    Promise.all([
      getSystemSettings(),
      canTransferOwnership() ? getAllUsers() : Promise.resolve([]),
    ])
      .then(([settingsRes, usersData]) => {
        setSettings(settingsRes.data.data);
        setUsers(
          Array.isArray(usersData)
            ? usersData.filter((u) => u.id !== user?.id && u.role === "ADMIN")
            : [],
        );
      })
      .catch(() => toast.error("Failed to load system settings"))
      .finally(() => setLoading(false));
  }, [canTransferOwnership, user?.id]);
  const handleTransfer = async () => {
    if (!selectedTarget) {
      toast.error("Please select a target user");
      return;
    }
    setTransferring(true);
    try {
      await transferOwnership(Number(selectedTarget));
      toast.success("Ownership transferred successfully");
      setSelectedTarget("");
    } catch (err) {
      toast.error(
        err.response?.data?.message || "Failed to transfer ownership",
      );
    } finally {
      setTransferring(false);
    }
  };
  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
         
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600" /> 
      </div>
    );
  }
  const featuresList = settings?.features
    ? Object.entries(settings.features).map(([key, enabled]) => ({
        key,
        label: key.replace(/([A-Z])/g, " $1").trim(),
        enabled,
      }))
    : [];
  return (
    <div className="max-w-4xl mx-auto space-y-6">
       
      <div className="flex items-center gap-3">
         
        <div className="p-2 bg-indigo-100 rounded-lg">
           
          <Settings className="w-6 h-6 text-indigo-600" /> 
        </div> 
        <div>
           
          <h1 className="text-2xl font-bold text-gray-900">
            System Settings
          </h1> 
          <p className="text-sm text-gray-500">
            Workspace-wide configuration
          </p> 
        </div> 
      </div> 
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
         
        <div className="px-6 py-4 border-b border-gray-100">
           
          <div className="flex items-center gap-2">
             
            <Shield className="w-5 h-5 text-indigo-600" /> 
            <h2 className="text-lg font-semibold text-gray-900">
              Workspace
            </h2> 
          </div> 
        </div> 
        <div className="px-6 py-4 space-y-4">
           
          <div className="flex items-center justify-between">
             
            <div className="flex items-center gap-3">
               
              <Users className="w-5 h-5 text-gray-400" /> 
              <div>
                 
                <p className="text-sm font-medium text-gray-900">
                  Workspace Name
                </p> 
                <p className="text-xs text-gray-500">
                  The name of your workspace
                </p> 
              </div> 
            </div> 
            <span className="text-sm font-semibold text-gray-900 bg-gray-50 px-3 py-1.5 rounded-lg border border-gray-200">
               
              {settings?.workspaceName || "PMT-SK"} 
            </span> 
          </div> 
          <div className="flex items-center justify-between">
             
            <div className="flex items-center gap-3">
               
              <Activity className="w-5 h-5 text-gray-400" /> 
              <div>
                 
                <p className="text-sm font-medium text-gray-900">
                  Your Role
                </p> 
                <p className="text-xs text-gray-500">Your access level</p> 
              </div> 
            </div> 
            <span className="text-xs font-medium bg-indigo-50 text-indigo-700 px-3 py-1.5 rounded-full">
               
              Workspace Owner 
            </span> 
          </div> 
        </div> 
      </div> 
      {canTransferOwnership() && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
           
          <div className="px-6 py-4 border-b border-gray-100">
             
            <div className="flex items-center gap-2">
               
              <ArrowRight className="w-5 h-5 text-indigo-600" /> 
              <h2 className="text-lg font-semibold text-gray-900">
                Transfer Ownership
              </h2> 
            </div> 
            <p className="text-xs text-gray-500 mt-0.5">
              Transfer workspace ownership to another ADMIN user
            </p> 
          </div> 
          <div className="px-6 py-4 space-y-4">
             
            <div className="flex items-center gap-3">
               
              <select
                value={selectedTarget}
                onChange={(e) => setSelectedTarget(e.target.value)}
                className="flex-1 rounded-lg border border-gray-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              >
                 
                <option value="">Select an ADMIN user...</option> 
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name} ({u.email})
                  </option>
                ))} 
              </select> 
              <button
                onClick={handleTransfer}
                disabled={transferring || !selectedTarget}
                className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition-colors text-sm font-medium"
              >
                 
                {transferring ? "Transferring..." : "Transfer"} 
              </button> 
            </div> 
          </div> 
        </div>
      )} 
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
         
        <div className="px-6 py-4 border-b border-gray-100">
           
          <div className="flex items-center gap-2">
             
            <ToggleLeft className="w-5 h-5 text-indigo-600" /> 
            <h2 className="text-lg font-semibold text-gray-900">
              Feature Toggles
            </h2> 
          </div> 
          <p className="text-xs text-gray-500 mt-0.5">
            Enable or disable workspace-wide features
          </p> 
        </div> 
        <div className="divide-y divide-gray-100">
           
          {featuresList.map(({ key, label, enabled }) => (
            <div
              key={key}
              className="flex items-center justify-between px-6 py-4"
            >
               
              <div>
                 
                <p className="text-sm font-medium text-gray-900">
                  {label}
                </p> 
                <p className="text-xs text-gray-500">
                   
                  {enabled ? "Feature is active" : "Feature is disabled"} 
                </p> 
              </div> 
              <ToggleSwitch
                enabled={enabled}
                onChange={() => toast.info("Feature toggle update coming soon")}
                label={label}
              /> 
            </div>
          ))} 
          {featuresList.length === 0 && (
            <div className="px-6 py-8 text-center text-sm text-gray-400">
               
              No feature toggles available 
            </div>
          )} 
        </div> 
      </div> 
    </div>
  );
}
