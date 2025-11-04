package com.learning.server.dto.websocket;

import jakarta.validation.constraints.NotNull;
/**
 * DTO để gửi thông báo typing qua WebSocket
 * Tuân thủ quy tắc DTO trong coding instructions
 */
public record TypingNotificationDTO(
        @NotNull(message = "ID người nhận không được để trống")
        Long receiverId,

        @NotNull(message = "Trạng thái typing không được để trống")
        Boolean isTyping
) {
    /**
     * Compact constructor để validate input
     */
    public TypingNotificationDTO {
        if (receiverId == null) {
            throw new IllegalArgumentException("ID người nhận không được để trống");
        }
        if (isTyping == null) {
            throw new IllegalArgumentException("Trạng thái typing không được để trống");
        }
    }
}