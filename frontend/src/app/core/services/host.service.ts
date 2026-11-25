import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class HostService {
  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getOnboardingStatus(): Observable<any> {
    return this.http.get(`${this.baseUrl}/api/host-onboarding/status`);
  }

  saveOnboardingDraft(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/host-onboarding/apply`, payload);
  }

  uploadDocument(file: File, docKind: string = 'GOVERNMENT_ID'): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(
      `${this.baseUrl}/api/host-onboarding/documents/upload?docKind=${encodeURIComponent(docKind)}`,
      formData
    );
  }

  submitOnboarding(): Observable<any> {
    return this.http.post(`${this.baseUrl}/api/host-onboarding/submit`, {});
  }

  getHostDashboard(): Observable<any> {
    return this.http.get(`${this.baseUrl}/host/dashboard`);
  }

  getMyVehicles(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/host/vehicles`);
  }

  createVehicle(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/host/vehicles`, payload);
  }

  toggleVehicleAvailability(carId: number, active: boolean): Observable<any> {
    return this.http.patch(`${this.baseUrl}/host/vehicles/${carId}/availability`, { active });
  }

  getMyBookings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/host/bookings`);
  }

  getMyEarnings(): Observable<any> {
    return this.http.get(`${this.baseUrl}/host/earnings`);
  }
}
