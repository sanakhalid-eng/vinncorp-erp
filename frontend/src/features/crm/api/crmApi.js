import API from '../../../api/axios';

// ΓöÇΓöÇΓöÇ Contacts ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const listContacts = async (page = 0, size = 20, search = '') => {
  const res = await API.get('/crm/contacts', { params: { page, size, search } });
  return res.data?.data;
};

export const getContact = async (id) => {
  const res = await API.get(`/crm/contacts/${id}`);
  return res.data?.data;
};

export const createContact = async (data) => {
  const res = await API.post('/crm/contacts', data);
  return res.data?.data;
};

export const updateContact = async (id, data) => {
  const res = await API.put(`/crm/contacts/${id}`, data);
  return res.data?.data;
};

export const deleteContact = async (id) => {
  await API.delete(`/crm/contacts/${id}`);
};

// ΓöÇΓöÇΓöÇ Customers ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const listCustomers = async (page = 0, size = 20, search = '') => {
  const res = await API.get('/crm/customers', { params: { page, size, search } });
  return res.data?.data;
};

export const getCustomer = async (id) => {
  const res = await API.get(`/crm/customers/${id}`);
  return res.data?.data;
};

export const createCustomer = async (data) => {
  const res = await API.post('/crm/customers', data);
  return res.data?.data;
};

export const updateCustomer = async (id, data) => {
  const res = await API.put(`/crm/customers/${id}`, data);
  return res.data?.data;
};

export const deleteCustomer = async (id) => {
  await API.delete(`/crm/customers/${id}`);
};

export const linkContactToCustomer = async (customerId, contactId, relation) => {
  const res = await API.post(`/crm/customers/${customerId}/contacts`, { contactId, relation });
  return res.data?.data;
};

export const unlinkContactFromCustomer = async (customerId, contactId) => {
  await API.delete(`/crm/customers/${customerId}/contacts/${contactId}`);
};

export const getCustomerSummary = async (customerId) => {
  const res = await API.get(`/crm/customers/${customerId}/summary`);
  return res.data?.data;
};

// ΓöÇΓöÇΓöÇ Leads ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const listLeads = async (page = 0, size = 20, search = '', status = '') => {
  const res = await API.get('/crm/leads', { params: { page, size, search, status } });
  return res.data?.data;
};

export const getLead = async (id) => {
  const res = await API.get(`/crm/leads/${id}`);
  return res.data?.data;
};

export const createLead = async (data) => {
  const res = await API.post('/crm/leads', data);
  return res.data?.data;
};

export const updateLead = async (id, data) => {
  const res = await API.put(`/crm/leads/${id}`, data);
  return res.data?.data;
};

export const deleteLead = async (id) => {
  await API.delete(`/crm/leads/${id}`);
};

export const convertLead = async (id) => {
  const res = await API.post(`/crm/leads/${id}/convert`);
  return res.data?.data;
};

// ΓöÇΓöÇΓöÇ Pipelines ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const listPipelines = async () => {
  const res = await API.get('/crm/pipelines');
  const data = res.data?.data;
  return Array.isArray(data) ? data : data?.content || [];
};

export const getPipeline = async (id) => {
  const res = await API.get(`/crm/pipelines/${id}`);
  return res.data?.data;
};

export const createPipeline = async (data) => {
  const res = await API.post('/crm/pipelines', data);
  return res.data?.data;
};

export const updatePipeline = async (id, data) => {
  const res = await API.put(`/crm/pipelines/${id}`, data);
  return res.data?.data;
};

export const deletePipeline = async (id) => {
  await API.delete(`/crm/pipelines/${id}`);
};

export const listPipelineStages = async (pipelineId) => {
  const res = await API.get(`/crm/pipelines/${pipelineId}/stages`);
  return Array.isArray(res.data?.data) ? res.data.data : res.data?.data?.content || [];
};

export const createPipelineStage = async (pipelineId, data) => {
  const res = await API.post(`/crm/pipelines/${pipelineId}/stages`, data);
  return res.data?.data;
};

export const updatePipelineStage = async (pipelineId, stageId, data) => {
  const res = await API.put(`/crm/pipelines/${pipelineId}/stages/${stageId}`, data);
  return res.data?.data;
};

export const deletePipelineStage = async (pipelineId, stageId) => {
  await API.delete(`/crm/pipelines/${pipelineId}/stages/${stageId}`);
};

// ΓöÇΓöÇΓöÇ Opportunities ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const listOpportunities = async (page = 0, size = 20, search = '') => {
  const res = await API.get('/crm/opportunities', { params: { page, size, search } });
  return res.data?.data;
};

export const getOpportunity = async (id) => {
  const res = await API.get(`/crm/opportunities/${id}`);
  return res.data?.data;
};

export const createOpportunity = async (data) => {
  const res = await API.post('/crm/opportunities', data);
  return res.data?.data;
};

export const updateOpportunity = async (id, data) => {
  const res = await API.put(`/crm/opportunities/${id}`, data);
  return res.data?.data;
};

export const deleteOpportunity = async (id) => {
  await API.delete(`/crm/opportunities/${id}`);
};

export const markOpportunityWon = async (id) => {
  const res = await API.post(`/crm/opportunities/${id}/won`);
  return res.data?.data;
};

export const markOpportunityLost = async (id, reason) => {
  const res = await API.post(`/crm/opportunities/${id}/lost`, { reason });
  return res.data?.data;
};

export const changeOpportunityStage = async (id, stageId) => {
  const res = await API.post(`/crm/opportunities/${id}/stage/${stageId}`);
  return res.data?.data;
};

// ΓöÇΓöÇΓöÇ Activities ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const listActivities = async (page = 0, size = 20, entityType = '', entityId = '') => {
  const res = await API.get('/crm/activities', { params: { page, size, entityType, entityId } });
  return res.data?.data;
};

export const listActivitiesByLead = async (leadId) => {
  const res = await API.get(`/crm/activities/lead/${leadId}`);
  return Array.isArray(res.data?.data) ? res.data.data : res.data || [];
};

export const listActivitiesByCustomer = async (customerId) => {
  const res = await API.get(`/crm/activities/customer/${customerId}`);
  return Array.isArray(res.data?.data) ? res.data.data : res.data || [];
};

export const listActivitiesByOpportunity = async (opportunityId) => {
  const res = await API.get(`/crm/activities/opportunity/${opportunityId}`);
  return Array.isArray(res.data?.data) ? res.data.data : res.data || [];
};

export const getActivity = async (id) => {
  const res = await API.get(`/crm/activities/${id}`);
  return res.data?.data;
};

export const createActivity = async (data) => {
  const res = await API.post('/crm/activities', data);
  return res.data?.data;
};

export const updateActivity = async (id, data) => {
  const res = await API.put(`/crm/activities/${id}`, data);
  return res.data?.data;
};

export const deleteActivity = async (id) => {
  await API.delete(`/crm/activities/${id}`);
};

// ΓöÇΓöÇΓöÇ Dashboard ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const getCrmDashboard = async () => {
  const res = await API.get('/crm/dashboard');
  return res.data?.data;
};

// ΓöÇΓöÇΓöÇ Audit ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const listAuditEvents = async (page = 0, size = 20, filters = {}) => {
  const res = await API.get('/crm/audit', { params: { page, size, ...filters } });
  return res.data?.data;
};

export const getEntityAuditHistory = async (entityType, entityId) => {
  const workspaceId = localStorage.getItem('activeWorkspaceId');
  const res = await API.get(`/crm/audit/entity/${entityType}/${entityId}`, { params: { workspaceId } });
  return res.data?.data;
};

// ΓöÇΓöÇΓöÇ CRM Γåö Project Links ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
export const getProjectByOpportunity = async (opportunityId) => {
  const res = await API.get(`/crm/opportunities/${opportunityId}/project`);
  return res.data?.data;
};

export const getProjectsByCustomer = async (customerId) => {
  const res = await API.get(`/crm/customers/${customerId}/projects`);
  return Array.isArray(res.data?.data) ? res.data.data : [];
};

export const getOpportunitiesByLead = async (leadId) => {
  const res = await API.get(`/crm/leads/${leadId}/opportunities`);
  return Array.isArray(res.data?.data) ? res.data.data : [];
};
