import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DriverProfile,
  AppUser,
  ServiceCity,
  CarPricing,
  DashboardStats,
  HostSummary,
  HostDetail,
  UserDetail,
  PlatformSettings,
  AuditLog
} from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SuperAdminService {

  private baseUrl = `${environment.apiUrl}/super-admin`;

  constructor(private http: HttpClient) {}

  // ================= ADMINS =================

  getAdmins(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.baseUrl}/admins`);
  }

  createAdmin(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/admins`, data);
  }

  deactivateAdmin(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/admins/${id}/deactivate`, {});
  }

  reactivateAdmin(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/admins/${id}/reactivate`, {});
  }

  deleteAdmin(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/admins/${id}`);
  }

  // ================= USERS =================

  getUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.baseUrl}/users`);
  }

  getUserDetails(id: number): Observable<UserDetail> {
    return this.http.get<UserDetail>(`${this.baseUrl}/users/${id}/details`);
  }

  deactivateUser(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/users/${id}/deactivate`, {});
  }

  reactivateUser(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/users/${id}/reactivate`, {});
  }

  suspendUser(id: number, reason: string): Observable<any> {
    return this.http.patch(`${this.baseUrl}/users/${id}/suspend`, { reason });
  }

  unsuspendUser(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/users/${id}/unsuspend`, {});
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/users/${id}`);
  }

  // ================= HOSTS (REPLACES DRIVERS) =================

  getHosts(search?: string, verificationStatus?: string, active?: string): Observable<HostSummary[]> {
    let params = new HttpParams();
    if (search) params = params.set('search', search);
    if (verificationStatus) params = params.set('verificationStatus', verificationStatus);
    if (active === 'true' || active === 'false') params = params.set('active', active);
    return this.http.get<HostSummary[]>(`${this.baseUrl}/hosts`, { params });
  }

  getHostDetails(id: number): Observable<HostDetail> {
    return this.http.get<HostDetail>(`${this.baseUrl}/hosts/${id}`);
  }

  verifyHost(id: number, status: string, reason?: string): Observable<HostSummary> {
    return this.http.patch<HostSummary>(`${this.baseUrl}/hosts/${id}/verify`, { status, reason });
  }

  deactivateHost(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/hosts/${id}/deactivate`, {});
  }

  reactivateHost(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/hosts/${id}/reactivate`, {});
  }

  suspendHost(id: number, reason: string): Observable<any> {
    return this.http.patch(`${this.baseUrl}/hosts/${id}/suspend`, { reason });
  }

  unsuspendHost(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/hosts/${id}/unsuspend`, {});
  }

  // Legacy driver methods preserved for backward compatibility
  getDrivers(): Observable<DriverProfile[]> {
    return this.http.get<DriverProfile[]>(`${this.baseUrl}/drivers`);
  }

  deactivateDriver(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/drivers/${id}/deactivate`, {});
  }

  reactivateDriver(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/drivers/${id}/reactivate`, {});
  }

  // ================= CITIES =================

  getCities(): Observable<ServiceCity[]> {
    return this.http.get<ServiceCity[]>(`${this.baseUrl}/cities`);
  }

  createCity(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/cities`, data);
  }

  updateCity(id: number, data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/cities/${id}`, data);
  }

  deactivateCity(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/cities/${id}/deactivate`, {});
  }

  reactivateCity(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/cities/${id}/reactivate`, {});
  }

  deleteCity(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/cities/${id}`);
  }

  assignAdmin(cityId: number, adminId: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/cities/${cityId}/assign-admin/${adminId}`, {});
  }

  updateOneWayFee(cityId: number, fee: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/cities/${cityId}/one-way-fee?fee=${fee}`, {});
  }

  // ================= PRICING =================

  getPricing(): Observable<CarPricing[]> {
    return this.http.get<CarPricing[]>(`${this.baseUrl}/pricing`);
  }

  createPricing(data: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/pricing`, data);
  }

  updatePricing(id: number, data: any): Observable<any> {
    return this.http.put(`${this.baseUrl}/pricing/${id}`, data);
  }

  deactivatePricing(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/pricing/${id}/deactivate`, {});
  }

  reactivatePricing(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/pricing/${id}/reactivate`, {});
  }

  deletePricing(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/pricing/${id}`);
  }

  // ================= FLEET MANAGERS =================

  getFleetManagers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.baseUrl}/fleet-managers`);
  }

  createFleetManager(data: any): Observable<AppUser> {
    return this.http.post<AppUser>(`${this.baseUrl}/fleet-managers`, data);
  }

  assignFleetManagerScope(id: number, city: string, operationalScope: string): Observable<AppUser> {
    return this.http.patch<AppUser>(`${this.baseUrl}/fleet-managers/${id}/assign-scope`, { city, operationalScope });
  }

  deactivateFleetManager(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/fleet-managers/${id}/deactivate`, {});
  }

  reactivateFleetManager(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/fleet-managers/${id}/reactivate`, {});
  }

  // ================= SUPPORT AGENTS =================

  getSupportAgents(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.baseUrl}/support-agents`);
  }

  createSupportAgent(data: any): Observable<AppUser> {
    return this.http.post<AppUser>(`${this.baseUrl}/support-agents`, data);
  }

  assignSupportAgentScope(id: number, city: string, operationalScope: string): Observable<AppUser> {
    return this.http.patch<AppUser>(`${this.baseUrl}/support-agents/${id}/assign-scope`, { city, operationalScope });
  }

  deactivateSupportAgent(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/support-agents/${id}/deactivate`, {});
  }

  reactivateSupportAgent(id: number): Observable<any> {
    return this.http.patch(`${this.baseUrl}/support-agents/${id}/reactivate`, {});
  }

  // ================= PLATFORM SETTINGS & AUDIT LOGS =================

  getPlatformSettings(): Observable<PlatformSettings> {
    return this.http.get<PlatformSettings>(`${this.baseUrl}/settings`);
  }

  updatePlatformSettings(data: PlatformSettings): Observable<PlatformSettings> {
    return this.http.put<PlatformSettings>(`${this.baseUrl}/settings`, data);
  }

  getAuditLogs(page = 0, size = 20): Observable<{ content: AuditLog[]; totalElements: number; totalPages: number }> {
    return this.http.get<any>(`${this.baseUrl}/audit-logs?page=${page}&size=${size}`);
  }

  // ================= DASHBOARD =================

  getDashboardStats(): Observable<DashboardStats> {
    return this.http.get<DashboardStats>(`${this.baseUrl}/dashboard/stats`);
  }
}
