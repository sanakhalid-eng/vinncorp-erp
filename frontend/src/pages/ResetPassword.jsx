import { useState, useEffect } from "react";
import { useNavigate, Link, useSearchParams } from "react-router-dom";
import API from "../api/axios";
import { toast } from "sonner";
import Logo from "../assets/Logo - PMT-SK.png";
export default function ResetPassword() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [validating, setValidating] = useState(true);
  const [tokenValid, setTokenValid] = useState(false);
  const [success, setSuccess] = useState(false);
  useEffect(() => {
    if (!token) {
      setError("Invalid reset link. Please request a new one.");
      setValidating(false);
      return;
    }
    const verifyToken = async () => {
      try {
        const res = await API.get("/auth/reset-password/verify", {
          params: { token },
        });
        setTokenValid(res.data?.data === true);
        if (res.data?.data !== true) {
          setError(
            "This reset link is invalid or has expired. Please request a new one.",
          );
        }
      } catch {
        setError(
          "This reset link is invalid or has expired. Please request a new one.",
        );
      } finally {
        setValidating(false);
      }
    };
    verifyToken();
  }, [token]);
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (!newPassword || !confirmPassword) {
      setError("Both password fields are required");
      return;
    }
    if (newPassword.length < 6) {
      setError("Password must be at least 6 characters");
      return;
    }
    if (newPassword !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }
    setLoading(true);
    try {
      await API.post("/auth/reset-password", { token, newPassword });
      setSuccess(true);
      toast.success("Password reset successfully!");
    } catch (err) {
      console.error("Reset password error:", err);
      setError(
        err.response?.data?.message ||
          "Failed to reset password. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };
  if (validating) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
         
        <div className="animate-pulse space-y-4 w-full max-w-md px-8">
           
          <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded-lg"></div> 
          <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded-lg"></div> 
          <div className="h-10 bg-gray-300 dark:bg-gray-600 rounded-lg w-1/2 mx-auto"></div> 
        </div> 
      </div>
    );
  }
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col lg:flex-row">
       
      <Link
        to="/login"
        className="absolute top-4 left-4 z-20 flex items-center gap-2 bg-white/90 dark:bg-gray-800/90 lg:bg-transparent backdrop-blur-sm lg:backdrop-blur-none px-3 py-2 rounded-lg shadow-md lg:shadow-none hover:bg-white dark:hover:bg-gray-800 lg:hover:bg-indigo-50 dark:lg:hover:bg-indigo-900/30 transition-all group"
      >
         
        <svg
          className="w-5 h-5 text-indigo-600"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
           
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M10 19l-7-7m0 0l7-7m-7 7h18"
          /> 
        </svg> 
        <span className="text-sm font-medium text-gray-700 dark:text-gray-200 lg:text-indigo-600">
          Back to Login
        </span> 
      </Link> 
      <div className="w-full lg:w-1/2 bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-500 relative overflow-hidden lg:min-h-screen">
         
        <div className="absolute inset-0 bg-black opacity-10"></div> 
        <div className="relative z-10 flex flex-col justify-center items-center p-8 lg:p-12 text-white min-h-[300px] lg:min-h-screen">
           
          <img
            src={Logo}
            alt="PMT-SK"
            className="w-32 h-32 lg:w-48 lg:h-48 mb-4 lg:mb-8 object-contain"
          /> 
          <h1 className="text-3xl lg:text-5xl font-bold mb-3 lg:mb-6 text-center">
             
            Create New
            <br />
            Password 
          </h1> 
          <p className="text-base lg:text-xl text-center mb-4 lg:mb-8 opacity-90 max-w-md">
             
            Choose a strong password to secure your account. 
          </p> 
        </div> 
      </div> 
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
         
        <div className="w-full max-w-md">
           
          <div className="hidden lg:block mb-8">
             
            <h2 className="text-3xl font-bold text-gray-800 dark:text-gray-100 mb-2">
              Reset Password
            </h2> 
            <p className="text-gray-600 dark:text-gray-300">
              Enter your new password
            </p> 
          </div> 
          {error && (
            <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
               
              <p className="text-red-700 dark:text-red-300 text-sm">
                {error}
              </p> 
            </div>
          )} 
          {success ? (
            <div className="text-center">
               
              <div className="mb-6 p-6 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
                 
                <svg
                  className="w-16 h-16 text-green-500 mx-auto mb-4"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                   
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                  /> 
                </svg> 
                <h3 className="text-xl font-semibold text-green-800 dark:text-green-200 mb-2">
                  Password Reset!
                </h3> 
                <p className="text-green-700 dark:text-green-300 text-sm">
                   
                  Your password has been reset successfully. 
                </p> 
              </div> 
              <Link
                to="/login"
                className="inline-block py-3 px-6 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-xl hover:from-indigo-700 hover:to-purple-700 transition-all duration-300 shadow-lg"
              >
                 
                Sign In 
              </Link> 
            </div>
          ) : tokenValid ? (
            <form onSubmit={handleSubmit} className="space-y-6">
               
              <div>
                 
                <label
                  htmlFor="new-password"
                  className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2"
                >
                   
                  New Password 
                </label> 
                <div className="relative">
                   
                  <input
                    id="new-password"
                    type={showPassword ? "text" : "password"}
                    placeholder="Enter new password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all pr-20"
                  /> 
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-sm text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200"
                  >
                     
                    {showPassword ? "Hide" : "Show"} 
                  </button> 
                </div> 
              </div> 
              <div>
                 
                <label
                  htmlFor="confirm-password"
                  className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2"
                >
                   
                  Confirm Password 
                </label> 
                <input
                  id="confirm-password"
                  type="password"
                  placeholder="Confirm new password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
                /> 
              </div> 
              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 px-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-xl hover:from-indigo-700 hover:to-purple-700 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 shadow-lg hover:shadow-xl"
              >
                 
                {loading ? "Resetting..." : "Reset Password"} 
              </button> 
              <p className="text-center">
                 
                <Link
                  to="/login"
                  className="text-sm text-indigo-600 hover:text-indigo-500 font-medium"
                >
                   
                  Back to Login 
                </Link> 
              </p> 
            </form>
          ) : (
            <div className="text-center">
               
              <Link
                to="/forgot-password"
                className="inline-block py-3 px-6 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-xl hover:from-indigo-700 hover:to-purple-700 transition-all duration-300 shadow-lg"
              >
                 
                Request New Reset Link 
              </Link> 
            </div>
          )} 
        </div> 
      </div> 
    </div>
  );
}
