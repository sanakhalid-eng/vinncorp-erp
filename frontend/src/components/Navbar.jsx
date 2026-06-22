import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth.js";
import ThemeToggle from "../features/settings/components/ThemeToggle";
import Logo from "../assets/Logo - PMT-SK.png";
import { User, ChevronDown, LogOut, Activity, Settings } from "lucide-react";
const getUserAvatar = (user) => {
  return user?.avatarUrl || user?.avatar;
};
const getUserInitials = (name) => {
  return name ? name.charAt(0).toUpperCase() : "U";
};
const Navbar = () => {
  const { token, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const { user } = useAuth();
  const userName = user?.name || localStorage.getItem("userName") || "User";
  const userInitial = getUserInitials(userName);
  const handleLogout = () => {
    logout();
    navigate("/");
    setMobileOpen(false);
    setDropdownOpen(false);
  };
  return (
    <nav className="sticky top-0 z-50 transition-all duration-300 bg-white/95 backdrop-blur-md shadow-lg border-b border-gray-200/50">
       
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
         
        <div className="flex justify-between items-center h-16 lg:h-20">
           
          <Link to="/" className="flex items-center space-x-2 group">
             
            <img
              src={Logo}
              alt="PMT-SK"
              className="h-10 lg:h-12 object-contain group-hover:scale-105 transition-transform duration-200"
            /> 
            <span className="font-bold text-xl lg:text-2xl bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
               
              PMT-SK 
            </span> 
          </Link> 
          <div className="hidden md:flex items-center space-x-8">
             
            <Link
              to={token ? "/user-home" : "/"}
              className="text-lg font-medium text-gray-700 hover:text-indigo-600 transition-colors px-3 py-2 rounded-lg hover:bg-indigo-50"
              onClick={(e) => {
                e.preventDefault();
                if (token) {
                  navigate("/user-home");
                } else {
                  window.scrollTo({ top: 0, behavior: "smooth" });
                }
              }}
            >
               
              Home 
            </Link> 
            <Link
              to="/features"
              className="text-lg font-medium text-gray-700 hover:text-indigo-600 transition-colors px-3 py-2 rounded-lg hover:bg-indigo-50"
            >
               
              Features 
            </Link> 
            <Link
              to="/manage"
              className="text-lg font-medium text-gray-700 hover:text-indigo-600 transition-colors px-3 py-2 rounded-lg hover:bg-indigo-50"
            >

              Features
            </Link>
            <Link
              to="/contact-us"
              className="text-lg font-medium text-gray-700 hover:text-indigo-600 transition-colors px-3 py-2 rounded-lg hover:bg-indigo-50"
            >
               
              Contact 
            </Link> 
            {token ? (
              <>
                 
                {/* Advanced Theme Toggle */} <ThemeToggle className="mr-2" /> 
                <div className="relative">
                   
                  <button
                    onClick={() => setDropdownOpen(!dropdownOpen)}
                    className="flex items-center space-x-2 text-gray-700 hover:text-gray-900 transition-colors px-2 py-2 rounded-lg hover:bg-gray-100"
                  >
                     
                    <div className="w-10 h-10 rounded-full shadow-lg overflow-hidden">
                       
                      {getUserAvatar(user) ? (
                        <img
                          src={getUserAvatar(user)}
                          alt="Profile"
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            e.target.style.display = "none";
                          }}
                        />
                      ) : (
                        <div className="w-full h-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-semibold text-sm">
                           
                          {userInitial} 
                        </div>
                      )} 
                    </div> 
                    <ChevronDown className="w-4 h-4 transition-transform rotate-0" /> 
                  </button> 
                  {dropdownOpen && (
                    <div className="absolute right-0 mt-2 w-48 bg-white rounded-2xl shadow-2xl border border-gray-200 py-1 z-50">
                       
                      <div className="px-4 py-3 border-b border-gray-100">
                         
                        <p className="font-semibold text-gray-900">
                          {userName}
                        </p> 
                        <p className="text-sm text-gray-500">
                          {user?.role || "Pro User"}
                        </p> 
                      </div> 
                      <Link
                        to="/profile"
                        className="flex items-center px-4 py-3 text-gray-700 hover:bg-gray-50 rounded-lg transition-colors w-full"
                        onClick={() => setDropdownOpen(false)}
                      >
                         
                        <User className="w-4 h-4 mr-3" /> Profile 
                      </Link> 
                      <Link
                        to="/user-home"
                        className="flex items-center px-4 py-3 text-gray-700 hover:bg-gray-50 rounded-lg transition-colors w-full"
                        onClick={() => setDropdownOpen(false)}
                      >
                         
                        <Activity className="w-4 h-4 mr-3" /> My Home 
                      </Link> 
                      <Link
                        to="/settings"
                        className="flex items-center px-4 py-3 text-gray-700 hover:bg-gray-50 rounded-lg transition-colors w-full"
                        onClick={() => setDropdownOpen(false)}
                      >
                         
                        <Settings className="w-4 h-4 mr-3" /> Settings 
                      </Link> 
                      <button
                        onClick={handleLogout}
                        className="flex items-center px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg transition-colors w-full"
                      >
                         
                        <LogOut className="w-4 h-4 mr-3" /> Logout 
                      </button> 
                    </div>
                  )} 
                </div> 
              </>
            ) : (
              <>
                 
                <Link
                  to="/login"
                  className="text-lg font-medium text-gray-700 hover:text-indigo-600 transition-colors px-3 py-2 rounded-lg hover:bg-indigo-50"
                >
                   
                  Login 
                </Link> 
                <Link
                  to="/register"
                  className="bg-gradient-to-r from-indigo-600 to-purple-600 text-white px-6 py-2 lg:py-3 rounded-xl font-semibold text-lg hover:from-indigo-700 hover:to-purple-700 hover:shadow-lg transition-all duration-300 hover:scale-105"
                >
                   
                  Get Started 
                </Link> 
              </>
            )} 
          </div> 
          <div className="md:hidden">
             
            <button
              onClick={() => setMobileOpen(!mobileOpen)}
              className="p-2 rounded-lg hover:bg-gray-100 transition-colors"
              aria-label="Toggle menu"
            >
               
              <svg
                className="h-6 w-6 text-gray-800"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                 
                {mobileOpen ? (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M6 18L18 6M6 6l12 12"
                  />
                ) : (
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M4 6h16M4 12h16M4 18h16"
                  />
                )} 
              </svg> 
            </button> 
          </div> 
        </div> 
        {mobileOpen && (
          <div className="md:hidden pb-4 border-t border-gray-200">
             
            <div className="px-2 pt-2 pb-3 space-y-1 sm:px-3 bg-white/50 backdrop-blur-sm">
               
              <Link
                to={token ? "/user-home" : "/"}
                className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg"
                onClick={() => {
                  setMobileOpen(false);
                  if (token) {
                    navigate("/user-home");
                  } else {
                    window.scrollTo({ top: 0, behavior: "smooth" });
                  }
                }}
              >
                 
                Home 
              </Link> 
              <Link
                to="/features"
                className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg"
                onClick={() => setMobileOpen(false)}
              >
                 
                Features 
              </Link> 
              <Link
                to="/manage"
                className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg"
                onClick={() => setMobileOpen(false)}
              >

                Features
              </Link>
              <Link
                to="/contact-us"
                className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg"
                onClick={() => setMobileOpen(false)}
              >
                 
                Contact 
              </Link> 
              {token ? (
                <>
                   
                  <div className="pt-2">
                     
                    <div className="flex items-center space-x-3 px-3 py-2 bg-indigo-50 rounded-lg mb-2">
                       
                      <div className="w-10 h-10 rounded-full shadow-lg overflow-hidden">
                         
                        {getUserAvatar(user) ? (
                          <img
                            src={getUserAvatar(user)}
                            alt="Profile"
                            className="w-full h-full object-cover"
                            onError={(e) => {
                              e.target.style.display = "none";
                            }}
                          />
                        ) : (
                          <div className="w-full h-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-semibold text-sm">
                             
                            {userInitial} 
                          </div>
                        )} 
                      </div> 
                      <div>
                         
                        <p className="font-semibold text-gray-900 text-sm">
                          {userName}
                        </p> 
                        <p className="text-xs text-gray-500">Pro User</p> 
                      </div> 
                    </div> 
                    <Link
                      to="/profile"
                      className="pl-11 pr-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-lg flex items-center"
                      onClick={() => setMobileOpen(false)}
                    >
                       
                      Profile 
                    </Link> 
                    <Link
                      to="/user-home"
                      className="pl-11 pr-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-lg flex items-center"
                      onClick={() => setMobileOpen(false)}
                    >
                       
                      My Home 
                    </Link> 
                    <Link
                      to="/settings"
                      className="pl-11 pr-3 py-2 text-sm text-gray-700 hover:bg-gray-100 rounded-lg flex items-center"
                      onClick={() => setMobileOpen(false)}
                    >
                       
                      Settings 
                    </Link> 
                    <button
                      onClick={handleLogout}
                      className="block w-full text-left pl-11 pr-3 py-2 text-sm text-red-600 hover:bg-red-50 rounded-lg"
                    >
                       
                      Logout 
                    </button> 
                  </div> 
                </>
              ) : (
                <>
                   
                  <Link
                    to="/login"
                    className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg"
                    onClick={() => setMobileOpen(false)}
                  >
                     
                    Login 
                  </Link> 
                  <Link
                    to="/register"
                    className="block w-full text-center bg-gradient-to-r from-indigo-600 to-purple-600 text-white px-3 py-2 rounded-xl font-semibold text-base hover:from-indigo-700 hover:to-purple-700 transition-all duration-300"
                    onClick={() => setMobileOpen(false)}
                  >
                     
                    Get Started 
                  </Link> 
                </>
              )} 
            </div> 
          </div>
        )} 
      </div> 
    </nav>
  );
};
export default Navbar;
