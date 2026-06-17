import { useState, useEffect } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom";
import { useAuth } from "../context/useAuth.js";
import API from "../api/axios";
import TwoFactorModal from "../components/TwoFactorModal";
import { acceptInvitation } from "../api/invitationApi";
import notify from "../lib/toast";
import Logo from "../assets/Logo - PMT-SK.png";

export default function Login() {
  const { login, token } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [identifier, setIdentifier] = useState(localStorage.getItem("rememberedEmail") || "");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [rememberMe, setRememberMe] = useState(!!localStorage.getItem("rememberedEmail"));
  const [animate, setAnimate] = useState(false);
  const [show2FAModal, setShow2FAModal] = useState(false);
  const [tempIdentifier, setTempIdentifier] = useState("");
  const [tempPassword, setTempPassword] = useState("");

  useEffect(() => {
    if (token) {
      navigate("/user-home", { replace: true });
    } else {
      setTimeout(() => setAnimate(true), 100);
    }
  }, [token, navigate]);

  const handleGithubLogin = () => {
    window.location.href = "/oauth2/authorization/github";
  };

  const validateIdentifier = (value) => {
    const trimmed = value.trim();
    if (!trimmed) return false;
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const usernameRegex = /^[a-zA-Z0-9._-]{3,30}$/;
    return emailRegex.test(trimmed) || usernameRegex.test(trimmed);
  };

  const handleLogin = async (e) => {
    if (e) e.preventDefault();

    const identifierTrimmed = identifier.trim();
    const passwordTrimmed = password.trim();

    if (!identifierTrimmed || !passwordTrimmed) {
      setError("Email/username and password are required");
      return;
    }

    if (!validateIdentifier(identifierTrimmed)) {
      setError("Please enter a valid email or username (3-30 chars, letters/numbers/./_/-)");
      return;
    }

    if (passwordTrimmed.length < 6) {
      setError("Password must be at least 6 characters");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await API.post("/auth/login", {
        identifier: identifierTrimmed,
        password: passwordTrimmed,
      });

      // Check if 2FA is required
      if (res.data?.message === "2FA_REQUIRED" || res.data?.data?.requires2FA) {
        setTempIdentifier(identifierTrimmed);
        setTempPassword(passwordTrimmed);
        setShow2FAModal(true);
        setLoading(false);
        return;
      }

      const tokenPayload = res.data.data?.accessToken || res.data.data?.token || res.data.token || res.data.message;
      const refreshPayload = res.data.data?.refreshToken;
      const userIdPayload = res.data.data?.userId;
      const workspacePayload = res.data.data?.currentWorkspace;

      if (refreshPayload) localStorage.setItem('refreshToken', refreshPayload);
      if (userIdPayload) localStorage.setItem('userId', userIdPayload);
      if (workspacePayload?.id) localStorage.setItem('activeWorkspaceId', workspacePayload.id);

      if (!tokenPayload) {
        throw new Error("No access token found in response");
      }

      // Derive display name from identifier if not yet set
      const isEmail = identifierTrimmed.includes('@');
      const derivedName = isEmail
        ? identifierTrimmed.split('@')[0].replace(/[^a-zA-Z]/g, ' ').trim()
        : identifierTrimmed.replace(/[._-]/g, ' ').trim();
      const displayName = (derivedName || 'User').charAt(0).toUpperCase() + (derivedName || 'User').slice(1).toLowerCase();
      localStorage.setItem('userName', displayName);

      if (rememberMe) {
        localStorage.setItem('rememberedEmail', identifierTrimmed);
      } else {
        localStorage.removeItem('rememberedEmail');
      }

      login(tokenPayload);

      // Check for pending invitation recovery
      const pendingInvite = localStorage.getItem('pendingInviteToken');
      if (pendingInvite) {
        try {
          await acceptInvitation(pendingInvite);
          localStorage.removeItem('pendingInviteToken');
          notify.success("Invitation accepted! Redirecting to project...");
          navigate("/projects", { replace: true });
          return;
        } catch {
          localStorage.removeItem('pendingInviteToken');
        }
      }

      const redirectTo = location.state?.redirectTo || "/user-home";
      navigate(redirectTo, { replace: true });

    } catch (err) {
      console.error("Login error:", err);
      const msg = err.response?.data?.message || err.message || "Login failed. Backend may not be running.";
      setError(msg);
      if (err.message?.includes('ERR_CONNECTION_REFUSED')) {
        setError("Backend server not running. Please contact support.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handle2FASuccess = async (data) => {
    try {
      const tokenPayload = data?.accessToken || data?.token;
      const refreshPayload = data?.refreshToken;
      const userIdPayload = data?.userId;
      
      if (refreshPayload) localStorage.setItem('refreshToken', refreshPayload);
      if (userIdPayload) localStorage.setItem('userId', userIdPayload);
      
      const isEmail = tempIdentifier.includes('@');
      const derivedName = isEmail
        ? tempIdentifier.split('@')[0].replace(/[^a-zA-Z]/g, ' ').trim()
        : tempIdentifier.replace(/[._-]/g, ' ').trim();
      const displayName = (derivedName || 'User').charAt(0).toUpperCase() + (derivedName || 'User').slice(1).toLowerCase();
      localStorage.setItem('userName', displayName);
      
      login(tokenPayload);
      
      navigate("/user-home", { replace: true });
    } catch (err) {
      console.error("Login after 2FA error:", err);
      setError(err.response?.data?.message || "Login failed");
    }
  };

  const handle2FACancel = () => {
    setShow2FAModal(false);
    setTempIdentifier("");
    setTempPassword("");
    setLoading(false);
  };

  if (!animate) {
    return (
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center">
        <div className="animate-pulse space-y-4 w-full max-w-md px-8">
          <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded-lg"></div>
          <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded-lg"></div>
          <div className="h-12 bg-gray-200 dark:bg-gray-700 rounded-lg"></div>
          <div className="h-10 bg-gray-300 dark:bg-gray-600 rounded-lg w-1/2 mx-auto"></div>
        </div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col lg:flex-row transition-all duration-700 ${animate ? 'opacity-100' : 'opacity-0'}`}>
      {/* Back to Home Link */}
      <Link to="/" className="absolute top-4 left-4 z-20 flex items-center gap-2 bg-white/90 dark:bg-gray-800/90 lg:bg-transparent backdrop-blur-sm lg:backdrop-blur-none px-3 py-2 rounded-lg shadow-md lg:shadow-none hover:bg-white dark:hover:bg-gray-800 lg:hover:bg-indigo-50 dark:lg:hover:bg-indigo-900/30 transition-all group">
        <svg className="w-5 h-5 text-indigo-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
        </svg>
        <span className="text-sm font-medium text-gray-700 dark:text-gray-200 lg:text-indigo-600">Back to Home</span>
      </Link>

      {/* Left Side - Branding */}
      <div className="w-full lg:w-1/2 bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-500 relative overflow-hidden lg:min-h-screen">
        <div className="absolute inset-0 bg-black opacity-10"></div>
        <div className="relative z-10 flex flex-col justify-center items-center p-8 lg:p-12 text-white min-h-[300px] lg:min-h-screen">
          <img src={Logo} alt="PMT-SK" className="w-32 h-32 lg:w-48 lg:h-48 mb-4 lg:mb-8 object-contain" />
          <h1 className="text-3xl lg:text-5xl font-bold mb-3 lg:mb-6 text-center">
            Welcome Back to<br />PMT-SK
          </h1>
          <p className="text-base lg:text-xl text-center mb-4 lg:mb-8 opacity-90 max-w-md">
            Streamline your projects with real-time Slack integration, custom webhooks, and powerful task management.
          </p>
        </div>
      </div>

      {/* Right Side - Login Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          <div className="lg:hidden text-center mb-8">
            <img src={Logo} alt="PMT-SK" className="w-24 h-24 mx-auto mb-4 object-contain" />
            <h1 className="text-3xl font-bold text-gray-800 dark:text-gray-100">Welcome Back</h1>
          </div>

          <div className="hidden lg:block mb-8">
            <h2 className="text-3xl font-bold text-gray-800 dark:text-gray-100 mb-2">Sign In</h2>
            <p className="text-gray-600 dark:text-gray-300">Access your projects and teams</p>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg flex items-center gap-3">
              <p className="text-red-700 dark:text-red-300 text-sm">{error}</p>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-6">
            <div>
              <label htmlFor="login-identifier" className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-2">
                Email or Username
              </label>
              <input
                id="login-identifier"
                type="text"
                autoComplete="username"
                placeholder="Enter your email or username"
                value={identifier}
                onChange={(e) => setIdentifier(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 bg-white dark:bg-gray-700 rounded-xl focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
              />
            </div>

            <div>
              <div className="flex justify-between items-center mb-2">
                <label htmlFor="login-password" className="block text-sm font-medium text-gray-700 dark:text-gray-200">
                  Password
                </label>
                <button
                  type="button"
                  onClick={() => navigate("/forgot-password")}
                  className="text-sm text-indigo-600 hover:text-indigo-500 font-medium"
                >
                  Forgot password?
                </button>
              </div>
              <div className="relative">
                <input
                  id="login-password"
                  type={showPassword ? "text" : "password"}
                  autoComplete="current-password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
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

            <div className="flex items-center justify-between">
              <label className="flex items-center gap-2 cursor-pointer">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="w-4 h-4 text-indigo-600 border-gray-300 dark:border-gray-600 rounded focus:ring-indigo-500"
                />
                <span className="text-sm text-gray-700 dark:text-gray-200">Remember me</span>
              </label>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-xl hover:from-indigo-700 hover:to-purple-700 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 shadow-lg hover:shadow-xl"
            >
              {loading ? "Signing in..." : "Sign In"}
            </button>
          </form>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300 dark:border-gray-600"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-4 bg-gray-50 dark:bg-gray-900 text-gray-500 dark:text-gray-400">Or continue with</span>
              </div>
            </div>

            <button
              type="button"
              onClick={handleGithubLogin}
              className="mt-6 w-full py-3 px-4 bg-gray-800 text-white font-semibold rounded-xl hover:bg-gray-900 focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-all duration-300 flex items-center justify-center gap-2 shadow-lg hover:shadow-xl"
            >
              Continue with GitHub
            </button>
          </div>

          <p className="mt-8 text-center">
            <span className="text-gray-600 dark:text-gray-300">Don't have an account? </span>
            <Link to="/register" className="inline-flex items-center gap-1 text-indigo-600 hover:text-indigo-800 dark:hover:text-indigo-400 font-semibold bg-indigo-50 dark:bg-indigo-900/20 hover:bg-indigo-100 dark:hover:bg-indigo-900/40 px-4 py-2 rounded-lg transition-all">
              Sign Up
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </Link>
          </p>
        </div>
      </div>

      {/* 2FA Modal */}
      {show2FAModal && (
        <TwoFactorModal
          identifier={tempIdentifier}
          onSuccess={handle2FASuccess}
          onCancel={handle2FACancel}
        />
      )}
    </div>
  );
}