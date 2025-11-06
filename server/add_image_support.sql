-- Migration script to add image support to messages table
-- Run this script in PostgreSQL to add message_type and image_url columns

-- Add message_type column with default value TEXT
ALTER TABLE messages 
ADD COLUMN IF NOT EXISTS message_type VARCHAR(10) DEFAULT 'TEXT' NOT NULL;

-- Add image_url column (nullable)
ALTER TABLE messages 
ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Update existing records to have TEXT as message_type
UPDATE messages 
SET message_type = 'TEXT' 
WHERE message_type IS NULL;

-- Verify the changes
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'messages'
ORDER BY ordinal_position;
