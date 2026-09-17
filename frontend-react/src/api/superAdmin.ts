import axiosInstance from './axiosInstance';
import type { AppUser, DriverProfile, ServiceCity, CarPricing } from '../types';

export const superAdminApi = {
  // ── Admins ────────────────────────────────────────────────
  getAdmins: () => axiosInstance.get<AppUser[]>('/super-admin/admins'),
  createAdmin: (data: any) => axiosInstance.post('/super-admin/admins', data),
  deactivateAdmin: (id: number) => axiosInstance.patch(`/super-admin/admins/${id}/deactivate`, {}),
  reactivateAdmin: (id: number) => axiosInstance.patch(`/super-admin/admins/${id}/reactivate`, {}),
  deleteAdmin: (id: number) => axiosInstance.delete(`/super-admin/admins/${id}`),

  // ── Users ─────────────────────────────────────────────────
  getUsers: () => axiosInstance.get<AppUser[]>('/super-admin/users'),
  deactivateUser: (id: number) => axiosInstance.patch(`/super-admin/users/${id}/deactivate`, {}),
  reactivateUser: (id: number) => axiosInstance.patch(`/super-admin/users/${id}/reactivate`, {}),
  deleteUser: (id: number) => axiosInstance.delete(`/super-admin/users/${id}`),

  // ── Drivers ───────────────────────────────────────────────
  getDrivers: () => axiosInstance.get<DriverProfile[]>('/super-admin/drivers'),
  deactivateDriver: (id: number) => axiosInstance.patch(`/super-admin/drivers/${id}/deactivate`, {}),
  reactivateDriver: (id: number) => axiosInstance.patch(`/super-admin/drivers/${id}/reactivate`, {}),

  // ── Cities ────────────────────────────────────────────────
  getCities: () => axiosInstance.get<ServiceCity[]>('/super-admin/cities'),
  createCity: (data: any) => axiosInstance.post('/super-admin/cities', data),
  deactivateCity: (id: number) => axiosInstance.patch(`/super-admin/cities/${id}/deactivate`, {}),
  reactivateCity: (id: number) => axiosInstance.patch(`/super-admin/cities/${id}/reactivate`, {}),
  assignAdmin: (cityId: number, adminId: number) =>
    axiosInstance.patch(`/super-admin/cities/${cityId}/assign-admin/${adminId}`, {}),
  updateOneWayFee: (cityId: number, fee: number) =>
    axiosInstance.patch(`/super-admin/cities/${cityId}/one-way-fee?fee=${fee}`, {}),

  // ── Pricing ───────────────────────────────────────────────
  getPricing: () => axiosInstance.get<CarPricing[]>('/super-admin/pricing'),
  createPricing: (data: any) => axiosInstance.post('/super-admin/pricing', data),
  updatePricing: (id: number, data: any) => axiosInstance.put(`/super-admin/pricing/${id}`, data),
  deletePricing: (id: number) => axiosInstance.delete(`/super-admin/pricing/${id}`),

  // ── Dashboard ─────────────────────────────────────────────
  getDashboardStats: () => axiosInstance.get<any>('/super-admin/dashboard/stats'),
};
