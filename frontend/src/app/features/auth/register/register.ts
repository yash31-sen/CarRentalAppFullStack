import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { environment } from '../../../../environments/environment';

declare var google: any;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class Register implements OnInit {

  errorMessage = '';
  successMessage = '';
  loading = false;
  registerForm!: FormGroup;
  mode: 'user' | 'host' = 'user';

  private googleInitialized = false;
  private googleRetries = 0;
  private maxGoogleRetries = 20;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.registerForm = this.fb.group(
      {
        name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
        email: ['', [Validators.required, Validators.email]],
        mobileNumber: ['', [Validators.maxLength(20)]],
        password: [
          '',
          [
            Validators.required,
            Validators.minLength(8),
            Validators.pattern(/^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$/),
          ],
        ],
        confirmPassword: ['', [Validators.required]],
        termsAccepted: [false],
      },
      { validators: this.passwordMatchValidator }
    );
  }

  ngOnInit() {
    this.route.queryParamMap.subscribe((params) => {
      const m = params.get('mode');
      this.setMode(m === 'host' ? 'host' : 'user');
    });
    this.initGoogleSignIn();
  }

  setMode(mode: 'user' | 'host') {
    this.mode = mode;
    this.errorMessage = '';
    this.successMessage = '';
    const termsCtrl = this.registerForm.get('termsAccepted');
    const mobileCtrl = this.registerForm.get('mobileNumber');
    if (mode === 'host') {
      termsCtrl?.setValidators([Validators.requiredTrue]);
      mobileCtrl?.setValidators([Validators.required, Validators.maxLength(20)]);
    } else {
      termsCtrl?.clearValidators();
      mobileCtrl?.setValidators([Validators.maxLength(20)]);
    }
    termsCtrl?.updateValueAndValidity();
    mobileCtrl?.updateValueAndValidity();
  }

  passwordMatchValidator(group: AbstractControl): ValidationErrors | null {
    const pass = group.get('password')?.value;
    const confirm = group.get('confirmPassword')?.value;
    if (!pass || !confirm) return null;
    return pass === confirm ? null : { passwordMismatch: true };
  }

  initGoogleSignIn() {
    if (this.googleInitialized) return;
    if (typeof google !== 'undefined') {
      this.googleInitialized = true;
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: this.handleGoogleCredential.bind(this),
      });
      const btnEl = document.getElementById('google-btn');
      if (btnEl) {
        google.accounts.id.renderButton(btnEl, {
          theme: 'outline',
          size: 'large',
          text: 'signup_with',
          shape: 'rectangular',
          width: 320,
        });
      }
    } else if (this.googleRetries < this.maxGoogleRetries) {
      this.googleRetries++;
      setTimeout(() => this.initGoogleSignIn(), 250);
    }
  }

  handleGoogleCredential(response: any) {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.googleLogin(response.credential).subscribe({
      next: (res: any) => {
        this.authService.setToken(res.token, res.tokenType, res.refreshToken);
        const roles = res.roles || this.authService.getRoles();
        this.successMessage = 'Login successful! Redirecting...';
        const target = this.mode === 'host'
          ? '/host-onboarding'
          : this.authService.getPostLoginRoute(roles, res.hostApplicationStatus);
        this.router.navigate([target]);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Google Login failed';
        this.loading = false;
      },
    });
  }

  register() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const val = this.registerForm.value;
    this.authService
      .register({
        name: val.name,
        email: val.email,
        password: val.password,
        mobileNumber: val.mobileNumber || undefined,
        registrationIntent: this.mode === 'host' ? 'HOST_APPLICATION' : 'USER',
        termsAccepted: !!val.termsAccepted,
      })
      .subscribe({
        next: (res: any) => {
          this.loading = false;
          this.successMessage =
            res?.message ||
            'Registration successful! Please check your email for the verification link.';
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage = err.error?.message || 'Registration failed. Please try again.';
        },
      });
  }
}
