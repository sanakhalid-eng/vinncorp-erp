import { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import { Plus, Link2 } from "lucide-react";
import api from "../../../api/axios";
import notify from "../../../lib/toast";
import { TableRowSkeleton } from "../../../components/LoadingSkeleton";
import EmptyState from "../../../components/EmptyState";
import ErrorState from "../../../components/ErrorState";
import ConfirmationDialog from "../../projects/components/members/ConfirmationDialog";

const Webhooks = () => {
  const { projectId } = useParams();
  const [webhooks, setWebhooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showLogsModal, setShowLogsModal] = useState(false);
  const [selectedWebhook, setSelectedWebhook] = useState(null);
  const [deliveries, setDeliveries] = useState([]);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [webhookToDelete, setWebhookToDelete] = useState(null);
  const [formData, setFormData] = useState({
    url: "",
    secret: "",
    events: [],
    isActive: true,
  });
  const availableEvents = [
    "TASK_CREATED",
    "TASK_UPDATED",
    "TASK_STATUS_CHANGED",
    "COMMENT_CREATED",
    "TIME_LOG_CREATED",
    "SPRINT_STARTED",
    "SPRINT_COMPLETED",
  ];
  useEffect(() => {
    fetchWebhooks();
  }, [projectId]);
  const fetchWebhooks = async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await api.get(`/projects/${projectId}/webhooks`);
      setWebhooks(response.data.data || []);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };
  const handleCreateWebhook = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/projects/${projectId}/webhooks`, formData);
      notify.success("Webhook created successfully");
      setShowCreateModal(false);
      setFormData({ url: "", secret: "", events: [], isActive: true });
      fetchWebhooks();
    } catch (error) {
      notify.error("Failed to create webhook");
    }
  };
  const handleDeleteWebhook = (webhookId) => {
    setWebhookToDelete(webhookId);
    setShowConfirmDialog(true);
  };

  const confirmDeleteWebhook = async () => {
    if (!webhookToDelete) return;
    try {
      await api.delete(`/webhooks/${webhookToDelete}`);
      notify.success("Webhook deleted successfully");
      fetchWebhooks();
    } catch (err) {
      notify.error("Failed to delete webhook");
    } finally {
      setShowConfirmDialog(false);
      setWebhookToDelete(null);
    }
  };
  const handleToggleActive = async (webhook) => {
    try {
      await api.put(`/webhooks/${webhook.id}`, { isActive: !webhook.isActive });
      notify.success(
        `Webhook ${!webhook.isActive ? "activated" : "deactivated"} successfully`,
      );
      fetchWebhooks();
    } catch (error) {
      notify.error("Failed to update webhook");
    }
  };
  const handleSendTest = async (webhookId) => {
    try {
      await api.post(`/webhooks/${webhookId}/test`);
      notify.success("Test event sent successfully");
    } catch (error) {
      notify.error("Failed to send test event");
    }
  };
  const fetchDeliveryLogs = async (webhook) => {
    setSelectedWebhook(webhook);
    try {
      const response = await api.get(`/webhooks/${webhook.id}/deliveries`);
      setDeliveries(response.data.data || []);
      setShowLogsModal(true);
    } catch (error) {
      notify.error("Failed to fetch delivery logs");
    }
  };
  const handleEventToggle = (event) => {
    setFormData((prev) => ({
      ...prev,
      events: prev.events.includes(event)
        ? prev.events.filter((e) => e !== event)
        : [...prev.events, event],
    }));
  };
  return (
    <div className="container mx-auto px-4 py-8">
       
      <div className="flex justify-between items-center mb-6">
         
        <h1 className="text-3xl font-bold text-gray-800">Webhooks</h1> 
        <button
          onClick={() => setShowCreateModal(true)}
          className="bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-lg transition duration-200"
        >
           
          Add Webhook 
        </button> 
      </div> 
      {loading ? (
        <div className="overflow-hidden rounded-lg bg-white shadow-md">
          <table className="min-w-full">
            <tbody>
              {[1, 2, 3, 4].map((i) => <TableRowSkeleton key={i} columns={4} />)}
            </tbody>
          </table>
        </div>
      ) : error ? (
        <ErrorState error={error} onRetry={fetchWebhooks} />
      ) : webhooks.length === 0 ? (
        <EmptyState
          icon={Link2}
          title="No webhooks configured"
          description="Send real-time events to external services when tasks, comments, or sprints change."
          action={{ label: "Add Webhook", icon: Plus, onClick: () => setShowCreateModal(true) }}
        />
      ) : (
        <div className="bg-white shadow-md rounded-lg overflow-hidden">
           
          <table className="min-w-full divide-y divide-gray-200">
             
            <thead className="bg-gray-50">
               
              <tr>
                 
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                   
                  URL 
                </th> 
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                   
                  Events 
                </th> 
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                   
                  Status 
                </th> 
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                   
                  Actions 
                </th> 
              </tr> 
            </thead> 
            <tbody className="bg-white divide-y divide-gray-200">
               
              {webhooks.map((webhook) => (
                <tr key={webhook.id}>
                   
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                     
                    {webhook.url} 
                  </td> 
                  <td className="px-6 py-4 text-sm text-gray-500">
                     
                    <div className="flex flex-wrap gap-1">
                       
                      {JSON.parse(webhook.events || "[]").map((event) => (
                        <span
                          key={event}
                          className="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded"
                        >
                           
                          {event} 
                        </span>
                      ))} 
                    </div> 
                  </td> 
                  <td className="px-6 py-4 whitespace-nowrap">
                     
                    <span
                      className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${webhook.isActive ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"}`}
                    >
                       
                      {webhook.isActive ? "Active" : "Inactive"} 
                    </span> 
                  </td> 
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                     
                    <button
                      onClick={() => handleToggleActive(webhook)}
                      className="text-indigo-600 hover:text-indigo-900"
                    >
                       
                      {webhook.isActive ? "Deactivate" : "Activate"} 
                    </button> 
                    <button
                      onClick={() => fetchDeliveryLogs(webhook)}
                      className="text-blue-600 hover:text-blue-900"
                    >
                       
                      Logs 
                    </button> 
                    <button
                      onClick={() => handleSendTest(webhook.id)}
                      className="text-green-600 hover:text-green-900"
                    >
                       
                      Test 
                    </button> 
                    <button
                      onClick={() => handleDeleteWebhook(webhook.id)}
                      className="text-red-600 hover:text-red-900"
                    >
                       
                      Delete 
                    </button> 
                  </td> 
                </tr>
              ))} 
            </tbody> 
          </table> 
        </div>
      )} 
      <ConfirmationDialog
        isOpen={showConfirmDialog}
        onClose={() => { setShowConfirmDialog(false); setWebhookToDelete(null); }}
        onConfirm={confirmDeleteWebhook}
        title="Delete webhook?"
        message="This webhook will stop receiving events immediately. This cannot be undone."
        confirmText="Delete"
      />
      {/* Create Webhook Modal */} 
      {showCreateModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
           
          <div className="relative top-20 mx-auto p-5 border w-96 shadow-lg rounded-md bg-white">
             
            <h3 className="text-lg font-bold mb-4">Create Webhook</h3> 
            <form onSubmit={handleCreateWebhook}>
               
              <div className="mb-4">
                 
                <label className="block text-sm font-medium mb-2">
                  URL
                </label> 
                <input
                  type="url"
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  value={formData.url}
                  onChange={(e) =>
                    setFormData({ ...formData, url: e.target.value })
                  }
                  placeholder="https://your-app.com/webhook"
                /> 
              </div> 
              <div className="mb-4">
                 
                <label className="block text-sm font-medium mb-2">
                  Secret
                </label> 
                <input
                  type="text"
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md"
                  value={formData.secret}
                  onChange={(e) =>
                    setFormData({ ...formData, secret: e.target.value })
                  }
                  placeholder="Enter a secret key for HMAC signature"
                /> 
              </div> 
              <div className="mb-4">
                 
                <label className="block text-sm font-medium mb-2">
                  Events
                </label> 
                <div className="space-y-2">
                   
                  {availableEvents.map((event) => (
                    <label key={event} className="flex items-center">
                       
                      <input
                        type="checkbox"
                        checked={formData.events.includes(event)}
                        onChange={() => handleEventToggle(event)}
                        className="mr-2"
                      /> 
                      <span className="text-sm">{event}</span> 
                    </label>
                  ))} 
                </div> 
              </div> 
              <div className="flex justify-end space-x-2">
                 
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400"
                >
                   
                  Cancel 
                </button> 
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                   
                  Create 
                </button> 
              </div> 
            </form> 
          </div> 
        </div>
      )} 
      {/* Delivery Logs Modal */} 
      {showLogsModal && selectedWebhook && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
           
          <div className="relative top-10 mx-auto p-5 border w-3/4 shadow-lg rounded-md bg-white">
             
            <div className="flex justify-between items-center mb-4">
               
              <h3 className="text-lg font-bold">
                Delivery Logs - {selectedWebhook.url}
              </h3> 
              <button
                onClick={() => setShowLogsModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                 
                Γ£ò 
              </button> 
            </div> 
            <div className="max-h-96 overflow-y-auto">
               
              <table className="min-w-full divide-y divide-gray-200">
                 
                <thead className="bg-gray-50">
                   
                  <tr>
                     
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">
                      Event
                    </th> 
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">
                      Status
                    </th> 
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">
                      Response
                    </th> 
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">
                      Retries
                    </th> 
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500">
                      Created
                    </th> 
                  </tr> 
                </thead> 
                <tbody className="divide-y divide-gray-200">
                   
                  {deliveries.map((delivery) => (
                    <tr key={delivery.id}>
                       
                      <td className="px-4 py-2 text-sm">
                        {delivery.eventType}
                      </td> 
                      <td className="px-4 py-2">
                         
                        <span
                          className={`px-2 py-1 text-xs rounded ${delivery.status === "SUCCESS" ? "bg-green-100 text-green-800" : delivery.status === "FAILED" ? "bg-red-100 text-red-800" : "bg-yellow-100 text-yellow-800"}`}
                        >
                           
                          {delivery.status} 
                        </span> 
                      </td> 
                      <td className="px-4 py-2 text-sm">
                         
                        {delivery.responseStatus} - 
                        {delivery.responseBody?.substring(0, 50)} 
                      </td> 
                      <td className="px-4 py-2 text-sm">
                        {delivery.retryCount}
                      </td> 
                      <td className="px-4 py-2 text-sm">
                         
                        {new Date(delivery.createdAt).toLocaleString()} 
                      </td> 
                    </tr>
                  ))} 
                </tbody> 
              </table> 
            </div> 
          </div> 
        </div>
      )} 
    </div>
  );
};
export default Webhooks;
