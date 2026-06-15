import { Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import Footer from "../components/Footer";
const PrivacyPolicy = () => {
  return (
    <div className="min-h-screen flex flex-col bg-white">
       
      <Navbar /> 
      <main className="flex-1 py-12 px-4 md:px-8 lg:px-16">
         
        <div className="max-w-4xl mx-auto">
           
          <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-8">
            Privacy Policy
          </h1> 
          <p className="text-sm text-gray-500 mb-12">
            Last updated: May 5, 2026
          </p> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              1. Information We Collect
            </h2> 
            <p className="text-gray-700 leading-relaxed mb-4">
              We collect the following types of information:
            </p> 
            <h3 className="text-xl font-semibold text-gray-800 mb-3">
              a) Personal Information
            </h3> 
            <ul className="list-disc list-inside text-gray-700 space-y-2 mb-4">
               
              <li>Name and email address (during registration)</li> 
              <li>Profile information (avatar, job title, etc.)</li> 
              <li>Project and task data you create</li> 
              <li>Team member information</li> 
            </ul> 
            <h3 className="text-xl font-semibold text-gray-800 mb-3">
              b) Usage Data
            </h3> 
            <ul className="list-disc list-inside text-gray-700 space-y-2">
               
              <li>Log data (IP address, browser type, pages visited)</li> 
              <li>Performance and error logs</li> 
              <li>Time tracking and activity data</li> 
            </ul> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              2. How We Use Your Information
            </h2> 
            <ul className="list-disc list-inside text-gray-700 space-y-2">
               
              <li>Provide and maintain the Service</li> 
              <li>Notify you about changes to our Service</li> 
              <li>Allow you to participate in interactive features</li> 
              <li>Provide customer support</li> 
              <li>Monitor usage and improve the Service</li> 
              <li>Detect and prevent technical issues</li> 
              <li>
                Send emails (verification, password reset, notifications)
              </li> 
            </ul> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              3. Third-Party Services
            </h2> 
            <p className="text-gray-700 leading-relaxed mb-4">
              We integrate with these third-party services:
            </p> 
            <ul className="list-disc list-inside text-gray-700 space-y-2">
               
              <li>
                <strong>Slack:</strong> For notifications and bot integration
                (requires your Slack workspace authorization)
              </li> 
              <li>
                <strong>GitHub:</strong> For OAuth login (we receive your GitHub
                username and email)
              </li> 
              <li>
                <strong>Cloudinary:</strong> For file uploads and storage
              </li> 
              <li>
                <strong>Webhooks:</strong> We send data to your configured
                endpoints
              </li> 
            </ul> 
            <p className="text-gray-700 leading-relaxed mt-4">
               
              These services have their own privacy policies. We encourage you
              to read them. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              4. Data Storage and Security
            </h2> 
            <p className="text-gray-700 leading-relaxed mb-4">
               
              Your data is stored in secure databases hosted on reliable cloud
              infrastructure. We implement appropriate security measures to
              protect against unauthorized access, alteration, disclosure, or
              destruction of your data. 
            </p> 
            <p className="text-gray-700 leading-relaxed">
               
              However, no method of transmission over the Internet is 100%
              secure. We cannot guarantee absolute security. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              5. Data Retention
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              We retain your personal information for as long as your account is
              active or as needed to provide you services. You may request
              deletion of your data at any time by contacting us. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              6. Your Rights (GDPR)
            </h2> 
            <p className="text-gray-700 leading-relaxed mb-4">
              If you are in the EU, you have rights under GDPR:
            </p> 
            <ul className="list-disc list-inside text-gray-700 space-y-2">
               
              <li>Right to access your personal data</li> 
              <li>Right to rectification (correct inaccurate data)</li> 
              <li>Right to erasure (delete your data)</li> 
              <li>Right to restrict processing</li> 
              <li>Right to data portability</li> 
              <li>Right to object to processing</li> 
            </ul> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              7. Cookies
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              We use cookies to maintain your session and remember preferences.
              You can set your browser to refuse all cookies or to indicate when
              a cookie is being sent. However, some features may not function
              properly without cookies. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              8. Children's Privacy
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              Our Service does not address anyone under the age of 13. We do not
              knowingly collect personal information from children under 13. If
              we discover that a child under 13 has provided us with personal
              information, we immediately delete it. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              9. Changes to This Policy
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              We may update our Privacy Policy from time to time. We will notify
              you of any changes by posting the new Privacy Policy on this page
              and updating the "Last updated" date. 
            </p> 
          </section> 
          <section className="mb-10">
             
            <h2 className="text-2xl font-bold text-gray-800 mb-4">
              10. Contact Us
            </h2> 
            <p className="text-gray-700 leading-relaxed">
               
              If you have questions about this Privacy Policy, please contact us
              at: <br /> 
              <a
                href="mailto:privacy@pmt-sk.com"
                className="text-indigo-600 hover:underline"
              >
                privacy@pmt-sk.com
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
export default PrivacyPolicy;
