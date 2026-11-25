export interface AppUser {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'HOST' | 'ADMIN' | 'SUPER_ADMIN' | 'DRIVER' | 'GUEST' | 'FLEET_MANAGER' | 'SUPPORT_AGENT';
  active: boolean;
  suspended?: boolean;
  suspensionReason?: string;
  emailVerified: boolean;
  city?: string;
  assignedCities?: string[];
  operationalScope?: string;
  createdAt?: string;
}

export interface DashboardStats {
  totalAdmins: number;
  totalUsers: number;
  totalHosts: number;
  totalCities: number;
  totalListedCars: number;
  totalBookings: number;
  totalRevenue: number;
  platformEarnings: number;
  pendingHostVerifications: number;
  pendingVehicleApprovals: number;
  activeBookings: number;
  availableCars: number;
  unavailableCars: number;
  pendingHostPayouts: number;
  pendingHostPayoutAmount: number;
}

export interface HostSummary {
  id: number;
  userId: number;
  name: string;
  email: string;
  phone?: string;
  city?: string;
  businessName?: string;
  verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED' | 'RESUBMISSION_REQUIRED';
  rejectionReason?: string;
  active: boolean;
  suspended: boolean;
  emailVerified: boolean;
  vehicleCount: number;
  createdAt?: string;
}

export interface HostDetail extends HostSummary {
  address?: string;
  governmentIdNumber?: string;
  documentType?: string;
  documentUrl?: string;
  verifiedBy?: string;
  verifiedAt?: string;
  suspensionReason?: string;
  totalBookings: number;
  completedBookings: number;
  totalEarnings: number;
  vehicles: any[];
  bookingHistory: any[];
  payoutHistory: any[];
  reviews: any[];
  reportedIncidents: any[];
}

export interface UserDetail {
  id: number;
  name: string;
  email: string;
  active: boolean;
  suspended: boolean;
  suspensionReason?: string;
  emailVerified: boolean;
  createdAt?: string;
  bookingHistory: any[];
  paymentHistory: any[];
  reviews: any[];
  reportedIncidents: any[];
}

export interface DriverProfile {
  id: number;
  user: AppUser;
  licenseNumber: string;
  experienceYears: number;
  currentLocation: string;
  status: 'AVAILABLE' | 'BUSY' | 'OFFLINE';
  active: boolean;
}

export interface ServiceCity {
  id: number;
  cityName: string;
  parkingAddress: string;
  parkingContact: string;
  oneWayFee: number;
  localServiceCharge?: number;
  admin: AppUser | null;
  active: boolean;
  bookingAvailable?: boolean;
  hostCount?: number;
  vehicleCount?: number;
  activeBookingCount?: number;
}

export interface CarPricing {
  id: number;
  carClass: 'ECONOMY' | 'HATCHBACK' | 'SEDAN' | 'SUV' | 'LUXURY';
  baseFare: number;
  perDayRate: number;
  minPerDayRate: number;
  maxPerDayRate: number;
  freeKmPerDay: number;
  extraKmRate: number;
  platformCommissionPercent?: number;
  minPlatformFee?: number;
  maxPlatformFee?: number;
  minBookingDurationDays?: number;
  maxBookingDurationDays?: number;
  active?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface PlatformSettings {
  defaultPlatformCommissionPercent: number;
  defaultPlatformServiceFee: number;
  minBookingDurationDays: number;
  maxBookingDurationDays: number;
  cancellationPolicy: string;
  refundPolicy: string;
  hostVerificationRequired: boolean;
  guestVerificationRequired: boolean;
  vehicleListingApprovalRequired: boolean;
  oneWayBookingEnabled: boolean;
  updatedAt?: string;
  updatedBy?: string;
}

export interface AuditLog {
  id: number;
  actorEmail: string;
  actorRole: string;
  action: string;
  entityType: string;
  entityId?: string;
  details?: string;
  createdAt: string;
}

export interface Car {
  id: number;
  carNumber: string;
  brand: string;
  model: string;
  carClass: 'ECONOMY' | 'HATCHBACK' | 'SEDAN' | 'SUV' | 'LUXURY';
  fuelType: 'PETROL' | 'DIESEL' | 'CNG' | 'ELECTRIC';
  transmission: 'MANUAL' | 'AUTOMATIC';
  pricePerDay: number;
  homeCity: string;
  currentCity: string;
  currentLocation: string;
  status: 'AVAILABLE' | 'BOOKED' | 'PENDING_INSPECTION' | 'MAINTENANCE' | 'DISABLED';
  active: boolean;
}

export interface Booking {
  id: number;
  user: AppUser;
  car: Car;
  driverProfile?: DriverProfile;
  bookingType: 'SELF_DRIVE' | 'WITH_DRIVER';
  tripType: 'ROUND_TRIP' | 'ONE_WAY';
  status:
    | 'PENDING_PAYMENT'
    | 'PAYMENT_SUCCESS'
    | 'VERIFICATION_PENDING'
    | 'DRIVER_ASSIGNMENT_PENDING'
    | 'CONFIRMED'
    | 'IN_PROGRESS'
    | 'INSPECTION_PENDING'
    | 'COMPLETED'
    | 'REJECTED'
    | 'CANCELLED';
  startDate: string;
  endDate: string;
  pickupCity: string;
  dropCity: string;
  pickupLocation: string;
  dropLocation: string;
  basePrice: number;
  oneWayFee: number;
  totalPrice: number;
  paymentRef?: string;
  drivingLicenseNumber?: string;
  licenceSubmittedAt?: string;
  licenceVerifiedAt?: string;
  licenceVerifiedBy?: string;
  startOdometer?: number;
  endOdometer?: number;
  totalKmDriven?: number;
  startOdometerPhoto?: string;
  endOdometerPhoto?: string;
  baseFare: number;
  estimatedPrice: number;
  extraKmCharge: number;
  finalPrice: number;
  createdAt: string;
  updatedAt: string;
}
