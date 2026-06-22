import { useEffect, useMemo, useState } from "react";
import {
  Camera,
  CalendarDays,
  FolderKanban,
  KeyRound,
  Mail,
  PencilLine,
  ShieldAlert,
  Target,
  UserCircle2,
} from "lucide-react";
import notify from "../../../lib/toast";
import AppLayout from "../../../layouts/AppLayout";
import Button from "../../../components/Button";
import { useAuth } from "../../../context/useAuth.js";
import {
  updateProfile,
  changePassword,
  uploadAvatar,
  deactivateAccount,
} from "../../../api/userApi";
const formatDate = (value) => {
  if (!value) return "Not available";
  return new Date(value).toLocaleDateString();
};
const formatRole = (roles = []) => {
  const value = roles[0];
  if (!value) return "Workspace Member";
  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
};
export default function Profile() {
  const { user, setUser, logout, fetchProfile } = useAuth();
  const [showEdit, setShowEdit] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showDeactivateModal, setShowDeactivateModal] = useState(false);
  const [selectedFile, setSelectedFile] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [deactivating, setDeactivating] = useState(false);
  const [formData, setFormData] = useState({
    name: user?.name || "",
    email: user?.email || "",
  });
  const [passwordData, setPasswordData] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const initials = useMemo(
    () => user?.name?.charAt(0)?.toUpperCase() || "U",
    [user?.name],
  );
  if (!user) {
    return (
      <AppLayout>
         
        <div className="flex min-h-[70vh] items-center justify-center">
           
          <div className="text-center">
             
            <div className="mx-auto mb-4 h-12 w-12 animate-spin rounded-full border-b-2 border-indigo-600" /> 
            <p className="text-gray-600 dark:text-gray-300">
              Loading profile...
            </p> 
          </div> 
        </div> 
      </AppLayout>
    );
  }
  const handleProfileSave = async () => {
    if (!formData.name.trim()) {
      notify.error("Name is required");
      return;
    }
    try {
      setSavingProfile(true);
      const response = await updateProfile({
        name: formData.name.trim(),
        email: formData.email.trim(),
      });
      setUser(response.data.data);
      localStorage.setItem("userName", response.data.data.name);
      setShowEdit(false);
      notify.success("Profile updated");
    } catch (error) {
      notify.error(error.response?.data?.message || "Failed to update profile");
    } finally {
      setSavingProfile(false);
    }
  };
  const handlePasswordSave = async () => {
    if (!passwordData.oldPassword) {
      notify.error("Old password is required");
      return;
    }
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      notify.error("Passwords do not match");
      return;
    }
    try {
      setSavingPassword(true);
      await changePassword(passwordData);
      setPasswordData({
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      setShowPasswordModal(false);
      notify.success("Password updated");
    } catch (error) {
      notify.error(error.response?.data?.message || "Failed to update password");
    } finally {
      setSavingPassword(false);
    }
  };
  const handleFileChange = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith("image/")) {
      notify.error("Only images are allowed");
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      notify.error("Max image size is 2MB");
      return;
    }
    setSelectedFile(file);
    setAvatarPreview(URL.createObjectURL(file));
  };
  const handleUploadAvatar = async () => {
    if (!selectedFile) return;
    try {
      setUploading(true);
      await uploadAvatar(selectedFile);
      await fetchProfile();
      setSelectedFile(null);
      setAvatarPreview(null);
      notify.success("Avatar updated");
    } catch {
      notify.error("Failed to upload avatar");
    } finally {
      setUploading(false);
    }
  };
  const handleDeactivate = async () => {
    try {
      setDeactivating(true);
      await deactivateAccount();
      logout();
      notify.success("Account deactivated");
      window.location.href = "/";
    } catch {
      notify.error("Failed to deactivate account");
    } finally {
      setDeactivating(false);
    }
  };
  return (
    <AppLayout>
      <div className="space-y-8">
         
        <section className="relative isolate rounded-[2rem] border border-white/60 bg-[radial-gradient(circle_at_top_right,_rgba(59,130,246,0.22),_transparent_26%),linear-gradient(135deg,_#111827_0%,_#1f2937_48%,_#312e81_100%)] px-6 py-7 text-white shadow-[0_30px_80px_rgba(15,23,42,0.18)] sm:px-8 sm:py-8">
           
          <div className="grid items-start gap-8 xl:grid-cols-[1.2fr_0.8fr]">
             
            <div className="flex flex-col gap-5 sm:flex-row sm:items-start">
               
              <div className="relative mx-auto h-28 w-28 shrink-0 overflow-hidden rounded-[2rem] border border-white/15 bg-white/10 sm:mx-0">
                 
                {avatarPreview || user.avatarUrl ? (
                  <img
                    src={avatarPreview || user.avatarUrl}
                    alt="avatar"
                    className="h-full w-full object-cover"
                  />
                ) : (
                  <div className="flex h-full w-full items-center justify-center text-4xl font-black">
                     
                    {initials} 
                  </div>
                )} 
                <label className="absolute bottom-2 right-2 cursor-pointer rounded-xl bg-black/55 p-2 text-white transition hover:bg-black/75">
                   
                  <Camera className="h-4 w-4" /> 
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleFileChange}
                    className="hidden"
                  /> 
                </label> 
              </div> 
              <div className="min-w-0 flex-1 text-center sm:text-left">
                 
                <p className="mb-3 inline-flex rounded-full bg-white/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-cyan-100">
                   
                  Account Hub 
                </p> 
                <h1 className="break-words text-3xl font-black leading-tight sm:text-4xl">
                  {user.name}
                </h1> 
                <div className="mt-4 flex flex-wrap justify-center gap-3 text-sm text-slate-200 sm:justify-start">
                   
                  <span className="inline-flex max-w-full items-center gap-2 rounded-full bg-white/10 px-3 py-1.5">
                     
                    <Mail className="h-4 w-4" /> 
                    <span className="max-w-[220px] truncate sm:max-w-none">
                      {user.email}
                    </span> 
                  </span> 
                  <span className="inline-flex max-w-full items-center gap-2 rounded-full bg-white/10 px-3 py-1.5">
                     
                    <UserCircle2 className="h-4 w-4" /> 
                    {formatRole(user.roles)} 
                  </span> 
                </div> 
                <p className="mx-auto mt-5 max-w-2xl text-sm leading-7 text-slate-200 sm:mx-0">
                   
                  Manage your identity, keep your avatar fresh, and review your
                  live workspace footprint from one place. 
                </p> 
                {selectedFile && (
                  <div className="mt-5 flex flex-wrap justify-center gap-3 sm:justify-start">
                     
                    <Button
                      onClick={handleUploadAvatar}
                      disabled={uploading}
                      className="rounded-xl bg-white text-slate-900 hover:bg-slate-100"
                    >
                       
                      {uploading ? "Uploading..." : "Save Photo"} 
                    </Button> 
                    <Button
                      type="secondary"
                      onClick={() => {
                        setSelectedFile(null);
                        setAvatarPreview(null);
                      }}
                      className="rounded-xl bg-white/10 text-white hover:bg-white/15"
                    >
                       
                      Cancel 
                    </Button> 
                  </div>
                )} 
              </div> 
            </div> 
            <div className="rounded-3xl border border-white/10 bg-white/10 p-8 backdrop-blur-sm text-center">
               
              <p className="text-slate-300 text-sm">
                 
                Navigate to <strong>Projects</strong> and <strong>Tasks</strong> 
                from the sidebar for detailed statistics. 
              </p> 
            </div> 
          </div> 
        </section> 
        <section className="grid gap-8 xl:grid-cols-[1.05fr_0.95fr]">
           
          <div className="space-y-8">
             
            <div className="rounded-[2rem] border border-slate-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-8 shadow-xl backdrop-blur">
               
              <div className="mb-6 flex items-center justify-between">
                 
                <div>
                   
                  <h2 className="text-2xl font-bold text-slate-900 dark:text-gray-100">
                    Profile Details
                  </h2> 
                  <p className="text-sm text-slate-500 dark:text-gray-400">
                    Live account information and preferences.
                  </p> 
                </div> 
                <Button
                  onClick={() => {
                    setFormData({
                      name: user.name || "",
                      email: user.email || "",
                    });
                    setShowEdit(true);
                  }}
                  className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
                >
                   
                  <PencilLine className="mr-2 inline h-4 w-4" /> Edit 
                </Button> 
              </div> 
              <div className="grid gap-4 md:grid-cols-2">
                 
                <div className="rounded-2xl bg-slate-50 dark:bg-gray-700 p-4">
                   
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-400 dark:text-gray-400">
                    Full Name
                  </p> 
                  <p className="mt-2 text-lg font-semibold text-slate-900 dark:text-gray-100">
                    {user.name}
                  </p> 
                </div> 
                <div className="rounded-2xl bg-slate-50 dark:bg-gray-700 p-4">
                   
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-400 dark:text-gray-400">
                    Email
                  </p> 
                  <p className="mt-2 text-lg font-semibold text-slate-900 dark:text-gray-100">
                    {user.email}
                  </p> 
                </div> 
                <div className="rounded-2xl bg-slate-50 dark:bg-gray-700 p-4">
                   
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-400 dark:text-gray-400">
                    Primary Role
                  </p> 
                  <p className="mt-2 text-lg font-semibold text-slate-900 dark:text-gray-100">
                    {formatRole(user.roles)}
                  </p> 
                </div> 
                <div className="rounded-2xl bg-slate-50 dark:bg-gray-700 p-4">
                   
                  <p className="text-xs uppercase tracking-[0.2em] text-slate-400 dark:text-gray-400">
                    Member Since
                  </p> 
                  <p className="mt-2 text-lg font-semibold text-slate-900 dark:text-gray-100">
                    {formatDate(user.createdAt)}
                  </p> 
                </div> 
              </div> 
            </div> 
            <div className="rounded-[2rem] border border-slate-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-8 shadow-xl backdrop-blur">
               
              <h2 className="text-2xl font-bold text-slate-900 dark:text-gray-100">
                Security
              </h2> 
              <p className="mt-1 text-sm text-slate-500 dark:text-gray-400">
                Keep your account access secure and under control.
              </p> 
              <div className="mt-6 flex flex-wrap gap-3">
                 
                <Button
                  onClick={() => setShowPasswordModal(true)}
                  className="rounded-xl bg-amber-500 text-white hover:bg-amber-600"
                >
                   
                  <KeyRound className="mr-2 inline h-4 w-4" /> Change
                  Password 
                </Button> 
                <Button
                  type="secondary"
                  onClick={() => window.location.assign("/user-home")}
                  className="rounded-xl"
                >
                   
                  <FolderKanban className="mr-2 inline h-4 w-4" /> Back to
                  Home 
                </Button> 
              </div> 
            </div> 
          </div> 
          <div className="space-y-8">
             
            <div className="rounded-[2rem] border border-slate-200 dark:border-gray-700 bg-white dark:bg-gray-800 p-8 shadow-xl backdrop-blur">
               
              <h2 className="text-2xl font-bold text-slate-900 dark:text-gray-100">
                Quick Access
              </h2> 
              <div className="mt-6 space-y-4">
                 
                <div className="flex items-center justify-between rounded-2xl bg-slate-50 dark:bg-gray-700 p-4">
                   
                  <div className="flex items-center gap-3">
                     
                    <FolderKanban className="h-5 w-5 text-indigo-600" /> 
                    <span className="font-medium text-slate-700 dark:text-gray-200">
                      Projects
                    </span> 
                  </div> 
                  <span className="text-sm text-indigo-600">View ΓåÆ</span> 
                </div> 
                <div className="flex items-center justify-between rounded-2xl bg-slate-50 dark:bg-gray-700 p-4">
                   
                  <div className="flex items-center gap-3">
                     
                    <Target className="h-5 w-5 text-emerald-600" /> 
                    <span className="font-medium text-slate-700 dark:text-gray-200">
                      Tasks
                    </span> 
                  </div> 
                  <span className="text-sm text-indigo-600">View ΓåÆ</span> 
                </div> 
                <div className="flex items-center justify-between rounded-2xl bg-slate-50 dark:bg-gray-700 p-4">
                   
                  <div className="flex items-center gap-3">
                     
                    <CalendarDays className="h-5 w-5 text-amber-600" /> 
                    <span className="font-medium text-slate-700 dark:text-gray-200">
                      Calendar
                    </span> 
                  </div> 
                  <span className="text-sm text-indigo-600">View ΓåÆ</span> 
                </div> 
              </div> 
            </div> 
            <div className="rounded-[2rem] border border-red-200 dark:border-red-800 bg-red-50/80 dark:bg-red-900/20 p-8 shadow-xl">
               
              <div className="flex items-center gap-3 text-red-600">
                 
                <ShieldAlert className="h-6 w-6" /> 
                <h2 className="text-2xl font-bold">Danger Zone</h2> 
              </div> 
              <p className="mt-3 text-sm leading-6 text-slate-600 dark:text-gray-300">
                 
                Deactivating your account removes your access to the workspace
                until an administrator restores it. 
              </p> 
              <Button
                onClick={() => setShowDeactivateModal(true)}
                className="mt-6 rounded-xl bg-red-600 text-white hover:bg-red-700"
              >
                 
                Deactivate Account 
              </Button> 
            </div> 
          </div> 
        </section> 
      </div> 
      {showEdit && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 dark:bg-black/60 p-4 backdrop-blur-sm">
           
          <div className="w-full max-w-lg rounded-[2rem] bg-white dark:bg-gray-800 p-8 shadow-2xl">
             
            <h3 className="text-2xl font-bold text-slate-900 dark:text-gray-100">
              Edit Profile
            </h3> 
            <div className="mt-6 space-y-4">
               
              <input
                type="text"
                value={formData.name}
                onChange={(event) =>
                  setFormData((state) => ({
                    ...state,
                    name: event.target.value,
                  }))
                }
                placeholder="Full name"
                className="w-full rounded-2xl border border-slate-200 dark:border-gray-600 bg-slate-50 dark:bg-gray-700 px-4 py-3 outline-none focus:border-indigo-400 focus:bg-white dark:focus:bg-gray-600 text-gray-900 dark:text-gray-100"
              /> 
              <input
                type="email"
                value={formData.email}
                onChange={(event) =>
                  setFormData((state) => ({
                    ...state,
                    email: event.target.value,
                  }))
                }
                placeholder="Email address"
                className="w-full rounded-2xl border border-slate-200 dark:border-gray-600 bg-slate-50 dark:bg-gray-700 px-4 py-3 outline-none focus:border-indigo-400 focus:bg-white dark:focus:bg-gray-600 text-gray-900 dark:text-gray-100"
              /> 
            </div> 
            <div className="mt-6 flex justify-end gap-3">
               
              <Button type="secondary" onClick={() => setShowEdit(false)}>
                Cancel
              </Button> 
              <Button
                onClick={handleProfileSave}
                disabled={savingProfile}
                className="bg-indigo-600 text-white hover:bg-indigo-700"
              >
                 
                {savingProfile ? "Saving..." : "Save Changes"} 
              </Button> 
            </div> 
          </div> 
        </div>
      )} 
      {showPasswordModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 dark:bg-black/60 p-4 backdrop-blur-sm">
           
          <div className="w-full max-w-lg rounded-[2rem] bg-white dark:bg-gray-800 p-8 shadow-2xl">
             
            <h3 className="text-2xl font-bold text-slate-900 dark:text-gray-100">
              Change Password
            </h3> 
            <div className="mt-6 space-y-4">
               
              <input
                type="password"
                value={passwordData.oldPassword}
                onChange={(event) =>
                  setPasswordData((state) => ({
                    ...state,
                    oldPassword: event.target.value,
                  }))
                }
                placeholder="Old password"
                className="w-full rounded-2xl border border-slate-200 dark:border-gray-600 bg-slate-50 dark:bg-gray-700 px-4 py-3 outline-none focus:border-indigo-400 focus:bg-white dark:focus:bg-gray-600 text-gray-900 dark:text-gray-100"
              /> 
              <input
                type="password"
                value={passwordData.newPassword}
                onChange={(event) =>
                  setPasswordData((state) => ({
                    ...state,
                    newPassword: event.target.value,
                  }))
                }
                placeholder="New password"
                className="w-full rounded-2xl border border-slate-200 dark:border-gray-600 bg-slate-50 dark:bg-gray-700 px-4 py-3 outline-none focus:border-indigo-400 focus:bg-white dark:focus:bg-gray-600 text-gray-900 dark:text-gray-100"
              /> 
              <input
                type="password"
                value={passwordData.confirmPassword}
                onChange={(event) =>
                  setPasswordData((state) => ({
                    ...state,
                    confirmPassword: event.target.value,
                  }))
                }
                placeholder="Confirm new password"
                className="w-full rounded-2xl border border-slate-200 dark:border-gray-600 bg-slate-50 dark:bg-gray-700 px-4 py-3 outline-none focus:border-indigo-400 focus:bg-white dark:focus:bg-gray-600 text-gray-900 dark:text-gray-100"
              /> 
            </div> 
            <div className="mt-6 flex justify-end gap-3">
               
              <Button
                type="secondary"
                onClick={() => setShowPasswordModal(false)}
              >
                Cancel
              </Button> 
              <Button
                onClick={handlePasswordSave}
                disabled={savingPassword}
                className="bg-amber-500 text-white hover:bg-amber-600"
              >
                 
                {savingPassword ? "Saving..." : "Update Password"} 
              </Button> 
            </div> 
          </div> 
        </div>
      )} 
      {showDeactivateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/45 dark:bg-black/60 p-4 backdrop-blur-sm">
           
          <div className="w-full max-w-lg rounded-[2rem] bg-white dark:bg-gray-800 p-8 shadow-2xl">
             
            <h3 className="text-2xl font-bold text-red-600">
              Deactivate Account
            </h3> 
            <p className="mt-4 text-sm leading-6 text-slate-600 dark:text-gray-300">
               
              This will remove your active access. You will need help from an
              administrator to re-enable the account. 
            </p> 
            <div className="mt-6 flex justify-end gap-3">
               
              <Button
                type="secondary"
                onClick={() => setShowDeactivateModal(false)}
              >
                Cancel
              </Button> 
              <Button
                onClick={handleDeactivate}
                disabled={deactivating}
                className="bg-red-600 text-white hover:bg-red-700"
              >
                 
                {deactivating ? "Deactivating..." : "Deactivate"} 
              </Button> 
            </div> 
          </div> 
        </div>
      )} 
    </AppLayout>
  );
}
