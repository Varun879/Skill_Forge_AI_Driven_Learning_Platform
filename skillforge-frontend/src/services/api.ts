import axios from 'axios';

const apiBaseUrl = (import.meta as any).env?.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: apiBaseUrl,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token') || sessionStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
