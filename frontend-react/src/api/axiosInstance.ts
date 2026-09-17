import axios from 'axios';

const API_URL = 'http://localhost:8080';

const axiosInstance = axios.create({
  baseURL: API_URL,
});

// ─── Request interceptor: attach token ───────────────────────────────────────
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = token;
  }
  return config;
});

// ─── Response interceptor: handle 401 + token refresh ────────────────────────
let isRefreshing = false;
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: any) => void }> = [];

function processQueue(error: any, token: string | null = null) {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token!);
    }
  });
  failedQueue = [];
}

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    const isAuthEndpoint =
      originalRequest.url?.includes('/auth/login') ||
      originalRequest.url?.includes('/auth/refresh');

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = token;
          return axiosInstance(originalRequest);
        }).catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      const refreshToken = localStorage.getItem('refreshToken');

      try {
        const res = await axios.post(`${API_URL}/auth/refresh`, { refreshToken });
        const { token, tokenType, refreshToken: newRefreshToken } = res.data;
        const fullToken = `${tokenType} ${token}`;
        localStorage.setItem('token', fullToken);
        if (newRefreshToken) localStorage.setItem('refreshToken', newRefreshToken);
        processQueue(null, fullToken);
        originalRequest.headers.Authorization = fullToken;
        return axiosInstance(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
