import { useState, useEffect } from "react";
import { useTheme } from "../context/ThemeContext";
import { Sun, Moon } from "lucide-react";
const ThemeToggle = ({ className = "" }) => {
  const { theme, setTheme, toggleTheme, getCurrentTheme } = useTheme();
  const [mounted, setMounted] = useState(false);
  useEffect(() => {
    setMounted(true);
  }, []);
  if (!mounted) {
    return <div className={`w-10 h-10 ${className}`} />;
  }
  const isDark = getCurrentTheme() === "dark";
  return (
    <button
      onClick={toggleTheme}
      className={`p-2 rounded-lg transition-colors duration-200         hover:bg-gray-100 dark:hover:bg-gray-700         text-gray-700 dark:text-gray-200 ${className}`}
      aria-label={`Switch to ${isDark ? "light" : "dark"} mode`}
      title={`Switch to ${isDark ? "light" : "dark"} mode`}
    >
       
      {isDark ? (
        <Sun className="w-5 h-5 transition-transform duration-200 hover:scale-110" />
      ) : (
        <Moon className="w-5 h-5 transition-transform duration-200 hover:scale-110" />
      )} 
    </button>
  );
};
export default ThemeToggle;
