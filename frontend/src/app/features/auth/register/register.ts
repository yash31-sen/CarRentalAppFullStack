import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
 
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { environment } from '../../../../environments/environment';

declare var google: any;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls:['./register.css']
})
export class Register implements OnInit {

  errorMessage = '';
  loading = false;
  registerForm!: FormGroup;
 

  private googleInitialized = false;
  private googleRetries = 0;
  private maxGoogleRetries = 20;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$/)
      ]]
    });
  }

  ngOnInit() {
    this.initGoogleSignIn();
  }

  initGoogleSignIn() {
    if (this.googleInitialized) return;
    if (typeof google !== 'undefined') {
      this.googleInitialized = true;
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: this.handleGoogleCredential.bind(this)
      });
      const btnEl = document.getElementById('google-btn');
      if (btnEl) {
        google.accounts.id.renderButton(btnEl, {
          theme: 'outline',
          size: 'large',
          text: 'signup_with',
          shape: 'rectangular',
          width: 320
        });
      }
    } else if (this.googleRetries < this.maxGoogleRetries) {
      this.googleRetries++;
      setTimeout(() => this.initGoogleSignIn(), 250);
    } else {
      console.warn('Google Sign-In SDK failed to load after retries');
    }
  }

  handleGoogleCredential(response: any) {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.googleLogin(response.credential).subscribe({
      next: (res: any) => {
        this.authService.setToken(res.token, res.tokenType, res.refreshToken);
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

successMessage = '';
 

register() {
  if (this.registerForm.invalid) return;

  this.loading = true;
  this.errorMessage = '';
  this.successMessage = '';

  this.authService.register(this.registerForm.value).subscribe({
    next: (res: any) => {
      this.loading = false;
      this.successMessage = res.message || '📧 Check your email for a verification link';
      setTimeout(() => this.router.navigate(['/login']), 3000);
    },
    error: (err) => {
      this.loading = false;
      this.successMessage = '';
      // Extract the most useful message: details object, message string, or fallback
      const body = err.error;
      if (body?.details) {
        this.errorMessage = Object.values(body.details).join(' · ');
      } else {
        this.errorMessage = body?.message || 'Registration failed. Please try again.';
      }
      console.error('Registration failed:', err);
    }
  });
}

}