import {
  FolderKanban,
  CheckCircle2,
  Search,
  Users,
  PackagePlus,
  Inbox,
} from "lucide-react";
import Button from "./Button";

export function EmptyState({ title = "No items found", message = "There are no items to display." }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 mb-4">
        <Inbox className="h-8 w-8 text-slate-300" />
      </div>
      <h3 className="text-lg font-semibold text-slate-700 mb-1">{title}</h3>
      <p className="text-sm text-slate-400 max-w-sm">{message}</p>
    </div>
  );
}

export function EmptyWorkspaceState({ onCreateProject, onInvite }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
       
      <div className="flex h-20 w-20 items-center justify-center rounded-3xl bg-gradient-to-br from-indigo-500 to-cyan-500 shadow-lg shadow-indigo-200 mb-6">
         
        <FolderKanban className="h-10 w-10 text-white" /> 
      </div> 
      <h3 className="text-2xl font-bold text-slate-800 mb-2">
        Your workspace is empty
      </h3> 
      <p className="text-slate-500 mb-8 max-w-md">
         
        Create your first project to start organizing tasks, collaborating with
        your team, and tracking progress. 
      </p> 
      <div className="flex gap-3">
         
        {onCreateProject && (
          <Button
            onClick={onCreateProject}
            className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
          >
             
            <PackagePlus className="w-4 h-4 mr-1.5 inline" /> Create
            Project 
          </Button>
        )} 
        {onInvite && (
          <Button
            onClick={onInvite}
            variant="outline"
            className="rounded-xl border-indigo-200 text-indigo-600 hover:bg-indigo-50"
          >
             
            <Users className="w-4 h-4 mr-1.5 inline" /> Invite Team 
          </Button>
        )} 
      </div> 
    </div>
  );
}
export function EmptyProjectState({ onCreateTask }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
       
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 mb-4">
         
        <CheckCircle2 className="h-8 w-8 text-slate-300" /> 
      </div> 
      <h3 className="text-lg font-semibold text-slate-700 mb-1">
        No tasks yet
      </h3> 
      <p className="text-sm text-slate-400 mb-6 max-w-sm">
         
        Create your first task to begin tracking work in this project. 
      </p> 
      {onCreateTask && (
        <Button
          onClick={onCreateTask}
          className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
        >
           
          <PackagePlus className="w-4 h-4 mr-1.5 inline" /> Create Task 
        </Button>
      )} 
    </div>
  );
}
export function EmptyTaskState({ onCreateTask, onBack }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
       
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 mb-4">
         
        <CheckCircle2 className="h-8 w-8 text-slate-300" /> 
      </div> 
      <h3 className="text-lg font-semibold text-slate-700 mb-1">
        No tasks found
      </h3> 
      <p className="text-sm text-slate-400 mb-6">
        Get started by creating a new task.
      </p> 
      {onCreateTask && (
        <Button
          onClick={onCreateTask}
          className="rounded-xl bg-indigo-600 text-white hover:bg-indigo-700"
        >
           
          <PackagePlus className="w-4 h-4 mr-1.5 inline" /> Create Task 
        </Button>
      )} 
    </div>
  );
}
export function EmptySearchState({ query }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
       
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-slate-100 mb-4">
         
        <Search className="h-8 w-8 text-slate-300" /> 
      </div> 
      <h3 className="text-lg font-semibold text-slate-700 mb-1">
        No results found
      </h3> 
      {query && (
        <p className="text-sm text-slate-400">No matches for "{query}"</p>
      )} 
    </div>
  );
}
