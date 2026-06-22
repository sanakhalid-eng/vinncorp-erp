import { useState, useEffect } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import { useAuth } from "../../../context/useAuth.js";
import api from "../../../api/axios";
import { acceptInvitation } from "../api/invitationApi";
import notify from "../../../lib/toast";
import Logo from "../../../assets/Logo - PMT-SK.png";

const VerifyEmail = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [code, setCode] = useState(["", "", "", "", "", ""]);
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [resendLoading, setResendLoading] = useState(false);
  const [timeLeft, setTimeLeft] = useState(0);
  const [animate, setAnimate] = useState(false);

  useEffect(() => {
    const emailFromUrl = searchParams.get("email");

    if (emailFromUrl) {
      setEmail(emailFromUrl);
    } else {
      notify.error("No email provided");
      setTimeout(() => navigate("/register"), 2000);
    }
  }, [searchParams, navigate]);

  useEffect(() => {
    setTimeout(() => setAnimate(true), 100);
  }, []);

  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft]);

  const handleCodeChange = (index, value) => {
    if (value.length > 1) {
      const pastedCode = value.replace(/\D/g, "").slice(0, 6).split("");
      const newCode = [...code];
      pastedCode.forEach((char, i) => {
        if (i < 6) newCode[i] = char;
      });
      setCode(newCode);
      const lastFilledIndex = Math.min(pastedCode.length - 1, 5);
      document.getElementById(`code-${lastFilledIndex}`)?.focus();
    } else if (value.match(/^\d*$/)) {
      const newCode = [...code];
      newCode[index] = value;
      setCode(newCode);
      if (value && index < 5) {
        document.getElementById(`code-${index + 1}`)?.focus();
      }
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      document.getElementById(`code-${index - 1}`)?.focus();
    }
  };

  const handleVerify = async () => {
    const verificationCode = code.join("");
    if (verificationCode.length !== 6) {
      notify.error("Please enter all 6 digits");
      return;
    }

    setLoading(true);
    try {
      const response = await api.post("/auth/verify-email", {
        code: verificationCode,
        email: email,
      });

      notify.success("Email verified successfully!");

      const accessToken = response.data.data.accessToken;
      const refreshToken = response.data.data.refreshToken;
      if (refreshToken) {
        localStorage.setItem("refreshToken", refreshToken);
      }
      login(accessToken);

      // Check for pending invitation recovery

      const pendingInvite = localStorage.getItem("pendingInviteToken");
      if (pendingInvite) {
        try {
          await acceptInvitation(pendingInvite);
          localStorage.removeItem("pendingInviteToken");
          notify.success("Invitation accepted! Redirecting...");
          setTimeout(() => navigate("/projects", { replace: true }), 500);
          return;
        } catch {
          localStorage.removeItem("pendingInviteToken");
        }
      }

      setTimeout(() => navigate("/user-home", { replace: true }), 500);
    } catch (error) {
      notify.error(error.response?.data?.message || "Invalid verification code");
      setCode(["", "", "", "", "", ""]);
      document.getElementById("code-0")?.focus();
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (timeLeft > 0) return;

    setResendLoading(true);
    try {
      await api.post("/auth/resend-verification", { email });
      notify.success("New verification code sent!");
      setTimeLeft(60);
      setCode(["", "", "", "", "", ""]);
    } catch (error) {
      notify.error("Failed to resend code");
    } finally {
      setResendLoading(false);
    }
  };

  if (!animate) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-pulse space-y-4 w-full max-w-md px-8">
          <div className="h-12 bg-gray-200 rounded-lg"></div>
          <div className="h-12 bg-gray-200 rounded-lg"></div>
          <div className="h-12 bg-gray-200 rounded-lg"></div>
          <div className="h-10 bg-gray-300 rounded-lg w-1/2 mx-auto"></div>
        </div>
      </div>
    );
  }

  return (
    <div
      className={`min-h-screen bg-gray-50 flex flex-col lg:flex-row transition-all duration-700 ${animate ? "opacity-100" : "opacity-0"}`}
    >
      {/* Back to Home Link */}
      <button
        onClick={() => navigate("/")}
        className="absolute top-4 left-4 z-20 flex items-center gap-2 bg-white/90 lg:bg-transparent backdrop-blur-sm lg:backdrop-blur-none px-3 py-2 rounded-lg shadow-md lg:shadow-none hover:bg-white lg:hover:bg-indigo-50 transition-all group"
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
        <span className="text-sm font-medium text-gray-700 lg:text-indigo-600">
          Back to Home
        </span>
      </button>

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
            Verify Your Email
          </h1>
          <p className="text-base lg:text-xl text-center mb-4 lg:mb-8 opacity-90 max-w-md">
            We've sent a 6-digit verification code to your email. Please enter
            it below to complete your registration.
          </p>
          <div className="space-y-4 text-lg">
            <div className="flex items-center gap-3">
              <svg
                className="w-6 h-6"
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
              <span>Secure Verification</span>
            </div>
            <div className="flex items-center gap-3">
              <svg
                className="w-6 h-6"
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
              <span>Check Your Email</span>
            </div>
          </div>
        </div>
      </div>

      {/* Right Side - Verification Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8">
        <div className="w-full max-w-md">
          <div className="lg:hidden text-center mb-8">
            <h1 className="text-3xl font-bold text-gray-800">
              Verify Your Email
            </h1>
          </div>

          <div className="bg-white p-8 rounded-2xl shadow-lg">
            <h2 className="text-2xl font-bold text-gray-800 mb-2">
              Enter Verification Code
            </h2>
            <p className="text-gray-600 mb-8">
              We sent a code to 
              <span className="font-semibold">{email || "your email"}</span>
            </p>

            {/* Code Input */}
            <div className="flex gap-2 justify-center mb-8">
              {code.map((digit, index) => (
                <input
                  key={index}
                  id={`code-${index}`}
                  type="text"
                  inputMode="numeric"
                  maxLength={6}
                  value={digit}
                  onChange={(e) => handleCodeChange(index, e.target.value)}
                  onKeyDown={(e) => handleKeyDown(index, e)}
                  className="w-12 h-14 text-center text-2xl font-bold border-2 border-gray-300 rounded-xl focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200 transition-all"
                  autoFocus={index === 0}
                />
              ))}
            </div>

            <button
              onClick={handleVerify}
              disabled={loading || code.join("").length !== 6}
              className="w-full py-3 px-4 bg-gradient-to-r from-indigo-600 to-purple-600 text-white font-semibold rounded-xl hover:from-indigo-700 hover:to-purple-700 focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 shadow-lg hover:shadow-xl mb-6"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin h-5 w-5" viewBox="0 0 24 24">
                    <circle
                      className="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      strokeWidth="4"
                      fill="none"
                    />
                    <path
                      className="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                  </svg>
                  Verifying...
                </span>
              ) : (
                "Verify Email"
              )}
            </button>

            {/* Resend Code */}
            <div className="text-center">
              <p className="text-sm text-gray-600 mb-2">
                Didn't receive the code?
              </p>
              <button
                onClick={handleResend}
                disabled={resendLoading || timeLeft > 0}
                className="text-indigo-600 hover:text-indigo-500 font-medium text-sm disabled:text-gray-400 disabled:cursor-not-allowed"
              >
                {timeLeft > 0
                  ? `Resend in ${timeLeft}s`
                  : resendLoading
                    ? "Sending..."
                    : "Resend Code"}
              </button>
            </div>
          </div>

          <p className="mt-6 text-center text-gray-500 text-sm">
            <button
              onClick={() => navigate("/login")}
              className="text-indigo-600 hover:underline font-medium"
            >
              ΓåÉ Back to Sign In
            </button>
          </p>
        </div>
      </div>
    </div>
  );
};

export default VerifyEmail;
