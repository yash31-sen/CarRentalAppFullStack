import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SuperAdminService } from '../../../core/services/super-admin';
import { ToastService } from '../../../core/services/toast';
import { AppUser, UserDetail } from '../../../core/models';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manage-users.html',
  styleUrls: ['./manage-users.css']
})
export class ManageUsers implements OnInit {

  users: AppUser[] = [];
  loading = false;

  // SEARCH FILTERS
  searchName = '';
  searchEmail = '';
  searchStatus = '';

  get filteredUsers() {
    return this.users.filter(user => {
      const matchName = !this.searchName || user.name?.toLowerCase().includes(this.searchName.toLowerCase());
      const matchEmail = !this.searchEmail || user.email?.toLowerCase().includes(this.searchEmail.toLowerCase());
      const matchStatus = !this.searchStatus ||
        (this.searchStatus === 'active' && user.active && !user.suspended) ||
        (this.searchStatus === 'inactive' && !user.active && !user.suspended) ||
        (this.searchStatus === 'suspended' && user.suspended);
      return matchName && matchEmail && matchStatus;
    });
  }

  // Pagination
  currentPage = 0;
  pageSize = 10;
  get totalPages() { return Math.ceil(this.filteredUsers.length / this.pageSize); }
  get pagedUsers() {
    const start = this.currentPage * this.pageSize;
    return this.filteredUsers.slice(start, start + this.pageSize);
  }
  getPages(total: number) { return Array.from({ length: total }, (_, i) => i); }
  onFilterChange() { this.currentPage = 0; }

  // ACTION POPUP
  showActionPopup = false;
  selectedUserId!: number;
  actionType = '';

  // SUSPEND MODAL
  showSuspendModal = false;
  suspendReason = '';

  // USER DETAIL MODAL
  showDetailModal = false;
  detailLoading = false;
  selectedUserDetail: UserDetail | null = null;
  detailTab: 'profile' | 'bookings' | 'payments' | 'reviews' | 'incidents' = 'profile';

  constructor(
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;
    this.service.getUsers().subscribe({
      next: (res) => {
        this.users = res;
        this.loading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to load users');
        this.loading = false;
      }
    });
  }

  openDetails(userId: number) {
    this.detailLoading = true;
    this.showDetailModal = true;
    this.detailTab = 'profile';
    this.service.getUserDetails(userId).subscribe({
      next: (res) => {
        this.selectedUserDetail = res;
        this.detailLoading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to load user details');
        this.detailLoading = false;
        this.showDetailModal = false;
      }
    });
  }

  closeDetails() {
    this.showDetailModal = false;
    this.selectedUserDetail = null;
  }

  openPopup(id: number, action: string) {
    this.selectedUserId = id;
    this.actionType = action;
    if (action === 'suspend') {
      this.suspendReason = '';
      this.showSuspendModal = true;
    } else {
      this.showActionPopup = true;
    }
  }

  cancelAction() {
    this.showActionPopup = false;
    this.showSuspendModal = false;
  }

  confirmSuspend() {
    if (!this.suspendReason.trim()) {
      this.toastService.showError('Please enter a suspension reason');
      return;
    }
    this.service.suspendUser(this.selectedUserId, this.suspendReason.trim()).subscribe({
      next: () => {
        this.toastService.showSuccess('User suspended successfully');
        this.showSuspendModal = false;
        this.loadUsers();
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to suspend user');
        this.showSuspendModal = false;
      }
    });
  }

  confirmAction() {
    let request;
    if (this.actionType === 'deactivate') {
      request = this.service.deactivateUser(this.selectedUserId);
    } else if (this.actionType === 'reactivate') {
      request = this.service.reactivateUser(this.selectedUserId);
    } else if (this.actionType === 'unsuspend') {
      request = this.service.unsuspendUser(this.selectedUserId);
    } else if (this.actionType === 'delete') {
      request = this.service.deleteUser(this.selectedUserId);
    }

    request?.subscribe({
      next: () => {
        this.toastService.showSuccess(`User ${this.actionType}d successfully`);
        this.showActionPopup = false;
        this.loadUsers();
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || `Failed to ${this.actionType} user`);
        this.showActionPopup = false;
      }
    });
  }
}
