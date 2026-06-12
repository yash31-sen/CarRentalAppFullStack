import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SuperAdminService }
from '../../../core/services/super-admin';
import { ToastService }
from '../../../core/services/toast';
import { DriverProfile } from '../../../core/models';

@Component({
  selector: 'app-manage-drivers',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './manage-drivers.html',
  styleUrls: ['./manage-drivers.css']
})
export class ManageDrivers implements OnInit {

  drivers: DriverProfile[] = [];

  // SEARCH FILTERS
  searchName = '';
  searchEmail = '';
  searchLicense = '';
  searchStatus = '';

  get filteredDrivers() {
    return this.drivers.filter(driver => {
      const matchName = !this.searchName || driver.user?.name?.toLowerCase().includes(this.searchName.toLowerCase());
      const matchEmail = !this.searchEmail || driver.user?.email?.toLowerCase().includes(this.searchEmail.toLowerCase());
      const matchLicense = !this.searchLicense || driver.licenseNumber?.toLowerCase().includes(this.searchLicense.toLowerCase());
      const matchStatus = !this.searchStatus || 
        (this.searchStatus === 'active' && driver.active) || 
        (this.searchStatus === 'inactive' && !driver.active);
      return matchName && matchEmail && matchLicense && matchStatus;
    });
  }

  // POPUP
  showActionPopup = false;

  selectedDriverId!: number;

  actionType = '';

  constructor(
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {

    this.loadDrivers();
  }

  // LOAD DRIVERS
  loadDrivers() {

    this.service
      .getDrivers()
      .subscribe({

        next: (res) => {

          this.drivers = res;
        },

        error: (err) => {

          console.log(err);
        }
      });
  }

  // OPEN POPUP
  openPopup(
    id: number,
    action: string
  ) {

    this.selectedDriverId = id;

    this.actionType = action;

    this.showActionPopup = true;
  }

  // CANCEL ACTION
  cancelAction() {

    this.showActionPopup = false;
  }

  // CONFIRM ACTION
  confirmAction() {

    let request;

    if (this.actionType === 'deactivate') {

      request =
        this.service
          .deactivateDriver(
            this.selectedDriverId
          );
    }

    else if (
      this.actionType === 'reactivate'
    ) {

      request =
        this.service
          .reactivateDriver(
            this.selectedDriverId
          );
    }

    request?.subscribe({

      next: () => {

        this.toastService.showSuccess(
          `Driver ${this.actionType}d successfully`
        );

        this.showActionPopup = false;

        this.loadDrivers();
      },

      error: (err) => {

        console.log(err);

        this.toastService.showError(
          `Failed to ${this.actionType} driver`
        );

        this.showActionPopup = false;
      }
    });
  }
}