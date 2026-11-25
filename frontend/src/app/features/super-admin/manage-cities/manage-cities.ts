import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';

import { SuperAdminService } from '../../../core/services/super-admin';
import { ToastService } from '../../../core/services/toast';
import { AppUser, ServiceCity } from '../../../core/models';

@Component({
  selector: 'app-manage-cities',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './manage-cities.html',
  styleUrls: ['./manage-cities.css']
})
export class ManageCities implements OnInit {

  cityForm!: FormGroup;
  cities: ServiceCity[] = [];
  admins: AppUser[] = [];

  editMode = false;
  editingCityId = 0;

  // SEARCH FILTERS
  searchCityName = '';
  searchAddress = '';
  searchStatus = '';

  get filteredCities() {
    return this.cities.filter(city => {
      const matchCityName = !this.searchCityName || city.cityName?.toLowerCase().includes(this.searchCityName.toLowerCase());
      const matchAddress = !this.searchAddress || city.parkingAddress?.toLowerCase().includes(this.searchAddress.toLowerCase());
      const matchStatus = !this.searchStatus ||
        (this.searchStatus === 'active' && city.active) ||
        (this.searchStatus === 'inactive' && !city.active);
      return matchCityName && matchAddress && matchStatus;
    });
  }

  // Pagination
  currentPage = 0;
  pageSize = 10;
  get totalPages() { return Math.ceil(this.filteredCities.length / this.pageSize); }
  get pagedCities() {
    const start = this.currentPage * this.pageSize;
    return this.filteredCities.slice(start, start + this.pageSize);
  }
  getPages(total: number) { return Array.from({ length: total }, (_, i) => i); }
  onFilterChange() { this.currentPage = 0; }

  loading = false;

  constructor(
    private fb: FormBuilder,
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.cityForm = this.fb.group({
      cityName: ['', Validators.required],
      parkingAddress: ['', Validators.required],
      parkingContact: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      oneWayFee: [500, [Validators.required, Validators.min(0)]],
      localServiceCharge: [0, [Validators.min(0)]],
      bookingAvailable: [true],
      adminId: ['']
    });

    this.loadCities();
    this.loadAdmins();
  }

  loadCities() {
    this.service.getCities().subscribe({
      next: (res) => this.cities = res,
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load cities')
    });
  }

  loadAdmins() {
    this.service.getAdmins().subscribe({
      next: (res) => this.admins = res,
      error: () => {}
    });
  }

  submitCity() {
    if (this.cityForm.invalid) return;
    this.loading = true;
    const payload = {
      ...this.cityForm.value,
      adminId: this.cityForm.value.adminId ? Number(this.cityForm.value.adminId) : null
    };

    const req = this.editMode
      ? this.service.updateCity(this.editingCityId, payload)
      : this.service.createCity(payload);

    req.subscribe({
      next: () => {
        this.toastService.showSuccess(this.editMode ? 'City updated successfully' : 'City created successfully');
        this.resetForm();
        this.loadCities();
        this.loading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to save city');
        this.loading = false;
      }
    });
  }

  editCity(city: ServiceCity) {
    this.editMode = true;
    this.editingCityId = city.id;
    this.cityForm.patchValue({
      cityName: city.cityName,
      parkingAddress: city.parkingAddress,
      parkingContact: city.parkingContact,
      oneWayFee: city.oneWayFee,
      localServiceCharge: city.localServiceCharge ?? 0,
      bookingAvailable: city.bookingAvailable !== false,
      adminId: city.admin?.id || ''
    });
  }

  resetForm() {
    this.editMode = false;
    this.editingCityId = 0;
    this.cityForm.reset({ oneWayFee: 500, localServiceCharge: 0, bookingAvailable: true, adminId: '' });
  }

  toggleCityStatus(city: ServiceCity) {
    const req = city.active ? this.service.deactivateCity(city.id) : this.service.reactivateCity(city.id);
    req.subscribe({
      next: () => {
        this.toastService.showSuccess(`City ${city.active ? 'deactivated' : 'reactivated'}`);
        this.loadCities();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to update city status')
    });
  }

  deleteCity(city: ServiceCity) {
    if (!confirm(`Delete city "${city.cityName}"?`)) return;
    this.service.deleteCity(city.id).subscribe({
      next: () => {
        this.toastService.showSuccess('City deleted successfully');
        this.loadCities();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Cannot delete city')
    });
  }
}
