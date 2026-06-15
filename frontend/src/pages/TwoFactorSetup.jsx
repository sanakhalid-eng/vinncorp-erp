import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  setupTwoFactor,
  verifyTwoFactorSetup,
  getTwoFactorStatus,
} from "../api/twoFactorApi";
import notify from "../lib/toast";
import { QrCode, Shield, Copy, CheckCircle, AlertTriangle } from "lucide-react";

// Helper to extract data from API response
const extractData = (res) => res?.data || res;

const TwoFactorSetup = () => {
  const [status, setStatus] = useState(null);
  const [setupData, setSetupData] = useState(null);
  const [verificationCode, setVerificationCode] = useState("");
  const [loading, setLoading] = useState(true);
  const [verifying, setVerifying] = useState(false);
  const [backupCodes, setBackupCodes] = useState([]);
  const [copied, setCopied] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    loadStatus();
  }, []);

  const loadStatus = async () => {
    try {
      const res = await getTwoFactorStatus();
      setStatus(res?.data);
    } catch (error) {
      console.error("Failed to load 2FA status:", error);
    } finally {
      setLoading(false);
    }
  };

  const handleSetup = async () => {
    try {
      setLoading(true);
      const res = await setupTwoFactor();

      // res = { success, message, data: { secretKey, qrCodeUrl, totpAuthUrl } }
      setSetupData(extractData(res));
    } catch (error) {
      notify.error(error.response?.data?.message || "Failed to setup 2FA");
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async () => {
    if (verificationCode.length !== 6) {
      notify.error("Please enter a 6-digit code");
      return;
    }

    try {
      setVerifying(true);
      const res = await verifyTwoFactorSetup(verificationCode);

      // res = { success, message, data: { backupCodes, message } }
      const data = extractData(res);
      setBackupCodes(data?.backupCodes || []);
      notify.success("Two-factor authentication enabled!");
    } catch (error) {
      notify.error(error.response?.data?.message || "Invalid verification code");
      setVerificationCode("");
    } finally {
      setVerifying(false);
    }
  };

  const copyBackupCodes = () => {
    navigator.clipboard.writeText(backupCodes.join("\n"));
    setCopied(true);
    notify.success("Backup codes copied to clipboard!");
    setTimeout(() => setCopied(false), 3000);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  // Show backup codes after successful setup
  if (backupCodes.length > 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="max-w-md w-full">
          <div className="bg-white rounded-2xl shadow-lg p-8">
            <div className="text-center mb-6">
              <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-gray-800">2FA Enabled!</h2>
              <p className="text-gray-600 mt-2">
                Save these backup codes in a safe place
              </p>
            </div>

            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
              <div className="flex items-center gap-2 mb-2">
                <AlertTriangle className="w-5 h-5 text-yellow-600" />
                <span className="font-medium text-yellow-800">Important!</span>
              </div>
              <p className="text-sm text-yellow-700">
                These codes can be used to access your account if you lose your
                authenticator device. Each code can only be used once.
              </p>
            </div>

            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <div className="grid grid-cols-2 gap-2 font-mono text-sm">
                {backupCodes.map((code, index) => (
                  <div key={index} className="p-2 bg-white rounded border">
                    {code}
                  </div>
                ))}
              </div>
            </div>

            <button
              onClick={copyBackupCodes}
              className="w-full py-3 px-4 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 flex items-center justify-center gap-2 mb-4"
            >
              {copied ? (
                <CheckCircle className="w-5 h-5" />
              ) : (
                <Copy className="w-5 h-5" />
              )}
              {copied ? "Copied!" : "Copy All Codes"}
            </button>

            <button
              onClick={() =>
                navigate("/user-home", { state: { twoFactorUpdated: true } })
              }
              className="w-full py-3 px-4 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200"
            >
              Done
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="max-w-md w-full">
        <div className="bg-white rounded-2xl shadow-lg p-8">
          <div className="text-center mb-6">
            <Shield className="w-16 h-16 text-indigo-600 mx-auto mb-4" />
            <h2 className="text-2xl font-bold text-gray-800">
              Two-Factor Authentication
            </h2>
            <p className="text-gray-600 mt-2">
              {status?.enabled
                ? "Manage your 2FA settings"
                : "Add an extra layer of security to your account"}
            </p>
          </div>

          {!status?.enabled && !setupData && (
            <div>
              <div className="bg-blue-50 rounded-lg p-4 mb-6">
                <h3 className="font-medium text-blue-800 mb-2">
                  How it works:
                </h3>
                <ol className="list-decimal list-inside text-sm text-blue-700 space-y-1">
                  <li>Click "Setup 2FA" below</li>
                  <li>
                    Scan the QR code with Google Authenticator or similar app
                  </li>
                  <li>Enter the 6-digit code to verify</li>
                  <li>Save your backup codes securely</li>
                </ol>
              </div>

              <button
                onClick={handleSetup}
                disabled={loading}
                className="w-full py-3 px-4 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50"
              >
                {loading ? "Setting up..." : "Setup 2FA"}
              </button>
            </div>
          )}

          {setupData && (
            <div>
              <div className="text-center mb-6">
                <img
                  src={setupData.qrCodeUrl}
                  alt="QR Code"
                  className="mx-auto mb-4"
                />
                <p className="text-sm text-gray-600 mb-2">
                  Scan this QR code with your authenticator app
                </p>
                <div className="bg-gray-50 rounded p-3 font-mono text-sm break-all">
                  <p className="text-gray-500 mb-1">
                    Or enter this secret manually:
                  </p>
                  <p className="font-medium">{setupData.secretKey}</p>
                </div>
              </div>

              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Enter verification code from your app
                </label>
                <input
                  type="text"
                  value={verificationCode}
                  onChange={(e) =>
                    setVerificationCode(
                      e.target.value.replace(/\D/g, "").slice(0, 6),
                    )
                  }
                  placeholder="000000"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg text-center text-2xl font-mono tracking-widest focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                  maxLength={6}
                />
              </div>

              <button
                onClick={handleVerify}
                disabled={verifying || verificationCode.length !== 6}
                className="w-full py-3 px-4 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 disabled:opacity-50"
              >
                {verifying ? "Verifying..." : "Verify & Enable"}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default TwoFactorSetup;
