import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DriverService {
  private baseUrl = `${environment.apiUrl}/driver`;

  constructor(private http: HttpClient) {}

  getProfile() {
    return this.http.get<any>(`${this.baseUrl}/me`);
  }

  updateLocation(location: string) {
    return this.http.patch(`${this.baseUrl}/location`, { currentLocation: location });
  }

  updateStatus(status: string) {
    return this.http.patch(`${this.baseUrl}/status`, { status });
  }
}
