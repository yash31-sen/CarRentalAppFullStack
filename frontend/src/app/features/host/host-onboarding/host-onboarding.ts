import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HostService } from '../../../core/services/host.service';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-host-onboarding',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './host-onboarding.html',
})
export class HostOnboarding implements OnInit {
  loading = true;
  saving = false;
  submitting = false;
  uploadingDoc = false;
  verifyingMobile = false;

  statusData: any = null;
  availableCities: string[] = [];
  history: any[] = [];

  form = {
    fullName: '',
    businessName: '',
    phone: '',
    city: '',
    address: '',
    state: '',
    postalCode: '',
    documentType: 'AADHAAR',
    governmentIdNumber: '',
    panNumber: '',
    description: '',
    termsAccepted: false,
  };

  message = '';
  error = '';
  mobileMessage = '';

  constructor(
    private hostService: HostService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadStatus();
  }

  loadStatus() {
    this.loading = true;
    this.hostService.getOnboardingStatus().subscribe({
      next: (res) => {
        this.loading = false;
        this.statusData = res;
        this.availableCities = res.availableCities || [];
        this.history = res.history || [];
        this.form.fullName = res.fullName || res.name || '';
        this.form.businessName = res.businessName || '';
        this.form.phone = res.phone || res.mobileNumber || '';
        this.form.city = res.city && res.city !== 'PENDING' ? res.city : (this.availableCities[0] || '');
        this.form.address = res.address || '';
        this.form.state = res.state || '';
        this.form.postalCode = res.postalCode || '';
        this.form.documentType = res.documentType || 'AADHAAR';
        this.form.governmentIdNumber = res.governmentIdNumber || '';
        this.form.panNumber = res.panNumber || '';
        this.form.description = res.description || '';
        this.form.termsAccepted = !!res.termsAccepted;
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Failed to load Host onboarding status.';
      },
    });
  }

  get isApproved(): boolean {
    const s = this.statusData?.status;
    return s === 'VERIFIED' || s === 'APPROVED';
  }

  get isLockedForReview(): boolean {
    return this.statusData?.status === 'PENDING_ADMIN_APPROVAL' || this.isApproved;
  }

  saveDraft() {
    this.saving = true;
    this.message = '';
    this.error = '';
    this.hostService.saveOnboardingDraft(this.form).subscribe({
      next: (res) => {
        this.saving = false;
        this.statusData = res;
        this.history = res.history || [];
        this.message = 'Onboarding draft saved.';
      },
      error: (err) => {
        this.saving = false;
        this.error = err.error?.message || 'Failed to save draft.';
      },
    });
  }

  onFileSelected(event: Event, docKind: string) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];

    this.uploadingDoc = true;
    this.message = '';
    this.error = '';
    this.hostService.uploadDocument(file, docKind).subscribe({
      next: (res) => {
        this.uploadingDoc = false;
        this.message = res.message || 'Document uploaded.';
        this.loadStatus();
      },
      error: (err) => {
        this.uploadingDoc = false;
        this.error = err.error?.message || 'Failed to upload document.';
      },
    });
  }

  sendMobileVerification() {
    if (!this.form.phone.trim()) {
      this.error = 'Please enter your mobile number first.';
      return;
    }
    this.verifyingMobile = true;
    this.mobileMessage = '';
    this.authService.sendMobileVerification(this.form.phone.trim()).subscribe({
      next: (res: any) => {
        this.verifyingMobile = false;
        this.mobileMessage = res.message;
        this.loadStatus();
      },
      error: (err) => {
        this.verifyingMobile = false;
        this.mobileMessage = err.error?.message || 'Could not initiate mobile verification.';
      },
    });
  }

  submitApplication() {
    this.submitting = true;
    this.message = '';
    this.error = '';
    this.hostService.saveOnboardingDraft(this.form).subscribe({
      next: () => {
        this.hostService.submitOnboarding().subscribe({
          next: (res) => {
            this.submitting = false;
            this.statusData = res;
            this.history = res.history || [];
            this.message = 'Host application submitted for Admin verification!';
          },
          error: (err) => {
            this.submitting = false;
            this.error = err.error?.message || 'Please complete all required fields and upload your ID document.';
          },
        });
      },
      error: (err) => {
        this.submitting = false;
        this.error = err.error?.message || 'Failed to save details before submission.';
      },
    });
  }

  enterHostPortal() {
    if (this.authService.hasRole('HOST')) {
      this.router.navigate(['/host-dashboard']);
      return;
    }
    this.authService.refreshToken().subscribe({
      next: (res: any) => {
        this.authService.setToken(res.token, res.tokenType || 'Bearer', res.refreshToken);
        this.router.navigate(['/host-dashboard']);
      },
      error: () => {
        this.router.navigate(['/login']);
      },
    });
  }
}
