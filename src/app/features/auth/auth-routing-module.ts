import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth-guard';

export const routes: Routes = [

  {
    path: 'login',
    loadComponent: () =>
      import('./login/login').then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./register/register').then(m => m.Register)
  },
  {
    path: 'verify',
    loadComponent: () =>
      import('./verify-email/verify-email').then(m => m.VerifyEmail)
  },

  // 🔐 PROTECTED DASHBOARDS

  {
    path: 'user-dashboard',
    loadComponent: () =>
      import('../dashboard/user-dashboard/user-dashboard')
        .then(m => m.UserDashboard),
    canActivate: [authGuard],
    data: { roles: ['USER'] }
  },
  {
    path: 'admin-dashboard',
    loadComponent: () =>
      import('../dashboard/admin-dashboard/admin-dashboard')
        .then(m => m.AdminDashboard),
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'super-admin-dashboard',
    loadComponent: () =>
      import('../dashboard/super-admin-dashboard/super-admin-dashboard')
        .then(m => m.SuperAdminDashboard),
    canActivate: [authGuard],
    data: { roles: ['SUPER_ADMIN'] }
  },
  {
    path: 'driver-dashboard',
    loadComponent: () =>
      import('../dashboard/driver-dashboard/driver-dashboard')
        .then(m => m.DriverDashboard),
    canActivate: [authGuard],
    data: { roles: ['DRIVER'] }
  }
];