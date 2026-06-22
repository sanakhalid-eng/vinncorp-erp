import { useState, useEffect } from "react";
import {
  Search,
  Edit3,
  Trash2,
  ChevronDown,
  Users,
  Shield,
} from "lucide-react";
import RoleBadge from "../../projects/components/members/RoleBadge.jsx";
import { cn } from "../../../utils/cn.js";
import { usePermission } from "../../../context/usePermission.js";
const UserTable = ({
  users: initialUsers = [],
  onEdit,
  onDelete,
  onAssignSystemRole,
  canAssignSystemRole = false,
  loading = false,
}) => {
  const [users, setUsers] = useState(initialUsers);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("all");
  const [sortConfig, setSortConfig] = useState({ key: null, direction: "asc" });
  const { canUpdateUser, canDeleteUser } = usePermission();
  useEffect(() => {
    setUsers(initialUsers);
  }, [initialUsers]);
  const filteredUsers = users.filter((user) => {
    const searchLower = search.toLowerCase();
    const roleName =
      Array.isArray(user.roles) && user.roles.length > 0
        ? user.roles[0]
        : user.role || "";
    return (
      user.name.toLowerCase().includes(searchLower) ||
      user.email.toLowerCase().includes(searchLower) ||
      roleName.toLowerCase().includes(searchLower)
    );
  });
  const sortedUsers = [...filteredUsers].sort((a, b) => {
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
                 
                <div className="h-12 w-12 bg-gray-200 rounded-full" /> 
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
  const getInitials = (name) => {
    return name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .slice(0, 2);
  };
  return (
    <div className="bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-2xl border border-white/50 overflow-hidden">
       
      {/* Search & Filter */} 
      <div className="flex flex-col lg:flex-row gap-4 mb-8 p-6 bg-gradient-to-r from-emerald-50 to-blue-50 rounded-3xl">
         
        <div className="relative flex-1 max-w-md">
           
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" /> 
          <input
            type="text"
            placeholder="Search users by name, email..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-emerald-500 focus:border-transparent bg-white shadow-sm"
          /> 
        </div> 
        <select
          value={roleFilter}
          onChange={(e) => setRoleFilter(e.target.value)}
          className="px-4 py-3 border border-gray-200 rounded-2xl focus:ring-2 focus:ring-emerald-500 bg-white shadow-sm"
        >
           
          <option value="all">All Users</option> 
          <option value="ADMIN">Admins</option> 
          <option value="PROJECT_MANAGER">Managers</option> 
          <option value="TEAM_MEMBER">Members</option> 
        </select> 
      </div> 
      {/* Table */} 
      <div className="overflow-x-auto">
         
        <table className="w-full">
           
          <thead>
             
            <tr className="border-b border-gray-200">
               
              <th className="text-left py-6 font-semibold text-gray-900 w-16"></th> 
              <th
                className="text-left py-6 font-semibold text-gray-900 cursor-pointer hover:text-emerald-600 group"
                onClick={() => handleSort("name")}
              >
                 
                Name 
                <ChevronDown className="inline ml-1 w-4 h-4 opacity-50 group-hover:opacity-100 transition" /> 
              </th> 
              <th className="text-left py-6 font-semibold text-gray-900 w-80">
                Email
              </th> 
              <th className="text-left py-6 font-semibold text-gray-900 w-32">
                Role
              </th> 
              <th className="text-left py-6 font-semibold text-gray-900 w-24">
                Projects
              </th> 
              <th
                className="text-left py-6 font-semibold text-gray-900 w-32"
                onClick={() => handleSort("createdAt")}
              >
                 
                Created 
                <ChevronDown className="inline ml-1 w-4 h-4 opacity-50 transition" /> 
              </th> 
              <th className="text-right py-6 font-semibold text-gray-900 w-32">
                Actions
              </th> 
            </tr> 
          </thead> 
          <tbody className="divide-y divide-gray-100">
             
            {sortedUsers.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                 
                <td className="py-6">
                   
                  <div className="w-12 h-12 bg-gradient-to-r from-emerald-500 to-teal-600 rounded-full flex items-center justify-center text-white font-bold text-sm shadow-lg">
                     
                    {getInitials(user.name)} 
                  </div> 
                </td> 
                <td className="py-6 font-semibold text-gray-900">
                   
                  {user.name} 
                </td> 
                <td className="py-6 text-gray-600">
                   
                  <span className="font-mono text-sm">{user.email}</span> 
                </td> 
                <td className="py-6">
                   
                  <RoleBadge
                    role={
                      Array.isArray(user.roles) && user.roles.length > 0
                        ? user.roles[0]
                        : user.role || "TEAM_MEMBER"
                    }
                  /> 
                </td> 
                <td className="py-6">
                   
                  <div className="flex items-center gap-1 text-sm font-medium">
                     
                    <span className="text-emerald-600 font-semibold">
                      {user.projectCount || 0}
                    </span> 
                  </div> 
                </td> 
                <td className="py-6">
                   
                  <span className="text-sm text-gray-500">
                     
                    {user.createdAt
                      ? new Date(user.createdAt).toLocaleDateString("en-US", {
                          year: "numeric",
                          month: "short",
                          day: "numeric",
                        })
                      : "Unknown"} 
                  </span> 
                </td> 
                <td className="py-6 text-right space-x-2">
                   
                  {canUpdateUser() && (
                    <button
                      onClick={() => onEdit(user)}
                      className="p-2 text-emerald-600 hover:bg-emerald-100 rounded-xl transition-colors"
                      title="Edit User"
                    >
                       
                      <Edit3 className="w-5 h-5" /> 
                    </button>
                  )} 
                  {canAssignSystemRole && onAssignSystemRole && (
                    <button
                      onClick={() => onAssignSystemRole(user)}
                      className="p-2 text-blue-600 hover:bg-blue-100 rounded-xl transition-colors"
                      title="Manage System Role"
                    >
                       
                      <Shield className="w-5 h-5" /> 
                    </button>
                  )} 
                  {canDeleteUser() && (
                    <button
                      onClick={() => onDelete(user)}
                      className="p-2 text-red-600 hover:bg-red-100 rounded-xl transition-colors"
                      title="Delete User"
                    >
                       
                      <Trash2 className="w-5 h-5" /> 
                    </button>
                  )} 
                </td> 
              </tr>
            ))} 
          </tbody> 
        </table> 
      </div> 
      {sortedUsers.length === 0 && (
        <div className="text-center py-24 text-gray-500">
           
          {search || roleFilter !== "all"
            ? "No matching users found"
            : "No users yet. Create your first user!"} 
        </div>
      )} 
    </div>
  );
};
export default UserTable;
