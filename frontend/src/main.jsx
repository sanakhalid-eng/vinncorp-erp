import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import "./index.css";
import { AuthProvider } from "./context/AuthContext.jsx";
import { ProjectPermissionProvider } from "./context/ProjectPermissionContext.jsx";
import { ThemeProvider } from "./context/ThemeContext.jsx";
import { Toaster } from "sonner";

const Root = () => {
  return (
    <ThemeProvider>
      <AuthProvider>
        <ProjectPermissionProvider>
          <App />
          <Toaster position="top-right" richColors closeButton />
        </ProjectPermissionProvider>
      </AuthProvider>
    </ThemeProvider>
  );
};

ReactDOM.createRoot(document.getElementById("root")).render(<Root />);
