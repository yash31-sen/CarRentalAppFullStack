import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
 
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './verify-email.html'
})
export class VerifyEmail implements OnInit {

  message = 'Verifying your email...';
  success = false;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
  const token = this.route.snapshot.queryParamMap.get('token');

  if (!token) {
    this.message = 'Invalid verification link';
    return;
  }

  this.authService.verifyEmail(token).subscribe({
    next: (res: any) => {

      // ✅ Save JWT
      this.authService.setToken(res.token, 'Bearer');
      this.message = '✅ Email verified! Logging you in...';
      this.success = true;

      // ✅ Decode role from JWT via AuthService
      const role = this.authService.getRole();

      // ✅ Redirect based on role
      setTimeout(() => {
        switch (role) {
          case 'ADMIN':
            this.router.navigate(['/admin-dashboard']);
            break;
          case 'SUPER_ADMIN':
            this.router.navigate(['/super-admin-dashboard']);
            break;
          case 'DRIVER':
            this.router.navigate(['/driver-dashboard']);
            break;
          default:
            this.router.navigate(['/user-dashboard']);
        }
      }, 1500);
    },

    error: () => {
      this.message = '❌ Verification failed or expired';
    }
  });
}
}