import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { SuperAdminService } from '../../../core/services/super-admin';
import { ToastService } from '../../../core/services/toast';
import { AppUser } from '../../../core/models';

@Component({
  selector: 'app-manage-fleet-managers',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './manage-fleet-managers.html',
  styleUrls: ['./manage-fleet-managers.css']
})
export class ManageFleetManagers implements OnInit {
  fleetManagers: AppUser[] = [];
  form!: FormGroup;
  loading = false;

  searchQuery = '';
  statusFilter = '';
  currentPage = 0;
  pageSize = 10;

  showScopeModal = false;
  scopeUserId = 0;
  scopeCity = '';
  scopeValue = '';

  constructor(
    private fb: FormBuilder,
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      city: [''],
      operationalScope: ['']
    });
    this.loadFleetManagers();
  }

  loadFleetManagers(): void {
    this.service.getFleetManagers().subscribe({
      next: (res) => this.fleetManagers = res,
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load fleet managers')
    });
  }

  get filteredManagers(): AppUser[] {
    return this.fleetManagers.filter(m => {
      const q = this.searchQuery.toLowerCase();
      const matchQ = !q || m.name.toLowerCase().includes(q) || m.email.toLowerCase().includes(q) || (m.city || '').toLowerCase().includes(q);
      const matchStatus = !this.statusFilter || (this.statusFilter === 'active' && m.active) || (this.statusFilter === 'inactive' && !m.active);
      return matchQ && matchStatus;
    });
  }

  get totalPages(): number { return Math.ceil(this.filteredManagers.length / this.pageSize); }
  get pagedManagers(): AppUser[] {
    const start = this.currentPage * this.pageSize;
    return this.filteredManagers.slice(start, start + this.pageSize);
  }
  getPages(total: number): number[] { return Array.from({ length: total }, (_, i) => i); }

  createFleetManager(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.service.createFleetManager(this.form.value).subscribe({
      next: () => {
        this.toastService.showSuccess('Fleet Manager created and setup invitation sent');
        this.form.reset();
        this.loadFleetManagers();
        this.loading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to create Fleet Manager');
        this.loading = false;
      }
    });
  }

  openScopeModal(m: AppUser): void {
    this.scopeUserId = m.id;
    this.scopeCity = m.city || '';
    this.scopeValue = m.operationalScope || '';
    this.showScopeModal = true;
  }

  saveScope(): void {
    this.service.assignFleetManagerScope(this.scopeUserId, this.scopeCity, this.scopeValue).subscribe({
      next: () => {
        this.toastService.showSuccess('Operational scope updated');
        this.showScopeModal = false;
        this.loadFleetManagers();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to update scope')
    });
  }

  toggleStatus(m: AppUser): void {
    const req = m.active ? this.service.deactivateFleetManager(m.id) : this.service.reactivateFleetManager(m.id);
    req.subscribe({
      next: () => {
        this.toastService.showSuccess(`Fleet Manager ${m.active ? 'deactivated' : 'reactivated'}`);
        this.loadFleetManagers();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Action failed')
    });
  }
}
