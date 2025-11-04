/**
 * Message Model - Đại diện cho tin nhắn
 */
export interface Message {
  id: number;
  senderId: number;
  receiverId: number;
  content: string;
  sentAt: string;
  isRead: boolean;
}

/**
 * Chat Message DTO - Dùng để gửi qua WebSocket
 */
export interface ChatMessageDTO {
  receiverId: number;
  content: string;
}

/**
 * Typing Notification - Thông báo đang gõ
 */
export interface TypingNotification {
  userId: number;
  receiverId: number;
  isTyping: boolean;
}
