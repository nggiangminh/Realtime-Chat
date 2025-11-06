package com.learning.server.dto.websocket;

import com.learning.server.entity.Message;
import jakarta.validation.constraints.NotNull;

/**
 * DTO để gửi tin nhắn qua WebSocket
 * Tuân thủ quy tắc DTO trong coding instructions
 */
public record ChatMessageDTO(
        @NotNull(message = "ID người nhận không được để trống")
        Long receiverId,

        String content,

        String messageType,

        String imageUrl
) {
    /**
     * Compact constructor để validate input
     */
    public ChatMessageDTO {
        if (receiverId == null) {
            throw new IllegalArgumentException("ID người nhận không được để trống");
        }
        
        // Validate based on message type
        if ("IMAGE".equals(messageType)) {
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new IllegalArgumentException("URL ảnh không được để trống khi gửi ảnh");
            }
        } else {
            // Default to TEXT
            if (content == null || content.trim().isEmpty()) {
                throw new IllegalArgumentException("Nội dung tin nhắn không được để trống");
            }
        }
    }

    /**
     * Helper method to get MessageType enum
     */
    public Message.MessageType getMessageTypeEnum() {
        if ("IMAGE".equals(messageType)) {
            return Message.MessageType.IMAGE;
        }
        return Message.MessageType.TEXT;
    }
}
