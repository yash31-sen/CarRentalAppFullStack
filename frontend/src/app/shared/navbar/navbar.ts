import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class Navbar {
  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  get isLoggedIn(): boolean {
    return !!this.authService.getToken();
  }

  get role(): string | null {
    return this.authService.getRole();
  }

  get roles(): string[] {
    return this.authService.getRoles();
  }

  hasRole(role: string): boolean {
    return this.authService.hasRole(role);
  }

  logout() {
    this.authService.logoutRemote().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }
}
