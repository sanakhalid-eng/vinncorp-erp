const StatsSection = () => {
  return (
    <section
      className="py-20 bg-gradient-to-r from-indigo-500 to-purple-600 text-white scroll-mt-16"
      id="stats"
    >
       
      <div className="max-w-4xl mx-auto text-center">
         
        <div className="grid md:grid-cols-3 gap-12">
           
          <div>
             
            <div className="text-4xl md:text-5xl font-bold mb-4">10K+</div> 
            <div className="text-xl opacity-90">Projects Created</div> 
          </div> 
          <div>
             
            <div className="text-4xl md:text-5xl font-bold mb-4">50K+</div> 
            <div className="text-xl opacity-90">Active Users</div> 
          </div> 
          <div>
             
            <div className="text-4xl md:text-5xl font-bold mb-4">
              99.9%
            </div> 
            <div className="text-xl opacity-90">Uptime</div> 
          </div> 
        </div> 
      </div> 
    </section>
  );
};
export default StatsSection;
