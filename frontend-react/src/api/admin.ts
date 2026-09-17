import axiosInstance from './axiosInstance';
import type { Car, DriverProfile, Booking } from '../types';

export const adminApi = {
  // ── Cars ──────────────────────────────────────────────────
  getCars: () => axiosInstance.get<Car[]>('/admin/cars'),
  addCar: (data: any) => axiosInstance.post<Car>('/admin/cars', data),
  updateCar: (id: number, data: any) => axiosInstance.put<Car>(`/admin/cars/${id}`, data),
  disableCar: (id: number) => axiosInstance.delete(`/admin/cars/${id}`, { responseType: 'text' }),

  // ── Drivers ───────────────────────────────────────────────
  getDrivers: () => axiosInstance.get<DriverProfile[]>('/admin/drivers'),
  addDriver: (data: any) => axiosInstance.post<DriverProfile>('/admin/drivers', data),
  updateDriver: (id: number, data: any) => axiosInstance.put<DriverProfile>(`/admin/drivers/${id}`, data),
  disableDriver: (id: number) => axiosInstance.delete(`/admin/drivers/${id}`, { responseType: 'text' }),

  // ── Bookings ──────────────────────────────────────────────
  getBookings: () => axiosInstance.get<Booking[]>('/admin/bookings'),
  verifyLicense: (id: number, approved: boolean) =>
    axiosInstance.post<Booking>(`/admin/bookings/${id}/verify-license?approved=${approved}`, {}),
  assignDriver: (id: number, driverId: number) =>
    axiosInstance.post<Booking>(`/admin/bookings/${id}/assign-driver/${driverId}`, {}),
  completeInspection: (id: number) =>
    axiosInstance.post<Booking>(`/admin/bookings/${id}/complete-inspection`, {}),
};
