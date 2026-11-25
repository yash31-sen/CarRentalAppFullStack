import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SuperAdminService } from '../../../core/services/super-admin';
import { ToastService } from '../../../core/services/toast';
import { HostDetail, HostSummary } from '../../../core/models';

@Component({
  selector: 'app-manage-hosts',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manage-hosts.html',
  styleUrls: ['./manage-hosts.css']
})
export class ManageHosts implements OnInit {
  hosts: HostSummary[] = [];
  loading = false;

  // Filters
  searchQuery = '';
  verificationFilter = '';
  statusFilter = '';

  // Pagination
  currentPage = 0;
  pageSize = 10;

  // Detail & Verification modal
  selectedHost: HostDetail | null = null;
  showDetailModal = false;
  detailLoading = false;
  detailTab: 'profile' | 'vehicles' | 'bookings' | 'payouts' | 'reviews' | 'incidents' = 'profile';

  // Verification / Suspension reason modal
  showReasonModal = false;
  reasonAction: 'REJECTED' | 'RESUBMISSION_REQUIRED' | 'SUSPEND' = 'REJECTED';
  reasonHostId = 0;
  actionReason = '';

  // Confirmation modal
  showConfirmModal = false;
  confirmHostId = 0;
  confirmActionType: 'VERIFIED' | 'deactivate' | 'reactivate' | 'unsuspend' = 'VERIFIED';

  constructor(
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadHosts();
  }

  loadHosts(): void {
    this.loading = true;
    this.service.getHosts(
      this.searchQuery.trim() || undefined,
      this.verificationFilter || undefined,
      this.statusFilter || undefined
    ).subscribe({
      next: (res) => {
        this.hosts = res;
        this.currentPage = 0;
        this.loading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to load hosts');
        this.loading = false;
      }
    });
  }

  get totalPages(): number {
    return Math.ceil(this.hosts.length / this.pageSize);
  }

  get pagedHosts(): HostSummary[] {
    const start = this.currentPage * this.pageSize;
    return this.hosts.slice(start, start + this.pageSize);
  }

  getPages(total: number): number[] {
    return Array.from({ length: total }, (_, i) => i);
  }

  openDetails(hostId: number): void {
    this.detailLoading = true;
    this.showDetailModal = true;
    this.detailTab = 'profile';
    this.service.getHostDetails(hostId).subscribe({
      next: (res) => {
        this.selectedHost = res;
        this.detailLoading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to load host details');
        this.detailLoading = false;
        this.showDetailModal = false;
      }
    });
  }

  closeDetails(): void {
    this.showDetailModal = false;
    this.selectedHost = null;
  }

  openConfirm(hostId: number, action: 'VERIFIED' | 'deactivate' | 'reactivate' | 'unsuspend'): void {
    this.confirmHostId = hostId;
    this.confirmActionType = action;
    this.showConfirmModal = true;
  }

  confirmAction(): void {
    let req;
    if (this.confirmActionType === 'VERIFIED') {
      req = this.service.verifyHost(this.confirmHostId, 'VERIFIED');
    } else if (this.confirmActionType === 'deactivate') {
      req = this.service.deactivateHost(this.confirmHostId);
    } else if (this.confirmActionType === 'reactivate') {
      req = this.service.reactivateHost(this.confirmHostId);
    } else {
      req = this.service.unsuspendHost(this.confirmHostId);
    }

    req.subscribe({
      next: () => {
        this.toastService.showSuccess('Host updated successfully');
        this.showConfirmModal = false;
        this.loadHosts();
        if (this.selectedHost && this.selectedHost.id === this.confirmHostId) {
          this.openDetails(this.confirmHostId);
        }
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Action failed');
        this.showConfirmModal = false;
      }
    });
  }

  openReasonModal(hostId: number, action: 'REJECTED' | 'RESUBMISSION_REQUIRED' | 'SUSPEND'): void {
    this.reasonHostId = hostId;
    this.reasonAction = action;
    this.actionReason = '';
    this.showReasonModal = true;
  }

  submitReasonAction(): void {
    if (!this.actionReason.trim()) {
      this.toastService.showError('Please provide a reason');
      return;
    }

    const req = this.reasonAction === 'SUSPEND'
      ? this.service.suspendHost(this.reasonHostId, this.actionReason.trim())
      : this.service.verifyHost(this.reasonHostId, this.reasonAction, this.actionReason.trim());

    req.subscribe({
      next: () => {
        this.toastService.showSuccess('Host updated successfully');
        this.showReasonModal = false;
        this.loadHosts();
        if (this.selectedHost && this.selectedHost.id === this.reasonHostId) {
          this.openDetails(this.reasonHostId);
        }
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Action failed');
      }
    });
  }
}
