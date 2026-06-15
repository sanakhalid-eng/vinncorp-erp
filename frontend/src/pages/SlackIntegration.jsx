import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/axios";
import notify from "../lib/toast";

const SlackIntegration = () => {
  const { projectId } = useParams();
  const navigate = useNavigate();
  const [integration, setIntegration] = useState(null);
  const [loading, setLoading] = useState(true);
  const [workspaceId, setWorkspaceId] = useState("");

  useEffect(() => {
    checkIntegration();
  }, [projectId]);

  const checkIntegration = async () => {
    try {
      const response = await api.get("/slack/integration", {
        params: { projectId },
      });
      if (response.data.success) {
        setIntegration(response.data.data);
      }
    } catch (error) {
      console.log("No integration found");
    } finally {
      setLoading(false);
    }
  };

  const handleConnectSlack = async () => {
    try {
      const response = await api.get("/slack/oauth/url", {
        params: { projectId, workspaceId },
      });

      // Redirect to Slack OAuth
      window.location.href = response.data.data.url;
    } catch (error) {
      notify.error("Failed to get OAuth URL");
    }
  };

  const handleRemoveIntegration = async () => {
    if (!window.confirm("Are you sure you want to remove Slack integration?"))
      return;
    try {
      await api.delete(`/slack/integration/${integration.id}`);
      notify.success("Slack integration removed");
      setIntegration(null);
    } catch (error) {
      notify.error("Failed to remove integration");
    }
  };

  const handleSyncUsers = async () => {
    try {
      await api.post(`/slack/sync-users/${integration.workspaceId}`);
      notify.success("Users synced successfully");
    } catch (error) {
      notify.error("Failed to sync users");
    }
  };

  if (loading) {
    return <div className="text-center py-8">Loading...</div>;
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold text-gray-800 mb-6">
        Slack Integration
      </h1>

      {!integration ? (
        <div className="bg-white shadow-md rounded-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Connect to Slack</h2>
          <p className="text-gray-600 mb-4">
            Integrate your project with Slack to receive notifications and
            manage tasks directly from Slack.
          </p>

          <div className="mb-4">
            <label className="block text-sm font-medium mb-2">
              Workspace ID (Optional)
            </label>
            <input
              type="text"
              className="w-full px-3 py-2 border border-gray-300 rounded-md"
              value={workspaceId}
              onChange={(e) => setWorkspaceId(e.target.value)}
              placeholder="Enter your Slack Workspace ID"
            />
          </div>

          <button
            onClick={handleConnectSlack}
            className="bg-[#4A154B] hover:bg-[#3B0E3C] text-white font-medium py-2 px-6 rounded-lg flex items-center gap-2"
          >
            <svg
              width="20"
              height="20"
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M6 15C3.79086 15 2 13.2091 2 11C2 8.79086 3.79086 7 6 7C6.34684 7 6.68742 7.03976 7.01589 7.11583C7.61348 5.5326 9.10686 4.38105 10.8902 4.38105C12.6735 4.38105 14.1669 5.5326 14.7645 7.11583C15.0929 7.03976 15.4335 7 15.7804 7C17.9895 7 19.7804 8.79086 19.7804 11C19.7804 13.2091 17.9895 15 15.7804 15H6Z"
                fill="#E01E5A"
              />
              <path
                d="M12.5635 7.38105C12.5652 7.38105 12.5669 7.38105 12.5685 7.38105C14.3518 7.38105 15.8452 8.5326 16.4428 10.1158C16.7712 10.0398 17.1118 10 17.4587 10C19.6678 10 21.4587 11.7909 21.4587 14C21.4587 16.2091 19.6678 18 17.4587 18H8.66836C6.45921 18 4.66836 16.2091 4.66836 14C4.66836 11.7909 6.45921 10 8.66836 10C9.0152 10 9.35578 10.0398 9.68425 10.1158C10.2818 8.5326 11.7752 7.38105 13.5585 7.38105H12.5635Z"
                fill="#36C5F0"
              />
              <path
                d="M17.4587 9C17.8055 9 18.1461 9.03976 18.4746 9.11583C19.0722 7.5326 20.5656 6.38105 22.3489 6.38105C24.558 6.38105 26.3489 8.1719 26.3489 10.381C26.3489 12.5902 24.558 14.381 22.3489 14.381H13.5585V14.381C13.5602 14.381 13.5618 14.381 13.5635 14.381C15.3468 14.381 16.8402 13.2295 17.4378 11.6462C17.7662 11.7223 18.1068 11.762 18.4537 11.762C20.6628 11.762 22.4537 10.5288 23.0512 8.94556C22.7228 9.02163 22.3822 9.0614 22.0353 9.0614C20.252 9.0614 18.4537 7.82823 18.4537 6.38105C18.4537 6.38105 18.4537 6.38105 18.4537 9Z"
                fill="#2EB67D"
              />
              <path
                d="M8.66836 10C9.0152 10 9.35578 10.0398 9.68425 10.1158C10.2818 8.5326 11.7752 7.38105 13.5585 7.38105H12.5635C10.7802 7.38105 9.28678 8.5326 8.68925 10.1158C8.36078 10.0398 8.0202 10 7.67336 10C5.46421 10 3.67336 11.7909 3.67336 14C3.67336 16.2091 5.46421 18 7.67336 18H16.4587C14.6678 18 13.2357 16.7909 12.8635 15.381H7.67336C5.46421 15.381 3.67336 13.5902 3.67336 11.381C3.67336 9.17186 5.46421 7.38105 7.67336 7.38105C7.67336 7.38105 7.67336 7.38105 7.67336 10Z"
                fill="#ECB22E"
              />
            </svg>
            Connect to Slack
          </button>
        </div>
      ) : (
        <div className="bg-white shadow-md rounded-lg p-6">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h2 className="text-xl font-semibold mb-2">
                Slack Integration Active
              </h2>
              <p className="text-gray-600">
                <span className="font-medium">Workspace:</span> 
                {integration.workspaceName}
              </p>
              <p className="text-gray-600">
                <span className="font-medium">Channel:</span> 
                {integration.channelName || "Not set"}
              </p>
              <p className="text-gray-600">
                <span className="font-medium">Connected:</span> 
                {new Date(integration.createdAt).toLocaleDateString()}
              </p>
            </div>
            <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm">
              Active
            </span>
          </div>

          <div className="flex gap-3">
            <button
              onClick={handleSyncUsers}
              className="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg"
            >
              Sync Users
            </button>
            <button
              onClick={handleRemoveIntegration}
              className="bg-red-600 hover:bg-red-700 text-white font-medium py-2 px-4 rounded-lg"
            >
              Remove Integration
            </button>
          </div>

          <div className="mt-6 p-4 bg-gray-50 rounded-lg">
            <h3 className="font-semibold mb-2">Features Enabled:</h3>
            <ul className="list-disc list-inside text-gray-600 space-y-1">
              <li>Task created notifications</li>
              <li>Task status change notifications</li>
              <li>Comment notifications with @mentions</li>
              <li>Interactive buttons (approve/reject from Slack)</li>
              <li>Bi-directional sync (Slack ↔ PMT-SK)</li>
            </ul>
          </div>

          <div className="mt-4 p-4 bg-blue-50 rounded-lg">
            <h3 className="font-semibold mb-2">Setup Instructions:</h3>
            <ol className="list-decimal list-inside text-gray-600 space-y-1">
              <li>
                Invite the bot to your Slack channel: 
                <code>/invite @YourBotName</code>
              </li>
              <li>
                Set up webhook URL in Slack: 
                <code>{window.location.origin}/api/slack/webhook</code>
              </li>
              <li>
                Use <code>/sync-users</code> to map Slack users to PMT-SK users
              </li>
            </ol>
          </div>
        </div>
      )}
    </div>
  );
};

export default SlackIntegration;
