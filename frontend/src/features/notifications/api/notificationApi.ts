import API from '../../../api/axios';

// Notification Inbox
export const getNotifications = async (page: any= 0, size: any= 10) => {
  try {
    const res = await API.get('/notifications', { params: { page, size } });
    return res.data;
  } catch (error) {
    console.error('Get notifications error:', error);
    throw error;
  }
};

export const getUnreadCount = async () => {
  try {
    const res = await API.get('/notifications/unread-count');
    return res.data;
  } catch (error) {
    console.error('Get unread count error:', error);
    throw error;
  }
};

export const markAsRead = async (id: any) => {
  try {
    const res = await API.put(`/notifications/${id}/read`);
    return res.data;
  } catch (error) {
    console.error('Mark as read error:', error);
    throw error;
  }
};

export const markAllAsRead = async () => {
  try {
    const res = await API.put('/notifications/read-all');
    return res.data;
  } catch (error) {
    console.error('Mark all as read error:', error);
    throw error;
  }
};

export const deleteNotification = async (id: any) => {
  try {
    const res = await API.delete(`/notifications/${id}`);
    return res.data;
  } catch (error) {
    console.error('Delete notification error:', error);
    throw error;
  }
};

export const getFilteredNotifications = async (params: any= {}) => {
  try {
    const res = await API.get('/notifications/filtered', { params });
    return res.data;
  } catch (error) {
    console.error('Get filtered notifications error:', error);
    throw error;
  }
};

export const getUnreadCountByCategory = async () => {
  try {
    const res = await API.get('/notifications/unread-by-category');
    return res.data;
  } catch (error) {
    console.error('Get unread count by category error:', error);
    throw error;
  }
};

// Notification Preferences
export const getNotificationPreferences = async () => {
  try {
    const res = await API.get('/notifications/preferences');
    return res.data;
  } catch (error) {
    console.error('Get notification prefs error:', error);
    throw error;
  }
};

export const updateNotificationPreferences = async (preferences: any) => {
  try {
    const res = await API.put('/notifications/preferences', preferences);
    return res.data;
  } catch (error) {
    console.error('Update notification prefs error:', error);
    throw error;
  }
};
