import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HostService } from '../../../core/services/host.service';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-host-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './host-dashboard.html',
})
export class HostDashboard implements OnInit {
  activeTab: 'overview' | 'vehicles' | 'bookings' | 'earnings' = 'overview';
  loading = true;
  error = '';
  message = '';

  dashboard: any = null;
  vehicles: any[] = [];
  bookings: any[] = [];
  earnings: any = null;

  showAddForm = false;
  submittingCar = false;
  newCar = {
    carNumber: '',
    brand: '',
    model: '',
    carClass: 'ECONOMY',
    fuelType: 'PETROL',
    transmission: 'MANUAL',
    pricePerDay: 1500,
    city: '',
    currentLocation: '',
    manufacturingYear: 2023,
  };

  readonly carClasses = ['ECONOMY', 'HATCHBACK', 'SEDAN', 'SUV', 'LUXURY'];
  readonly fuelTypes = ['PETROL', 'DIESEL', 'ELECTRIC', 'HYBRID', 'CNG'];
  readonly transmissions = ['MANUAL', 'AUTOMATIC'];

  constructor(
    private hostService: HostService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.loading = true;
    this.error = '';
    this.hostService.getHostDashboard().subscribe({
      next: (res) => {
        this.loading = false;
        this.dashboard = res;
        if (!this.newCar.city && res.city) {
          this.newCar.city = res.city;
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Unable to load Host Dashboard.';
      },
    });
    this.loadVehicles();
    this.loadBookings();
    this.loadEarnings();
  }

  loadVehicles() {
    this.hostService.getMyVehicles().subscribe({
      next: (res) => (this.vehicles = res || []),
      error: () => {},
    });
  }

  loadBookings() {
    this.hostService.getMyBookings().subscribe({
      next: (res) => (this.bookings = res || []),
      error: () => {},
    });
  }

  loadEarnings() {
    this.hostService.getMyEarnings().subscribe({
      next: (res) => (this.earnings = res),
      error: () => {},
    });
  }

  createVehicle() {
    this.submittingCar = true;
    this.error = '';
    this.message = '';
    this.hostService.createVehicle(this.newCar).subscribe({
      next: () => {
        this.submittingCar = false;
        this.showAddForm = false;
        this.message = 'Vehicle submitted for Admin approval and Fleet inspection.';
        this.newCar.carNumber = '';
        this.newCar.brand = '';
        this.newCar.model = '';
        this.loadAll();
      },
      error: (err) => {
        this.submittingCar = false;
        this.error = err.error?.message || 'Failed to add vehicle. Check category pricing limits.';
      },
    });
  }

  toggleAvailability(car: any) {
    this.hostService.toggleVehicleAvailability(car.id, !car.active).subscribe({
      next: () => {
        this.message = `Vehicle ${car.carNumber} availability updated.`;
        this.loadAll();
      },
      error: (err) => {
        this.error = err.error?.message || 'Could not update vehicle availability.';
      },
    });
  }

  switchToUserPortal() {
    this.router.navigate(['/user-dashboard']);
  }

  logout() {
    this.authService.logoutRemote().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }
}
