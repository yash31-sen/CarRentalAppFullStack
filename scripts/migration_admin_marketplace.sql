-- ============================================================================
-- Migration Script: Admin Portal City-Level Marketplace Operations
-- Database: PostgreSQL (carrentaldb)
-- Note: Safe & idempotent migration preserving existing Driver/Booking/Car data.
-- ============================================================================

-- 1. Car marketplace listing details
ALTER TABLE car ADD COLUMN IF NOT EXISTS manufacturing_year INTEGER;
ALTER TABLE car ADD COLUMN IF NOT EXISTS seating_capacity INTEGER;
ALTER TABLE car ADD COLUMN IF NOT EXISTS description VARCHAR(1500);
ALTER TABLE car ADD COLUMN IF NOT EXISTS rc_document_url VARCHAR(500);
ALTER TABLE car ADD COLUMN IF NOT EXISTS insurance_document_url VARCHAR(500);
ALTER TABLE car ADD COLUMN IF NOT EXISTS puc_document_url VARCHAR(500);
ALTER TABLE car ADD COLUMN IF NOT EXISTS other_document_url VARCHAR(500);
ALTER TABLE car ADD COLUMN IF NOT EXISTS photo_urls VARCHAR(2000);
ALTER TABLE car ADD COLUMN IF NOT EXISTS admin_remarks VARCHAR(1000);
ALTER TABLE car ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW();
UPDATE car SET submitted_at = NOW() WHERE submitted_at IS NULL;

-- 2. Booking licence verification enhancements
ALTER TABLE booking ADD COLUMN IF NOT EXISTS licence_document_url VARCHAR(500);
ALTER TABLE booking ADD COLUMN IF NOT EXISTS licence_remarks VARCHAR(1000);

-- 3. IncidentReport / Booking Issue enhancements
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS issue_type VARCHAR(100);
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS assigned_support_agent_id BIGINT;
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS internal_remarks VARCHAR(1500);
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS resolution_details VARCHAR(1500);
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS escalated_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE incident_report ADD COLUMN IF NOT EXISTS resolved_at TIMESTAMP WITH TIME ZONE;

-- 4. Update PostgreSQL enum CHECK constraints for new marketplace roles and statuses
ALTER TABLE app_user DROP CONSTRAINT IF EXISTS app_user_role_check;
ALTER TABLE app_user ADD CONSTRAINT app_user_role_check CHECK (role IN ('USER','HOST','ADMIN','SUPER_ADMIN','DRIVER','GUEST','FLEET_MANAGER','SUPPORT_AGENT'));

ALTER TABLE car DROP CONSTRAINT IF EXISTS car_status_check;
ALTER TABLE car ADD CONSTRAINT car_status_check CHECK (status IN ('AVAILABLE','BOOKED','NEEDS_REPOSITIONING','MAINTENANCE','PENDING_INSPECTION','SUSPENDED','DISABLED'));

ALTER TABLE car DROP CONSTRAINT IF EXISTS car_approval_status_check;
ALTER TABLE car ADD CONSTRAINT car_approval_status_check CHECK (approval_status IN ('PENDING','APPROVED','REJECTED','RESUBMISSION_REQUESTED'));

ALTER TABLE host_profile DROP CONSTRAINT IF EXISTS host_profile_verification_status_check;
ALTER TABLE host_profile ADD CONSTRAINT host_profile_verification_status_check CHECK (verification_status IN ('PENDING','VERIFIED','REJECTED','RESUBMISSION_REQUIRED','RESUBMISSION_REQUESTED'));
