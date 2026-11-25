import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../../../core/services/admin';
import { AuthService } from '../../../core/services/auth';
import { ToastService } from '../../../core/services/toast';
import { HostDetail, HostSummary } from '../../../core/models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css']
})
export class AdminDashboard implements OnInit {
  activeTab: 'dashboard' | 'cars' | 'hosts' | 'bookings' | 'inspections' = 'dashboard';
  bookingSubTab: 'all' | 'verification' | 'issues' = 'all';

  // Admin identity & city scope
  adminName = 'Administrator';
  adminEmail = '';
  adminRole = 'ADMIN';
  assignedCities: string[] = [];
  selectedCity = '';

  // Overview stats & dynamic action queue
  loadingDashboard = false;
  stats = {
    totalHosts: 0,
    verifiedHosts: 0,
    pendingHostApprovals: 0,
    totalListedVehicles: 0,
    availableVehicles: 0,
    bookedVehicles: 0,
    pendingVehicleApprovals: 0,
    activeBookings: 0,
    pendingLicenceVerifications: 0,
    pendingInspections: 0
  };
  actionItems: any[] = [];

  // Lists
  cars: any[] = [];
  hosts: HostSummary[] = [];
  bookings: any[] = [];
  issues: any[] = [];
  inspections: any[] = [];
  supportAgents: any[] = [];

  // Filters
  carFilter = { search: '', approvalStatus: '', status: '', category: '' };
  hostFilter = { search: '', verificationStatus: '', accountStatus: '' };
  bookingFilter = { search: '', status: '', paymentStatus: '' };
  inspectionFilter = { status: '', type: '' };

  // Pagination
  carPage = 0; carPageSize = 10;
  hostPage = 0; hostPageSize = 10;
  bookingPage = 0; bookingPageSize = 10;
  inspectionPage = 0; inspectionPageSize = 10;

  // Detail Modals
  selectedCar: any = null;
  showCarDetailModal = false;
  selectedHost: HostDetail | null = null;
  showHostDetailModal = false;
  selectedBooking: any = null;
  showBookingDetailModal = false;
  selectedInspection: any = null;
  showInspectionDetailModal = false;

  // Licence Verification Modal
  showLicensePopup = false;
  licenseRemarks = '';

  // Reason / Action Modal (for Reject / Resubmission / Suspend / Disable)
  showReasonModal = false;
  reasonTitle = '';
  reasonTargetType: 'car' | 'host' = 'car';
  reasonAction: 'reject' | 'resubmission' | 'suspend' | 'disable' = 'reject';
  reasonTargetId = 0;
  actionReason = '';

  // Escalate / Resolve Issue Modal
  showIssueModal = false;
  selectedIssue: any = null;
  issueMode: 'escalate' | 'resolve' = 'escalate';
  selectedSupportAgentId: number | null = null;
  issueRemarks = '';

  // Escalate Inspection Modal
  showEscalateInspectionModal = false;
  escalateInspectionId = 0;
  inspectionEscalationRemarks = '';
  suspendVehicleOnEscalate = false;

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private toastService: ToastService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadOverview();
    this.loadCars();
    this.loadHosts();
    this.loadBookings();
    this.loadIssues();
    this.loadInspections();
    this.loadSupportAgents();
  }

  onCityFilterChange(): void {
    this.carPage = 0;
    this.hostPage = 0;
    this.bookingPage = 0;
    this.inspectionPage = 0;
    this.loadOverview();
    this.loadCars();
    this.loadHosts();
    this.loadBookings();
    this.loadIssues();
    this.loadInspections();
  }

  loadOverview(): void {
    this.loadingDashboard = true;
    this.adminService.getDashboardStats(this.selectedCity || undefined).subscribe({
      next: (res) => {
        this.adminName = res.adminName || 'Administrator';
        this.adminEmail = res.adminEmail || '';
        this.adminRole = res.adminRole || 'ADMIN';
        this.assignedCities = res.assignedCities || [];
        this.stats = {
          totalHosts: res.totalHosts || 0,
          verifiedHosts: res.verifiedHosts || 0,
          pendingHostApprovals: res.pendingHostApprovals || 0,
          totalListedVehicles: res.totalListedVehicles || 0,
          availableVehicles: res.availableVehicles || 0,
          bookedVehicles: res.bookedVehicles || 0,
          pendingVehicleApprovals: res.pendingVehicleApprovals || 0,
          activeBookings: res.activeBookings || 0,
          pendingLicenceVerifications: res.pendingLicenceVerifications || 0,
          pendingInspections: res.pendingInspections || 0
        };
        this.actionItems = res.actionItems || [];
        this.loadingDashboard = false;
      },
      error: (err) => {
        this.toastService.showError(err.error?.message || 'Failed to load dashboard overview');
        this.loadingDashboard = false;
      }
    });
  }

  loadCars(): void {
    this.adminService.getCars({
      city: this.selectedCity || undefined,
      approvalStatus: this.carFilter.approvalStatus || undefined,
      status: this.carFilter.status || undefined,
      category: this.carFilter.category || undefined,
      search: this.carFilter.search.trim() || undefined
    }).subscribe({
      next: (res) => { this.cars = res; this.carPage = 0; },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load vehicle listings')
    });
  }

  loadHosts(): void {
    this.adminService.getHosts({
      city: this.selectedCity || undefined,
      verificationStatus: this.hostFilter.verificationStatus || undefined,
      accountStatus: this.hostFilter.accountStatus || undefined,
      search: this.hostFilter.search.trim() || undefined
    }).subscribe({
      next: (res) => { this.hosts = res; this.hostPage = 0; },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load hosts')
    });
  }

  loadBookings(): void {
    this.adminService.getBookings({
      city: this.selectedCity || undefined,
      status: this.bookingFilter.status || undefined,
      paymentStatus: this.bookingFilter.paymentStatus || undefined,
      search: this.bookingFilter.search.trim() || undefined
    }).subscribe({
      next: (res) => { this.bookings = res; this.bookingPage = 0; },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load bookings')
    });
  }

  loadIssues(): void {
    this.adminService.getIssues(this.selectedCity || undefined).subscribe({
      next: (res) => this.issues = res,
      error: () => {}
    });
  }

  loadSupportAgents(): void {
    this.adminService.getSupportAgents().subscribe({
      next: (res) => this.supportAgents = res,
      error: () => {}
    });
  }

  loadInspections(): void {
    this.adminService.getInspections({
      city: this.selectedCity || undefined,
      status: this.inspectionFilter.status || undefined,
      type: this.inspectionFilter.type || undefined
    }).subscribe({
      next: (res) => { this.inspections = res; this.inspectionPage = 0; },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load inspections')
    });
  }

  switchTab(tab: typeof this.activeTab): void {
    this.activeTab = tab;
    if (tab === 'dashboard') this.loadOverview();
    if (tab === 'cars') this.loadCars();
    if (tab === 'hosts') this.loadHosts();
    if (tab === 'bookings') { this.loadBookings(); this.loadIssues(); }
    if (tab === 'inspections') this.loadInspections();
  }

  switchBookingSubTab(subTab: typeof this.bookingSubTab): void {
    this.bookingSubTab = subTab;
    this.bookingPage = 0;
  }

  navigateActionItem(item: any): void {
    if (item.targetTab) {
      this.switchTab(item.targetTab);
      if (item.targetTab === 'bookings' && item.targetSubTab) {
        this.switchBookingSubTab(item.targetSubTab);
      }
    }
  }

  getPages(totalPages: number): number[] {
    return Array.from({ length: totalPages }, (_, i) => i);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  // ================= VEHICLE LISTING ACTIONS =================

  get carTotalPages(): number { return Math.ceil(this.cars.length / this.carPageSize); }
  get pagedCars(): any[] {
    const start = this.carPage * this.carPageSize;
    return this.cars.slice(start, start + this.carPageSize);
  }

  openCarDetail(carId: number): void {
    this.adminService.getCarDetails(carId).subscribe({
      next: (res) => {
        this.selectedCar = res;
        this.showCarDetailModal = true;
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load vehicle details')
    });
  }

  approveCar(carId: number): void {
    this.adminService.approveCar(carId).subscribe({
      next: () => {
        this.toastService.showSuccess('Vehicle listing approved');
        this.loadCars();
        this.loadOverview();
        if (this.showCarDetailModal && this.selectedCar?.id === carId) this.openCarDetail(carId);
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to approve vehicle')
    });
  }

  reactivateCar(carId: number): void {
    this.adminService.reactivateCar(carId).subscribe({
      next: () => {
        this.toastService.showSuccess('Vehicle reactivated');
        this.loadCars();
        this.loadOverview();
        if (this.showCarDetailModal && this.selectedCar?.id === carId) this.openCarDetail(carId);
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to reactivate vehicle')
    });
  }

  // ================= HOST ACTIONS =================

  get hostTotalPages(): number { return Math.ceil(this.hosts.length / this.hostPageSize); }
  get pagedHosts(): HostSummary[] {
    const start = this.hostPage * this.hostPageSize;
    return this.hosts.slice(start, start + this.hostPageSize);
  }

  openHostDetail(hostId: number): void {
    this.adminService.getHostDetails(hostId).subscribe({
      next: (res) => {
        this.selectedHost = res;
        this.showHostDetailModal = true;
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load host details')
    });
  }

  approveHost(hostId: number): void {
    this.adminService.approveHost(hostId).subscribe({
      next: () => {
        this.toastService.showSuccess('Host approved successfully');
        this.loadHosts();
        this.loadOverview();
        if (this.showHostDetailModal && this.selectedHost?.id === hostId) this.openHostDetail(hostId);
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to approve host')
    });
  }

  toggleHostActive(host: HostSummary): void {
    const req = host.active ? this.adminService.deactivateHost(host.id) : this.adminService.reactivateHost(host.id);
    req.subscribe({
      next: () => {
        this.toastService.showSuccess(`Host ${host.active ? 'deactivated' : 'reactivated'}`);
        this.loadHosts();
        this.loadOverview();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to update host status')
    });
  }

  // ================= SHARED REASON MODAL =================

  openReasonModal(targetType: 'car' | 'host', id: number, action: 'reject' | 'resubmission' | 'suspend' | 'disable'): void {
    this.reasonTargetType = targetType;
    this.reasonTargetId = id;
    this.reasonAction = action;
    this.actionReason = '';
    const label = targetType === 'car' ? 'Vehicle' : 'Host';
    this.reasonTitle = `${action.charAt(0).toUpperCase() + action.slice(1)} ${label}`;
    this.showReasonModal = true;
  }

  submitReasonAction(): void {
    if (this.reasonAction !== 'disable' && !this.actionReason.trim()) {
      this.toastService.showError('Please provide a reason');
      return;
    }

    let req;
    if (this.reasonTargetType === 'car') {
      if (this.reasonAction === 'reject') req = this.adminService.rejectCar(this.reasonTargetId, this.actionReason.trim());
      else if (this.reasonAction === 'resubmission') req = this.adminService.requestCarResubmission(this.reasonTargetId, this.actionReason.trim());
      else if (this.reasonAction === 'suspend') req = this.adminService.suspendCar(this.reasonTargetId, this.actionReason.trim());
      else req = this.adminService.disableCar(this.reasonTargetId, this.actionReason.trim());
    } else {
      if (this.reasonAction === 'reject') req = this.adminService.rejectHost(this.reasonTargetId, this.actionReason.trim());
      else if (this.reasonAction === 'resubmission') req = this.adminService.requestHostResubmission(this.reasonTargetId, this.actionReason.trim());
      else req = this.adminService.suspendHost(this.reasonTargetId, this.actionReason.trim());
    }

    req.subscribe({
      next: () => {
        this.toastService.showSuccess('Action completed successfully');
        this.showReasonModal = false;
        if (this.reasonTargetType === 'car') {
          this.loadCars();
          if (this.showCarDetailModal && this.selectedCar?.id === this.reasonTargetId) this.openCarDetail(this.reasonTargetId);
        } else {
          this.loadHosts();
          if (this.showHostDetailModal && this.selectedHost?.id === this.reasonTargetId) this.openHostDetail(this.reasonTargetId);
        }
        this.loadOverview();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Action failed')
    });
  }

  // ================= BOOKING OPERATIONS =================

  get filteredBookings(): any[] {
    if (this.bookingSubTab === 'verification') {
      return this.bookings.filter(b => b.bookingStatus === 'VERIFICATION_PENDING');
    }
    return this.bookings;
  }

  get bookingTotalPages(): number { return Math.ceil(this.filteredBookings.length / this.bookingPageSize); }
  get pagedBookings(): any[] {
    const start = this.bookingPage * this.bookingPageSize;
    return this.filteredBookings.slice(start, start + this.bookingPageSize);
  }

  openBookingDetail(id: number): void {
    this.adminService.getBookingDetails(id).subscribe({
      next: (res) => {
        this.selectedBooking = res;
        this.showBookingDetailModal = true;
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load booking details')
    });
  }

  openLicensePopup(booking: any): void {
    this.selectedBooking = booking;
    this.licenseRemarks = '';
    this.showLicensePopup = true;
  }

  processLicenseVerification(approved: boolean, resubmissionRequested = false): void {
    if (!this.selectedBooking) return;
    if (!approved && !this.licenseRemarks.trim()) {
      this.toastService.showError('Please add remarks when rejecting or requesting resubmission');
      return;
    }
    this.adminService.verifyLicense(this.selectedBooking.id, approved, resubmissionRequested, this.licenseRemarks.trim()).subscribe({
      next: () => {
        this.toastService.showSuccess('Driving licence verification updated');
        this.showLicensePopup = false;
        this.loadBookings();
        this.loadOverview();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Verification failed')
    });
  }

  // ================= BOOKING ISSUES & ESCALATIONS =================

  openIssueModal(issue: any, mode: 'escalate' | 'resolve'): void {
    this.selectedIssue = issue;
    this.issueMode = mode;
    this.selectedSupportAgentId = issue.assignedSupportAgentId || null;
    this.issueRemarks = '';
    this.showIssueModal = true;
  }

  submitIssueAction(): void {
    if (!this.selectedIssue) return;
    if (!this.issueRemarks.trim()) {
      this.toastService.showError('Please enter remarks or resolution details');
      return;
    }

    const req = this.issueMode === 'escalate'
      ? this.adminService.escalateIssue(this.selectedIssue.id, this.selectedSupportAgentId, this.issueRemarks.trim())
      : this.adminService.resolveIssue(this.selectedIssue.id, this.issueRemarks.trim(), 'RESOLVED');

    req.subscribe({
      next: () => {
        this.toastService.showSuccess(this.issueMode === 'escalate' ? 'Issue escalated to Support Agent' : 'Issue resolved');
        this.showIssueModal = false;
        this.loadIssues();
        this.loadOverview();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to update issue')
    });
  }

  // ================= INSPECTION MANAGEMENT =================

  get inspectionTotalPages(): number { return Math.ceil(this.inspections.length / this.inspectionPageSize); }
  get pagedInspections(): any[] {
    const start = this.inspectionPage * this.inspectionPageSize;
    return this.inspections.slice(start, start + this.inspectionPageSize);
  }

  openInspectionDetail(id: number): void {
    this.adminService.getInspectionDetails(id).subscribe({
      next: (res) => {
        this.selectedInspection = res;
        this.showInspectionDetailModal = true;
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to load inspection details')
    });
  }

  openEscalateInspection(insp: any): void {
    this.escalateInspectionId = insp.id;
    this.inspectionEscalationRemarks = '';
    this.suspendVehicleOnEscalate = false;
    this.showEscalateInspectionModal = true;
  }

  submitInspectionEscalation(): void {
    if (!this.inspectionEscalationRemarks.trim()) {
      this.toastService.showError('Please provide escalation remarks');
      return;
    }
    this.adminService.escalateInspection(
      this.escalateInspectionId,
      this.inspectionEscalationRemarks.trim(),
      this.suspendVehicleOnEscalate
    ).subscribe({
      next: () => {
        this.toastService.showSuccess('Inspection escalated');
        this.showEscalateInspectionModal = false;
        this.loadInspections();
        this.loadCars();
        this.loadOverview();
      },
      error: (err) => this.toastService.showError(err.error?.message || 'Failed to escalate inspection')
    });
  }
}
