import { Check } from "lucide-react";

const STEPS = [
  { key: "plan", label: "Select Plan" },
  { key: "summary", label: "Summary" },
  { key: "payment", label: "Payment" },
  { key: "details", label: "Details" },
  { key: "proof", label: "Upload Proof" },
  { key: "review", label: "Review" },
  { key: "submitted", label: "Submitted" },
];

export default function StepIndicator({ currentStep }) {
  const currentIndex = STEPS.findIndex((s) => s.key === currentStep);

  return (
    <div className="w-full mb-8">
      <div className="flex items-center justify-between">
        {STEPS.slice(0, 6).map((step, idx) => {
          const isCompleted = idx < currentIndex;
          const isCurrent = idx === currentIndex;
          const isUpcoming = idx > currentIndex;

          return (
            <div key={step.key} className="flex items-center flex-1">
              <div className="flex flex-col items-center">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold transition-all duration-300 ${
                    isCompleted
                      ? "bg-green-500 text-white"
                      : isCurrent
                        ? "bg-indigo-600 text-white ring-4 ring-indigo-200"
                        : "bg-gray-200 dark:bg-gray-700 text-gray-500 dark:text-gray-400"
                  }`}
                >
                  {isCompleted ? (
                    <Check className="w-5 h-5" />
                  ) : (
                    idx + 1
                  )}
                </div>
                <span
                  className={`text-xs mt-1.5 hidden md:block font-medium ${
                    isCurrent
                      ? "text-indigo-600 dark:text-indigo-400"
                      : isCompleted
                        ? "text-green-600 dark:text-green-400"
                        : "text-gray-400 dark:text-gray-500"
                  }`}
                >
                  {step.label}
                </span>
              </div>
              {idx < STEPS.length - 2 && (
                <div
                  className={`flex-1 h-0.5 mx-2 ${
                    isCompleted
                      ? "bg-green-500"
                      : isCurrent
                        ? "bg-gradient-to-r from-indigo-600 to-gray-300"
                        : "bg-gray-200 dark:bg-gray-700"
                  }`}
                />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export { STEPS };
