-- V3__ldap_user.sql
-- Add auth_provider column to track whether user authenticates via LOCAL db or LDAP
ALTER TABLE users ADD COLUMN auth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

-- Ensure all existing users are marked as LOCAL
UPDATE users SET auth_provider = 'LOCAL';

-- Add index for potential lookups by provider
CREATE INDEX idx_users_auth_provider ON users(auth_provider);
