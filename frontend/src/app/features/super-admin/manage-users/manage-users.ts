import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { SuperAdminService }
from '../../../core/services/super-admin';
import { ToastService }
from '../../../core/services/toast';
import { AppUser } from '../../../core/models';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './manage-users.html',
  styleUrls: ['./manage-users.css']
})
export class ManageUsers implements OnInit {

  users: AppUser[] = [];

  // SEARCH FILTERS
  searchName = '';
  searchEmail = '';
  searchStatus = '';

  get filteredUsers() {
    return this.users.filter(user => {
      const matchName = !this.searchName || user.name?.toLowerCase().includes(this.searchName.toLowerCase());
      const matchEmail = !this.searchEmail || user.email?.toLowerCase().includes(this.searchEmail.toLowerCase());
      const matchStatus = !this.searchStatus || 
        (this.searchStatus === 'active' && user.active) || 
        (this.searchStatus === 'inactive' && !user.active);
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

  // POPUP
  showActionPopup = false;

  selectedUserId!: number;

  actionType = '';

  constructor(
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {

    this.loadUsers();
  }

  // LOAD USERS
  loadUsers() {

    this.service
      .getUsers()
      .subscribe({

        next: (res) => {

          this.users = res;
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

    this.selectedUserId = id;

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
          .deactivateUser(this.selectedUserId);
    }

    else if (
      this.actionType === 'reactivate'
    ) {

      request =
        this.service
          .reactivateUser(this.selectedUserId);
    }

    else if (
      this.actionType === 'delete'
    ) {

      request =
        this.service
          .deleteUser(this.selectedUserId);
    }

    request?.subscribe({

      next: () => {

        this.toastService.showSuccess(
          `User ${this.actionType}d successfully`
        );

        this.showActionPopup = false;

        this.loadUsers();
      },

      error: (err) => {

        console.log(err);

        this.toastService.showError(
          `Failed to ${this.actionType} user`
        );

        this.showActionPopup = false;
      }
    });
  }
}