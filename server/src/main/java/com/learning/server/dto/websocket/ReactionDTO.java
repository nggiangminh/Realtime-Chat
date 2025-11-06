package com.learning.server.dto.websocket;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO để gửi/xóa reaction qua WebSocket
 */
public record ReactionDTO(
        @NotNull(message = "Message ID không được để trống")
        Long messageId,

        @NotEmpty(message = "Emoji không được để trống")
        String emoji
) {
    public ReactionDTO {
        if (messageId == null) {
            throw new IllegalArgumentException("Message ID không được để trống");
        }
        if (emoji == null || emoji.trim().isEmpty()) {
            throw new IllegalArgumentException("Emoji không được để trống");
        }
    }
}
