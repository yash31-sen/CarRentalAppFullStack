import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SuperAdminService } from '../../../core/services/super-admin';
import { ToastService } from '../../../core/services/toast';
import { AuditLog } from '../../../core/models';

@Component({
  selector: 'app-platform-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './platform-settings.html',
  styleUrls: ['./platform-settings.css']
})
export class PlatformSettingsComponent implements OnInit {
  settingsForm!: FormGroup;
  loading = false;
  saving = false;
  lastUpdatedInfo = '';

  auditLogs: AuditLog[] = [];
  auditPage = 0;
  auditTotalPages = 0;

  constructor(
    private fb: FormBuilder,
    private service: SuperAdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.settingsForm = this.fb.group({
      defaultPlatformCommissionPercent: [20, [Validators.required, Validators.min(0), Validators.max(100)]],
      defaultPlatformServiceFee: [99, [Validators.required, Validators.min(0)]],
      minBookingDurationDays: [1, [Validators.required, Validators.min(1)]],
      maxBookingDurationDays: [30, [Validators.required, Validators.min(1)]],
      cancellationPolicy: ['', Validators.required],
      refundPolicy: ['', Validators.required],
      hostVerificationRequired: [true],
      guestVerificationRequired: [true],
      vehicleListingApprovalRequired: [true],
      oneWayBookingEnabled: [true]
    });

    this.loadSettings();
    this.loadAuditLogs();
  }

  loadSettings(): void {
    this.loading = true;
    this.service.getPlatformSettings().subscribe({
      next: (res) => {
        this.settingsForm.patchValue(res);
        if (res.updatedAt) {
          this.lastUpdatedInfo = `Last updated by ${res.updatedBy || 'SYSTEM'} on ${new Date(res.updatedAt).toLocaleString()}`;
        }
        this.loading = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to load platform settings');
        this.loading = false;
      }
    });
  }

  saveSettings(): void {
    if (this.settingsForm.invalid) return;
    this.saving = true;
    this.service.updatePlatformSettings(this.settingsForm.value).subscribe({
      next: (res) => {
        this.toastService.showSuccess('Platform settings updated and logged');
        this.settingsForm.patchValue(res);
        this.saving = false;
        this.loadAuditLogs();
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to save platform settings');
        this.saving = false;
      }
    });
  }

  loadAuditLogs(page = 0): void {
    this.auditPage = page;
    this.service.getAuditLogs(page, 10).subscribe({
      next: (res) => {
        this.auditLogs = res.content || [];
        this.auditTotalPages = res.totalPages || 0;
      },
      error: () => {}
    });
  }
}
