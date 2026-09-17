import axiosInstance from './axiosInstance';

export const authApi = {
  login: (data: { email: string; password: string }) =>
    axiosInstance.post<any>('/auth/login', data),

  googleLogin: (credential: string) =>
    axiosInstance.post<any>('/auth/google', { credential }),

  register: (data: { name: string; email: string; password: string }) =>
    axiosInstance.post<any>('/auth/register', data),

  verifyEmail: (token: string) =>
    axiosInstance.get<any>(`/auth/verify?token=${token}`),

  forgotPassword: (email: string) =>
    axiosInstance.post('/auth/forgot-password', { email }, { responseType: 'text' }),

  resetPassword: (data: { token: string; newPassword: string }) =>
    axiosInstance.post('/auth/reset-password', data, { responseType: 'text' }),

  setupAccount: (data: any) =>
    axiosInstance.post('/auth/setup-account', data, { responseType: 'text' }),

  refreshToken: (refreshToken: string) =>
    axiosInstance.post<any>('/auth/refresh', { refreshToken }),
};
