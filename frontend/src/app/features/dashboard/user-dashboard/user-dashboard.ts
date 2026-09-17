import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { Car, Booking, ServiceCity } from '../../../core/models';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-user-dashboard',
  imports: [CommonModule, FormsModule],
  templateUrl: './user-dashboard.html',
  styleUrl: './user-dashboard.css',
})
export class UserDashboard implements OnInit {

  // ─── STATE ───────────────────────────────────────────────────────────────
  activeTab: 'browse' | 'bookings' | 'profile' = 'browse';
  userEmail = '';
  userName  = '';

  // ─── CARS ─────────────────────────────────────────────────────────────────
  allCars:  Car[]     = [];
  cities:   ServiceCity[] = [];
  cityFilter    = '';
  classFilter   = '';
  typeFilter    = '';   // SELF_DRIVE | WITH_DRIVER
  loadingCars   = false;

  readonly carClasses = ['ECONOMY','HATCHBACK','SEDAN','SUV','LUXURY'];

  // ─── BOOKINGS ────────────────────────────────────────────────────────────
  myBookings:     Booking[]  = [];
  bookingStatusFilter = '';
  loadingBookings = false;

  // Pagination – cars
  carPage     = 0;
  carPageSize = 9;

  // Pagination – bookings
  bkPage     = 0;
  bkPageSize = 8;

  // ─── BOOKING MODAL ───────────────────────────────────────────────────────
  showBookingModal = false;
  selectedCar: Car | null = null;
  bookingForm = {
    bookingType:         'SELF_DRIVE' as 'SELF_DRIVE' | 'WITH_DRIVER',
    tripType:            'ROUND_TRIP' as 'ROUND_TRIP' | 'ONE_WAY',
    startDate:           '',
    endDate:             '',
    pickupCity:          '',
    dropCity:            '',
    pickupLocation:      '',
    dropLocation:        '',
    drivingLicenseNumber: '',
  };
  bookingLoading = false;
  bookingError   = '';

  // ─── CANCEL CONFIRM ───────────────────────────────────────────────────────
  showCancelConfirm  = false;
  cancelTargetId: number | null = null;

  // ─── TOAST ────────────────────────────────────────────────────────────────
  toastMsg  = '';
  toastType: 'success' | 'error' | 'info' = 'info';
  toastVisible = false;

  private baseUrl = environment.apiUrl;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.userEmail = this.getUserEmail();
    this.userName  = this.userEmail.split('@')[0];
    this.loadCities();
    this.loadCars();
    this.loadMyBookings();
  }

  // ─── DATA LOADERS ────────────────────────────────────────────────────────

  loadCars() {
    this.loadingCars = true;
    const params = this.cityFilter ? `?city=${encodeURIComponent(this.cityFilter)}` : '';
    this.http.get<Car[]>(`${this.baseUrl}/api/cars/available${params}`).subscribe({
      next: cars => { this.allCars = cars; this.loadingCars = false; this.carPage = 0; },
      error: ()  => { this.loadingCars = false; }
    });
  }

  loadCities() {
    this.http.get<ServiceCity[]>(`${this.baseUrl}/super-admin/cities`).subscribe({
      next: c => this.cities = c.filter(x => x.active),
      error: () => {}
    });
  }

  loadMyBookings() {
    this.loadingBookings = true;
    this.http.get<Booking[]>(`${this.baseUrl}/api/bookings/my`).subscribe({
      next: b => { this.myBookings = b; this.loadingBookings = false; },
      error: () => { this.loadingBookings = false; }
    });
  }

  // ─── FILTERED / PAGED GETTERS ────────────────────────────────────────────

  get filteredCars(): Car[] {
    return this.allCars.filter(c => {
      const matchClass = !this.classFilter || c.carClass === this.classFilter;
      return matchClass;
    });
  }

  get carTotalPages() { return Math.ceil(this.filteredCars.length / this.carPageSize); }
  get pagedCars(): Car[] {
    const s = this.carPage * this.carPageSize;
    return this.filteredCars.slice(s, s + this.carPageSize);
  }

  get filteredBookings(): Booking[] {
    return this.myBookings.filter(b => {
      return !this.bookingStatusFilter || b.status === this.bookingStatusFilter;
    });
  }

  get bkTotalPages() { return Math.ceil(this.filteredBookings.length / this.bkPageSize); }
  get pagedBookings(): Booking[] {
    const s = this.bkPage * this.bkPageSize;
    return this.filteredBookings.slice(s, s + this.bkPageSize);
  }

  getPages(total: number) { return Array.from({ length: total }, (_, i) => i); }

  // ─── STATS ───────────────────────────────────────────────────────────────

  get activeBookingsCount() {
    return this.myBookings.filter(b =>
      ['PENDING_PAYMENT','PAYMENT_SUCCESS','VERIFICATION_PENDING',
       'DRIVER_ASSIGNMENT_PENDING','CONFIRMED','IN_PROGRESS'].includes(b.status)).length;
  }

  get completedCount() {
    return this.myBookings.filter(b => b.status === 'COMPLETED').length;
  }

  get totalSpent(): number {
    return this.myBookings
      .filter(b => b.status === 'COMPLETED')
      .reduce((sum, b) => sum + (b.finalPrice || b.totalPrice || 0), 0);
  }

  // ─── BOOKING FLOW ────────────────────────────────────────────────────────

  openBookingModal(car: Car) {
    this.selectedCar = car;
    this.bookingForm = {
      bookingType: 'SELF_DRIVE',
      tripType: 'ROUND_TRIP',
      startDate: '',
      endDate: '',
      pickupCity: car.currentCity,
      dropCity: car.currentCity,
      pickupLocation: '',
      dropLocation: '',
      drivingLicenseNumber: '',
    };
    this.bookingError = '';
    this.showBookingModal = true;
  }

  closeBookingModal() {
    this.showBookingModal = false;
    this.selectedCar = null;
    this.bookingError = '';
  }

  onTripTypeChange() {
    if (this.bookingForm.tripType === 'ROUND_TRIP') {
      this.bookingForm.dropCity = this.bookingForm.pickupCity;
    } else {
      this.bookingForm.dropCity = '';
    }
  }

  get estimatedDays(): number {
    if (!this.bookingForm.startDate || !this.bookingForm.endDate) return 0;
    const s = new Date(this.bookingForm.startDate);
    const e = new Date(this.bookingForm.endDate);
    return Math.max(1, Math.ceil((e.getTime() - s.getTime()) / 86400000) + 1);
  }

  get estimatedPrice(): number {
    if (!this.selectedCar) return 0;
    return this.estimatedDays * this.selectedCar.pricePerDay;
  }

  get today(): string {
    return new Date().toISOString().split('T')[0];
  }

  submitBooking() {
    if (!this.selectedCar) return;
    this.bookingError = '';

    if (!this.bookingForm.startDate || !this.bookingForm.endDate) {
      this.bookingError = 'Please select start and end dates.'; return;
    }
    if (this.bookingForm.bookingType === 'SELF_DRIVE' && !this.bookingForm.drivingLicenseNumber) {
      this.bookingError = 'Driving license number is required for self-drive.'; return;
    }

    this.bookingLoading = true;
    const payload = {
      carId:                this.selectedCar.id,
      bookingType:          this.bookingForm.bookingType,
      tripType:             this.bookingForm.tripType,
      startDate:            this.bookingForm.startDate,
      endDate:              this.bookingForm.endDate,
      pickupCity:           this.bookingForm.pickupCity,
      dropCity:             this.bookingForm.dropCity,
      pickupLocation:       this.bookingForm.pickupLocation || this.bookingForm.pickupCity,
      dropLocation:         this.bookingForm.dropLocation   || this.bookingForm.dropCity,
      drivingLicenseNumber: this.bookingForm.drivingLicenseNumber || null,
    };

    this.http.post<Booking>(`${this.baseUrl}/api/bookings`, payload).subscribe({
      next: booking => {
        this.bookingLoading = false;
        this.closeBookingModal();
        this.showToast(`Booking #${booking.id} created! Proceed to payment.`, 'success');
        // Simulate payment immediately (payment gateway integration can be added later)
        this.confirmPayment(booking.id);
      },
      error: err => {
        this.bookingLoading = false;
        this.bookingError = err?.error?.message || err?.error || 'Booking failed. Please try again.';
      }
    });
  }

  confirmPayment(bookingId: number) {
    const ref = 'PAY-' + Math.random().toString(36).slice(2, 10).toUpperCase();
    this.http.post<Booking>(`${this.baseUrl}/api/bookings/${bookingId}/payment-success`,
      { paymentRef: ref }).subscribe({
      next: () => {
        this.loadMyBookings();
        this.loadCars();
        this.activeTab = 'bookings';
        this.showToast('Payment confirmed! Your booking is being processed.', 'success');
      },
      error: () => {
        this.loadMyBookings();
      }
    });
  }

  // ─── CANCEL ──────────────────────────────────────────────────────────────

  openCancelConfirm(id: number) {
    this.cancelTargetId = id;
    this.showCancelConfirm = true;
  }

  closeCancelConfirm() {
    this.showCancelConfirm = false;
    this.cancelTargetId = null;
  }

  doCancel() {
    if (!this.cancelTargetId) return;
    this.http.delete<Booking>(`${this.baseUrl}/api/bookings/${this.cancelTargetId}/cancel`).subscribe({
      next: () => {
        this.closeCancelConfirm();
        this.loadMyBookings();
        this.showToast('Booking cancelled successfully.', 'info');
      },
      error: err => {
        this.closeCancelConfirm();
        this.showToast(err?.error?.message || 'Cancellation failed.', 'error');
      }
    });
  }

  // ─── CANCELLABLE CHECK ───────────────────────────────────────────────────

  isCancellable(booking: Booking): boolean {
    return ['PENDING_PAYMENT','PAYMENT_SUCCESS','VERIFICATION_PENDING',
            'DRIVER_ASSIGNMENT_PENDING','CONFIRMED'].includes(booking.status);
  }

  // ─── TOAST ────────────────────────────────────────────────────────────────

  showToast(msg: string, type: 'success' | 'error' | 'info' = 'info') {
    this.toastMsg     = msg;
    this.toastType    = type;
    this.toastVisible = true;
    setTimeout(() => this.toastVisible = false, 4000);
  }

  // ─── AUTH ─────────────────────────────────────────────────────────────────

  getUserEmail(): string {
    const token = this.auth.getToken();
    if (!token) return '';
    try {
      const jwt = token.split(' ')[1];
      const payload = JSON.parse(atob(jwt.split('.')[1]));
      return payload.sub || payload.email || '';
    } catch { return ''; }
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  // ─── HELPERS ──────────────────────────────────────────────────────────────

  getClassEmoji(cls: string): string {
    const map: Record<string,string> = {
      ECONOMY: '🚗', HATCHBACK: '🚙', SEDAN: '🚘', SUV: '🛻', LUXURY: '🏎️'
    };
    return map[cls] || '🚗';
  }

  getStatusColor(status: string): string {
    const map: Record<string,string> = {
      PENDING_PAYMENT: 'warning',
      PAYMENT_SUCCESS: 'info',
      VERIFICATION_PENDING: 'warning',
      DRIVER_ASSIGNMENT_PENDING: 'info',
      CONFIRMED: 'success',
      IN_PROGRESS: 'purple',
      INSPECTION_PENDING: 'warning',
      COMPLETED: 'success',
      REJECTED: 'danger',
      CANCELLED: 'muted',
    };
    return map[status] || 'muted';
  }

  trackById(_: number, item: { id: number }) { return item.id; }
}
