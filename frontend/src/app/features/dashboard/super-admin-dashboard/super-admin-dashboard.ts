import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';

import { ManageAdmins } from '../../super-admin/manage-admins/manage-admins';
import { ManageUsers } from '../../super-admin/manage-users/manage-users';
import { ManageHosts } from '../../super-admin/manage-hosts/manage-hosts';
import { ManageCities } from '../../super-admin/manage-cities/manage-cities';
import { ManagePricing } from '../../super-admin/manage-pricing/manage-pricing';
import { ManageFleetManagers } from '../../super-admin/manage-fleet-managers/manage-fleet-managers';
import { ManageSupportAgents } from '../../super-admin/manage-support-agents/manage-support-agents';
import { PlatformSettingsComponent } from '../../super-admin/platform-settings/platform-settings';

import { SuperAdminService } from '../../../core/services/super-admin';
import { AuthService } from '../../../core/services/auth';
import { DashboardStats } from '../../../core/models';

@Component({
  selector: 'app-super-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    ManageAdmins,
    ManageUsers,
    ManageHosts,
    ManageCities,
    ManagePricing,
    ManageFleetManagers,
    ManageSupportAgents,
    PlatformSettingsComponent
  ],
  templateUrl: './super-admin-dashboard.html',
  styleUrls: ['./super-admin-dashboard.css']
})
export class SuperAdminDashboard implements OnInit {

  activeTab = 'dashboard';
  loadingStats = false;

  readonly validTabs = [
    'dashboard',
    'admins',
    'users',
    'hosts',
    'cities',
    'pricing',
    'fleet-managers',
    'support-agents',
    'settings'
  ];

  stats: DashboardStats = {
    totalAdmins: 0,
    totalUsers: 0,
    totalHosts: 0,
    totalCities: 0,
    totalListedCars: 0,
    totalBookings: 0,
    totalRevenue: 0,
    platformEarnings: 0,
    pendingHostVerifications: 0,
    pendingVehicleApprovals: 0,
    activeBookings: 0,
    availableCars: 0,
    unavailableCars: 0,
    pendingHostPayouts: 0,
    pendingHostPayoutAmount: 0
  };

  constructor(
    private service: SuperAdminService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const tab = params.get('tab');
      if (tab && this.validTabs.includes(tab)) {
        this.activeTab = tab;
      } else {
        this.activeTab = 'dashboard';
      }
      if (this.activeTab === 'dashboard') {
        this.loadDashboardStats();
      }
    });
  }

  navigate(tab: string) {
    this.activeTab = tab;
    if (tab === 'dashboard') {
      this.router.navigate(['/super-admin-dashboard']);
      this.loadDashboardStats();
    } else {
      this.router.navigate(['/super-admin-dashboard', tab]);
    }
  }

  loadDashboardStats() {
    this.loadingStats = true;
    this.service.getDashboardStats().subscribe({
      next: (res) => {
        this.stats = res;
        this.loadingStats = false;
      },
      error: (err) => {
        console.error(err);
        this.loadingStats = false;
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
