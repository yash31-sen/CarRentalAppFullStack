import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Car, DriverProfile, Booking } from '../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private baseAdminUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  // ================= CARS =================

  getCars(): Observable<Car[]> {
    return this.http.get<Car[]>(`${this.baseAdminUrl}/cars`);
  }

  addCar(data: any): Observable<Car> {
    return this.http.post<Car>(`${this.baseAdminUrl}/cars`, data);
  }

  updateCar(id: number, data: any): Observable<Car> {
    return this.http.put<Car>(`${this.baseAdminUrl}/cars/${id}`, data);
  }

  disableCar(id: number): Observable<any> {
    return this.http.delete(`${this.baseAdminUrl}/cars/${id}`, { responseType: 'text' });
  }

  // ================= DRIVERS =================

  getDrivers(): Observable<DriverProfile[]> {
    return this.http.get<DriverProfile[]>(`${this.baseAdminUrl}/drivers`);
  }

  addDriver(data: any): Observable<DriverProfile> {
    return this.http.post<DriverProfile>(`${this.baseAdminUrl}/drivers`, data);
  }

  updateDriver(id: number, data: any): Observable<DriverProfile> {
    return this.http.put<DriverProfile>(`${this.baseAdminUrl}/drivers/${id}`, data);
  }

  disableDriver(id: number): Observable<any> {
    return this.http.delete(`${this.baseAdminUrl}/drivers/${id}`, { responseType: 'text' });
  }

  // ================= BOOKINGS =================

  getBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.baseAdminUrl}/bookings`);
  }

  verifyLicense(id: number, approved: boolean): Observable<Booking> {
    return this.http.post<Booking>(
      `${this.baseAdminUrl}/bookings/${id}/verify-license?approved=${approved}`,
      {}
    );
  }

  assignDriver(id: number, driverId: number): Observable<Booking> {
    return this.http.post<Booking>(
      `${this.baseAdminUrl}/bookings/${id}/assign-driver/${driverId}`,
      {}
    );
  }

  completeInspection(id: number): Observable<Booking> {
    return this.http.post<Booking>(`${this.baseAdminUrl}/bookings/${id}/complete-inspection`, {});
  }
}
