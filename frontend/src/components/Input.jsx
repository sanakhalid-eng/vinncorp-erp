import { forwardRef, useId } from "react";
const Input = forwardRef(
  (
    {
      label,
      type = "text",
      placeholder,
      className,
      error,
      autoComplete,
      id: propId,
      ...props
    },
    ref,
  ) => {
    const generatedId = useId();
    const id = propId || generatedId;
    const autocompleteMap = {
      email: "email",
      password: "current-password",
      "new-password": "new-password",
      name: "name",
      tel: "tel",
      username: "username",
    };
    return (
      <div className="flex flex-col">
         
        {label && (
          <label htmlFor={id} className="mb-1 text-gray-700 font-medium">
             
            {label} 
          </label>
        )}
        <input
          ref={ref}
          id={id}
          type={type}
          placeholder={placeholder}
          autoComplete={autocompleteMap[type] || autoComplete || "off"}
          className={`h-12 p-4 rounded-lg border border-gray-300 focus:border-blue-500 focus:ring-1 focus:ring-blue-300 transition-all bg-white ${className || ""} ${error ? "border-red-500" : ""}`}
          {...props}
        /> 
        {error && <p className="mt-1 text-sm text-red-500">{error}</p>} 
      </div>
    );
  },
);
Input.displayName = "Input";
export default Input;
