

package com.learning.server.dto.websocket;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO để gửi tin nhắn qua WebSocket
 * Tuân thủ quy tắc DTO trong coding instructions
 */
public record ChatMessageDTO(
        @NotNull(message = "ID người nhận không được để trống")
        Long receiverId,

        @NotEmpty(message = "Nội dung tin nhắn không được để trống")
        String content
) {
    /**
     * Compact constructor để validate input
     */
    public ChatMessageDTO {
        if (receiverId == null) {
            throw new IllegalArgumentException("ID người nhận không được để trống");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được để trống");
        }
    }
}
