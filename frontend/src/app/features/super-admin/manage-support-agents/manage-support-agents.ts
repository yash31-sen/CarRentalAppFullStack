import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { SuperAdminService } from '../../../core/services/super-admin';
import { ToastService } from '../../../core/services/toast';
import { AppUser } from '../../../core/models';

@Component({
  selector: 'app-manage-support-agents',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './manage-support-agents.html',
  styleUrls: ['./manage-support-agents.css']
})
export class ManageSupportAgents implements OnInit {
  agents: AppUser[] = [];
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
    this.loadAgents();
  }

  loadAgents(): void {
    this.service.getSupportAgents().subscribe({
      next: (res) => this.agents = res,
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load support agents')
    });
  }

  get filteredAgents(): AppUser[] {
    return this.agents.filter(a => {
      const q = this.searchQuery.toLowerCase();
      const matchQ = !q || a.name.toLowerCase().includes(q) || a.email.toLowerCase().includes(q) || (a.city || '').toLowerCase().includes(q);
      const matchStatus = !this.statusFilter || (this.statusFilter === 'active' && a.active) || (this.statusFilter === 'inactive' && !a.active);
      return matchQ && matchStatus;
    });
  }

  get pagedAgents(): AppUser[] {
    const start = this.currentPage * this.pageSize;
    return this.filteredAgents.slice(start, start + this.pageSize);
  }

  createAgent(): void {
    if (this.form.invalid) return;
    this.loading = true;
    this.service.createSupportAgent(this.form.value).subscribe({
      next: () => {
        this.toastService.showSuccess('Support Agent created and setup invitation sent');
        this.form.reset();
        this.loadAgents();
        this.loading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to create Support Agent');
        this.loading = false;
      }
    });
  }

  openScopeModal(a: AppUser): void {
    this.scopeUserId = a.id;
    this.scopeCity = a.city || '';
    this.scopeValue = a.operationalScope || '';
    this.showScopeModal = true;
  }

  saveScope(): void {
    this.service.assignSupportAgentScope(this.scopeUserId, this.scopeCity, this.scopeValue).subscribe({
      next: () => {
        this.toastService.showSuccess('Support scope updated');
        this.showScopeModal = false;
        this.loadAgents();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to update scope')
    });
  }

  toggleStatus(a: AppUser): void {
    const req = a.active ? this.service.deactivateSupportAgent(a.id) : this.service.reactivateSupportAgent(a.id);
    req.subscribe({
      next: () => {
        this.toastService.showSuccess(`Support Agent ${a.active ? 'deactivated' : 'reactivated'}`);
        this.loadAgents();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Action failed')
    });
  }
}
