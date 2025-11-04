package com.learning.server.dto.auth;

import java.time.LocalDateTime;

/**
 * DTO cho user response - thông tin user trả về client (không chứa password)
 * Tuân thủ quy tắc DTOs là record type trong coding instructions
 */
public record UserResponseDTO(
    Long id,
    String email,
    String displayName,
    LocalDateTime createdAt,
    LocalDateTime lastSeen
) {
    /**
     * Compact canonical constructor để validate input
     */
    public UserResponseDTO {
        if (id == null) {
            throw new IllegalArgumentException("ID không được null");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được null hoặc trống");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Tên hiển thị không được null hoặc trống");
        }
    }
}
