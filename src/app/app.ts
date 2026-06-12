import { Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Navbar } from './shared/navbar/navbar';
import { ToastService } from './core/services/toast';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ReactiveFormsModule, Navbar, CommonModule],
  template: `
    <app-navbar></app-navbar>
    <router-outlet></router-outlet>

    <!-- Global Toast Notifications Overlay -->
    <div class="toast-container">
      @for (toast of toasts(); track toast.id) {
        <div class="toast" [ngClass]="toast.type" (click)="removeToast(toast.id)">
          <span class="icon">
            {{ toast.type === 'success' ? '✨' : '⚠️' }}
          </span>
          <span class="message">{{ toast.message }}</span>
          <button class="close-btn" (click)="$event.stopPropagation(); removeToast(toast.id)">&times;</button>
        </div>
      }
    </div>
  `,
  styleUrl: './app.css'
})
export class App {
  private toastService = inject(ToastService);
  toasts = this.toastService.toasts;

  removeToast(id: number) {
    this.toastService.remove(id);
  }
}
