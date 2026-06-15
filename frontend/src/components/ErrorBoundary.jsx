import { Component } from "react";
import { Link } from "react-router-dom";
export class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null, errorId: null };
  }
  static getDerivedStateFromError(error) {
    const errorId =
      "ERR-" +
      Date.now().toString(36) +
      "-" +
      Math.random().toString(36).substr(2, 6).toUpperCase();
    return { hasError: true, error, errorId };
  }
  componentDidCatch(error, errorInfo) {
    console.error(
      `[${this.state.errorId}] ErrorBoundary caught:`,
      error,
      errorInfo,
    );
  }
  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center p-8">
           
          <div className="max-w-md w-full bg-white rounded-2xl shadow-lg p-8 text-center">
             
            <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
               
              <svg
                className="w-8 h-8 text-red-500"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                 
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                /> 
              </svg> 
            </div> 
            <h2 className="text-2xl font-bold text-gray-800 mb-2">
              Something went wrong
            </h2> 
            <p className="text-gray-500 mb-2">
               
              An unexpected error occurred. Our team has been notified. 
            </p> 
            {this.state.errorId && (
              <p className="text-xs text-gray-400 mb-6 font-mono">
                 
                Error ID: {this.state.errorId} 
              </p>
            )} 
            <div className="flex gap-3 justify-center">
               
              <button
                onClick={() => window.location.reload()}
                className="px-5 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors text-sm font-medium"
              >
                 
                Reload Page 
              </button> 
              <Link
                to="/user-home"
                className="px-5 py-2.5 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors text-sm font-medium"
              >
                 
                Go to Dashboard 
              </Link> 
            </div> 
          </div> 
        </div>
      );
    }
    return this.props.children;
  }
}
export default ErrorBoundary;
