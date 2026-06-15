import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import { FaSlack } from "react-icons/fa";
import {
  CheckSquare,
  MessageSquare,
  Webhook,
  Users,
  BarChart3,
  Timer,
  Shield,
  Zap,
  Layers,
  GitBranch,
  Bell,
  Globe,
} from "lucide-react";
const featureGroups = [
  {
    title: "Task & Project Management",
    subtitle: "Everything you need to organize work",
    features: [
      {
        icon: CheckSquare,
        color: "from-emerald-500 to-green-600",
        title: "Smart Tasks",
        desc: "Create, assign, prioritize, and track tasks with dependencies, subtasks, and custom statuses.",
        tags: ["Subtasks", "Priorities", "Dependencies", "Custom Fields"],
      },
      {
        icon: Layers,
        color: "from-blue-500 to-cyan-600",
        title: "Kanban Boards",
        desc: "Visualize workflows with drag-and-drop boards. Customize columns to match your process.",
        tags: ["Drag & Drop", "Custom Columns", "WIP Limits", "Swimlanes"],
      },
      {
        icon: Timer,
        color: "from-orange-500 to-red-600",
        title: "Time Tracking",
        desc: "Log hours on tasks, generate timesheets, and get insights into team productivity.",
        tags: ["Timesheets", "Manual Log", "Timer", "Reports"],
      },
      {
        icon: GitBranch,
        color: "from-purple-500 to-pink-600",
        title: "Agile Sprints",
        desc: "Plan sprints, track velocity, and monitor burndown charts with full Scrum support.",
        tags: ["Sprint Planning", "Burndown", "Velocity", "Retrospectives"],
      },
    ],
  },
  {
    title: "Collaboration & Communication",
    subtitle: "Keep your team in sync",
    features: [
      {
        icon: Users,
        color: "from-indigo-500 to-purple-600",
        title: "Team Management",
        desc: "Invite members, assign roles, control permissions, and manage access at every level.",
        tags: ["Roles", "Permissions", "SSO", "2FA"],
      },
      {
        icon: MessageSquare,
        color: "from-sky-500 to-blue-600",
        title: "Comments & @Mentions",
        desc: "Discuss tasks in context with threaded comments, file attachments, and real-time mentions.",
        tags: ["Threaded", "Attachments", "Mentions", "Rich Text"],
      },
      {
        icon: Bell,
        color: "from-amber-500 to-yellow-600",
        title: "Smart Notifications",
        desc: "Stay updated with configurable alerts for task changes, mentions, and project updates.",
        tags: ["In-App", "Email", "FaSlack", "Preferences"],
      },
    ],
  },
  {
    title: "Integrations & Extensibility",
    subtitle: "Connect your favorite tools",
    features: [
      {
        icon: MessageSquare,
        color: "from-purple-500 to-pink-600",
        title: "Slack Integration",
        desc: "Receive notifications, approve tasks, and manage projects directly from Slack with interactive buttons.",
        tags: ["Notifications", "Interactive", "Commands", "Channels"],
      },
      {
        icon: Webhook,
        color: "from-teal-500 to-emerald-600",
        title: "Webhooks & APIs",
        desc: "Connect to 1000+ tools with custom webhooks. Secure HMAC signatures with automatic retries.",
        tags: ["REST API", "Webhooks", "HMAC", "Rate Limiting"],
      },
      {
        icon: Globe,
        color: "from-cyan-500 to-blue-600",
        title: "OAuth & SSO",
        desc: "Seamless authentication with GitHub OAuth, Google SSO, and enterprise directory integration.",
        tags: ["GitHub", "Google", "LDAP", "SAML"],
      },
    ],
  },
  {
    title: "Analytics & Intelligence",
    subtitle: "Data-driven decision making",
    features: [
      {
        icon: BarChart3,
        color: "from-rose-500 to-orange-600",
        title: "Analytics Dashboard",
        desc: "Beautiful dashboards with real-time metrics, exportable reports in CSV and PDF formats.",
        tags: ["Dashboards", "CSV Export", "PDF Export", "Custom Reports"],
      },
      {
        icon: Shield,
        color: "from-violet-500 to-indigo-600",
        title: "Audit Logs",
        desc: "Track every change with detailed audit trails. Maintain compliance with full activity history.",
        tags: ["Audit Trail", "Compliance", "History", "Activity Log"],
      },
      {
        icon: Zap,
        color: "from-fuchsia-500 to-pink-600",
        title: "Performance Metrics",
        desc: "Monitor team velocity, cycle time, and delivery forecasts with actionable insights.",
        tags: ["Velocity", "Cycle Time", "Forecasts", "Burndown"],
      },
    ],
  },
];
const FeatureCard = ({ icon: Icon, color, title, desc, tags, index }) => (
  <div
    className="group p-8 bg-white dark:bg-gray-800 rounded-2xl shadow-lg hover:shadow-2xl hover:-translate-y-1 transition-all duration-500 border border-gray-100 dark:border-gray-700 animate-fade-in"
    style={{ animationDelay: `${index * 100}ms` }}
  >
     
    <div
      className={`w-16 h-16 bg-gradient-to-br ${color} rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform duration-300 shadow-lg`}
    >
       
      <Icon className="w-8 h-8 text-white" /> 
    </div> 
    <h3 className="text-xl font-bold text-gray-800 dark:text-gray-100 mb-3">
      {title}
    </h3> 
    <p className="text-gray-600 dark:text-gray-300 mb-4 leading-relaxed">
      {desc}
    </p> 
    <div className="flex flex-wrap gap-2">
       
      {tags.map((tag) => (
        <span
          key={tag}
          className="px-3 py-1 bg-indigo-50 dark:bg-indigo-900/30 text-indigo-600 dark:text-indigo-400 rounded-full text-xs font-medium"
        >
           
          {tag} 
        </span>
      ))} 
    </div> 
  </div>
);
const Features = () => {
  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-blue-50 via-white to-indigo-100 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
       
      <Navbar /> 
      <main className="flex-1">
         
        <section className="relative py-24 md:py-32 px-4 md:px-8 lg:px-16 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-500 overflow-hidden">
           
          <div className="absolute inset-0">
             
            <div className="absolute -top-40 -right-40 w-96 h-96 bg-white opacity-10 rounded-full blur-3xl"></div> 
            <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-white opacity-10 rounded-full blur-3xl"></div> 
          </div> 
          <div className="max-w-5xl mx-auto text-center relative z-10">
             
            <div className="inline-flex items-center gap-2 bg-white/20 backdrop-blur-sm px-4 py-2 rounded-full text-sm text-white mb-8">
               
              <Zap className="w-4 h-4" /> Powerful Feature Set 
            </div> 
            <h1 className="text-5xl md:text-7xl font-bold text-white mb-6 tracking-tight">
               
              Everything You Need 
            </h1> 
            <p className="text-xl md:text-2xl text-white/90 max-w-3xl mx-auto leading-relaxed">
               
              From task management to enterprise integrations — PMT-SK equips
              your team with every tool to ship faster and collaborate
              better. 
            </p> 
          </div> 
        </section> 
        {featureGroups.map((group, gi) => (
          <section
            key={group.title}
            className="py-20 md:py-24 px-4 md:px-8 lg:px-16"
          >
             
            <div className="max-w-7xl mx-auto">
               
              <div className="text-center mb-16">
                 
                <h2 className="text-4xl md:text-5xl font-bold text-gray-800 dark:text-gray-100 mb-4">
                   
                  {group.title} 
                </h2> 
                <p className="text-xl text-gray-600 dark:text-gray-300 max-w-2xl mx-auto">
                   
                  {group.subtitle} 
                </p> 
                {gi < featureGroups.length - 1 && (
                  <div className="mt-8 w-24 h-1 bg-gradient-to-r from-indigo-500 to-purple-500 rounded-full mx-auto"></div>
                )} 
              </div> 
              <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
                 
                {group.features.map((feature, fi) => (
                  <FeatureCard key={feature.title} {...feature} index={fi} />
                ))} 
              </div> 
            </div> 
          </section>
        ))} 
        <section className="py-24 px-4 md:px-8 lg:px-16 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-500">
           
          <div className="max-w-3xl mx-auto text-center">
             
            <h2 className="text-4xl md:text-5xl font-bold text-white mb-6">
               
              Ready to Transform Your Workflow? 
            </h2> 
            <p className="text-xl text-white/90 mb-10">
               
              Join thousands of teams already using PMT-SK to deliver projects
              on time. 
            </p> 
            <Link
              to="/register"
              className="inline-flex items-center gap-3 px-10 py-5 bg-white text-indigo-600 font-semibold text-lg rounded-2xl hover:shadow-2xl hover:scale-105 transition-all duration-300"
            >
               
              <Zap className="w-5 h-5" /> Get Started Free 
            </Link> 
          </div> 
        </section> 
      </main> 
      <Footer /> 
    </div>
  );
};
export default Features;
