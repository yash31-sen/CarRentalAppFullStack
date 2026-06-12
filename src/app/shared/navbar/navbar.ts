import { Component, inject } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';

import { filter } from 'rxjs';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class Navbar {

  private authService = inject(AuthService);
  private router = inject(Router);

  role = this.authService.role;
  currentRoute: string = '';

  constructor() {
    // 🔥 route tracking
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe((event: any) => {
        this.currentRoute = event.url;
      });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  goLogin() {
    this.router.navigate(['/login']);
  }

  goRegister() {
    this.router.navigate(['/register']);
  }

  getShortRole(role: string | null): string {
    if (!role) return '';
    switch (role) {
      case 'SUPER_ADMIN': return 'SUPER';
      case 'ADMIN': return 'ADMIN';
      case 'USER': return 'USER';
      case 'DRIVER': return 'DRIVER';
      default: return role;
    }
  }
}