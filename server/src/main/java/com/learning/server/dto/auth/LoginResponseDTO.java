package com.learning.server.dto.auth;

/**
 * DTO cho login response - trả về token và thông tin user
 * Tuân thủ quy tắc DTOs là record type trong coding instructions
 */
public record LoginResponseDTO(
    String token,
    UserResponseDTO user
) {
    /**
     * Compact canonical constructor để validate input
     */
    public LoginResponseDTO {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Token không được null hoặc trống");
        }
        if (user == null) {
            throw new IllegalArgumentException("User không được null");
        }
    }
}
