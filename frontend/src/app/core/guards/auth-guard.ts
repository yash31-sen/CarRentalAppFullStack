import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();
  const roles = authService.getRoles();

  if (!token || roles.length === 0) {
    router.navigate(['/login']);
    return false;
  }

  const allowedRoles = route.data['roles'] as string[] | undefined;
  if (allowedRoles && !roles.some((r) => allowedRoles.includes(r))) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};
