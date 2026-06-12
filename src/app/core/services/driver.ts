import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DriverService {
  private baseUrl = 'http://localhost:8080/driver';

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
