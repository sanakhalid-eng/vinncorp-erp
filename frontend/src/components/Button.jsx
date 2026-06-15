export default function Button({
  children,
  type = "button",
  onClick,
  className = "",
  disabled = false,
}) {
  const base =
    "px-4 py-2.5 rounded-lg font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-60";
  const styles = {
    primary:
      "bg-blue-600 hover:bg-blue-700 text-white active:bg-blue-800 focus:ring-offset-0 focus:ring-2 focus:ring-blue-200/50",
    secondary:
      "bg-gray-100 text-gray-800 hover:bg-gray-200 focus:ring-2 focus:ring-gray-300",
  };
  const VALID_HTML_TYPES = ["button", "submit", "reset"];
  const htmlType = VALID_HTML_TYPES.includes(type) ? type : "button";
  const variant = VALID_HTML_TYPES.includes(type)
    ? "primary"
    : styles[type]
      ? type
      : "primary";
  return (
    <button
      type={htmlType}
      onClick={onClick}
      disabled={disabled}
      className={`${base} ${styles[variant] || styles.primary} ${className}`}
    >
       
      {children} 
    </button>
  );
}
