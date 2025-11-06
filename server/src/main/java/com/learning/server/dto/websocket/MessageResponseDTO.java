package com.learning.server.dto.websocket;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO response cho tin nhắn qua WebSocket
 * Tuân thủ quy tắc DTO trong coding instructions
 */
public record MessageResponseDTO(
        Long id,
        Long senderId,
        Long receiverId,
        String content,
        LocalDateTime sentAt,
        Boolean isRead,
        String senderDisplayName,
        String messageType,
        String imageUrl,
        Map<String, Integer> reactions // emoji -> count
) {
    /**
     * Compact constructor để validate input
     */
    public MessageResponseDTO {
        if (senderId == null) {
            throw new IllegalArgumentException("ID người gửi không được để trống");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("ID người nhận không được để trống");
        }
        // Content can be empty for image messages
        if (sentAt == null) {
            throw new IllegalArgumentException("Thời gian gửi không được để trống");
        }
        if (isRead == null) {
            throw new IllegalArgumentException("Trạng thái đã đọc không được để trống");
        }
    }
}
