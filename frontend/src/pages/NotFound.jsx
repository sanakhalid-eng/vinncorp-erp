import { useNavigate, useLocation } from "react-router-dom";
import { ArrowLeft, AlertTriangle, Home } from "lucide-react";
import Button from "../components/Button";
import { useAuth } from "../context/useAuth.js";
import Navbar from "../components/Navbar";
const NotFound = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-200">
       
      <Navbar /> 
      <div className="container mx-auto px-4 py-12 max-w-4xl flex items-center justify-center min-h-[60vh]">
         
        <div className="max-w-md w-full text-center space-y-8">
           
          {/* Icon */} 
          <div className="w-32 h-32 mx-auto bg-gradient-to-br from-orange-400 to-red-500 rounded-3xl flex items-center justify-center shadow-2xl">
             
            <AlertTriangle className="w-20 h-20 text-white" /> 
          </div> 
          {/* Title */} 
          <div className="space-y-4">
             
            <h1 className="text-6xl md:text-7xl font-black bg-gradient-to-r from-gray-900 to-gray-700 bg-clip-text text-transparent">
               
              404 
            </h1> 
            <h2 className="text-2xl md:text-3xl font-bold text-gray-900">
               
              Page Not Found 
            </h2> 
            <p className="text-xl text-gray-600 max-w-md mx-auto leading-relaxed">
               
              Sorry, the page 
              <span className="font-mono bg-gray-100 px-2 py-1 rounded text-sm font-bold text-gray-900">
                {location.pathname}
              </span> 
              doesn't exist. 
            </p> 
          </div> 
          {/* Actions */} 
          {token ? (
            <div className="flex flex-col sm:flex-row gap-4 justify-center pt-8">
               
              <Button
                onClick={() => navigate("/user-home")}
                className="flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 shadow-xl"
              >
                 
                <Home className="w-5 h-5" /> Go to Home 
              </Button> 
              <Button
                variant="outline"
                onClick={() => navigate(-1)}
                className="flex items-center gap-2"
              >
                 
                <ArrowLeft className="w-5 h-5" /> Go Back 
              </Button> 
            </div>
          ) : (
            <div className="flex flex-col sm:flex-row gap-4 justify-center pt-8">
               
              <Button
                onClick={() => navigate("/")}
                className="flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 shadow-xl"
              >
                 
                <Home className="w-5 h-5" /> Home 
              </Button> 
              <Button
                onClick={() => navigate("/login")}
                className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-700 text-white shadow-xl"
              >
                 
                Login 
              </Button> 
              <Button
                onClick={() => navigate("/register")}
                variant="outline"
                className="flex items-center gap-2"
              >
                 
                Sign Up 
              </Button> 
            </div>
          )} 
          <div className="text-sm text-gray-500 pt-8 border-t border-gray-200">
             
            <p>Looking for something specific?</p> 
            {token ? (
              <p className="text-xs mt-1">
                 
                Projects • Tasks • Users • Roles • Members • Workflow 
              </p>
            ) : (
              <p className="text-xs mt-1"> Login • Register • Home </p>
            )} 
          </div> 
        </div> 
      </div> 
    </div>
  );
};
export default NotFound;
