import { AlertCircle, RefreshCw, WifiOff, ShieldX, SearchX, ServerCrash } from "lucide-react";
import Button from "./Button";
import { parseApiError } from "../utils/apiError";

const ICONS = {
  network: WifiOff,
  timeout: WifiOff,
  forbidden: ShieldX,
  unauthorized: ShieldX,
  notFound: SearchX,
  validation: AlertCircle,
  server: ServerCrash,
  unknown: AlertCircle,
};

export default function ErrorState({
  title,
  message,
  error,
  type,
  onRetry,
}) {
  const parsed = error ? parseApiError(error) : null;
  const resolvedType = type || parsed?.type || "unknown";
  const Icon = ICONS[resolvedType] || AlertCircle;
  const resolvedTitle = title || parsed?.title || "Something went wrong";
  const resolvedMessage =
    message || parsed?.message || "An unexpected error occurred. Please try again.";

  return (
    <div className="flex flex-col items-center justify-center py-20 text-center px-4">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-red-50 mb-4">
        <Icon className="h-8 w-8 text-red-500" />
      </div>
      <h3 className="text-lg font-semibold text-slate-700 mb-1">{resolvedTitle}</h3>
      <p className="text-sm text-slate-400 mb-6 max-w-sm">{resolvedMessage}</p>
      {onRetry && (
        <Button
          onClick={onRetry}
          className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
        >
          <RefreshCw className="w-4 h-4 mr-1.5 inline" /> Try Again
        </Button>
      )}
    </div>
  );
}
