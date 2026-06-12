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
import { AppUser, ServiceCity } from '../../../core/models';

@Component({
  selector: 'app-manage-cities',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './manage-cities.html',
  styleUrls: ['./manage-cities.css']
})
export class ManageCities implements OnInit {

  cityForm!: FormGroup;

  cities: ServiceCity[] = [];

  admins: AppUser[] = [];

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

  loading = false;

  constructor(
    private fb: FormBuilder,
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {

    this.cityForm = this.fb.group({

      cityName: [
        '',
        Validators.required
      ],

      parkingAddress: [
        '',
        Validators.required
      ],

      parkingContact: [
        '',
        Validators.required
      ],

      oneWayFee: [
        '',
        Validators.required
      ],

      adminId: ['']
    });

    this.loadCities();

    this.loadAdmins();
  }

  // LOAD CITIES
  loadCities() {

    this.service
      .getCities()
      .subscribe({

        next: (res) => {

          this.cities = res;
        },

        error: (err) => {

          console.log(err);
        }
      });
  }

  // LOAD ADMINS
  loadAdmins() {

    this.service
      .getAdmins()
      .subscribe({

        next: (res) => {

          this.admins = res;
        },

        error: (err) => {

          console.log(err);
        }
      });
  }

  // CREATE CITY
  createCity() {

    this.loading = true;

    const payload = {

      ...this.cityForm.value,

      adminId:
        this.cityForm.value.adminId || null
    };

    this.service
      .createCity(payload)
      .subscribe({

        next: () => {

          this.toastService.showSuccess('City created successfully');

          this.cityForm.reset();

          this.loadCities();

          this.loading = false;
        },

        error: (err) => {

          console.log(err);

          this.toastService.showError(
            err.error?.message ||
            'Failed to create city'
          );

          this.loading = false;
        }
      });
  }
}