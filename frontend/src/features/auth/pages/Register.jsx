import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../../../context/useAuth.js";
import API from "../../../api/axios";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import Logo from "../../../assets/Logo - PMT-SK.png";

const schema = yup.object().shape({
  name: yup
    .string()
    .min(2, "Name must be at least 2 characters")
    .max(50, "Name max 50 characters")
    .required("Full name is required"),
  username: yup
    .string()
    .min(3, "Username must be at least 3 characters")
    .max(30, "Username max 30 characters")
    .matches(
      /^[a-zA-Z0-9._-]+$/,
      "Username: letters, numbers, _, -, . only (no spaces)",
    )
    .required("Username is required"),
  email: yup
    .string()
    .email("Invalid email format")
    .matches(
      /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      "Valid email required (e.g. user@company.com)",
    )
    .required("Email is required"),
  password: yup
    .string()
    .min(8, "Password must be at least 8 characters")
    .matches(
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
      "Must contain uppercase, lowercase, number, special char",
    )
    .required("Password is required"),
  confirmPassword: yup
    .string()
    .oneOf([yup.ref("password")], "Passwords must match")
    .required("Confirm password is required"),
});

export default function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState(0);
  const [animate, setAnimate] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  // Default: unchecked

  const {
    register,
    handleSubmit,
    formState: { errors, isValid },
    watch,
  } = useForm({
    resolver: yupResolver(schema),
    mode: "onChange",
  });

  const passwordValue = watch("password");

  useEffect(() => {
    setTimeout(() => setAnimate(true), 100);
  }, []);

  useEffect(() => {
    if (passwordValue) {
      setPasswordStrength(checkPasswordStrength(passwordValue));
    }
  }, [passwordValue]);

  const checkPasswordStrength = (password) => {
    if (!password) return 0;
    let score = 0;
    if (password.length >= 8) score++;
    if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
    if (/\d/.test(password)) score++;
    if (/[@$!%*?&]/.test(password)) score++;
    return score;
  };

  const getStrengthLabel = () => {
    if (passwordStrength === 0) return { label: "", color: "bg-gray-200" };
    if (passwordStrength <= 1) return { label: "Weak", color: "bg-red-500" };
    if (passwordStrength <= 2) return { label: "Fair", color: "bg-orange-500" };
    if (passwordStrength <= 3) return { label: "Good", color: "bg-yellow-500" };
    return { label: "Strong", color: "bg-green-500" };
  };

  const strengthInfo = getStrengthLabel();

  const onSubmit = async (data) => {
    setLoading(true);
    setError("");

    try {
      const res = await API.post("/auth/register", {
        name: data.name,
        username: data.username,
        email: data.email,
        password: data.password,
      });

      // Debug: Log response
      console.log("Register response:", res.data);

      // Save userName for display
      localStorage.setItem("userName", data.name);

      // Navigate to verification page with email and rememberMe flag
      navigate(
        `/verify-email?email=${encodeURIComponent(data.email)}&remember=${rememberMe}`,
      );

      // Note: Don't set loading=false here - component will unmount due to navigation
    } catch (err) {
      console.error("Register error:", err);
      const msg =
        err.response?.data?.message ||
        err.message ||
        "Registration failed. Backend may not be running.";
      setError(msg);
      setLoading(false); // Only set false on error

      if (err.message?.includes("ERR_CONNECTION_REFUSED")) {
        setError("Backend server not running. Start at http://localhost:8081");
      }
    }
  };

  return (
    <div
      className={`min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col lg:flex-row transition-all duration-700 ${animate ? "opacity-100" : "opacity-0"}`}
    >
      {/* Back to Home Link */}
      <Link
        to="/"
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
          Back to Home
        </span>
      </Link>

      {/* Left Side - Branding */}
      <div className="w-full lg:w-1/2 bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-500 relative overflow-hidden lg:min-h-screen">
        <div className="absolute inset-0 bg-black opacity-10"></div>
        <div className="relative z-10 flex flex-col justify-center items-center p-8 lg:p-12 text-white min-h-[300px] lg:min-h-screen">
          <img
            src={Logo}
            alt="PMT-SK"
            className="w-32 h-32 lg:w-48 lg:h-48 mb-4 lg:mb-8 object-contain"
          />
          <h1 className="text-3xl lg:text-5xl font-bold mb-3 lg:mb-6 text-center">
            Join PMT-SK Today
          </h1>
          <p className="text-base lg:text-xl text-center mb-4 lg:mb-8 opacity-90 max-w-md">
            Start managing your projects with Slack integration, webhooks, and
            powerful collaboration tools.
          </p>
        </div>
      </div>

      {/* Right Side - Registration Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          <div className="lg:hidden text-center mb-8">
            <img
              src={Logo}
              alt="PMT-SK"
              className="w-24 h-24 mx-auto mb-4 object-contain"
            />
            <h1 className="text-3xl font-bold text-gray-800 dark:text-gray-100">
              Create Account
            </h1>
          </div>

          <div className="hidden lg:block mb-8">
            <h2 className="text-3xl font-bold text-gray-800 dark:text-gray-100 mb-2">
              Create Account
            </h2>
            <p className="text-gray-600 dark:text-gray-300">
              Get started with your free account
            </p>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg flex items-center gap-3">
              <p className="text-red-700 dark:text-red-300 text-sm">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
            <div>
              <label
                htmlFor="register-name"
                className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2"
              >
                Full Name
              </label>
              <input
                id="register-name"
                type="text"
                placeholder="Enter your full name"
                {...register("name")}
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
              />
              {errors.name && (
                <p className="mt-2 text-sm text-red-600">
                  {errors.name.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="register-username"
                className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2"
              >
                Username
              </label>
              <input
                id="register-username"
                type="text"
                placeholder="Choose a username for login"
                {...register("username")}
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
              />
              {errors.username && (
                <p className="mt-2 text-sm text-red-600">
                  {errors.username.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="register-email"
                className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2"
              >
                Email Address
              </label>
              <input
                id="register-email"
                type="email"
                placeholder="Enter your email"
                {...register("email")}
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
              />
              {errors.email && (
                <p className="mt-2 text-sm text-red-600">
                  {errors.email.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="register-password"
                className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2"
              >
                Password
              </label>
              <div className="relative">
                <input
                  id="register-password"
                  type={showPassword ? "text" : "password"}
                  placeholder="Create a password"
                  {...register("password")}
                  className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all pr-12"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-sm text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200"
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
              {passwordValue && (
                <div className="mt-3">
                  <div className="flex gap-1 mb-2">
                    {[1, 2, 3, 4].map((level) => (
                      <div
                        key={level}
                        className={`h-1.5 flex-1 rounded-full ${
                          passwordStrength >= level
                            ? strengthInfo.color
                            : "bg-gray-200 dark:bg-gray-700"
                        } transition-colors duration-300`}
                      ></div>
                    ))}
                  </div>
                  {strengthInfo.label && (
                    <p
                      className={`text-xs font-medium ${
                        passwordStrength <= 1
                          ? "text-red-600"
                          : passwordStrength <= 2
                            ? "text-orange-600"
                            : passwordStrength <= 3
                              ? "text-yellow-600"
                              : "text-green-600"
                      }`}
                    >
                      Password strength: {strengthInfo.label}
                    </p>
                  )}
                </div>
              )}
              {errors.password && (
                <p className="mt-2 text-sm text-red-600">
                  {errors.password.message}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="register-confirm-password"
                className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2"
              >
                Confirm Password
              </label>
              <div className="relative">
                <input
                  id="register-confirm-password"
                  type={showConfirmPassword ? "text" : "password"}
                  placeholder="Confirm your password"
                  {...register("confirmPassword")}
                  className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all pr-12"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute inset-y-0 right-0 pr-3 flex items-center text-sm text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200"
                >
                  {showConfirmPassword ? "Hide" : "Show"}
                </button>
              </div>
              {errors.confirmPassword && (
                <p className="mt-2 text-sm text-red-600">
                  {errors.confirmPassword.message}
                </p>
              )}
            </div>

            <div className="flex items-start">
              <div className="flex items-center h-5">
                <input
                  id="terms"
                  type="checkbox"
                  required
                  className="w-4 h-4 text-indigo-600 border-gray-300 dark:border-gray-600 rounded focus:ring-indigo-500"
                />
              </div>
              <div className="ml-3 text-sm">
                <label
                  htmlFor="terms"
                  className="text-gray-600 dark:text-gray-300"
                >
                  I agree to the 
                  <Link
                    to="/terms"
                    className="text-indigo-600 hover:text-indigo-500"
                  >
                    Terms
                  </Link> 
                  and 
                  <Link
                    to="/privacy"
                    className="text-indigo-600 hover:text-indigo-500"
                  >
                    Privacy Policy
                  </Link>
                </label>
              </div>
            </div>

            <div className="flex items-center">
              <input
                id="remember-me-register"
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                className="w-4 h-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500"
              />
              <label
                htmlFor="remember-me-register"
                className="ml-2 block text-sm text-gray-700 dark:text-gray-200"
              >
                Remember me (auto-login after verification)
              </label>
            </div>

            <button
              type="submit"
              disabled={loading || !isValid}
              className="w-full py-3 px-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-xl hover:from-indigo-700 hover:to-purple-700 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 shadow-lg hover:shadow-xl"
            >
              {loading ? "Creating Account..." : "Create Account"}
            </button>
          </form>

          <p className="mt-6 text-center">
            <span className="text-gray-600 dark:text-gray-300">
              Already have an account? 
            </span>
            <Link
              to="/login"
              className="inline-flex items-center gap-1 text-indigo-600 hover:text-indigo-800 dark:hover:text-indigo-400 font-semibold bg-indigo-50 dark:bg-indigo-900/20 hover:bg-indigo-100 dark:hover:bg-indigo-900/40 px-4 py-2 rounded-lg transition-all"
            >
              <svg
                className="w-4 h-4"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M11 17l-5-5m0 0l5-5m-5 5h8"
                />
              </svg>
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
