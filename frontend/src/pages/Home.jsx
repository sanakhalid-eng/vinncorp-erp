import { useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/useAuth.js";
import Navbar from "../components/Navbar";
import HeroSection from "../components/HeroSection";
import FeaturesSection from "../components/FeaturesSection";
import StatsSection from "../features/analytics/components/StatsSection";
import Footer from "../components/Footer";
import { Sparkles, BarChart3, MessageSquare, ArrowRight } from "lucide-react";
const pageLinks = [
  {
    to: "/features",
    icon: Sparkles,
    gradient: "from-indigo-500 to-purple-600",
    title: "Explore Features",
    desc: "Discover all the powerful tools PMT-SK offers to streamline your workflow.",
  },
  {
    to: "/manage",
    icon: BarChart3,
    gradient: "from-purple-500 to-pink-600",
    title: "Platform Features",
    desc: "Explore everything our platform offers to streamline your team's workflow.",
  },
  {
    to: "/contact-us",
    icon: MessageSquare,
    gradient: "from-cyan-500 to-blue-600",
    title: "Contact Us",
    desc: "Have questions? We're here to help. Reach out anytime.",
  },
];
const Home = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  useEffect(() => {
    if (token) {
      navigate("/user-home", { replace: true });
    }
  }, [token, navigate]);
  if (token) {
    return null;
  }
  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-blue-50 via-white to-indigo-100 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
       
      <Navbar /> 
      <main className="flex-1">
         
        <HeroSection /> <FeaturesSection /> <StatsSection /> 
        {/* Quick Navigation to Dedicated Pages */} 
        <section className="py-20 px-4 md:px-8 lg:px-16 bg-white/50 dark:bg-gray-800/50 backdrop-blur-sm">
           
          <div className="max-w-7xl mx-auto">
             
            <div className="text-center mb-14">
               
              <h2 className="text-4xl md:text-5xl font-bold text-gray-800 dark:text-gray-100 mb-4">
                 
                Explore More 
              </h2> 
              <p className="text-xl text-gray-600 dark:text-gray-300 max-w-2xl mx-auto">
                 
                Dive deeper into everything PMT-SK has to offer. 
              </p> 
            </div> 
            <div className="grid md:grid-cols-3 gap-8">
               
              {pageLinks.map((link) => {
                const Icon = link.icon;
                return (
                  <Link
                    key={link.to}
                    to={link.to}
                    className="group p-8 bg-white dark:bg-gray-800 rounded-2xl shadow-lg hover:shadow-2xl hover:-translate-y-1 transition-all duration-300 border border-gray-100 dark:border-gray-700"
                  >
                     
                    <div
                      className={`w-16 h-16 bg-gradient-to-br ${link.gradient} rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform`}
                    >
                       
                      <Icon className="w-8 h-8 text-white" /> 
                    </div> 
                    <h3 className="text-2xl font-bold text-gray-800 dark:text-gray-100 mb-3">
                      {link.title}
                    </h3> 
                    <p className="text-gray-600 dark:text-gray-300 mb-6">
                      {link.desc}
                    </p> 
                    <span className="inline-flex items-center gap-2 text-indigo-600 dark:text-indigo-400 font-semibold group-hover:gap-3 transition-all">
                       
                      Learn More <ArrowRight className="w-4 h-4" /> 
                    </span> 
                  </Link>
                );
              })} 
            </div> 
          </div> 
        </section> 
        {/* Final CTA */} 
        <section className="py-24 px-4 md:px-8 lg:px-16 bg-white dark:bg-gray-800">
           
          <div className="max-w-2xl mx-auto text-center">
             
            <h2 className="text-4xl md:text-5xl font-bold text-gray-800 dark:text-gray-100 mb-6">
              Ready to get started?
            </h2> 
            <p className="text-xl text-gray-600 dark:text-gray-300 mb-12">
              Join thousands of teams transforming their project management.
            </p> 
            <Link
              to="/register"
              className="inline-block px-12 py-5 bg-indigo-600 text-white font-semibold text-xl rounded-3xl hover:bg-indigo-700 hover:scale-105 transition-all duration-300 shadow-xl hover:shadow-2xl"
            >
              
              Start Free Trial 
            </Link>
            <div className="mt-8 flex items-center justify-center gap-6 text-sm text-gray-400 dark:text-gray-500">
              <span className="flex items-center gap-1.5">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" /></svg>
                Enterprise-grade security
              </span>
              <span className="flex items-center gap-1.5">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" /></svg>
                GDPR compliant
              </span>
              <span className="flex items-center gap-1.5">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>
                Free 14-day trial
              </span>
            </div> 
          </div> 
        </section> 
      </main> 
      <Footer /> 
      <div className="text-center py-3 text-xs text-gray-400 dark:text-gray-600 bg-white/50 dark:bg-gray-800/50">
        VinnCorp ERP v1.0 &mdash; Built with Spring Boot 4 &amp; React 19
      </div>
    </div>
  );
};
export default Home;
