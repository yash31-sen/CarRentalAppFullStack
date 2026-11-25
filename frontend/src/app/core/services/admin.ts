import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { HostDetail, HostSummary } from '../models';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private baseAdminUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  // ================= OVERVIEW DASHBOARD =================

  getDashboardStats(city?: string): Observable<any> {
    let params = new HttpParams();
    if (city) params = params.set('city', city);
    return this.http.get<any>(`${this.baseAdminUrl}/dashboard/stats`, { params });
  }

  // ================= VEHICLE LISTINGS =================

  getCars(filters?: { city?: string; approvalStatus?: string; status?: string; category?: string; search?: string }): Observable<any[]> {
    let params = new HttpParams();
    if (filters?.city) params = params.set('city', filters.city);
    if (filters?.approvalStatus) params = params.set('approvalStatus', filters.approvalStatus);
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.category) params = params.set('category', filters.category);
    if (filters?.search) params = params.set('search', filters.search);
    return this.http.get<any[]>(`${this.baseAdminUrl}/cars`, { params });
  }

  getCarDetails(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseAdminUrl}/cars/${id}`);
  }

  approveCar(id: number, remarks?: string): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/cars/${id}/approve`, { remarks });
  }

  rejectCar(id: number, reason: string): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/cars/${id}/reject`, { reason });
  }

  requestCarResubmission(id: number, reason: string): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/cars/${id}/request-resubmission`, { reason });
  }

  suspendCar(id: number, reason: string): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/cars/${id}/suspend`, { reason });
  }

  disableCar(id: number, reason?: string): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/cars/${id}/disable`, { reason });
  }

  reactivateCar(id: number): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/cars/${id}/reactivate`, {});
  }

  // ================= HOST MANAGEMENT =================

  getHosts(filters?: { city?: string; verificationStatus?: string; accountStatus?: string; search?: string }): Observable<HostSummary[]> {
    let params = new HttpParams();
    if (filters?.city) params = params.set('city', filters.city);
    if (filters?.verificationStatus) params = params.set('verificationStatus', filters.verificationStatus);
    if (filters?.accountStatus) params = params.set('accountStatus', filters.accountStatus);
    if (filters?.search) params = params.set('search', filters.search);
    return this.http.get<HostSummary[]>(`${this.baseAdminUrl}/hosts`, { params });
  }

  getHostDetails(id: number): Observable<HostDetail> {
    return this.http.get<HostDetail>(`${this.baseAdminUrl}/hosts/${id}`);
  }

  approveHost(id: number): Observable<HostSummary> {
    return this.http.patch<HostSummary>(`${this.baseAdminUrl}/hosts/${id}/approve`, {});
  }

  rejectHost(id: number, reason: string): Observable<HostSummary> {
    return this.http.patch<HostSummary>(`${this.baseAdminUrl}/hosts/${id}/reject`, { reason });
  }

  requestHostResubmission(id: number, reason: string): Observable<HostSummary> {
    return this.http.patch<HostSummary>(`${this.baseAdminUrl}/hosts/${id}/request-resubmission`, { reason });
  }

  suspendHost(id: number, reason: string): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/hosts/${id}/suspend`, { reason });
  }

  deactivateHost(id: number): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/hosts/${id}/deactivate`, {});
  }

  reactivateHost(id: number): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/hosts/${id}/reactivate`, {});
  }

  // ================= BOOKING OPERATIONS (SELF-DRIVE) =================

  getBookings(filters?: { city?: string; status?: string; paymentStatus?: string; search?: string }): Observable<any[]> {
    let params = new HttpParams();
    if (filters?.city) params = params.set('city', filters.city);
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.paymentStatus) params = params.set('paymentStatus', filters.paymentStatus);
    if (filters?.search) params = params.set('search', filters.search);
    return this.http.get<any[]>(`${this.baseAdminUrl}/bookings`, { params });
  }

  getBookingDetails(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseAdminUrl}/bookings/${id}`);
  }

  verifyLicense(id: number, approved: boolean, resubmissionRequested = false, remarks?: string): Observable<any> {
    return this.http.post<any>(
      `${this.baseAdminUrl}/bookings/${id}/verify-license?approved=${approved}&resubmissionRequested=${resubmissionRequested}`,
      { remarks }
    );
  }

  // ================= BOOKING ISSUES & ESCALATIONS =================

  getIssues(city?: string): Observable<any[]> {
    let params = new HttpParams();
    if (city) params = params.set('city', city);
    return this.http.get<any[]>(`${this.baseAdminUrl}/issues`, { params });
  }

  getSupportAgents(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseAdminUrl}/support-agents`);
  }

  escalateIssue(id: number, supportAgentId: number | null, remarks: string): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/issues/${id}/escalate`, { supportAgentId, remarks });
  }

  resolveIssue(id: number, resolutionDetails: string, status = 'RESOLVED'): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/issues/${id}/resolve`, { resolutionDetails, status });
  }

  // ================= INSPECTION MANAGEMENT =================

  getInspections(filters?: { city?: string; status?: string; type?: string }): Observable<any[]> {
    let params = new HttpParams();
    if (filters?.city) params = params.set('city', filters.city);
    if (filters?.status) params = params.set('status', filters.status);
    if (filters?.type) params = params.set('type', filters.type);
    return this.http.get<any[]>(`${this.baseAdminUrl}/inspections`, { params });
  }

  getInspectionDetails(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseAdminUrl}/inspections/${id}`);
  }

  escalateInspection(id: number, remarks: string, suspendVehicle: boolean): Observable<any> {
    return this.http.patch<any>(`${this.baseAdminUrl}/inspections/${id}/escalate`, { remarks, suspendVehicle });
  }
}
