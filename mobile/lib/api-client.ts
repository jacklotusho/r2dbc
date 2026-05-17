import axios from 'axios';
import { secureStorage } from './secure-storage';

const apiClient = axios.create({
  baseURL: process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080/demo',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach Bearer token from SecureStore
apiClient.interceptors.request.use(
  async (config) => {
    const token = await secureStorage.getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid - clear auth and redirect handled by router
      await secureStorage.clearAuth();
    }
    return Promise.reject(error);
  }
);

export default apiClient;


