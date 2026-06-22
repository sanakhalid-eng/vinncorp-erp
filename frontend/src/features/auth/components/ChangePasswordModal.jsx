import { useState } from "react";
import { useAuth } from "../../../context/useAuth.js";
import { changePassword } from "../../../api/userApi";
import Button from "../../../components/Button.jsx";
import Input from "../../../components/Input.jsx";
import Card from "./Card.jsx";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
const schema = yup.object().shape({
  oldPassword: yup
    .string()
    .min(6, "Old password must be at least 6 characters")
    .required("Old password is required"),
  newPassword: yup
    .string()
    .min(8, "New password must be at least 8 characters")
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
      "Must contain uppercase, lowercase, number, special char",
    )
    .required("New password is required"),
  confirmPassword: yup
    .string()
    .oneOf([yup.ref("newPassword")], "Passwords must match")
    .required("Confirm new password is required"),
});
export default function ChangePasswordModal({ isOpen, onClose }) {
  const { logout } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const {
    register: formRegister,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm({ resolver: yupResolver(schema) });
  const onSubmit = async (data) => {
    setLoading(true);
    setError("");
    setSuccess("");
    try {
      await changePassword({
        currentPassword: data.oldPassword,
        newPassword: data.newPassword,
      });
      setSuccess("Password changed successfully!");
      reset();
      setTimeout(() => onClose(), 1500);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to change password");
      if (err.response?.status === 401) {
        logout();
      }
    } finally {
      setLoading(false);
    }
  };
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
       
      <Card className="w-full max-w-md">
         
        <div className="p-6">
           
          <h2 className="text-xl font-bold mb-4">Change Password</h2> 
          {error && (
            <p className="text-red-500 mb-4 text-sm bg-red-50 p-2 rounded">
              {error}
            </p>
          )} 
          {success && (
            <p className="text-green-600 mb-4 text-sm bg-green-50 p-2 rounded">
              {success}
            </p>
          )} 
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
             
            <Input
              label="Old Password"
              type="password"
              placeholder="Enter old password"
              {...formRegister("oldPassword")}
              error={errors.oldPassword?.message}
            /> 
            <Input
              label="New Password"
              type="password"
              placeholder="Enter new password"
              {...formRegister("newPassword")}
              error={errors.newPassword?.message}
            /> 
            <Input
              label="Confirm New Password"
              type="password"
              placeholder="Confirm new password"
              {...formRegister("confirmPassword")}
              error={errors.confirmPassword?.message}
            /> 
            <div className="flex gap-3 pt-2">
               
              <Button type="primary" className="flex-1" disabled={loading}>
                 
                {loading ? "Changing..." : "Change Password"} 
              </Button> 
              <Button
                type="secondary"
                onClick={onClose}
                disabled={loading}
                className="flex-1"
              >
                 
                Cancel 
              </Button> 
            </div> 
          </form> 
        </div> 
      </Card> 
    </div>
  );
}
