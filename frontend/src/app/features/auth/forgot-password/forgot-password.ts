import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.html',
})
export class ForgotPassword {
  forgotForm: any;
  loading = false;
  successMessage = '';
  errorMessage = '';

  constructor(private fb: FormBuilder, private authService: AuthService) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  submit() {
    if (this.forgotForm.invalid) return;

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    const email = this.forgotForm.value.email;

    this.authService.forgotPassword(email).subscribe({
      next: (res: any) => {
        this.successMessage = 'If the email exists, a password reset link has been sent!';
        this.loading = false;
        this.forgotForm.reset();
      },
      error: (err: any) => {
        this.errorMessage = err.error?.message || 'Something went wrong. Please try again.';
        this.loading = false;
      }
    });
  }
}
