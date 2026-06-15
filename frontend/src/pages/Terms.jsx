import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
const Terms = () => {
  return (
    <div className="min-h-screen flex flex-col bg-white">
       
      <Navbar /> 
      <main className="flex-1 py-12 px-4 md:px-8 lg:px-16">
         
        <div className="max-w-4xl mx-auto">
           
          <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-8">
            Terms and Conditions
          </h1> 
          <p className="text-sm text-gray-500 mb-12">
            Last updated: May 5, 2026
          </p> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              1. Acceptance of Terms
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              By accessing or using PMT-SK ("the Service"), you agree to be
              bound by these Terms and Conditions. If you disagree with any part
              of these terms, you may not access the Service. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              2. Description of Service
            </h2> 
            <p className="text-gray-700 leading-relaxed mb-4">
               
              PMT-SK is a project management platform that provides tools for
              task management, team collaboration, time tracking, and
              integrations with third-party services like Slack and
              webhooks. 
            </p> 
            <p className="text-gray-700 leading-relaxed">
               
              The Service is provided "as is" and "as available" without
              warranties of any kind. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              3. User Accounts
            </h2> 
            <p className="text-gray-700 leading-relaxed mb-4">
               
              To use certain features, you must register for an account. You
              agree to: 
            </p> 
            <ul className="list-disc list-inside text-gray-700 space-y-2">
               
              <li>
                Provide accurate and complete registration information
              </li> 
              <li>Maintain the security of your account credentials</li> 
              <li>
                Notify us immediately of any unauthorized use of your account
              </li> 
              <li>Be responsible for all activities under your account</li> 
            </ul> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              4. Acceptable Use
            </h2> 
            <p className="text-gray-700 leading-relaxed mb-4">
              You agree not to:
            </p> 
            <ul className="list-disc list-inside text-gray-700 space-y-2">
               
              <li>Use the Service for any illegal purpose</li> 
              <li>Violate any laws in your jurisdiction</li> 
              <li>Infringe intellectual property rights</li> 
              <li>Transmit harmful code, malware, or viruses</li> 
              <li>Attempt to gain unauthorized access to our systems</li> 
              <li>
                Interfere with the Service's integrity or performance
              </li> 
            </ul> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              5. Intellectual Property
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              The Service and its original content, features, and functionality
              are owned by PMT-SK and are protected by international copyright,
              trademark, patent, and trade secret laws. You may not copy,
              modify, distribute, or create derivative works without our express
              written permission. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              6. Data and Privacy
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              Your use of the Service is also governed by our Privacy Policy. We
              collect and process data as described in our 
              <Link
                to="/privacy-policy"
                className="text-indigo-600 hover:underline"
              >
                Privacy Policy
              </Link>
              . 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              7. Third-Party Services
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              The Service may integrate with third-party services (Slack,
              GitHub, Cloudinary). Your use of such services is subject to their
              respective terms and privacy policies. We are not responsible for
              third-party practices. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              8. Limitation of Liability
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              In no event shall PMT-SK be liable for any indirect, incidental,
              special, consequential, or punitive damages, including loss of
              profits, data, or goodwill, arising from your use of the
              Service. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              9. Termination
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              We may terminate or suspend your account and access to the Service
              immediately, without prior notice, for any reason, including
              breach of these Terms. Upon termination, your right to use the
              Service will cease immediately. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              10. Changes to Terms
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              We reserve the right to modify these Terms at any time. We will
              notify users of significant changes via email or through the
              Service. Continued use after changes constitutes acceptance of new
              Terms. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              11. Contact Us
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              If you have questions about these Terms, please contact us at: 
              <br /> 
              <a
                href="mailto:legal@pmt-sk.com"
                className="text-indigo-600 hover:underline"
              >
                legal@pmt-sk.com
              </a> 
            </p> 
          </section> 
          <div className="mt-12 pt-8 border-t border-gray-200">
             
            <Link
              to="/register"
              className="text-indigo-600 hover:underline font-medium"
            >
               
              ← Back to Registration 
            </Link> 
          </div> 
        </div> 
      </main> 
      <Footer /> 
    </div>
  );
};
export default Terms;
