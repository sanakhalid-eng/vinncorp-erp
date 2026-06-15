import Navbar from "../components/Navbar";
const AuthLayout = ({ children }) => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 to-slate-200">
       
      <Navbar /> 
      <main className="container mx-auto px-4 py-12 max-w-4xl">
         
        {children} 
      </main> 
    </div>
  );
};
export default AuthLayout;
