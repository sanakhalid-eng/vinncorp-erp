const FeaturesSection = () => {
  return (
    <section className="py-24 px-4 md:px-8 lg:px-16 scroll-mt-16" id="features">
       
      <div className="max-w-7xl mx-auto">
         
        <h2 className="text-4xl md:text-5xl font-bold text-center text-gray-800 mb-4">
           
          Everything You Need to Succeed 
        </h2> 
        <p className="text-xl text-gray-600 text-center mb-20 max-w-2xl mx-auto">
           
          Powerful features designed for modern teams, with real-time
          integrations. 
        </p> 
        {/* Main Features Grid */} 
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8 mb-16">
           
          {/* Slack Integration */} 
          <div className="group p-8 bg-white rounded-2xl shadow-lg hover:shadow-2xl hover:-translate-y-2 transition-all duration-300 border border-gray-100">
             
            <div className="w-20 h-20 bg-gradient-to-br from-purple-500 to-pink-500 rounded-3xl flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
               
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                 
                <path
                  d="M6 15C3.79086 15 2 13.2091 2 11C2 8.79086 3.79086 7 6 7C6.34684 7 6.68742 7.03976 7.01589 7.11583C7.61348 5.5326 9.10686 4.38105 10.8902 4.38105C12.6735 4.38105 14.1669 5.5326 14.7645 7.11583C15.0929 7.03976 15.4335 7 15.7804 7C17.9895 7 19.7804 8.79086 19.7804 11C19.7804 13.2091 17.9895 15 15.7804 15H6Z"
                  fill="white"
                /> 
                <path
                  d="M12.5635 7.38105C12.5652 7.38105 12.5669 7.38105 12.5685 7.38105C14.3518 7.38105 15.8452 8.5326 16.4428 10.1158C16.7712 10.0398 17.1118 10 17.4587 10C19.6678 10 21.4587 11.7909 21.4587 14C21.4587 16.2091 19.6678 18 17.4587 18H8.66836C6.45921 18 4.66836 16.2091 4.66836 14C4.66836 11.7909 6.45921 10 8.66836 10C9.0152 10 9.35578 10.0398 9.68425 10.1158C10.2818 8.5326 11.7752 7.38105 13.5585 7.38105H12.5635Z"
                  fill="#E01E5A"
                /> 
              </svg> 
            </div> 
            <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
              Slack Bot Integration
            </h3> 
            <p className="text-gray-600 text-center mb-4">
               
              Get real-time notifications, approve tasks, and manage projects
              directly from Slack with interactive buttons. 
            </p> 
            <div className="flex flex-wrap justify-center gap-2">
               
              <span className="px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-sm">
                Notifications
              </span> 
              <span className="px-3 py-1 bg-pink-100 text-pink-700 rounded-full text-sm">
                @Mentions
              </span> 
              <span className="px-3 py-1 bg-indigo-100 text-indigo-700 rounded-full text-sm">
                Interactive Buttons
              </span> 
            </div> 
          </div> 
          {/* Webhooks */} 
          <div className="group p-8 bg-white rounded-2xl shadow-lg hover:shadow-2xl hover:-translate-y-2 transition-all duration-300 border border-gray-100">
             
            <div className="w-20 h-20 bg-gradient-to-br from-blue-500 to-cyan-500 rounded-3xl flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
               
              <svg
                className="w-10 h-10 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                /> 
              </svg> 
            </div> 
            <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
              Webhooks & APIs
            </h3> 
            <p className="text-gray-600 text-center mb-4">
               
              Connect to 1000+ tools with custom webhooks. Secure HMAC
              signatures, automatic retries, and delivery logs. 
            </p> 
            <div className="flex flex-wrap justify-center gap-2">
               
              <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm">
                HMAC Security
              </span> 
              <span className="px-3 py-1 bg-cyan-100 text-cyan-700 rounded-full text-sm">
                Auto-Retry
              </span> 
              <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm">
                Delivery Logs
              </span> 
            </div> 
          </div> 
          {/* Task Management */} 
          <div className="group p-8 bg-white rounded-2xl shadow-lg hover:shadow-2xl hover:-translate-y-2 transition-all duration-300 border border-gray-100">
             
            <div className="w-20 h-20 bg-gradient-to-br from-green-500 to-emerald-500 rounded-3xl flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
               
              <svg
                className="w-10 h-10 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 012-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                /> 
              </svg> 
            </div> 
            <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
              Smart Task Management
            </h3> 
            <p className="text-gray-600 text-center mb-4">
               
              Break projects into tasks, set dependencies, track time, and
              monitor progress with Kanban boards. 
            </p> 
            <div className="flex flex-wrap justify-center gap-2">
               
              <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm">
                Subtasks
              </span> 
              <span className="px-3 py-1 bg-emerald-100 text-emerald-700 rounded-full text-sm">
                Time Tracking
              </span> 
            </div> 
          </div> 
          {/* Sprints */} 
          <div className="group p-8 bg-white rounded-2xl shadow-lg hover:shadow-2xl hover:-translate-y-2 transition-all duration-300 border border-gray-100">
             
            <div className="w-20 h-20 bg-gradient-to-br from-orange-500 to-red-500 rounded-3xl flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
               
              <svg
                className="w-10 h-10 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M13 10V3L4 14h7v7l9-11h-7z"
                /> 
              </svg> 
            </div> 
            <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
              Agile Sprints
            </h3> 
            <p className="text-gray-600 text-center mb-4">
               
              Plan sprints, track burndown charts, and deliver incremental value
              with Scrum methodology. 
            </p> 
          </div> 
          {/* Team Collaboration */} 
          <div className="group p-8 bg-white rounded-2xl shadow-lg hover:shadow-2xl hover:-translate-y-2 transition-all duration-300 border border-gray-100">
             
            <div className="w-20 h-20 bg-gradient-to-br from-indigo-500 to-purple-500 rounded-3xl flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
               
              <svg
                className="w-10 h-10 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                /> 
              </svg> 
            </div> 
            <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
              Team Collaboration
            </h3> 
            <p className="text-gray-600 text-center mb-4">
               
              Invite team members, set role-based permissions, comment with
              @mentions, and collaborate in real-time. 
            </p> 
          </div> 
          {/* Analytics */} 
          <div className="group p-8 bg-white rounded-2xl shadow-lg hover:shadow-2xl hover:-translate-y-2 transition-all duration-300 border border-gray-100">
             
            <div className="w-20 h-20 bg-gradient-to-br from-yellow-500 to-orange-500 rounded-3xl flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform">
               
              <svg
                className="w-10 h-10 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                /> 
              </svg> 
            </div> 
            <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
              Analytics & Reports
            </h3> 
            <p className="text-gray-600 text-center mb-4">
               
              Beautiful dashboards, export to CSV/PDF, and track productivity
              with detailed analytics. 
            </p> 
          </div> 
        </div> 
      </div> 
    </section>
  );
};
export default FeaturesSection;
