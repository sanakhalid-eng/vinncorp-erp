import { useState, useEffect } from "react";
import {
  Search,
  Edit3,
  Trash2,
  ChevronDown,
  Globe,
  Folder,
} from "lucide-react";
import RoleBadge from "./members/RoleBadge.jsx";
import { cn } from "../utils/cn.js";
import { getAllRoles } from "../api/roleApi";
const RoleTable = ({
  roles: initialRoles = [],
  onEdit,
  onDelete,
  loading = false,
}) => {
  const [roles, setRoles] = useState(initialRoles);
  const [search, setSearch] = useState("");
  const [scopeFilter, setScopeFilter] = useState("all");
  const [sortConfig, setSortConfig] = useState({ key: null, direction: "asc" });
  useEffect(() => {
    setRoles(initialRoles);
  }, [initialRoles]);
  const filteredRoles = roles
    .filter(
      (role) =>
        role.name.toLowerCase().includes(search.toLowerCase()) ||
        role.description?.toLowerCase().includes(search.toLowerCase()),
    )
    .filter((role) => {
      if (scopeFilter === "all") return true;
      return role.scope === scopeFilter;
    });
  const sortedRoles = [...filteredRoles].sort((a, b) => {
    if (!sortConfig.key) return 0;
    const aVal = a[sortConfig.key];
    const bVal = b[sortConfig.key];
    if (aVal < bVal) return sortConfig.direction === "asc" ? -1 : 1;
    if (aVal > bVal) return sortConfig.direction === "asc" ? 1 : -1;
    return 0;
  });
  const handleSort = (key) => {
    setSortConfig({
      key,
      direction:
        sortConfig.key === key && sortConfig.direction === "asc"
          ? "desc"
          : "asc",
    });
  };
  if (loading) {
    return (
      <div className="bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-2xl border border-white/50">
         
        <div className="animate-pulse space-y-4">
           
          <div className="h-10 bg-gray-200 rounded-xl w-72" /> 
          <div className="space-y-3">
             
            {[...Array(5)].map((_, i) => (
              <div
                key={i}
                className="flex items-center gap-4 p-4 bg-gray-100 rounded-2xl"
              >
                 
                <div className="h-12 w-12 bg-gray-200 rounded-xl" /> 
                <div className="flex-1 space-y-2">
                   
                  <div className="h-5 bg-gray-200 rounded w-48" /> 
                  <div className="h-4 bg-gray-200 rounded w-32" /> 
                </div> 
                <div className="h-10 w-20 bg-gray-200 rounded-xl" /> 
              </div>
            ))} 
          </div> 
        </div> 
      </div>
    );
  }
  return (
    <div className="bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-2xl border border-white/50 overflow-hidden">
       
      {/* Search & Filter */} 
      <div className="flex flex-col lg:flex-row gap-4 mb-8 p-6 bg-gradient-to-r from-indigo-50 to-purple-50 rounded-3xl">
         
        <div className="relative flex-1 max-w-md">
           
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" /> 
          <input
            type="text"
            placeholder="Search roles by name or description..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 focus:border-transparent bg-white shadow-sm"
          /> 
        </div> 
        <select
          value={scopeFilter}
          onChange={(e) => setScopeFilter(e.target.value)}
          className="px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-indigo-500 bg-white shadow-sm"
        >
           
          <option value="all">All Scopes</option> 
          <option value="SYSTEM">System</option> 
          <option value="PROJECT">Project</option> 
        </select> 
      </div> 
      {/* Table */} 
      <div className="overflow-x-auto">
         
        <table className="w-full">
           
          <thead>
             
            <tr className="border-b border-gray-200">
               
              <th className="text-left py-6 font-semibold text-gray-900 w-16"></th> 
              <th
                className="text-left py-6 font-semibold text-gray-900 cursor-pointer hover:text-indigo-600 group"
                onClick={() => handleSort("name")}
              >
                 
                Role Name 
                <ChevronDown className="inline ml-1 w-4 h-4 opacity-50 group-hover:opacity-100 transition" /> 
              </th> 
              <th className="text-left py-6 font-semibold text-gray-900 w-32">
                Scope
              </th> 
              <th className="text-left py-6 font-semibold text-gray-900 w-80">
                Description
              </th> 
              <th className="text-left py-6 font-semibold text-gray-900 w-32">
                 
                Permissions 
              </th> 
              <th className="text-right py-6 font-semibold text-gray-900 w-32">
                Actions
              </th> 
            </tr> 
          </thead> 
          <tbody className="divide-y divide-gray-100">
             
            {sortedRoles.map((role) => (
              <tr key={role.id} className="hover:bg-gray-50 transition-colors">
                 
                <td className="py-6">
                   
                  <RoleBadge role={role.name} /> 
                </td> 
                <td className="py-6 font-semibold text-gray-900 max-w-md truncate">
                   
                  {role.name} 
                </td> 
                <td className="py-6">
                   
                  <span
                    className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${role.scope === "SYSTEM" ? "bg-purple-100 text-purple-700" : "bg-emerald-100 text-emerald-700"}`}
                  >
                     
                    {role.scope === "SYSTEM" ? (
                      <Globe className="w-3 h-3" />
                    ) : (
                      <Folder className="w-3 h-3" />
                    )} 
                    {role.scope || "PROJECT"} 
                  </span> 
                </td> 
                <td className="py-6 text-gray-600 max-w-md truncate">
                   
                  {role.description || "-"} 
                </td> 
                <td className="py-6">
                   
                  <div className="flex items-center gap-1 text-sm font-medium">
                     
                    <span className="text-emerald-600 font-semibold">
                       
                      {role.permissions?.length || 0} 
                    </span> 
                  </div> 
                </td> 
                <td className="py-6 text-right space-x-2">
                   
                  <button
                    onClick={() => onEdit(role)}
                    className="p-2 text-indigo-600 hover:bg-indigo-100 rounded-xl transition-colors"
                    title="Edit Role"
                  >
                     
                    <Edit3 className="w-5 h-5" /> 
                  </button> 
                  <button
                    onClick={() => onDelete(role)}
                    className="p-2 text-red-600 hover:bg-red-100 rounded-xl transition-colors"
                    title="Delete Role"
                  >
                     
                    <Trash2 className="w-5 h-5" /> 
                  </button> 
                </td> 
              </tr>
            ))} 
          </tbody> 
        </table> 
      </div> 
      {sortedRoles.length === 0 && (
        <div className="text-center py-24 text-gray-500">
           
          {search || scopeFilter !== "all"
            ? "No matching roles found"
            : "No roles created yet. Create your first role!"} 
        </div>
      )} 
    </div>
  );
};
export default RoleTable;
