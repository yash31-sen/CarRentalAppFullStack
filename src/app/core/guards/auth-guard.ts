import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route) => {

  const router = inject(Router);
  const authService = inject(AuthService);
  const role = authService.getRole();

  if (!role) {
    router.navigate(['/login']);
    return false;
  }

  const expectedRoles = route.data?.['roles'];

  if (expectedRoles && !expectedRoles.includes(role)) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};