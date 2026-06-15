const Footer = () => {
  return (
    <footer className="bg-gradient-to-r from-gray-900 to-gray-800 text-white">
       
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
         
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 mb-12">
           
          {/* Logo & Description */} 
          <div className="lg:col-span-1">
             
            <div className="flex items-center space-x-3 mb-6">
               
              <div className="w-12 h-12 bg-gradient-to-r from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg">
                 
                <svg
                  className="w-7 h-7 text-white"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                   
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M7 7h.01M7 3h8c.621 0 1.129.504 1.09 1.124a1.002 1.002 0 01-.322.854l-1.697 1.697a.997.997 0 01-.854.263A1.002 1.002 0 0110 6h-2a1 1 0 01-1-1V3zM19 7h-2a1 1 0 01-1-1V3a1 1 0 011-1h1.378a1 1 0 01.854.263l1.697 1.697A1.002 1.002 0 0121 4.876V7a1 1 0 01-2 0zM9 13a3 3 0 11-6 0 3 3 0 016 0zM17 13a3 3 0 11-6 0 3 3 0 016 0zM12 3a9 9 0 00-9 9 9 9 0 0018 0 9 9 0 00-9-9z"
                  /> 
                </svg> 
              </div> 
              <div>
                 
                <h3 className="text-2xl font-bold bg-gradient-to-r from-white to-gray-200 bg-clip-text text-transparent">
                   
                  PMT-SK 
                </h3> 
                <p className="text-gray-400 text-sm mt-1">
                   
                  Streamline your project management 
                </p> 
              </div> 
            </div> 
            <p className="text-gray-400 leading-relaxed">
               
              Powerful tools to manage projects, tasks, and teams efficiently.
              Collaborate seamlessly and deliver results on time. 
            </p> 
          </div> 
          {/* Quick Links */} 
          <div>
             
            <h4 className="text-lg font-semibold text-white mb-6 tracking-wide">
              Quick Links
            </h4> 
            <ul className="space-y-3">
               
              <li>
                <a
                  href="/"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Home
                </a>
              </li> 
              <li>
                <a
                  href="/features"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Features
                </a>
              </li> 
              <li>
                <a
                  href="/manage"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Pricing
                </a>
              </li> 
              <li>
                <a
                  href="/contact-us"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Contact
                </a>
              </li> 
              <li>
                <a
                  href="/login"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Login
                </a>
              </li> 
              <li>
                <a
                  href="/register"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Register
                </a>
              </li> 
            </ul> 
          </div> 
          {/* Product */} 
          <div>
             
            <h4 className="text-lg font-semibold text-white mb-6 tracking-wide">
              Product
            </h4> 
            <ul className="space-y-3">
               
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Projects
                </a>
              </li> 
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Tasks
                </a>
              </li> 
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Team Management
                </a>
              </li> 
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Reports
                </a>
              </li> 
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  API
                </a>
              </li> 
            </ul> 
          </div> 
          {/* Company */} 
          <div>
             
            <h4 className="text-lg font-semibold text-white mb-6 tracking-wide">
              Company
            </h4> 
            <ul className="space-y-3">
               
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  About
                </a>
              </li> 
              <li>
                <a
                  href="#"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Careers
                </a>
              </li> 
              <li>
                <a
                  href="/privacy"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Privacy
                </a>
              </li> 
              <li>
                <a
                  href="/terms"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Terms
                </a>
              </li> 
              <li>
                <a
                  href="/contact-us"
                  className="text-gray-400 hover:text-white transition-colors duration-300 hover:translate-x-1 block"
                >
                  Contact
                </a>
              </li> 
            </ul> 
          </div> 
        </div> 
        {/* Bottom Bar */} 
        <div className="border-t border-gray-800 pt-8 flex flex-col md:flex-row justify-between items-center gap-4">
           
          <p className="text-gray-400 text-sm md:text-base">
             
            © 2024 PMT-SK. All rights reserved. 
          </p> 
          <div className="flex space-x-6">
             
            <a
              href="#"
              className="w-10 h-10 bg-gray-800/50 hover:bg-white/20 rounded-xl flex items-center justify-center transition-all duration-300 hover:scale-110 group"
            >
               
              <svg
                className="w-5 h-5 text-gray-400 group-hover:text-white"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path d="M24 4.557c-.883.392-1.832.656-2.828.775 1.017-.609 1.798-1.574 2.165-2.724-.951.564-2.005.974-3.127 1.195-.897-.957-2.178-1.555-3.594-1.555-3.179 0-5.515 2.966-4.797 6.045-4.091-.205-7.719-2.165-10.148-5.144-1.29 2.213-.669 5.108 1.523 6.574-.806-.026-1.566-.247-2.229-.616-.054 2.281 1.581 4.415 3.949 4.89-.693.188-1.452.232-2.224.084.626 1.956 2.444 3.379 4.6 3.419-2.07 1.623-4.678 2.348-7.29 2.04 2.179 1.397 4.768 2.212 7.548 2.212 9.142 0 14.307-7.721 13.995-14.646.962-.695 1.797-1.562 2.457-2.549z" /> 
              </svg> 
            </a> 
            <a
              href="#"
              className="w-10 h-10 bg-gray-800/50 hover:bg-white/20 rounded-xl flex items-center justify-center transition-all duration-300 hover:scale-110 group"
            >
               
              <svg
                className="w-5 h-5 text-gray-400 group-hover:text-white"
                fill="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path d="M22.46 6c-.77.35-1.6.58-2.46.69a4.3 4.3 0 001.88-2.36 8.38 6.38 0 01-2.64 1.01A4.18 4.18 0 0015.19 5a4.28 4.28 0 00-7.3 3.82 11.93 11.93 0 01-8.42-4.38.997.997 0 0 0 1.16.83A4.33 4.33 0 00.88 6.45a4.25 4.25 0 001.15 3.48A4.18 4.18 0 0 1 .92 8.18v.05c0 .83.42 1.57 1.07 2.01a4.48 4.48 0 0 1-1.91.07c.54 1.68 2.12 2.9 4 2.9a4.54 4.54 0 0 1-2.02.07 4.27 4.27 0 004 2.96A8.42 8.42 0 0 1 2 20.41a11.95 11.95 0 0 1 6.29 1.68c7.53 0 11.63-6.25 11.63-11.63 0-.18 0-.36-.05-.54A8.35 8.35 0 0 0 22.46 6z" /> 
              </svg> 
            </a> 
            <a
              href="#"
              className="w-10 h-10 bg-gray-800/50 hover:bg-white/20 rounded-xl flex items-center justify-center transition-all duration-300 hover:scale-110 group"
            >
               
              <svg
                className="w-5 h-5 text-gray-400 group-hover:text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 005.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                /> 
              </svg> 
            </a> 
          </div> 
        </div> 
      </div> 
    </footer>
  );
};
export default Footer;
