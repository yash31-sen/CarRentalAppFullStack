-- Host Registration, Verification & Multi-Role Auth Migration

ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS mobile_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS mobile_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS token_version INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS terms_accepted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS app_user_roles (
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

INSERT INTO app_user_roles (user_id, role)
SELECT id, role FROM app_user
WHERE role IS NOT NULL
ON CONFLICT DO NOTHING;

ALTER TABLE user_token
    ALTER COLUMN token TYPE VARCHAR(128);

ALTER TABLE user_token
    ADD COLUMN IF NOT EXISTS target_phone VARCHAR(20),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS revoked_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE user_token DROP CONSTRAINT IF EXISTS user_token_type_check;
ALTER TABLE user_token ADD CONSTRAINT user_token_type_check
    CHECK (type IN ('VERIFY_EMAIL', 'VERIFY_MOBILE', 'RESET_PASSWORD', 'ACCOUNT_SETUP', 'REFRESH_TOKEN'));

ALTER TABLE host_profile
    ADD COLUMN IF NOT EXISTS state VARCHAR(100),
    ADD COLUMN IF NOT EXISTS postal_code VARCHAR(20),
    ADD COLUMN IF NOT EXISTS pan_number VARCHAR(20),
    ADD COLUMN IF NOT EXISTS pan_verified BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS profile_photo_url VARCHAR(500),
    ADD COLUMN IF NOT EXISTS description VARCHAR(1500),
    ADD COLUMN IF NOT EXISTS terms_accepted BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS submitted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE host_profile DROP CONSTRAINT IF EXISTS host_profile_verification_status_check;
ALTER TABLE host_profile ADD CONSTRAINT host_profile_verification_status_check
    CHECK (verification_status IN (
        'DRAFT',
        'PENDING_EMAIL_VERIFICATION',
        'PENDING_DOCUMENTS',
        'PENDING_ADMIN_APPROVAL',
        'PENDING',
        'VERIFIED',
        'APPROVED',
        'REJECTED',
        'RESUBMISSION_REQUIRED',
        'RESUBMISSION_REQUESTED',
        'SUSPENDED'
    ));

CREATE TABLE IF NOT EXISTS host_status_history (
    id BIGSERIAL PRIMARY KEY,
    host_profile_id BIGINT NOT NULL REFERENCES host_profile(id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255),
    remarks VARCHAR(1000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
