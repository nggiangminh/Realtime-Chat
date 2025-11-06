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
  messageType?: 'TEXT' | 'IMAGE';
  imageUrl?: string;
  reactions?: { [emoji: string]: number }; // emoji -> count
}

/**
 * Chat Message DTO - Dùng để gửi qua WebSocket
 */
export interface ChatMessageDTO {
  receiverId: number;
  content: string;
  messageType?: string;
  imageUrl?: string;
}

/**
 * Reaction DTO - Dùng để toggle reaction
 */
export interface ReactionDTO {
  messageId: number;
  emoji: string;
}

/**
 * Reaction Response - Nhận từ server
 */
export interface ReactionResponse {
  messageId: number;
  userId: number;
  userDisplayName: string;
  emoji: string;
  action: 'ADD' | 'REMOVE';
  reactionCounts: { [emoji: string]: number };
}

/**
 * Typing Notification - Thông báo đang gõ
 */
export interface TypingNotification {
  userId: number;
  receiverId: number;
  isTyping: boolean;
}
