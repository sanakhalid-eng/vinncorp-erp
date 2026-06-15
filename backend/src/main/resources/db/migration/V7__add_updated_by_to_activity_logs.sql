-- V7: Add missing updated_by column to activity_logs
-- BaseAuditableEntity maps @LastModifiedBy to updated_by
-- but the column was never added by any previous migration

ALTER TABLE activity_logs
ADD COLUMN updated_by BIGINT NULL;
