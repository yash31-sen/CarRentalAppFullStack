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
import { AppUser } from '../../../core/models';

@Component({
  selector: 'app-manage-admins',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule
  ],
  templateUrl: './manage-admins.html',
  styleUrls: ['./manage-admins.css']
})
export class ManageAdmins implements OnInit {

  admins: AppUser[] = [];

  // SEARCH FILTERS
  searchName = '';
  searchEmail = '';
  searchCity = '';
  searchStatus = '';

  get filteredAdmins() {
    return this.admins.filter(admin => {
      const matchName = !this.searchName || admin.name?.toLowerCase().includes(this.searchName.toLowerCase());
      const matchEmail = !this.searchEmail || admin.email?.toLowerCase().includes(this.searchEmail.toLowerCase());
      const matchCity = !this.searchCity || admin.city?.toLowerCase().includes(this.searchCity.toLowerCase());
      const matchStatus = !this.searchStatus || 
        (this.searchStatus === 'active' && admin.active) || 
        (this.searchStatus === 'inactive' && !admin.active);
      return matchName && matchEmail && matchCity && matchStatus;
    });
  }

  loading = false;

  adminForm!: FormGroup;

  // POPUP
  showActionPopup = false;

  selectedAdminId!: number;

  actionType = '';

  constructor(
    private fb: FormBuilder,
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {

    this.adminForm = this.fb.group({

      name: ['', Validators.required],

      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],

      password: [
        '',
        [
          Validators.required,
          Validators.minLength(6)
        ]
      ],

      city: ['', Validators.required]

    });

    this.loadAdmins();
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

  // CREATE ADMIN
  createAdmin() {

    this.loading = true;

    this.service
      .createAdmin(this.adminForm.value)
      .subscribe({

        next: (res: any) => {

          this.toastService.showSuccess(res.message);

          this.adminForm.reset();

          this.loadAdmins();

          this.loading = false;
        },

        error: (err) => {

          console.log(err);

          this.toastService.showError(
            err.error?.message ||
            'Something went wrong'
          );

          this.loading = false;
        }
      });
  }

  // OPEN POPUP
  openPopup(
    id: number,
    action: string
  ) {

    this.selectedAdminId = id;

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
          .deactivateAdmin(this.selectedAdminId);
    }

    else if (
      this.actionType === 'reactivate'
    ) {

      request =
        this.service
          .reactivateAdmin(this.selectedAdminId);
    }

    else if (
      this.actionType === 'delete'
    ) {

      request =
        this.service
          .deleteAdmin(this.selectedAdminId);
    }

    request?.subscribe({

      next: () => {

        this.toastService.showSuccess(
          `Admin ${this.actionType}d successfully`
        );

        this.showActionPopup = false;

        this.loadAdmins();
      },

      error: (err) => {

        console.log(err);

        this.toastService.showError(
          `Failed to ${this.actionType} admin`
        );

        this.showActionPopup = false;
      }
    });
  }
}