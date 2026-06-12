import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppUser, DriverProfile, ServiceCity, CarPricing } from '../models';

@Injectable({
  providedIn: 'root'
})
export class SuperAdminService {

  baseUrl = 'http://localhost:8080/super-admin';

  constructor(private http: HttpClient) {}

  // ================= ADMINS =================

  getAdmins(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(
      `${this.baseUrl}/admins`
    );
  }

  createAdmin(data: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/admins`,
      data
    );
  }

  deactivateAdmin(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/admins/${id}/deactivate`,
      {}
    );
  }

  reactivateAdmin(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/admins/${id}/reactivate`,
      {}
    );
  }

  deleteAdmin(id: number): Observable<any> {
    return this.http.delete(
      `${this.baseUrl}/admins/${id}`
    );
  }
   // USERS

  getUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(
      `${this.baseUrl}/users`
    );
  }

  deactivateUser(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/users/${id}/deactivate`,
      {}
    );
  }

  reactivateUser(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/users/${id}/reactivate`,
      {}
    );
  }

  deleteUser(id: number): Observable<any> {
    return this.http.delete(
      `${this.baseUrl}/users/${id}`
    );
  }


   // DRIVERS

  getDrivers(): Observable<DriverProfile[]> {
    return this.http.get<DriverProfile[]>(
      `${this.baseUrl}/drivers`
    );
  }

  deactivateDriver(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/drivers/${id}/deactivate`,
      {}
    );
  }

  reactivateDriver(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/drivers/${id}/reactivate`,
      {}
    );
  }
   // CITIES

  getCities(): Observable<ServiceCity[]> {
    return this.http.get<ServiceCity[]>(
      `${this.baseUrl}/cities`
    );
  }

  createCity(data: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/cities`,
      data
    );
  }

  deactivateCity(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/cities/${id}/deactivate`,
      {}
    );
  }

  reactivateCity(id: number): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/cities/${id}/reactivate`,
      {}
    );
  }

  assignAdmin(
    cityId: number,
    adminId: number
  ): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/cities/${cityId}/assign-admin/${adminId}`,
      {}
    );
  }

  updateOneWayFee(
    cityId: number,
    fee: number
  ): Observable<any> {
    return this.http.patch(
      `${this.baseUrl}/cities/${cityId}/one-way-fee?fee=${fee}`,
      {}
    );
  }

   // =========================
  // PRICING
  // =========================

  getPricing(): Observable<CarPricing[]> {
    return this.http.get<CarPricing[]>(
      `${this.baseUrl}/pricing`
    );
  }

  createPricing(data: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/pricing`,
      data
    );
  }

  updatePricing(
    id: number,
    data: any
  ): Observable<any> {
    return this.http.put(
      `${this.baseUrl}/pricing/${id}`,
      data
    );
  }

  deletePricing(id: number): Observable<any> {
    return this.http.delete(
      `${this.baseUrl}/pricing/${id}`
    );
  }
  getDashboardStats(): Observable<any> {

  return this.http.get(
    `${this.baseUrl}/dashboard/stats`
  );
}
}