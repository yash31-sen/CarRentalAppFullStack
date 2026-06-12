import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
 
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

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
  registerForm:any
 

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
     this.registerForm = this.fb.group({
    name: ['', [Validators.required]],
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
          text: 'signup_with',
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

  this.authService.register(this.registerForm.value).subscribe({
    next: (res: any) => {
      this.loading = false;
      this.errorMessage = '';
      
      // 👇 show backend message
      this.successMessage = res.message || '📧 Check your email for verification link';
      console.log('Registration successful:', res);
      // optional: delay redirect
      setTimeout(() => {
        this.router.navigate(['/login']);
      }, 3000);
    },
    error: (err) => {
      this.loading = false;
      this.successMessage = '';

      this.errorMessage = err.error?.message || 'Registration failed';
      console.log('Registration failed:', err);
    }
  });
}

}