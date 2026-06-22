import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Rocket,
  Users,
  FolderKanban,
  Trophy,
  Check,
  ArrowRight,
  ArrowLeft,
  Loader2,
  Sparkles,
} from "lucide-react";
import notify from "../../../lib/toast";
import { useWorkspace } from "../../../context/WorkspaceContext";
import { createProject } from "../../projects/api/projectApi";
import Button from "../../../components/Button";
const STEPS = [
  { id: "welcome", icon: Rocket, title: "Welcome!" },
  { id: "project", icon: FolderKanban, title: "Create Project" },
  { id: "team", icon: Users, title: "Invite Team" },
  { id: "done", icon: Trophy, title: "You're Set!" },
];
export default function OnboardingFlow({ onComplete }) {
  const navigate = useNavigate();
  const { workspace, loadWorkspaces } = useWorkspace();
  const [step, setStep] = useState(0);
  const [projectName, setProjectName] = useState("");
  const [projectDesc, setProjectDesc] = useState("");
  const [creating, setCreating] = useState(false);
  const [inviteEmails, setInviteEmails] = useState("");
  const handleCreateProject = async () => {
    if (!projectName.trim()) {
      notify.error("Project name is required");
      return;
    }
    try {
      setCreating(true);
      await createProject({ name: projectName, description: projectDesc });
      notify.success("Project created!");
      setStep(2);
    } catch (err) {
      notify.error(err.response?.data?.message || "Failed to create project");
    } finally {
      setCreating(false);
    }
  };
  const handleSkipInvite = () => {
    setStep(3);
  };
  const handleFinish = () => {
    onComplete?.();
  };
  const Step = STEPS[step];
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/45 backdrop-blur-sm p-4">
       
      <div className="w-full max-w-lg rounded-2xl bg-white p-8 shadow-2xl">
         
        <div className="flex items-center gap-2 mb-8">
           
          {STEPS.map((s, i) => (
            <div key={s.id} className="flex items-center gap-2 flex-1">
               
              <div
                className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-bold transition ${i <= step ? "bg-indigo-600 text-white" : "bg-slate-100 text-slate-400"}`}
              >
                 
                {i < step ? <Check className="h-4 w-4" /> : i + 1} 
              </div> 
              {i < STEPS.length - 1 && (
                <div
                  className={`h-0.5 flex-1 transition ${i < step ? "bg-indigo-600" : "bg-slate-200"}`}
                />
              )} 
            </div>
          ))} 
        </div> 
        {step === 0 && (
          <div className="text-center py-4">
             
            <div className="flex justify-center mb-6">
               
              <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-gradient-to-br from-indigo-500 to-cyan-500 shadow-lg">
                 
                <Rocket className="h-10 w-10 text-white" /> 
              </div> 
            </div> 
            <h2 className="text-2xl font-bold text-slate-900 mb-2">
               
              Welcome to {workspace?.name || "your workspace"}! 
            </h2> 
            <p className="text-slate-500 mb-8">
               
              Let's get you set up in just a few steps. You'll create a project,
              invite your team, and be ready to go. 
            </p> 
            <Button
              onClick={() => setStep(1)}
              className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700 px-8"
            >
               
              Get Started <ArrowRight className="w-4 h-4 ml-2 inline" /> 
            </Button> 
          </div>
        )} 
        {step === 1 && (
          <div className="py-4">
             
            <h3 className="text-lg font-bold text-slate-900 mb-4">
              Create your first project
            </h3> 
            <div className="space-y-4">
               
              <div>
                 
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Project Name
                </label> 
                <input
                  type="text"
                  value={projectName}
                  onChange={(e) => setProjectName(e.target.value)}
                  placeholder="e.g., Product Launch"
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400"
                /> 
              </div> 
              <div>
                 
                <label className="block text-sm font-medium text-slate-700 mb-1">
                  Description (optional)
                </label> 
                <textarea
                  value={projectDesc}
                  onChange={(e) => setProjectDesc(e.target.value)}
                  rows={3}
                  placeholder="What's this project about?"
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400 resize-none"
                /> 
              </div> 
              <div className="flex justify-between pt-2">
                 
                <Button
                  onClick={() => setStep(0)}
                  variant="outline"
                  className="rounded-xl border-slate-200 text-slate-600"
                >
                   
                  <ArrowLeft className="w-4 h-4 mr-1.5 inline" /> Back 
                </Button> 
                <Button
                  onClick={handleCreateProject}
                  disabled={creating}
                  className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
                >
                   
                  {creating ? (
                    <Loader2 className="w-4 h-4 animate-spin mr-1.5 inline" />
                  ) : null} 
                  Create Project 
                  <ArrowRight className="w-4 h-4 ml-1.5 inline" /> 
                </Button> 
              </div> 
            </div> 
          </div>
        )} 
        {step === 2 && (
          <div className="py-4">
             
            <h3 className="text-lg font-bold text-slate-900 mb-2">
              Invite your team
            </h3> 
            <p className="text-sm text-slate-500 mb-4">
               
              Add team members by email to collaborate in this workspace. 
            </p> 
            <div className="space-y-4">
               
              <div>
                 
                <label className="block text-sm font-medium text-slate-700 mb-1">
                   
                  Email addresses 
                </label> 
                <textarea
                  value={inviteEmails}
                  onChange={(e) => setInviteEmails(e.target.value)}
                  rows={3}
                  placeholder="john@example.com, jane@example.com"
                  className="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 outline-none focus:border-indigo-400 resize-none"
                /> 
                <p className="text-xs text-slate-400 mt-1">
                  Separate emails with commas
                </p> 
              </div> 
              <div className="flex justify-between pt-2">
                 
                <Button
                  onClick={handleSkipInvite}
                  variant="outline"
                  className="rounded-xl border-slate-200 text-slate-600"
                >
                   
                  Skip for now 
                </Button> 
                <Button
                  onClick={() => setStep(3)}
                  className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
                >
                   
                  Continue <ArrowRight className="w-4 h-4 ml-1.5 inline" /> 
                </Button> 
              </div> 
            </div> 
          </div>
        )} 
        {step === 3 && (
          <div className="text-center py-4">
             
            <div className="flex justify-center mb-6">
               
              <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-gradient-to-br from-emerald-400 to-teal-500 shadow-lg">
                 
                <Sparkles className="h-10 w-10 text-white" /> 
              </div> 
            </div> 
            <h2 className="text-2xl font-bold text-slate-900 mb-2">
              You're all set!
            </h2> 
            <p className="text-slate-500 mb-8">
               
              Your workspace is ready. Start exploring projects, tasks, and
              collaboration features. 
            </p> 
            <Button
              onClick={handleFinish}
              className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700 px-8"
            >
               
              Go to Dashboard 
              <ArrowRight className="w-4 h-4 ml-2 inline" /> 
            </Button> 
          </div>
        )} 
      </div> 
    </div>
  );
}
