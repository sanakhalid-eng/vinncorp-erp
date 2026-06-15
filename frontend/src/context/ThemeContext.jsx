import { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';

const ThemeContext = createContext(undefined);

const THEME_KEY = 'pmt-sk-theme';

// Get system preference
const getSystemTheme = () => {
  if (typeof window === 'undefined') return 'light';
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

// Get initial theme
const getInitialTheme = () => {
  if (typeof window === 'undefined') return 'system';
  try {
    const saved = localStorage.getItem(THEME_KEY);
    return saved || 'system';
  } catch {
    return 'system';
  }
};

// Get effective theme (resolves 'system' to actual theme)
const getEffectiveTheme = (theme) => {
  if (theme === 'system') {
    return getSystemTheme();
  }
  return theme;
};

export const ThemeProvider = ({ children }) => {
  const [theme, setThemeState] = useState(getInitialTheme);

  // Apply theme to DOM
  const applyTheme = useCallback((newTheme) => {
    const root = window.document.documentElement;
    const effectiveTheme = getEffectiveTheme(newTheme);
    
    // Remove old class, add new
    root.classList.remove('light', 'dark');
    root.classList.add(effectiveTheme);
    
    // Save to localStorage
    try {
      localStorage.setItem(THEME_KEY, newTheme);
    } catch (e) {
      console.warn('Failed to save theme preference:', e);
    }
  }, []);

  // Set theme with optimization
  const setTheme = useCallback((newTheme) => {
    setThemeState(newTheme);
    applyTheme(newTheme);
  }, [applyTheme]);

  // Toggle between light/dark (skip system)
  const toggleTheme = useCallback(() => {
    const next = theme === 'dark' ? 'light' : 'dark';
    setTheme(next);
  }, [theme, setTheme]);

  // Get current effective theme
  const getCurrentTheme = useCallback(() => {
    return getEffectiveTheme(theme);
  }, [theme]);

  // Initialize theme on mount
  useEffect(() => {
    applyTheme(theme);
  }, []);

  // Listen for system theme changes
  useEffect(() => {
    if (theme !== 'system') return;

    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    
    const handleChange = () => {
      if (theme === 'system') {
        applyTheme('system');
      }
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, [theme, applyTheme]);

  // Context value (memoized to prevent re-renders)
  const value = useMemo(() => ({
    theme,
    setTheme,
    toggleTheme,
    getCurrentTheme,
    isDark: getCurrentTheme() === 'dark',
    isLight: getCurrentTheme() === 'light',
    isSystem: theme === 'system',
  }), [theme, setTheme, toggleTheme, getCurrentTheme]);

  return (
    <ThemeContext.Provider value={value}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
};
