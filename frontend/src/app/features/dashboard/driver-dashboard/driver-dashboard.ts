import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DriverService } from '../../../core/services/driver';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './driver-dashboard.html',
  styleUrl: './driver-dashboard.css',
})
export class DriverDashboard implements OnInit {

  // ─── Profile & Booking ────────────────────────────────────────────────────
  profile: any   = null;
  activeBooking: any = null;

  // ─── UI State ──────────────────────────────────────────────────────────────
  activeTab: 'overview' | 'trip' = 'overview';
  loading        = true;
  loadingBooking = false;
  updatingStatus   = false;
  updatingLocation = false;

  // ─── Inputs ────────────────────────────────────────────────────────────────
  locationInput = '';
  cities: string[] = [];  // populated from profile city for quick pick

  // ─── Toast ─────────────────────────────────────────────────────────────────
  toastMsg     = '';
  toastType: 'success' | 'error' | 'info' = 'info';
  toastVisible = false;

  constructor(
    private driverService: DriverService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  // ─── DATA LOADERS ─────────────────────────────────────────────────────────

  loadProfile() {
    this.loading = true;
    this.driverService.getProfile().subscribe({
      next: (res) => {
        this.profile = res;
        this.locationInput = res.currentLocation || '';
        this.loading = false;
        this.loadActiveBooking();
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Failed to load profile', 'error');
        this.loading = false;
      }
    });
  }

  loadActiveBooking() {
    this.loadingBooking = true;
    this.driverService.getMyActiveBooking().subscribe({
      next: (b) => {
        this.activeBooking = b;
        this.loadingBooking = false;
        if (b) this.activeTab = 'trip';
      },
      error: () => {
        this.activeBooking = null;
        this.loadingBooking = false;
      }
    });
  }

  // ─── STATUS ────────────────────────────────────────────────────────────────

  updateStatus(status: string) {
    if (this.profile?.status === status) return;
    this.updatingStatus = true;
    this.driverService.updateStatus(status).subscribe({
      next: (res: any) => {
        this.profile = res;
        this.updatingStatus = false;
        this.showToast(`Status set to ${this.statusLabel(status)}`, 'success');
      },
      error: (err) => {
        this.updatingStatus = false;
        this.showToast(err.error?.message || 'Failed to update status', 'error');
      }
    });
  }

  // ─── LOCATION ──────────────────────────────────────────────────────────────

  updateLocation() {
    if (!this.locationInput.trim()) return;
    this.updatingLocation = true;
    this.driverService.updateLocation(this.locationInput.trim()).subscribe({
      next: (res: any) => {
        this.profile = res;
        this.updatingLocation = false;
        this.showToast('Location updated successfully!', 'success');
      },
      error: (err) => {
        this.updatingLocation = false;
        this.showToast(err.error?.message || 'Failed to update location', 'error');
      }
    });
  }

  // ─── HELPERS ──────────────────────────────────────────────────────────────

  statusLabel(status: string): string {
    const map: Record<string, string> = {
      AVAILABLE: 'Available',
      ON_TRIP: 'On Trip',
      INACTIVE: 'Offline',
    };
    return map[status] || status;
  }

  statusIcon(status: string): string {
    const map: Record<string, string> = {
      AVAILABLE: '🟢',
      ON_TRIP:   '🚗',
      INACTIVE:   '⭕',
    };
    return map[status] || '❓';
  }

  bookingStatusLabel(status: string): string {
    return status?.replace(/_/g, ' ') || '';
  }

  getBookingStatusClass(status: string): string {
    const map: Record<string,string> = {
      CONFIRMED: 'success', IN_PROGRESS: 'purple',
    };
    return map[status] || 'info';
  }

  get tripDays(): number {
    if (!this.activeBooking?.startDate || !this.activeBooking?.endDate) return 0;
    const s = new Date(this.activeBooking.startDate);
    const e = new Date(this.activeBooking.endDate);
    return Math.ceil((e.getTime() - s.getTime()) / 86400000) + 1;
  }

  // ─── TOAST ─────────────────────────────────────────────────────────────────

  showToast(msg: string, type: 'success' | 'error' | 'info' = 'info') {
    this.toastMsg     = msg;
    this.toastType    = type;
    this.toastVisible = true;
    setTimeout(() => this.toastVisible = false, 3500);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
