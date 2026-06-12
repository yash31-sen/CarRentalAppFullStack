import { Component } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
 
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.html',
  styleUrls:['./register.css']
})
export class Register {

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