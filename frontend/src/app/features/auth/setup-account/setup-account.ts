import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-setup-account',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './setup-account.html'
})
export class SetupAccount implements OnInit {
  setupForm: any;
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
    this.setupForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validator: this.passwordMatchValidator });
  }

  ngOnInit() {
    this.token = this.route.snapshot.queryParamMap.get('token');
    if (!this.token) {
      this.errorMessage = 'Invalid or missing setup token.';
    }
  }

  passwordMatchValidator(g: any) {
    return g.get('password').value === g.get('confirmPassword').value
      ? null : { mismatch: true };
  }

  submit() {
    if (this.setupForm.invalid || !this.token) return;

    this.loading = true;
    this.errorMessage = '';

    const newPassword = this.setupForm.value.password;

    this.authService.setupAccount({ token: this.token, newPassword }).subscribe({
      next: (res: any) => {
        this.toastService.showSuccess('Account setup successful! You can now log in with your new password.');
        this.router.navigate(['/login']);
      },
      error: (err: any) => {
        this.errorMessage = err.error?.message || 'Setup failed. The invitation may be expired or invalid.';
        this.loading = false;
      }
    });
  }
}
