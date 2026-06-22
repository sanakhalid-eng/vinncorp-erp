import axios from '../../../api/axios';

export const getBoardByProject = async (projectId: any) => {
  const response = await axios.get(`/boards/project/${projectId}`);
  return response.data;
};

export const createBoard = async (projectId: any, name: any= 'Project Board') => {
  const response = await axios.post(`/boards/project/${projectId}`, { name });
  return response.data;
};

export const updateColumnOrder = async (boardId: any, columnIds: any) => {
  const response = await axios.put(`/boards/${boardId}/columns/order`, columnIds);
  return response.data;
};

export const addColumn = async (boardId: any, name: any) => {
  const response = await axios.post(`/boards/${boardId}/columns`, { name });
  return response.data;
};

export const deleteColumn = async (columnId: any) => {
  const response = await axios.delete(`/boards/columns/${columnId}`);
  return response.data;
};

export const updateColumn = async (columnId: any, name: any) => {
  const response = await axios.patch(`/boards/columns/${columnId}`, { name });
  return response.data;
};
