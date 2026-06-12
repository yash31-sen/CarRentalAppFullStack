import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { CommonModule } from '@angular/common';

declare var google: any;

@Component({
  selector: 'app-login',
  imports:[ReactiveFormsModule,CommonModule,RouterLink],
    standalone: true,
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class Login implements OnInit {

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

  ngOnInit() {
    this.initGoogleSignIn();
  }

  initGoogleSignIn() {
    if (typeof google !== 'undefined') {
      google.accounts.id.initialize({
        client_id: '109876543210-placeholder.apps.googleusercontent.com',
        callback: this.handleGoogleCredential.bind(this)
      });
      const btnEl = document.getElementById('google-btn');
      if (btnEl) {
        google.accounts.id.renderButton(btnEl, {
          theme: 'outline',
          size: 'large',
          text: 'signin_with',
          shape: 'rectangular',
          width: 320
        });
      }
    } else {
      setTimeout(() => this.initGoogleSignIn(), 250);
    }
  }

  handleGoogleCredential(response: any) {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.googleLogin(response.credential).subscribe({
      next: (res: any) => {
        this.authService.setToken(res.token, res.tokenType);
        const role = this.authService.getRole();
        this.successMessage = 'Login successful! Redirecting...';
        this.redirectUser(role);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Google Login failed';
        this.loading = false;
      }
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