import API from './axios.js';

/**
 * Project API - assumes backend endpoints exist
 */

const parseJSON = (str: any) => {
  try {
    return JSON.parse(str);
  } catch {
    let depth = 0;
    for (let i = 0; i < str.length; i++) {
      if (str[i] === '{') depth++;
      else if (str[i] === '}') {
        depth--;
        if (depth === 0) {
          try {
            return JSON.parse(str.slice(0, i + 1));
          } catch {
            break;
          }
        }
      }
    }
    console.error('Failed to parse API response as JSON');
    return null;
  }
};

const normalizeProject = (project: any= {}) => ({
  ...project,
  status: project.status ?? project.statusName ?? null,
  memberCount: project.memberCount ?? project.members?.length ?? 0,
});

// Create project
export const createProject = async (projectData: any) => {
  try {
    const res = await API.post('/projects', projectData);
    const body = typeof res.data === 'string' ? parseJSON(res.data) : res.data;
    return body ? normalizeProject(body.data || body) : null;
  } catch (error) {
    console.error('Create project error:', error);
    throw error;
  }
};

// List user's projects (paginated)
export const getUserProjects = async () => {
  try {
    const res = await API.get('/projects/user-projects');
    const body = typeof res.data === 'string' ? parseJSON(res.data) : res.data;
    if (!body) return [];
    const projects = Array.isArray(body?.data)
      ? body.data
      : Array.isArray(body)
      ? body
      : [];
    return projects.map(normalizeProject);
  } catch (error) {
    console.error('Get user projects error:', error);
    return [];
  }
};

// List ALL projects (admin only)
export const getProjects = async () => {
  try {
    const res = await API.get('/projects');
    const body = typeof res.data === 'string' ? parseJSON(res.data) : res.data;
    if (!body) return [];
    const data = Array.isArray(body?.data) ? body.data : Array.isArray(body) ? body : [];
    return data.map(normalizeProject);
  } catch (error) {
    console.error('Get all projects error:', error);
    return [];
  }
};

// Get single project
export const getProjectById = async (id: any) => {
  try {
    const res = await API.get(`/projects/${id}`);
    const body = typeof res.data === 'string' ? parseJSON(res.data) : res.data;
    return body ? normalizeProject(body.data || body) : null;
  } catch (error) {
    console.error('Get project error:', error);
    throw error;
  }
};

// Update project
export const updateProject = async (id: any, projectData: any) => {
  try {
    const res = await API.put(`/projects/${id}`, projectData);
    const body = typeof res.data === 'string' ? parseJSON(res.data) : res.data;
    return body ? normalizeProject(body.data || body) : null;
  } catch (error) {
    console.error('Update project error:', error);
    throw error;
  }
};

// Delete project
export const deleteProject = async (id: any) => {
  try {
    await API.delete(`/projects/${id}`);
  } catch (error) {
    console.error('Delete project error:', error);
    throw error;
  }
};
