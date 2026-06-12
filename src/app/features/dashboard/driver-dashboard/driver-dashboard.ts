import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DriverService } from '../../../core/services/driver';

@Component({
  selector: 'app-driver-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './driver-dashboard.html',
  styleUrl: './driver-dashboard.css',
})
export class DriverDashboard implements OnInit {
  profile: any = null;
  loading = true;
  errorMessage = '';
  successMessage = '';
  locationInput = '';

  constructor(private driverService: DriverService) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile() {
    this.loading = true;
    this.errorMessage = '';
    this.driverService.getProfile().subscribe({
      next: (res) => {
        this.profile = res;
        this.locationInput = res.currentLocation;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to load driver profile';
        this.loading = false;
      }
    });
  }

  updateLocation() {
    if (!this.locationInput.trim()) return;
    this.driverService.updateLocation(this.locationInput).subscribe({
      next: (res: any) => {
        this.profile = res;
        this.successMessage = 'Location updated successfully!';
        this.errorMessage = '';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to update location';
        this.successMessage = '';
      }
    });
  }

  updateStatus(status: string) {
    this.driverService.updateStatus(status).subscribe({
      next: (res: any) => {
        this.profile = res;
        this.successMessage = `Status updated to ${status} successfully!`;
        this.errorMessage = '';
        setTimeout(() => this.successMessage = '', 3000);
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Failed to update status';
        this.successMessage = '';
      }
    });
  }
}
