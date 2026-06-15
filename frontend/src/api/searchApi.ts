import API from "./axios";

export const globalSearch = (q: any) => API.get("/search", { params: { q } });
