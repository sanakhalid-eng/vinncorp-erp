import API from '../../../api/axios';

const unwrapData = (response: any) => response?.data?.data ?? response?.data ?? {};

const normalizeActivity = (activity: any) => ({
  ...activity,
  user: activity?.user ?? null,
  entityType: activity?.entityType ?? 'UNKNOWN',
  action: activity?.action ?? 'UNKNOWN',
  oldValue: activity?.oldValue ?? null,
  newValue: activity?.newValue ?? null,
  description: activity?.description ?? '',
  createdAt: activity?.createdAt ?? null,
});

const normalizePage = (pageData: any) => {
  const content = Array.isArray(pageData?.content)
    ? pageData.content.map(normalizeActivity)
    : [];

  return {
    content,
    page: pageData?.page ?? 0,
    size: pageData?.size ?? content.length,
    totalElements: pageData?.totalElements ?? content.length,
    totalPages: pageData?.totalPages ?? (content.length > 0 ? 1 : 0),
    last: pageData?.last ?? true,
  };
};

export const getProjectActivities = async (projectId: any, page: any= 0, size: any= 20) => {
  try {
    const res = await API.get(
      `/activities/project/${projectId}?page=${page}&size=${size}`
    );
    return normalizePage(unwrapData(res));
  } catch (error) {
    console.error('Get project activities error:', error);
    throw error;
  }
};

export const getEntityActivities = async (entityType: any, entityId: any, page: any= 0, size: any= 20) => {
  try {
    const res = await API.get(
      `/activities/entity/${entityType}/${entityId}?page=${page}&size=${size}`
    );
    return normalizePage(unwrapData(res));
  } catch (error) {
    console.error('Get entity activities error:', error);
    throw error;
  }
};

export const getUserActivities = async (userId: any, page: any= 0, size: any= 20) => {
  try {
    const res = await API.get(
      `/activities/user/${userId}?page=${page}&size=${size}`
    );
    return normalizePage(unwrapData(res));
  } catch (error) {
    console.error('Get user activities error:', error);
    throw error;
  }
};

export const getActivityLogs = async (params: any= {}) => {
  try {
    const { limit = 10, page = 0, size = 10 } = params;
    const res = await API.get(`/activities`, {
      params: { page, size: limit || size }
    });
    return normalizePage(unwrapData(res));
  } catch (error) {
    console.error('Get activity logs error:', error);
    throw error;
  }
};

export const getFilteredActivities = async (params: any= {}) => {
  try {
    const res = await API.get('/activities/filter', { params });
    return res.data;
  } catch (error) {
    console.error('Get filtered activities error:', error);
    throw error;
  }
};
