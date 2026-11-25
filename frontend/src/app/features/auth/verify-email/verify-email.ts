import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './verify-email.html'
})
export class VerifyEmail implements OnInit {

  message = 'Verifying your email...';
  success = false;
  loading = true;
  resendEmail = '';
  resendMessage = '';
  resendError = '';
  resending = false;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.loading = false;
      this.message = 'Invalid verification link';
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: (res: any) => {
        this.loading = false;
        this.authService.setToken(res.token, res.tokenType || 'Bearer', res.refreshToken);
        this.message = 'Email verified! Logging you in...';
        this.success = true;

        const roles = res.roles || this.authService.getRoles();
        const target = this.authService.getPostLoginRoute(roles, res.hostApplicationStatus);

        setTimeout(() => {
          this.router.navigate([target]);
        }, 1200);
      },
      error: (err) => {
        this.loading = false;
        this.message = err.error?.message || 'Verification failed or link expired.';
      }
    });
  }

  resendVerification() {
    if (!this.resendEmail.trim()) return;
    this.resending = true;
    this.resendMessage = '';
    this.resendError = '';
    this.authService.resendVerification(this.resendEmail.trim()).subscribe({
      next: (res: any) => {
        this.resending = false;
        this.resendMessage = res?.message || 'If an unverified account exists, a new verification link has been sent.';
      },
      error: (err) => {
        this.resending = false;
        this.resendError = err.error?.message || 'Could not resend verification email.';
      }
    });
  }
}
