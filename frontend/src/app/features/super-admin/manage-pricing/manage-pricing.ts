import { Component, OnInit } from '@angular/core';

import { CommonModule } from '@angular/common';

import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  FormsModule,
  Validators
} from '@angular/forms';

import { SuperAdminService }
from '../../../core/services/super-admin';
import { ToastService }
from '../../../core/services/toast';
import { CarPricing } from '../../../core/models';

@Component({
  selector: 'app-manage-pricing',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './manage-pricing.html',
  styleUrls: ['./manage-pricing.css']
})
export class ManagePricing implements OnInit {

  pricingForm!: FormGroup;

  pricingList: CarPricing[] = [];

  // SEARCH FILTERS
  searchCarClass = '';
  searchMaxBaseFare: number | null = null;

  get filteredPricing() {
    return this.pricingList.filter(p => {
      const matchCarClass = !this.searchCarClass || p.carClass === this.searchCarClass;
      const matchMaxFare = this.searchMaxBaseFare === null || this.searchMaxBaseFare === undefined || p.baseFare <= this.searchMaxBaseFare;
      return matchCarClass && matchMaxFare;
    });
  }

  loading = false;
  showDeletePopup = false;

selectedPricingId!: number;

  editMode = false;

 
  carClasses = [
    'ECONOMY',
    'HATCHBACK',
    'SEDAN',
    'SUV',
    'LUXURY'
  ];

  constructor(
    private fb: FormBuilder,
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {

    this.pricingForm = this.fb.group({

      carClass: [
        '',
        Validators.required
      ],

      baseFare: [
        '',
        Validators.required
      ],

      perDayRate: [
        '',
        Validators.required
      ],

      minPerDayRate: [
        '',
        Validators.required
      ],

      maxPerDayRate: [
        '',
        Validators.required
      ],

      freeKmPerDay: [
        '',
        Validators.required
      ],

      extraKmRate: [
        '',
        Validators.required
      ]
    });

    this.loadPricing();
  }

  // LOAD PRICING
  loadPricing() {

    this.service
      .getPricing()
      .subscribe({

        next: (res) => {

          this.pricingList = res;
        },

        error: (err) => {

          console.log(err);
        }
      });
  }

  // CREATE / UPDATE
  submitPricing() {

    this.loading = true;

    // UPDATE
    if (this.editMode) {

      this.service
        .updatePricing(
          this.selectedPricingId,
          this.pricingForm.value
        )
        .subscribe({

          next: () => {

            this.toastService.showSuccess('Pricing updated successfully');

            this.resetForm();

            this.loadPricing();

            this.loading = false;
          },

          error: (err) => {

            console.log(err);

            this.toastService.showError(
              err.error?.message ||
              'Failed to update pricing'
            );

            this.loading = false;
          }
        });

      return;
    }

    // CREATE
    this.service
      .createPricing(
        this.pricingForm.value
      )
      .subscribe({

        next: () => {

          this.toastService.showSuccess('Pricing created successfully');

          this.resetForm();

          this.loadPricing();

          this.loading = false;
        },

        error: (err) => {

          console.log(err);

          this.toastService.showError(
            err.error?.message ||
            'Failed to create pricing'
          );

          this.loading = false;
        }
      });
  }

  // EDIT
  editPricing(pricing: CarPricing) {

    this.editMode = true;

    this.selectedPricingId =
      pricing.id;

    this.pricingForm.patchValue({

      carClass:
        pricing.carClass,

      baseFare:
        pricing.baseFare,

      perDayRate:
        pricing.perDayRate,

      minPerDayRate:
        pricing.minPerDayRate,

      maxPerDayRate:
        pricing.maxPerDayRate,

      freeKmPerDay:
        pricing.freeKmPerDay,

      extraKmRate:
        pricing.extraKmRate
    });

    // 🔒 Disable Car Class field in Edit Mode
    this.pricingForm.get('carClass')?.disable();
  }

  // DELETE
  deletePricing(id: number) {

  this.selectedPricingId = id;

  this.showDeletePopup = true;

   
  }
  cancelDelete() {

  this.showDeletePopup = false;
}
confirmDelete() {

  this.service
    .deletePricing(this.selectedPricingId)
    .subscribe({

      next: () => {

        this.toastService.showSuccess('Pricing deleted successfully');

        this.showDeletePopup = false;

        this.loadPricing();
      },

      error: (err) => {

        console.log(err);

        this.toastService.showError('Failed to delete pricing');

        this.showDeletePopup = false;
      }
    });
}

  // RESET
  resetForm() {

    this.editMode = false;

    this.selectedPricingId = 0;

    this.pricingForm.reset();

    // 🔓 Re-enable Car Class field for new entries
    this.pricingForm.get('carClass')?.enable();
  }
}