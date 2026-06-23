import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html'
})
export class ResetPassword implements OnInit {
  resetForm: any;
  loading = false;
  errorMessage = '';
  token: string | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router
  ) {
    this.resetForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (!this.token) {
      this.errorMessage = 'Invalid or missing password reset token.';
    }
  }

  passwordMatchValidator(g: any) {
    return g.get('password').value === g.get('confirmPassword').value
      ? null : { mismatch: true };
  }

  submit() {
    if (this.resetForm.invalid || !this.token) return;

    this.loading = true;
    this.errorMessage = '';

    const newPassword = this.resetForm.value.password;

    this.authService.resetPassword({ token: this.token, newPassword }).subscribe({
      next: (res: any) => {
        this.toastService.showSuccess('Password reset successful! Please log in with your new password.');
        this.router.navigate(['/login']);
      },
      error: (err: any) => {
        this.errorMessage = err.error?.message || 'Reset failed. The token may be expired or invalid.';
        this.loading = false;
      }
    });
  }
}
