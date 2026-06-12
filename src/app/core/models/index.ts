export interface AppUser {
  id: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN' | 'SUPER_ADMIN' | 'DRIVER';
  active: boolean;
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
