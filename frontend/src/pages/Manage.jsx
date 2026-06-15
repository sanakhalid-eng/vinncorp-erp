import { useState } from "react";
import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import {
  Zap,
  Star,
  ChevronDown,
  ChevronUp,
  Users,
  Database,
  Headphones,
  Lock,
  Infinity,
  Layers,
  CheckCircle2,
} from "lucide-react";
const features = [
  {
    icon: CheckCircle2,
    title: "Project Management",
    desc: "Plan, track, and deliver projects on time with Kanban boards, Gantt charts, and sprint planning.",
  },
  {
    icon: Users,
    title: "Team Collaboration",
    desc: "Real-time collaboration with unlimited members, roles, and granular permissions.",
  },
  {
    icon: Database,
    title: "Time Tracking",
    desc: "Track time spent on tasks, generate timesheets, and analyze productivity across your team.",
  },
  {
    icon: Layers,
    title: "Custom Workflows",
    desc: "Build automated workflows tailored to your team's unique processes with our automation engine.",
  },
  {
    icon: Lock,
    title: "Security & Compliance",
    desc: "Role-based access control, audit logs, two-factor authentication, and enterprise-grade encryption.",
  },
  {
    icon: Headphones,
    title: "Integrations",
    desc: "Connect with Slack, GitHub, webhooks, and many more tools your team already uses.",
  },
];
const faqs = [
  {
    q: "How do I get started with the platform?",
    a: "Simply create an account, set up your workspace, and invite your team. You can start managing projects, tasks, and workflows right away.",
  },
  {
    q: "Is my data secure?",
    a: "Absolutely. We use industry-standard encryption, secure data centers, and regular backups to keep your information safe.",
  },
  {
    q: "Can I export my data?",
    a: "Yes, you can export all your projects, tasks, and data in standard formats (CSV, JSON) at any time. Your data is always yours.",
  },
  {
    q: "Do you offer custom enterprise solutions?",
    a: "Yes, we offer tailored solutions for large organizations. Contact our sales team to discuss your specific needs.",
  },
  {
    q: "How do I invite team members?",
    a: "Go to your workspace settings, navigate to Members, and send email invitations. New members will receive a link to join your workspace.",
  },
  {
    q: "What integrations are supported?",
    a: "We support Slack, GitHub, webhooks, and many more. Visit the Integrations page in your workspace to explore all options.",
  },
];
const Manage = () => {
  const [openFaq, setOpenFaq] = useState(null);
  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-blue-50 via-white to-indigo-100 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">

      <Navbar />
      <main className="flex-1">

        <section className="relative py-24 md:py-32 px-4 md:px-8 lg:px-16 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-500 overflow-hidden">

          <div className="absolute inset-0">

            <div className="absolute -top-40 -right-40 w-96 h-96 bg-white opacity-10 rounded-full blur-3xl"></div>
            <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-white opacity-10 rounded-full blur-3xl"></div>
          </div>
          <div className="max-w-4xl mx-auto text-center relative z-10">

            <div className="inline-flex items-center gap-2 bg-white/20 backdrop-blur-sm px-4 py-2 rounded-full text-sm text-white mb-8">

              <Star className="w-4 h-4" /> Built for Modern Teams
            </div>
            <h1 className="text-5xl md:text-7xl font-bold text-white mb-6 tracking-tight">

              Everything You Need to Ship
            </h1>
            <p className="text-xl md:text-2xl text-white/90 max-w-3xl mx-auto leading-relaxed">

              Plan, track, and deliver projects with a complete suite of tools your team will love.
            </p>
          </div>
        </section>
        <section className="py-20 md:py-28 px-4 md:px-8 lg:px-16">

          <div className="max-w-7xl mx-auto">

            <div className="grid md:grid-cols-3 gap-8 items-start max-w-6xl mx-auto">

              {features.map((item) => {
                const Icon = item.icon;
                return (
                  <div
                    key={item.title}
                    className="p-8 bg-white dark:bg-gray-800 rounded-2xl shadow-md hover:shadow-xl transition-all duration-300 border border-gray-100 dark:border-gray-700"
                  >

                    <div className="w-12 h-12 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center mb-4">

                      <Icon className="w-6 h-6 text-white" />
                    </div>
                    <h3 className="text-xl font-bold text-gray-800 dark:text-gray-100 mb-2">
                      {item.title}
                    </h3>
                    <p className="text-gray-600 dark:text-gray-300 text-sm leading-relaxed">
                      {item.desc}
                    </p>
                  </div>
                );
              })}
            </div>
          </div>
        </section>
        <section className="py-20 px-4 md:px-8 lg:px-16 bg-white/50 dark:bg-gray-800/50">
           
          <div className="max-w-4xl mx-auto">
             
            <div className="text-center mb-16">
               
              <h2 className="text-4xl md:text-5xl font-bold text-gray-800 dark:text-gray-100 mb-4">
                 
                Platform Features
              </h2> 
              <p className="text-xl text-gray-600 dark:text-gray-300 max-w-3xl mx-auto">
                 
                Everything your organization needs to scale with
                confidence. 
              </p> 
            </div> 
            <div className="grid md:grid-cols-3 gap-8">
               
              {[
                {
                  icon: Lock,
                  title: "SSO & SAML",
                  desc: "Integrate with your identity provider for seamless, secure access across your organization.",
                },
                {
                  icon: Database,
                  title: "Data Export",
                  desc: "Full data portability with CSV and JSON exports. Your data is always yours.",
                },
                {
                  icon: Users,
                  title: "Team Sync",
                  desc: "Real-time collaboration with unlimited members, roles, and granular permissions.",
                },
                {
                  icon: Layers,
                  title: "Custom Workflows",
                  desc: "Build automated workflows tailored to your team's unique processes.",
                },
                {
                  icon: Headphones,
                  title: "Priority Support",
                  desc: "Dedicated account manager and 24/7 phone support with guaranteed SLAs.",
                },
                {
                  icon: Infinity,
                  title: "Unlimited Storage",
                  desc: "Store all your files, attachments, and documents without worrying about caps.",
                },
              ].map((item) => {
                const Icon = item.icon;
                return (
                  <div
                    key={item.title}
                    className="p-6 bg-white dark:bg-gray-800 rounded-2xl shadow-md hover:shadow-xl transition-all duration-300 border border-gray-100 dark:border-gray-700"
                  >
                     
                    <div className="w-12 h-12 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center mb-4">
                       
                      <Icon className="w-6 h-6 text-white" /> 
                    </div> 
                    <h3 className="text-lg font-bold text-gray-800 dark:text-gray-100 mb-2">
                      {item.title}
                    </h3> 
                    <p className="text-gray-600 dark:text-gray-300 text-sm leading-relaxed">
                      {item.desc}
                    </p> 
                  </div>
                );
              })} 
            </div> 
          </div> 
        </section> 
        <section className="py-20 px-4 md:px-8 lg:px-16">
           
          <div className="max-w-3xl mx-auto">
             
            <div className="text-center mb-16">
               
              <h2 className="text-4xl md:text-5xl font-bold text-gray-800 dark:text-gray-100 mb-4">
                 
                Frequently Asked Questions 
              </h2> 
              <p className="text-xl text-gray-600 dark:text-gray-300">

                Everything you need to know about the platform.
              </p>
            </div> 
            <div className="space-y-4">
               
              {faqs.map((faq) => {
                const isOpen = openFaq === faq.q;
                return (
                  <div
                    key={faq.q}
                    className="bg-white dark:bg-gray-800 rounded-2xl shadow-md border border-gray-100 dark:border-gray-700 overflow-hidden transition-all duration-300"
                  >
                     
                    <button
                      onClick={() => setOpenFaq(isOpen ? null : faq.q)}
                      className="w-full flex items-center justify-between px-6 py-5 text-left"
                    >
                       
                      <span className="text-lg font-semibold text-gray-800 dark:text-gray-100 pr-4">
                        {faq.q}
                      </span> 
                      {isOpen ? (
                        <ChevronUp className="w-5 h-5 text-indigo-600 flex-shrink-0" />
                      ) : (
                        <ChevronDown className="w-5 h-5 text-gray-400 flex-shrink-0" />
                      )} 
                    </button> 
                    {isOpen && (
                      <div className="px-6 pb-5">
                         
                        <p className="text-gray-600 dark:text-gray-300 leading-relaxed">
                          {faq.a}
                        </p> 
                      </div>
                    )} 
                  </div>
                );
              })} 
            </div> 
            <div className="mt-12 text-center">
               
              <p className="text-gray-600 dark:text-gray-300 mb-4">
                Still have questions?
              </p> 
              <Link
                to="/contact-us"
                className="inline-flex items-center gap-2 text-indigo-600 dark:text-indigo-400 font-semibold hover:underline"
              >
                 
                Contact our sales team 
                <ChevronDown className="w-4 h-4 rotate-[-90deg]" /> 
              </Link> 
            </div> 
          </div> 
        </section> 
        <section className="py-24 px-4 md:px-8 lg:px-16 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-500">
           
          <div className="max-w-3xl mx-auto text-center">
             
            <h2 className="text-4xl md:text-5xl font-bold text-white mb-6">
               
              Start Your Free Trial Today 
            </h2> 
            <p className="text-xl text-white/90 mb-10">
               
              No credit card required. Full access to all Pro features for 14
              days. 
            </p> 
            <Link
              to="/register"
              className="inline-flex items-center gap-3 px-10 py-5 bg-white text-indigo-600 font-semibold text-lg rounded-2xl hover:shadow-2xl hover:scale-105 transition-all duration-300"
            >
               
              <Zap className="w-5 h-5" /> Start Free Trial 
            </Link> 
          </div> 
        </section> 
      </main> 
      <Footer /> 
    </div>
  );
};
export default Manage;
