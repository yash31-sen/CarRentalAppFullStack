import axiosInstance from './axiosInstance';

export const driverApi = {
  getProfile: () => axiosInstance.get<any>('/driver/me'),
  updateLocation: (currentLocation: string) =>
    axiosInstance.patch('/driver/location', { currentLocation }),
  updateStatus: (status: string) =>
    axiosInstance.patch('/driver/status', { status }),
};
