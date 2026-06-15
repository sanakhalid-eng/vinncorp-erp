import { Link } from "react-router-dom";
const HeroSection = () => {
  return (
    <section
      id="hero"
      className="py-20 md:py-32 px-4 md:px-8 lg:px-16 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-500 text-white overflow-visible relative"
    >
       
      {/* Background decoration */} 
      <div className="absolute inset-0 overflow-hidden">
         
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-white opacity-10 rounded-full blur-3xl"></div> 
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-white opacity-10 rounded-full blur-3xl"></div> 
      </div> 
      <div className="max-w-7xl mx-auto text-center relative z-20 pt-16 md:pt-0">
         
        {/* Badge */} 
        <div className="inline-flex items-center gap-2 bg-white/20 backdrop-blur-sm px-4 py-2 rounded-full text-sm mb-8">
           
          <span className="w-2 h-2 bg-green-400 rounded-full animate-pulse"></span> 
          Now with Slack & Webhook Integrations 
        </div> 
        {/* Main heading */} 
        <h1 className="text-4xl md:text-6xl lg:text-7xl font-bold mb-6 px-4 py-4 leading-none tracking-tight">
           
          <span className="bg-gradient-to-r from-white to-gray-100 bg-clip-text text-transparent">
             
            Smart Project Management 
          </span> 
          <br /> 
          <span className="text-white">with Real-time Integrations</span> 
        </h1> 
        {/* Subtitle */} 
        <p className="text-xl md:text-2xl mb-8 max-w-3xl mx-auto opacity-95 leading-relaxed px-4">
           
          Streamline your projects with 
          <span className="font-semibold">Slack notifications</span>, 
          <span className="font-semibold">interactive buttons</span>, and 
          <span className="font-semibold">custom webhooks</span> for 1000+
          tools. 
        </p> 
        {/* CTA Buttons */} 
        <div className="flex flex-col sm:flex-row gap-4 justify-center items-center px-4 mb-12">
           
          <Link
            to="/register"
            className="px-10 py-4 bg-white text-indigo-600 font-semibold text-lg rounded-2xl shadow-2xl hover:shadow-3xl hover:scale-105 transition-all duration-300"
          >
             
            Get Started Free 
          </Link> 
          <Link
            to="/login"
            className="px-10 py-4 border-2 border-white text-white font-semibold text-lg rounded-2xl hover:bg-white hover:text-indigo-600 transition-all duration-300"
          >
             
            Sign In 
          </Link> 
        </div> 
        {/* Integration badges */} 
        <div className="flex flex-wrap justify-center gap-4 px-4">
           
          <div className="bg-white/20 backdrop-blur-sm px-4 py-2 rounded-lg text-sm flex items-center gap-2">
             
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
               
              <path
                d="M6 15C3.79086 15 2 13.2091 2 11C2 8.79086 3.79086 7 6 7C6.34684 7 6.68742 7.03976 7.01589 7.11583C7.61348 5.5326 9.10686 4.38105 10.8902 4.38105C12.6735 4.38105 14.1669 5.5326 14.7645 7.11583C15.0929 7.03976 15.4335 7 15.7804 7C17.9895 7 19.7804 8.79086 19.7804 11C19.7804 13.2091 17.9895 15 15.7804 15H6Z"
                fill="#E01E5A"
              /> 
              <path
                d="M12.5635 7.38105C12.5652 7.38105 12.5669 7.38105 12.5685 7.38105C14.3518 7.38105 15.8452 8.5326 16.4428 10.1158C16.7712 10.0398 17.1118 10 17.4587 10C19.6678 10 21.4587 11.7909 21.4587 14C21.4587 16.2091 19.6678 18 17.4587 18H8.66836C6.45921 18 4.66836 16.2091 4.66836 14C4.66836 11.7909 6.45921 10 8.66836 10C9.0152 10 9.35578 10.0398 9.68425 10.1158C10.2818 8.5326 11.7752 7.38105 13.5585 7.38105H12.5635Z"
                fill="#36C5F0"
              /> 
            </svg> 
            Slack Integration 
          </div> 
          <div className="bg-white/20 backdrop-blur-sm px-4 py-2 rounded-lg text-sm flex items-center gap-2">
             
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
               
              <path d="M13 10V3L4 14h7v7l9-11h-7z" /> 
            </svg> 
            Webhooks API 
          </div> 
          <div className="bg-white/20 backdrop-blur-sm px-4 py-2 rounded-lg text-sm flex items-center gap-2">
             
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
            >
               
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" /> 
            </svg> 
            GitHub OAuth 
          </div> 
        </div> 
      </div> 
    </section>
  );
};
export default HeroSection;
