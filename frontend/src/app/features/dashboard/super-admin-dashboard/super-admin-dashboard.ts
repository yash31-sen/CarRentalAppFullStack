import {
  Component,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';

import { ManageAdmins }
from '../../super-admin/manage-admins/manage-admins';

import { ManageUsers }
from '../../super-admin/manage-users/manage-users';

import { ManageDrivers }
from '../../super-admin/manage-drivers/manage-drivers';

import { ManageCities }
from '../../super-admin/manage-cities/manage-cities';

import { ManagePricing }
from '../../super-admin/manage-pricing/manage-pricing';

import { SuperAdminService }
from '../../../core/services/super-admin';

@Component({
  selector: 'app-super-admin-dashboard',
  standalone: true,

  imports: [
    CommonModule,
    ManageAdmins,
    ManageUsers,
    ManageDrivers,
    ManageCities,
    ManagePricing
  ],

  templateUrl: './super-admin-dashboard.html',
  styleUrls: ['./super-admin-dashboard.css']
})
export class SuperAdminDashboard
implements OnInit {

  activeTab = 'dashboard';

  stats = {

    totalAdmins: 0,

    totalUsers: 0,

    totalDrivers: 0,

    totalCities: 0,

    totalCars: 0,

    totalBookings: 0,

    totalRevenue: 0
  };

  constructor(
    private service: SuperAdminService
  ) {}

  ngOnInit(): void {

    this.loadDashboardStats();
  }

  navigate(tab: string) {

    this.activeTab = tab;
  }

  loadDashboardStats() {

    this.service
      .getDashboardStats()
      .subscribe({

        next: (res: any) => {

          this.stats = res;
        },

        error: (err: any) => {

          console.log(err);
        }
      });
  }
}