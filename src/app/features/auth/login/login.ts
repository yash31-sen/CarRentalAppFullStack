import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  imports:[ReactiveFormsModule,CommonModule,RouterLink],
    standalone: true,
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login {

  errorMessage = '';
  loading = false;

  loginForm: any;
 successMessage = '';
  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
  
    private router: Router
  ) {
    this.loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });
  }

  login() {
    if (this.loginForm.invalid) return;

    this.loading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: (res: any) => {
        this.authService.setToken(res.token, res.tokenType);

        const role = this.authService.getRole();
this.successMessage = 'Login successful! Redirecting...';
        this.redirectUser(role);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Login failed';
        this.loading = false;
      }
    });
  }

  redirectUser(role: string | null) {
    switch (role) {
      case 'USER':
        this.router.navigate(['/user-dashboard']);
        break;
      case 'DRIVER':
        this.router.navigate(['/driver-dashboard']);
        break;
      case 'ADMIN':
        this.router.navigate(['/admin-dashboard']);
        break;
      case 'SUPER_ADMIN':
        this.router.navigate(['/super-admin-dashboard']);
        break;
      default:
        this.router.navigate(['/login']);
    }
  }
}