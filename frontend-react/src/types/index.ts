export interface AppUser {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPER_ADMIN' | 'DRIVER';
  active: boolean;
  emailVerified: boolean;
  city?: string;
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
  admin: AppUser | null;
  active: boolean;
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

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error';
}
