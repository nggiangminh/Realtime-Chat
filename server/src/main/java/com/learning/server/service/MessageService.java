package com.learning.server.service;

import com.learning.server.dto.websocket.MessageResponseDTO;
import com.learning.server.entity.Message;

import java.util.List;

/**
 * Service interface cho Message
 * Tuân thủ quy tắc Service trong coding instructions
 */
public interface MessageService {

    /**
     * Lưu tin nhắn mới vào database
     */
    MessageResponseDTO saveMessage(Long senderId, Long receiverId, String content);

    /**
     * Lưu tin nhắn mới với messageType và imageUrl
     */
    MessageResponseDTO saveMessage(Long senderId, Long receiverId, String content, 
                                  Message.MessageType messageType, String imageUrl);

    /**
     * Lấy lịch sử chat giữa current user và user khác
     */
    List<MessageResponseDTO> getChatHistory(Long currentUserId, Long otherUserId);

    /**
     * Lấy danh sách tin nhắn chưa đọc của user
     */
    List<MessageResponseDTO> getUnreadMessages(Long userId);

    /**
     * Đánh dấu tin nhắn là đã đọc (chỉ receiver mới có thể đánh dấu)
     */
    void markMessageAsRead(Long messageId, Long currentUserId);

    /**
     * Đánh dấu tất cả tin nhắn từ sender tới receiver là đã đọc
     */
    void markAllMessagesAsRead(Long senderId, Long receiverId);

    /**
     * Đếm số tin nhắn chưa đọc từ user cụ thể
     */
    Long countUnreadMessages(Long senderId, Long receiverId);

    /**
     * Lấy tin nhắn mới nhất giữa 2 users
     */
    MessageResponseDTO getLatestMessage(Long userId1, Long userId2);

    /**
     * Xóa tin nhắn (soft delete) - cả người gửi và người nhận đều không thấy
     */
    void deleteMessage(Long messageId, Long currentUserId);
}
