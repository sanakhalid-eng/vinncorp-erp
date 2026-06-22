import API from "../../../api/axios";

export const globalSearch = (q: any) => API.get("/search", { params: { q } });
