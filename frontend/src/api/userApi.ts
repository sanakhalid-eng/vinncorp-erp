import API from "./axios";

const normalizeUser = (user: any= {}) => ({
  ...user,
  // Backend enforces 1 system role per user now
  role: Array.isArray(user.roles) && user.roles.length > 0 ? user.roles[0] : (user.role || ""),
  roles: Array.isArray(user.roles) ? user.roles : (user.role ? [user.role] : []),
  projectCount: user.projectCount ?? 0,
});

// 🔹 Get current user
export const getProfile = () => API.get("/users/me");

// 🔹 Update profile
export const updateProfile = (data: any) => API.put("/users/me", data);

// 🔹 Change password
export const changePassword = (data: any) =>
  API.put("/users/change-password", data);

// 🔹 Deactivate account
export const deactivateAccount = () =>
  API.put("/users/deactivate");

// 🔹 Upload Avatar
export const uploadAvatar = (file: any) => {
  const formData = new FormData();
  formData.append("file", file);

  return API.put("/users/me/avatar", formData, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
};

// 🔹 Admin CRUD
export const getAllUsers = async () => {
  try {
    const res = await API.get('/users');
    return (res.data.data || []).map(normalizeUser);
  } catch (error) {
    console.error('Get all users error:', error);
    throw error;
  }
};

export const createUser = async (data: any) => {
  try {
    const res = await API.post('/users', data);
    return normalizeUser(res.data.data);
  } catch (error) {
    console.error('Create user error:', error);
    throw error;
  }
};

export const updateUser = async (id: any, data: any) => {
  try {
    const res = await API.put(`/users/${id}`, data);
    return normalizeUser(res.data.data);
  } catch (error) {
    console.error('Update user error:', error);
    throw error;
  }
};

export const deleteUser = async (id: any) => {
  try {
    const res = await API.delete(`/users/${id}`);
    return res.data;
  } catch (error) {
    console.error('Delete user error:', error);
    throw error;
  }
};
