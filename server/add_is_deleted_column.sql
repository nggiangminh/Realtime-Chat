-- Migration: Thêm cột is_deleted vào bảng messages
-- Mục đích: Hỗ trợ soft delete tin nhắn

ALTER TABLE messages 
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Tạo index để tối ưu query
CREATE INDEX idx_messages_is_deleted ON messages(is_deleted);

-- Comments
COMMENT ON COLUMN messages.is_deleted IS 'Đánh dấu tin nhắn đã bị xóa (soft delete)';
