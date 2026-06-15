import NotificationDropdown from "../components/notifications/NotificationDropdown";
function AppLayout({ children }) {
  return (
    <div className="min-w-0 flex-1 overflow-auto lg:max-h-screen">
       
      <header className="sticky top-0 z-30 flex items-center justify-end border-b border-surface-200/70 dark:border-surface-800/70 bg-white/80 dark:bg-surface-900/80 px-4 py-3 backdrop-blur lg:px-6">
         
        <NotificationDropdown /> 
      </header> 
      <div className="min-h-screen px-4 pb-6 pt-4 lg:px-6 lg:pt-4">
        {children}
      </div> 
    </div>
  );
}
export default AppLayout;
