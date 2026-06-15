import { useNavigate, Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
import Card from "../components/Card";
import Button from "../components/Button";
import Logo from "../assets/Logo - PMT-SK.png";
export default function RegistrationSuccess() {
  const navigate = useNavigate();
  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 via-white to-blue-50 flex flex-col">
       
      <Navbar /> 
      <main className="flex-1 flex items-center justify-center px-4 py-12">
         
        <Card className="w-96 max-w-md mx-auto p-0 shadow-lg hover:shadow-xl">
           
          <div className="p-8 flex flex-col items-center text-center">
             
            <div className="w-24 h-24 bg-green-100 rounded-2xl flex items-center justify-center mb-6">
               
              <svg
                className="w-12 h-12 text-green-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                /> 
              </svg> 
            </div> 
            <img
              src={Logo}
              alt="PMT-SK"
              className="w-28 h-28 mb-6 object-contain"
            /> 
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              Account Created!
            </h2> 
            <p className="text-gray-600 mb-8 max-w-sm">
               
              Your account has been successfully created. Please log in to
              continue. 
            </p> 
            <Button
              className="w-full mb-6 shadow-lg hover:shadow-xl transition-all duration-300"
              onClick={() => navigate("/login")}
            >
               
              Go to Login 
            </Button> 
            <p className="text-sm text-gray-500">
               
              Click the button above to log in 
            </p> 
          </div> 
        </Card> 
      </main> 
      <Footer /> 
    </div>
  );
}
