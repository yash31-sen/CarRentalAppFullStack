import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseUrl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient) {}

  private roleSignal = signal<string | null>(this.getRole());
  role = this.roleSignal.asReadonly();

  login(data: any) {
    return this.http.post<any>(`${this.baseUrl}/login`, data);
  }

  googleLogin(credential: string) {
    return this.http.post<any>(`${this.baseUrl}/google`, { credential });
  }

  register(data: any) {
    return this.http.post(`${this.baseUrl}/register`, data);
  }

  setToken(token: string, type: string, refreshToken?: string) {
    const fullToken = `${type} ${token}`;
    localStorage.setItem('token', fullToken);
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }

    const r = this.getRole();
    this.roleSignal.set(r);
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    this.roleSignal.set(null);
  }

  getRefreshToken() {
    return localStorage.getItem('refreshToken');
  }

  refreshToken() {
    const refreshToken = this.getRefreshToken();
    return this.http.post<any>(`${this.baseUrl}/refresh`, { refreshToken });
  }

  getToken() {
    return localStorage.getItem('token');
  }

  getRole(): string | null {
    const fullToken = this.getToken();
    if (!fullToken) return null;

    const token = fullToken.split(' ')[1];

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role;
    } catch {
      return null;
    }
  }

  verifyEmail(token: string) {
    return this.http.get(`${this.baseUrl}/verify?token=${token}`);
  }
}