import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import API from "../../../api/axios";
import { toast } from "sonner";
import Logo from "../../../assets/Logo - PMT-SK.png";
export default function ForgotPassword() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState("");
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    const trimmed = email.trim();
    if (!trimmed) {
      setError("Email is required");
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(trimmed)) {
      setError("Please enter a valid email address");
      return;
    }
    setLoading(true);
    try {
      await API.post("/auth/forgot-password", { email: trimmed });
      setSubmitted(true);
    } catch (err) {
      console.error("Forgot password error:", err);
      setError(
        err.response?.data?.message ||
          "Failed to send reset email. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };
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
             
            Reset Your
            <br />
            Password 
          </h1> 
          <p className="text-base lg:text-xl text-center mb-4 lg:mb-8 opacity-90 max-w-md">
             
            We'll send you a secure link to reset your password and regain
            access to your account. 
          </p> 
        </div> 
      </div> 
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
         
        <div className="w-full max-w-md">
           
          <div className="hidden lg:block mb-8">
             
            <h2 className="text-3xl font-bold text-gray-800 dark:text-gray-100 mb-2">
              Forgot Password
            </h2> 
            <p className="text-gray-600 dark:text-gray-300">
              Enter your email to receive a reset link
            </p> 
          </div> 
          {error && (
            <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
               
              <p className="text-red-700 dark:text-red-300 text-sm">
                {error}
              </p> 
            </div>
          )} 
          {submitted ? (
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
                    d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                  /> 
                </svg> 
                <h3 className="text-xl font-semibold text-green-800 dark:text-green-200 mb-2">
                  Check Your Email
                </h3> 
                <p className="text-green-700 dark:text-green-300 text-sm">
                   
                  We've sent a password reset link to 
                  <strong>{email}</strong> 
                </p> 
                <p className="text-green-600 dark:text-green-400 text-sm mt-2">
                   
                  The link will expire in 15 minutes. 
                </p> 
              </div> 
              <button
                onClick={() => setSubmitted(false)}
                className="text-sm text-indigo-600 hover:text-indigo-500 font-medium"
              >
                 
                Didn't receive it? Try again 
              </button> 
              <div className="mt-4">
                 
                <Link
                  to="/login"
                  className="text-sm text-gray-600 dark:text-gray-400 hover:text-indigo-600"
                >
                   
                  Back to Login 
                </Link> 
              </div> 
            </div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-6">
               
              <div>
                 
                <label
                  htmlFor="email"
                  className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2"
                >
                   
                  Email Address 
                </label> 
                <input
                  id="email"
                  type="email"
                  placeholder="Enter your email address"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
                /> 
              </div> 
              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 px-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-xl hover:from-indigo-700 hover:to-purple-700 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 shadow-lg hover:shadow-xl"
              >
                 
                {loading ? "Sending..." : "Send Reset Link"} 
              </button> 
              <p className="text-center">
                 
                <Link
                  to="/login"
                  className="text-sm text-indigo-600 hover:text-indigo-500 font-medium"
                >
                   
                  Remember your password? Sign in 
                </Link> 
              </p> 
            </form>
          )} 
        </div> 
      </div> 
    </div>
  );
}
