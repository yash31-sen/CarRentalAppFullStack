import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormsModule, Validators } from '@angular/forms';

import { SuperAdminService } from '../../../core/services/super-admin';
import { ToastService } from '../../../core/services/toast';
import { CarPricing } from '../../../core/models';

@Component({
  selector: 'app-manage-pricing',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './manage-pricing.html',
  styleUrls: ['./manage-pricing.css']
})
export class ManagePricing implements OnInit {

  pricingForm!: FormGroup;
  pricingList: CarPricing[] = [];

  // SEARCH FILTERS
  searchCarClass = '';
  searchStatus = '';

  get filteredPricing() {
    return this.pricingList.filter(p => {
      const matchCarClass = !this.searchCarClass || p.carClass === this.searchCarClass;
      const isActive = p.active !== false;
      const matchStatus = !this.searchStatus ||
        (this.searchStatus === 'active' && isActive) ||
        (this.searchStatus === 'inactive' && !isActive);
      return matchCarClass && matchStatus;
    });
  }

  // Pagination
  currentPage = 0;
  pageSize = 10;
  get totalPages() { return Math.ceil(this.filteredPricing.length / this.pageSize); }
  get pagedPricing() {
    const start = this.currentPage * this.pageSize;
    return this.filteredPricing.slice(start, start + this.pageSize);
  }
  getPages(total: number) { return Array.from({ length: total }, (_, i) => i); }
  onFilterChange() { this.currentPage = 0; }

  loading = false;
  showDeletePopup = false;
  selectedPricingId!: number;
  editMode = false;

  carClasses = ['ECONOMY', 'HATCHBACK', 'SEDAN', 'SUV', 'LUXURY'];

  constructor(
    private fb: FormBuilder,
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.pricingForm = this.fb.group({
      carClass: ['', Validators.required],
      minPerDayRate: ['', [Validators.required, Validators.min(1)]],
      maxPerDayRate: ['', [Validators.required, Validators.min(1)]],
      perDayRate: ['', [Validators.required, Validators.min(1)]],
      platformCommissionPercent: [20, [Validators.required, Validators.min(0), Validators.max(100)]],
      minPlatformFee: [0, [Validators.min(0)]],
      maxPlatformFee: [null],
      minBookingDurationDays: [1, [Validators.required, Validators.min(1)]],
      maxBookingDurationDays: [30, [Validators.required, Validators.min(1)]],
      baseFare: [0, [Validators.required, Validators.min(0)]],
      freeKmPerDay: [100, [Validators.required, Validators.min(0)]],
      extraKmRate: [12, [Validators.required, Validators.min(0)]],
      active: [true]
    });

    this.loadPricing();
  }

  loadPricing() {
    this.service.getPricing().subscribe({
      next: (res) => this.pricingList = res,
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load pricing rules')
    });
  }

  submitPricing() {
    if (this.pricingForm.invalid) return;
    this.loading = true;
    const raw = this.pricingForm.getRawValue();

    const req = this.editMode
      ? this.service.updatePricing(this.selectedPricingId, raw)
      : this.service.createPricing(raw);

    req.subscribe({
      next: () => {
        this.toastService.showSuccess(this.editMode ? 'Pricing updated successfully' : 'Pricing created successfully');
        this.resetForm();
        this.loadPricing();
        this.loading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to save pricing configuration');
        this.loading = false;
      }
    });
  }

  editPricing(pricing: CarPricing) {
    this.editMode = true;
    this.selectedPricingId = pricing.id;
    this.pricingForm.patchValue({
      carClass: pricing.carClass,
      minPerDayRate: pricing.minPerDayRate,
      maxPerDayRate: pricing.maxPerDayRate,
      perDayRate: pricing.perDayRate,
      platformCommissionPercent: pricing.platformCommissionPercent ?? 20,
      minPlatformFee: pricing.minPlatformFee ?? 0,
      maxPlatformFee: pricing.maxPlatformFee ?? null,
      minBookingDurationDays: pricing.minBookingDurationDays ?? 1,
      maxBookingDurationDays: pricing.maxBookingDurationDays ?? 30,
      baseFare: pricing.baseFare,
      freeKmPerDay: pricing.freeKmPerDay,
      extraKmRate: pricing.extraKmRate,
      active: pricing.active !== false
    });
    this.pricingForm.get('carClass')?.disable();
  }

  togglePricingStatus(p: CarPricing) {
    const isActive = p.active !== false;
    const req = isActive ? this.service.deactivatePricing(p.id) : this.service.reactivatePricing(p.id);
    req.subscribe({
      next: () => {
        this.toastService.showSuccess(`Pricing rule ${isActive ? 'deactivated' : 'activated'}`);
        this.loadPricing();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to update pricing status')
    });
  }

  deletePricing(id: number) {
    this.selectedPricingId = id;
    this.showDeletePopup = true;
  }

  cancelDelete() {
    this.showDeletePopup = false;
  }

  confirmDelete() {
    this.service.deletePricing(this.selectedPricingId).subscribe({
      next: () => {
        this.toastService.showSuccess('Pricing deleted successfully');
        this.showDeletePopup = false;
        this.loadPricing();
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to delete pricing');
        this.showDeletePopup = false;
      }
    });
  }

  resetForm() {
    this.editMode = false;
    this.selectedPricingId = 0;
    this.pricingForm.reset({
      platformCommissionPercent: 20,
      minPlatformFee: 0,
      minBookingDurationDays: 1,
      maxBookingDurationDays: 30,
      baseFare: 0,
      freeKmPerDay: 100,
      extraKmRate: 12,
      active: true
    });
    this.pricingForm.get('carClass')?.enable();
  }
}
