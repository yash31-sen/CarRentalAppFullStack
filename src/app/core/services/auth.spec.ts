import { BehaviorSubject } from 'rxjs';

export class AuthService {

  private roleSubject = new BehaviorSubject<string | null>(this.getRoleFromToken());
  role$ = this.roleSubject.asObservable();

  setToken(token: string) {
    localStorage.setItem('token', token);
    const role = this.getRoleFromToken();
    this.roleSubject.next(role); // 🔥 notify all components
  }

  logout() {
    localStorage.removeItem('token');
    this.roleSubject.next(null);
  }

  getRoleFromToken(): string | null {
    const token = localStorage.getItem('token');
    if (!token) return null;

    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role;
  }
}