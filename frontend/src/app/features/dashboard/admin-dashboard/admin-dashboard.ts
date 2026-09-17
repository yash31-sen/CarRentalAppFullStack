import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin';
import { AuthService } from '../../../core/services/auth';
import { ToastService } from '../../../core/services/toast';
import { Car, DriverProfile, Booking } from '../../../core/models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css']
})
export class AdminDashboard implements OnInit {
  activeTab: 'dashboard' | 'cars' | 'drivers' | 'bookings' = 'dashboard';
  bookingSubTab: 'all' | 'verification' | 'assignment' | 'inspection' = 'all';

  // Stats
  stats = {
    totalCars: 0,
    availableCars: 0,
    activeBookings: 0,
    pendingVerifications: 0,
    pendingAssignments: 0,
    pendingInspections: 0,
    totalDrivers: 0,
    availableDrivers: 0
  };

  // Lists
  cars: Car[] = [];
  drivers: DriverProfile[] = [];
  bookings: Booking[] = [];
  availableDriversForAssignment: DriverProfile[] = [];

  // Admin details
  adminEmail = '';
  adminCity = '';

  // Search Filters
  carFilter = { query: '', status: '' };
  driverFilter = { name: '', status: '' };
  bookingFilter = { id: '', type: '', status: '' };

  // Pagination – Cars
  carPage = 0;
  carPageSize = 10;

  // Pagination – Drivers
  driverPage = 0;
  driverPageSize = 10;

  // Pagination – Bookings
  bookingPage = 0;
  bookingPageSize = 10;

  // Modals & Action variables
  showCarModal = false;
  isEditCar = false;
  carForm = {
    id: 0,
    carNumber: '',
    brand: '',
    model: '',
    carClass: 'SEDAN' as Car['carClass'],
    fuelType: 'PETROL' as Car['fuelType'],
    transmission: 'AUTOMATIC' as Car['transmission'],
    pricePerDay: 1500,
    currentLocation: ''
  };

  showDriverModal = false;
  driverForm = {
    name: '',
    email: '',
    password: '',
    licenseNumber: '',
    experienceYears: 2,
    currentLocation: ''
  };

  // Booking details popups
  selectedBooking: Booking | null = null;
  showLicensePopup = false;
  showAssignmentPopup = false;
  selectedDriverIdForAssignment = '';

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.extractAdminDetails();
    this.loadAllData();
  }

  extractAdminDetails() {
    const fullToken = this.authService.getToken();
    if (fullToken) {
      try {
        const token = fullToken.split(' ')[1];
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.adminEmail = payload.sub || '';
        this.adminCity = payload.city || 'Global';
      } catch (e) {
        console.error('Error decoding token', e);
      }
    }
  }

  loadAllData() {
    this.loadCars();
    this.loadDrivers();
    this.loadBookings();
  }

  loadCars() {
    this.adminService.getCars().subscribe({
      next: (res) => {
        this.cars = res;
        this.calculateStats();
      },
      error: (err) => console.error(err)
    });
  }

  loadDrivers() {
    this.adminService.getDrivers().subscribe({
      next: (res) => {
        this.drivers = res;
        this.calculateStats();
      },
      error: (err) => console.error(err)
    });
  }

  loadBookings() {
    this.adminService.getBookings().subscribe({
      next: (res) => {
        this.bookings = res;
        this.calculateStats();
      },
      error: (err) => console.error(err)
    });
  }

  calculateStats() {
    this.stats.totalCars = this.cars.length;
    this.stats.availableCars = this.cars.filter(c => c.status === 'AVAILABLE' && c.active).length;
    this.stats.totalDrivers = this.drivers.length;
    this.stats.availableDrivers = this.drivers.filter(d => d.status === 'AVAILABLE' && d.active).length;

    this.stats.activeBookings = this.bookings.filter(
      b => b.status === 'IN_PROGRESS' || b.status === 'CONFIRMED'
    ).length;
    this.stats.pendingVerifications = this.bookings.filter(b => b.status === 'VERIFICATION_PENDING').length;
    this.stats.pendingAssignments = this.bookings.filter(b => b.status === 'DRIVER_ASSIGNMENT_PENDING').length;
    this.stats.pendingInspections = this.bookings.filter(b => b.status === 'INSPECTION_PENDING').length;
  }

  // NAVIGATION & TAB TOGGLES
  switchTab(tab: typeof this.activeTab) {
    this.activeTab = tab;
    this.carPage = 0;
    this.driverPage = 0;
    this.bookingPage = 0;
    this.loadAllData();
  }

  switchBookingSubTab(subTab: typeof this.bookingSubTab) {
    this.bookingSubTab = subTab;
    this.bookingPage = 0;
  }

  // PAGINATION HELPERS
  getPages(totalPages: number): number[] {
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  logout() {
    this.authService.logout();
    window.location.reload();
  }

  // CAR OPERATIONS
  openAddCarModal() {
    this.isEditCar = false;
    this.carForm = {
      id: 0,
      carNumber: '',
      brand: '',
      model: '',
      carClass: 'SEDAN',
      fuelType: 'PETROL',
      transmission: 'AUTOMATIC',
      pricePerDay: 1500,
      currentLocation: ''
    };
    this.showCarModal = true;
  }

  openEditCarModal(car: Car) {
    this.isEditCar = true;
    this.carForm = {
      id: car.id,
      carNumber: car.carNumber,
      brand: car.brand,
      model: car.model,
      carClass: car.carClass,
      fuelType: car.fuelType,
      transmission: car.transmission,
      pricePerDay: car.pricePerDay,
      currentLocation: car.currentLocation
    };
    this.showCarModal = true;
  }

  closeCarModal() {
    this.showCarModal = false;
  }

  submitCarForm() {
    if (this.isEditCar) {
      this.adminService.updateCar(this.carForm.id, this.carForm).subscribe({
        next: () => {
          this.toastService.showSuccess('Car details updated successfully');
          this.closeCarModal();
          this.loadCars();
        },
        error: (err) => {
          this.toastService.showError(err.error?.message || 'Failed to update car details');
        }
      });
    } else {
      this.adminService.addCar(this.carForm).subscribe({
        next: () => {
          this.toastService.showSuccess('New car added to inventory');
          this.closeCarModal();
          this.loadCars();
        },
        error: (err) => {
          this.toastService.showError(err.error?.message || 'Failed to add new car');
        }
      });
    }
  }

  toggleCarStatus(car: Car) {
    if (car.active) {
      if (confirm(`Are you sure you want to disable car ${car.brand} ${car.model}?`)) {
        this.adminService.disableCar(car.id).subscribe({
          next: () => {
            this.toastService.showSuccess('Car disabled successfully');
            this.loadCars();
          },
          error: (err) => this.toastService.showError('Failed to disable car')
        });
      }
    } else {
      this.adminService.updateCar(car.id, { active: true, status: 'AVAILABLE' }).subscribe({
        next: () => {
          this.toastService.showSuccess('Car enabled successfully');
          this.loadCars();
        },
        error: (err) => this.toastService.showError('Failed to enable car')
      });
    }
  }

  get filteredCars() {
    return this.cars.filter(car => {
      const matchQuery = !this.carFilter.query ||
        `${car.brand} ${car.model} ${car.carNumber}`.toLowerCase().includes(this.carFilter.query.toLowerCase());
      const matchStatus = !this.carFilter.status || car.status === this.carFilter.status;
      return matchQuery && matchStatus;
    });
  }

  get carTotalPages() { return Math.ceil(this.filteredCars.length / this.carPageSize); }
  get pagedCars() {
    const start = this.carPage * this.carPageSize;
    return this.filteredCars.slice(start, start + this.carPageSize);
  }
  onCarFilterChange() { this.carPage = 0; }

  // DRIVER OPERATIONS
  openAddDriverModal() {
    this.driverForm = {
      name: '',
      email: '',
      password: '',
      licenseNumber: '',
      experienceYears: 3,
      currentLocation: this.adminCity !== 'Global' ? this.adminCity : ''
    };
    this.showDriverModal = true;
  }

  closeDriverModal() {
    this.showDriverModal = false;
  }

  submitDriverForm() {
    this.adminService.addDriver(this.driverForm).subscribe({
      next: () => {
        this.toastService.showSuccess('Driver registered and invitation email sent');
        this.closeDriverModal();
        this.loadDrivers();
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to add driver');
      }
    });
  }

  toggleDriverStatus(driver: DriverProfile) {
    const action = driver.active ? 'disable' : 'activate';
    if (confirm(`Are you sure you want to ${action} driver ${driver.user.name}?`)) {
      if (driver.active) {
        this.adminService.disableDriver(driver.id).subscribe({
          next: () => {
            this.toastService.showSuccess('Driver disabled successfully');
            this.loadDrivers();
          },
          error: (err) => this.toastService.showError('Failed to disable driver')
        });
      } else {
        this.adminService.updateDriver(driver.id, { active: true, status: 'AVAILABLE' }).subscribe({
          next: () => {
            this.toastService.showSuccess('Driver activated successfully');
            this.loadDrivers();
          },
          error: (err) => this.toastService.showError('Failed to activate driver')
        });
      }
    }
  }

  get filteredDrivers() {
    return this.drivers.filter(driver => {
      const matchName = !this.driverFilter.name ||
        driver.user.name.toLowerCase().includes(this.driverFilter.name.toLowerCase()) ||
        driver.user.email.toLowerCase().includes(this.driverFilter.name.toLowerCase());
      const matchStatus = !this.driverFilter.status || driver.status === this.driverFilter.status;
      return matchName && matchStatus;
    });
  }

  get driverTotalPages() { return Math.ceil(this.filteredDrivers.length / this.driverPageSize); }
  get pagedDrivers() {
    const start = this.driverPage * this.driverPageSize;
    return this.filteredDrivers.slice(start, start + this.driverPageSize);
  }
  onDriverFilterChange() { this.driverPage = 0; }

  // BOOKING OPERATIONS
  get filteredBookings() {
    return this.bookings.filter(b => {
      // Subtab filtering
      if (this.bookingSubTab === 'verification' && b.status !== 'VERIFICATION_PENDING') return false;
      if (this.bookingSubTab === 'assignment' && b.status !== 'DRIVER_ASSIGNMENT_PENDING') return false;
      if (this.bookingSubTab === 'inspection' && b.status !== 'INSPECTION_PENDING') return false;

      // Input filters
      const matchId = !this.bookingFilter.id || b.id.toString().includes(this.bookingFilter.id);
      const matchType = !this.bookingFilter.type || b.bookingType === this.bookingFilter.type;
      const matchStatus = !this.bookingFilter.status || b.status === this.bookingFilter.status;

      return matchId && matchType && matchStatus;
    });
  }

  get bookingTotalPages() { return Math.ceil(this.filteredBookings.length / this.bookingPageSize); }
  get pagedBookings() {
    const start = this.bookingPage * this.bookingPageSize;
    return this.filteredBookings.slice(start, start + this.bookingPageSize);
  }
  onBookingFilterChange() { this.bookingPage = 0; }

  openLicensePopup(booking: Booking) {
    this.selectedBooking = booking;
    this.showLicensePopup = true;
  }

  closeLicensePopup() {
    this.showLicensePopup = false;
    this.selectedBooking = null;
  }

  processLicenseVerification(approved: boolean) {
    if (!this.selectedBooking) return;
    const action = approved ? 'approve' : 'reject';
    this.adminService.verifyLicense(this.selectedBooking.id, approved).subscribe({
      next: () => {
        this.toastService.showSuccess(`Driving license verification ${action}d`);
        this.closeLicensePopup();
        this.loadBookings();
      },
      error: (err) => this.toastService.showError(`Failed to ${action} license verification`)
    });
  }

  openAssignmentPopup(booking: Booking) {
    this.selectedBooking = booking;
    this.selectedDriverIdForAssignment = '';
    // Filter local drivers available in the pickupCity
    this.availableDriversForAssignment = this.drivers.filter(
      d => d.status === 'AVAILABLE' && d.active && d.currentLocation.toLowerCase() === booking.pickupCity.toLowerCase()
    );
    this.showAssignmentPopup = true;
  }

  closeAssignmentPopup() {
    this.showAssignmentPopup = false;
    this.selectedBooking = null;
  }

  submitDriverAssignment() {
    if (!this.selectedBooking || !this.selectedDriverIdForAssignment) return;
    const driverId = parseInt(this.selectedDriverIdForAssignment, 10);
    this.adminService.assignDriver(this.selectedBooking.id, driverId).subscribe({
      next: () => {
        this.toastService.showSuccess('Driver assigned to booking successfully');
        this.closeAssignmentPopup();
        this.loadBookings();
      },
      error: (err) => this.toastService.showError('Failed to assign driver')
    });
  }

  processInspectionCompletion(booking: Booking) {
    if (confirm(`Complete vehicle return inspection for Booking #${booking.id}? This will free the car.`)) {
      this.adminService.completeInspection(booking.id).subscribe({
        next: () => {
          this.toastService.showSuccess('Inspection completed. Car is now available.');
          this.loadBookings();
        },
        error: (err) => this.toastService.showError('Failed to complete inspection')
      });
    }
  }
}
