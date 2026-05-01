import axios from "axios";

const BASE_URL = "http://localhost:8080";

const api = axios.create({ baseURL: BASE_URL });

// Attach JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("jwt");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ── Auth ──
export const login    = (username, password) =>
  api.post("/api/auth/login", { username, password });

export const register = (username, email, password) =>
  api.post("/api/auth/register", { username, email, password });

export const logout   = () => api.post("/api/auth/logout");

// ── Files ──
export const listFiles     = () => api.get("/api/files/list");
export const getSharedByMe = () => api.get("/api/files/shared-by-me");

// Only chunked upload remains
export const uploadChunked = (file, onProgress) => {
  const formData = new FormData();
  formData.append("file", file);
  return api.post("/api/files/upload-chunked", formData, {
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress: (e) => {
      if (onProgress) onProgress(Math.round((e.loaded * 100) / e.total));
    },
  });
};

export const deleteFile = (fileId) => api.delete(`/api/files/${fileId}`);

export const downloadFile = (fileId, filename) =>
  api.get(`/api/files/download/${fileId}`, { responseType: "blob" }).then((res) => {
    const url  = window.URL.createObjectURL(new Blob([res.data]));
    const link = document.createElement("a");
    link.href  = url;
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  });

export const shareFile = (fileId, recipientUsernameOrEmail) =>
  api.post(`/api/files/share/${fileId}`, null, {
    params: { recipientUsernameOrEmail },
  });