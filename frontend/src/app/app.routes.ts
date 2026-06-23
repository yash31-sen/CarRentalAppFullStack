import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [

  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/login/login').then(m => m.Login)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/auth/register/register').then(m => m.Register)
  },
  {
    path: 'verify',
    loadComponent: () =>
      import('./features/auth/verify-email/verify-email').then(m => m.VerifyEmail)
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/forgot-password/forgot-password').then(m => m.ForgotPassword)
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./features/auth/reset-password/reset-password').then(m => m.ResetPassword)
  },
  {
    path: 'setup-account',
    loadComponent: () =>
      import('./features/auth/setup-account/setup-account').then(m => m.SetupAccount)
  },

  // 🔐 PROTECTED DASHBOARDS

  {
    path: 'user-dashboard',
    loadComponent: () =>
      import('./features/dashboard/user-dashboard/user-dashboard')
        .then(m => m.UserDashboard),
    canActivate: [authGuard],
    data: { roles: ['USER'] }
  },
  {
    path: 'admin-dashboard',
    loadComponent: () =>
      import('./features/dashboard/admin-dashboard/admin-dashboard')
        .then(m => m.AdminDashboard),
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] }
  },
  {
    path: 'super-admin-dashboard',
    loadComponent: () =>
      import('./features/dashboard/super-admin-dashboard/super-admin-dashboard')
        .then(m => m.SuperAdminDashboard),
    canActivate: [authGuard],
    data: { roles: ['SUPER_ADMIN'] }
  },
  {
    path: 'driver-dashboard',
    loadComponent: () =>
      import('./features/dashboard/driver-dashboard/driver-dashboard')
        .then(m => m.DriverDashboard),
    canActivate: [authGuard],
    data: { roles: ['DRIVER'] }
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'login'
  }
];