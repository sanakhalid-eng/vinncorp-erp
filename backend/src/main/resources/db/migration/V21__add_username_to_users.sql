-- Add username column to users table
ALTER TABLE users ADD COLUMN username VARCHAR(30) UNIQUE;

-- Populate username for existing users from email prefix
UPDATE users SET username = LOWER(SUBSTRING_INDEX(email, '@', 1)) WHERE username IS NULL;

-- Add temp_username column to email_verification_tokens
ALTER TABLE email_verification_tokens ADD COLUMN temp_username VARCHAR(30);
