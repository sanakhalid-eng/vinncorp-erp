import { useState } from "react";
import { validateTwoFactor } from "../api/twoFactorApi";
import notify from "../../../lib/toast";
import { Shield, Loader } from "lucide-react";

const TwoFactorModal = ({ identifier, onSuccess, onCancel }) => {
  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);

  const handleVerify = async () => {
    if (code.length !== 6) {
      notify.error("Please enter a 6-digit code");
      return;
    }

    try {
      setLoading(true);
      const res = await validateTwoFactor(identifier, code);
      notify.success("2FA verification successful!");
      onSuccess(res?.data?.data);
    } catch (error) {
      notify.error(error.response?.data?.message || "Invalid 2FA code");
      setCode("");
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (index, value) => {
    // Handle paste

    if (value.length > 1) {
      const pastedCode = value.replace(/\D/g, "").slice(0, 6);

      // In a real implementation, you'd want to split and set each digit
      setCode(pastedCode);
      return;
    }

    if (value.match(/^\d*$/)) {
      const newCode = code.split("");
      newCode[index] = value;
      const newCodeStr = newCode.join("");
      setCode(newCodeStr);

      // Auto-focus next input

      if (value && index < 5) {
        document.getElementById(`2fa-${index + 1}`)?.focus();
      }
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === "Backspace" && !code[index] && index > 0) {
      document.getElementById(`2fa-${index - 1}`)?.focus();
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4">
      <div className="bg-white rounded-2xl shadow-2xl p-8 max-w-md w-full">
        <div className="text-center mb-6">
          <Shield className="w-16 h-16 text-indigo-600 mx-auto mb-4" />
          <h2 className="text-2xl font-bold text-gray-800">
            Two-Factor Authentication
          </h2>
          <p className="text-gray-600 mt-2">
            Enter the 6-digit code from your authenticator app
          </p>
        </div>

        <div className="flex gap-2 justify-center mb-6">
          {[0, 1, 2, 3, 4, 5].map((index) => (
            <input
              key={index}
              id={`2fa-${index}`}
              type="text"
              inputMode="numeric"
              maxLength={6}
              value={code[index] || ""}
              onChange={(e) => handleChange(index, e.target.value)}
              onKeyDown={(e) => handleKeyDown(index, e)}
              className="w-12 h-14 text-center text-2xl font-bold border-2 border-gray-300 rounded-xl focus:border-indigo-500 focus:ring-2 focus:ring-indigo-200"
              autoFocus={index === 0}
            />
          ))}
        </div>

        <div className="space-y-3">
          <button
            onClick={handleVerify}
            disabled={loading || code.length !== 6}
            className="w-full py-3 px-4 bg-indigo-600 text-white font-semibold rounded-xl hover:bg-indigo-700 disabled:opacity-50 flex items-center justify-center gap-2"
          >
            {loading ? (
              <>
                <Loader className="w-5 h-5 animate-spin" />
                Verifying...
              </>
            ) : (
              "Verify"
            )}
          </button>

          <button
            onClick={onCancel}
            className="w-full py-3 px-4 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200"
          >
            Cancel
          </button>
        </div>

        <p className="text-center text-sm text-gray-500 mt-4">
          Don't have access to your authenticator? 
          <button className="text-indigo-600 hover:underline">
            Use a backup code
          </button>
        </p>
      </div>
    </div>
  );
};

export default TwoFactorModal;
