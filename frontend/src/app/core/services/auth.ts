import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  private baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  login(data: { email: string; password: string }) {
    return this.http.post(`${this.baseUrl}/auth/login`, data);
  }

  googleLogin(credential: string) {
    return this.http.post(`${this.baseUrl}/auth/google`, { credential });
  }

  register(data: {
    name: string;
    email: string;
    password: string;
    mobileNumber?: string;
    registrationIntent?: 'USER' | 'HOST_APPLICATION';
    termsAccepted?: boolean;
  }) {
    return this.http.post(`${this.baseUrl}/auth/register`, data);
  }

  resendVerification(email: string) {
    return this.http.post(`${this.baseUrl}/auth/resend-verification`, { email });
  }

  setToken(token: string, tokenType: string, refreshToken?: string) {
    localStorage.setItem('token', `${tokenType} ${token}`);
    localStorage.setItem('rawToken', token);
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }
  }

  refreshToken(): Observable<any> {
    const refreshToken = this.getRefreshToken();
    return this.http.post(`${this.baseUrl}/auth/refresh`, { refreshToken });
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('rawToken');
    localStorage.removeItem('refreshToken');
  }

  logoutRemote() {
    return this.http.post(`${this.baseUrl}/auth/logout`, {}).pipe(
      tap({
        next: () => this.logout(),
        error: () => this.logout(),
      })
    );
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  getRawToken(): string | null {
    return localStorage.getItem('rawToken');
  }

  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }

  isLoggedIn(): boolean {
    return !!this.getRawToken();
  }

  getRoles(): string[] {
    const raw = localStorage.getItem('rawToken');
    if (!raw) return [];
    try {
      const payload = JSON.parse(atob(raw.split('.')[1]));
      if (Array.isArray(payload.roles) && payload.roles.length > 0) {
        return payload.roles;
      }
      return payload.role ? [payload.role] : [];
    } catch {
      return [];
    }
  }

  hasRole(role: string): boolean {
    return this.getRoles().includes(role);
  }

  getRole(): string | null {
    const roles = this.getRoles();
    if (roles.includes('SUPER_ADMIN')) return 'SUPER_ADMIN';
    if (roles.includes('ADMIN')) return 'ADMIN';
    if (roles.includes('HOST')) return 'HOST';
    if (roles.includes('DRIVER')) return 'DRIVER';
    if (roles.includes('USER')) return 'USER';
    return roles[0] ?? null;
  }

  getUserEmail(): string | null {
    const raw = localStorage.getItem('rawToken');
    if (!raw) return null;
    try {
      const payload = JSON.parse(atob(raw.split('.')[1]));
      return payload.sub || null;
    } catch {
      return null;
    }
  }

  getPostLoginRoute(roles: string[], hostApplicationStatus?: string | null): string {
    if (roles.includes('SUPER_ADMIN')) return '/super-admin-dashboard';
    if (roles.includes('ADMIN')) return '/admin-dashboard';
    if (roles.includes('DRIVER')) return '/driver-dashboard';
    if (roles.includes('HOST')) return '/host-dashboard';
    if (
      hostApplicationStatus === 'PENDING_DOCUMENTS' ||
      hostApplicationStatus === 'RESUBMISSION_REQUIRED' ||
      hostApplicationStatus === 'RESUBMISSION_REQUESTED' ||
      hostApplicationStatus === 'PENDING_ADMIN_APPROVAL' ||
      hostApplicationStatus === 'DRAFT'
    ) {
      return '/host-onboarding';
    }
    return '/user-dashboard';
  }

  verifyEmail(token: string) {
    return this.http.get(`${this.baseUrl}/auth/verify?token=${encodeURIComponent(token)}`);
  }

  sendMobileVerification(mobileNumber: string) {
    return this.http.post(`${this.baseUrl}/auth/mobile/send-verification`, { mobileNumber });
  }

  verifyMobile(token: string) {
    return this.http.get(`${this.baseUrl}/auth/mobile/verify?token=${encodeURIComponent(token)}`);
  }

  forgotPassword(email: string) {
    return this.http.post(`${this.baseUrl}/auth/forgot-password`, { email });
  }

  resetPassword(dataOrToken: string | { token: string; newPassword: string }, newPassword?: string) {
    const body = typeof dataOrToken === 'string' ? { token: dataOrToken, newPassword } : dataOrToken;
    return this.http.post(`${this.baseUrl}/auth/reset-password`, body);
  }

  setupAccount(dataOrToken: string | { token: string; newPassword: string }, newPassword?: string) {
    const body = typeof dataOrToken === 'string' ? { token: dataOrToken, newPassword } : dataOrToken;
    return this.http.post(`${this.baseUrl}/auth/setup-account`, body);
  }
}
