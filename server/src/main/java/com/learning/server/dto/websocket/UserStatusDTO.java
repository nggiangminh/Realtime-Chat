package com.learning.server.dto.websocket;

/**
 * DTO để thông báo trạng thái online/offline của user
 * Tuân thủ quy tắc DTO trong coding instructions
 */
public record UserStatusDTO(
        Long userId,
        String displayName,
        UserStatus status
) {
    /**
     * Enum cho trạng thái user
     */
    public enum UserStatus {
        ONLINE,
        OFFLINE
    }

    /**
     * Compact constructor để validate input
     */
    public UserStatusDTO {
        if (userId == null) {
            throw new IllegalArgumentException("ID user không được để trống");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên hiển thị không được để trống");
        }
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái không được để trống");
        }
    }
}
