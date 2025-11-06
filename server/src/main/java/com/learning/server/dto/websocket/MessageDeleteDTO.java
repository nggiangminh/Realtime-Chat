package com.learning.server.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho thông báo xóa tin nhắn qua WebSocket
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDeleteDTO {
    private Long messageId;
    private Long deletedBy;
    private String action = "DELETE";
}
