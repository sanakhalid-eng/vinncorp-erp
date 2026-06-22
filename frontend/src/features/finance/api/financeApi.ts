import API from '../../../api/axios.js';

const parseJSON = (str) => {
  try { return JSON.parse(str); } catch { return null; }
};

const normalizeResponse = (res) => {
  const body = typeof res.data === 'string' ? parseJSON(res.data) : res.data;
  return body?.data || body;
};

export const getFinanceDashboard = async () => {
  const res = await API.get('/finance/dashboard');
  return normalizeResponse(res);
};

export const listInvoices = async (params = {}) => {
  const res = await API.get('/finance/invoices', { params });
  return normalizeResponse(res);
};

export const getInvoice = async (id) => {
  const res = await API.get(`/finance/invoices/${id}`);
  return normalizeResponse(res);
};

export const createInvoice = async (data) => {
  const res = await API.post('/finance/invoices', data);
  return normalizeResponse(res);
};

export const updateInvoice = async (id, data) => {
  const res = await API.put(`/finance/invoices/${id}`, data);
  return normalizeResponse(res);
};

export const deleteInvoice = async (id) => {
  await API.delete(`/finance/invoices/${id}`);
};

export const sendInvoice = async (id) => {
  const res = await API.post(`/finance/invoices/${id}/send`);
  return normalizeResponse(res);
};

export const markInvoicePaid = async (id) => {
  const res = await API.post(`/finance/invoices/${id}/mark-paid`);
  return normalizeResponse(res);
};

export const listPayments = async (params = {}) => {
  const res = await API.get('/finance/payments', { params });
  return normalizeResponse(res);
};

export const getPayment = async (id) => {
  const res = await API.get(`/finance/payments/${id}`);
  return normalizeResponse(res);
};

export const createPayment = async (data) => {
  const res = await API.post('/finance/payments', data);
  return normalizeResponse(res);
};

export const updatePayment = async (id, data) => {
  const res = await API.put(`/finance/payments/${id}`, data);
  return normalizeResponse(res);
};

export const deletePayment = async (id) => {
  await API.delete(`/finance/payments/${id}`);
};

export const listExpenses = async (params = {}) => {
  const res = await API.get('/finance/expenses', { params });
  return normalizeResponse(res);
};

export const getExpense = async (id) => {
  const res = await API.get(`/finance/expenses/${id}`);
  return normalizeResponse(res);
};

export const createExpense = async (data) => {
  const res = await API.post('/finance/expenses', data);
  return normalizeResponse(res);
};

export const updateExpense = async (id, data) => {
  const res = await API.put(`/finance/expenses/${id}`, data);
  return normalizeResponse(res);
};

export const deleteExpense = async (id) => {
  await API.delete(`/finance/expenses/${id}`);
};

export const approveExpense = async (id) => {
  const res = await API.post(`/finance/expenses/${id}/approve`);
  return normalizeResponse(res);
};

export const rejectExpense = async (id) => {
  const res = await API.post(`/finance/expenses/${id}/reject`);
  return normalizeResponse(res);
};

export const reimburseExpense = async (id) => {
  const res = await API.post(`/finance/expenses/${id}/reimburse`);
  return normalizeResponse(res);
};
