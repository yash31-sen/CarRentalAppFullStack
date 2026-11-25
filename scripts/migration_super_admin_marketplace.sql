-- ============================================================================
-- Migration Script: SUPER_ADMIN Marketplace Upgrade (Zoomcar-Style Model)
-- Database: PostgreSQL (carrentaldb)
-- Note: Safe & idempotent migration preserving existing DRIVER/booking data.
-- ============================================================================

-- 1. AppUser enhancements
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS operational_scope VARCHAR(255);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS suspended BOOLEAN DEFAULT FALSE;
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS suspension_reason VARCHAR(500);
ALTER TABLE app_user ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();
UPDATE app_user SET created_at = NOW() WHERE created_at IS NULL;
UPDATE app_user SET suspended = FALSE WHERE suspended IS NULL;

-- 2. ServiceCity enhancements
ALTER TABLE service_city ADD COLUMN IF NOT EXISTS booking_available BOOLEAN DEFAULT TRUE;
ALTER TABLE service_city ADD COLUMN IF NOT EXISTS local_service_charge NUMERIC(10, 2) DEFAULT 0.00;
UPDATE service_city SET booking_available = TRUE WHERE booking_available IS NULL;
UPDATE service_city SET local_service_charge = 0.00 WHERE local_service_charge IS NULL;

-- 3. CarPricing enhancements (allow multiple historical/inactive rules; 1 active per car_class)
ALTER TABLE car_pricing DROP CONSTRAINT IF EXISTS uk_car_pricing_car_class;
ALTER TABLE car_pricing DROP CONSTRAINT IF EXISTS car_pricing_car_class_key;
ALTER TABLE car_pricing ADD COLUMN IF NOT EXISTS platform_commission_percent NUMERIC(5, 2) DEFAULT 20.00;
ALTER TABLE car_pricing ADD COLUMN IF NOT EXISTS min_platform_fee NUMERIC(10, 2) DEFAULT 0.00;
ALTER TABLE car_pricing ADD COLUMN IF NOT EXISTS max_platform_fee NUMERIC(10, 2);
ALTER TABLE car_pricing ADD COLUMN IF NOT EXISTS min_booking_duration_days INTEGER DEFAULT 1;
ALTER TABLE car_pricing ADD COLUMN IF NOT EXISTS max_booking_duration_days INTEGER DEFAULT 30;
ALTER TABLE car_pricing ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
ALTER TABLE car_pricing ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();
ALTER TABLE car_pricing ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();
UPDATE car_pricing SET active = TRUE WHERE active IS NULL;
UPDATE car_pricing SET platform_commission_percent = 20.00 WHERE platform_commission_percent IS NULL;

-- 4. Booking commission snapshot columns
ALTER TABLE booking ADD COLUMN IF NOT EXISTS gross_amount NUMERIC(12, 2);
ALTER TABLE booking ADD COLUMN IF NOT EXISTS platform_commission_percent NUMERIC(5, 2);
ALTER TABLE booking ADD COLUMN IF NOT EXISTS platform_commission_amount NUMERIC(12, 2);
ALTER TABLE booking ADD COLUMN IF NOT EXISTS platform_fee_amount NUMERIC(12, 2);
ALTER TABLE booking ADD COLUMN IF NOT EXISTS host_earnings_amount NUMERIC(12, 2);
ALTER TABLE booking ADD COLUMN IF NOT EXISTS host_payout_status VARCHAR(50);

-- 5. Car marketplace columns (host_id nullable for backward compatibility)
ALTER TABLE car ADD COLUMN IF NOT EXISTS host_id BIGINT;
ALTER TABLE car ADD COLUMN IF NOT EXISTS approval_status VARCHAR(50) DEFAULT 'APPROVED';
ALTER TABLE car ADD COLUMN IF NOT EXISTS pricing_out_of_range BOOLEAN DEFAULT FALSE;
UPDATE car SET approval_status = 'APPROVED' WHERE approval_status IS NULL;
UPDATE car SET pricing_out_of_range = FALSE WHERE pricing_out_of_range IS NULL;

-- 6. Default Platform Settings (inserted automatically on first GET if empty, or seeded here)
INSERT INTO platform_settings (
    default_platform_commission_percent,
    default_platform_service_fee,
    min_booking_duration_days,
    max_booking_duration_days,
    cancellation_policy,
    refund_policy,
    host_verification_required,
    guest_verification_required,
    vehicle_listing_approval_required,
    one_way_booking_enabled,
    updated_at,
    updated_by
)
SELECT 20.00, 99.00, 1, 30,
       'Free cancellation up to 24 hours before pickup. 50% fee within 24 hours.',
       'Refunds are processed within 5-7 business days to the original payment method.',
       TRUE, TRUE, TRUE, TRUE, NOW(), 'SYSTEM_MIGRATION'
WHERE NOT EXISTS (SELECT 1 FROM platform_settings);
