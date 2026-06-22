import { Users, PieChart } from "lucide-react";
import RoleBadge from "../features/projects/components/members/RoleBadge.jsx";
import { Doughnut } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import { cn } from "../utils/cn.js";
ChartJS.register(ArcElement, Tooltip, Legend);
const MemberOverview = ({
  membersOverview = [],
  roleCounts = {},
  className = "",
}) => {
  const roleChartData =
    roleCounts && Object.keys(roleCounts).length > 0
      ? {
          labels: Object.keys(roleCounts),
          datasets: [
            {
              data: Object.values(roleCounts),
              backgroundColor: ["#10B981", "#3B82F6", "#F59E0B", "#EF4444"],
              borderWidth: 0,
            },
          ],
        }
      : null;
  return (
    <div
      className={cn(
        "bg-white/70 backdrop-blur-xl rounded-3xl p-8 shadow-2xl border border-white/50",
        className,
      )}
    >
       
      <div className="flex items-center gap-3 mb-8">
         
        <div className="w-3 h-3 bg-blue-500 rounded-full animate-pulse" /> 
        <h2 className="text-2xl font-bold text-gray-900">
          Team Members ({membersOverview.length})
        </h2> 
      </div> 
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
         
        {/* Role Distribution Chart */} 
        <div className="space-y-4">
           
          <h3 className="font-semibold text-gray-900 text-lg">
            Role Distribution
          </h3> 
          {roleChartData ? (
            <div className="h-48 flex items-center justify-center">
               
              <Doughnut
                data={roleChartData}
                options={{
                  responsive: true,
                  cutout: "65%",
                  plugins: {
                    legend: {
                      position: "bottom",
                      labels: { padding: 20, usePointStyle: true },
                    },
                  },
                }}
              /> 
            </div>
          ) : (
            <div className="h-48 flex items-center justify-center bg-gray-100 rounded-2xl">
               
              <Users className="w-12 h-12 text-gray-400" /> 
              <span className="ml-2 text-gray-500">No role data</span> 
            </div>
          )} 
        </div> 
        {/* Members Grid */} 
        <div className="space-y-4">
           
          <h3 className="font-semibold text-gray-900 text-lg">
            Recent Team Members
          </h3> 
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-64 overflow-y-auto pr-2">
             
            {membersOverview.length > 0 ? (
              membersOverview.map((member) => (
                <div
                  key={member.id}
                  className="group flex items-center gap-3 p-3 bg-gray-50/50 rounded-xl hover:bg-white/80 transition-all border border-white/50 hover:shadow-md"
                >
                   
                  <div className="w-10 h-10 bg-gradient-to-br from-indigo-400 to-blue-500 rounded-full flex items-center justify-center text-white font-semibold text-sm shadow-lg">
                     
                    {member.name?.charAt(0)?.toUpperCase() || "?"} 
                  </div> 
                  <div className="flex-1 min-w-0">
                     
                    <p className="font-medium text-gray-900 truncate">
                      {member.name}
                    </p> 
                    <p className="text-sm text-gray-500 truncate">
                      {member.email}
                    </p> 
                  </div> 
                  <RoleBadge role={member.role} size="sm" /> 
                </div>
              ))
            ) : (
              <div className="col-span-full text-center py-8 text-gray-500">
                 
                No team members yet 
              </div>
            )} 
          </div> 
        </div> 
      </div> 
    </div>
  );
};
export default MemberOverview;
