package com.learning.server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO cho login request
 * Tuân thủ quy tắc DTOs là record type trong coding instructions
 */
public record LoginRequestDTO(
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    String email,

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    String password
) {
    /**
     * Compact canonical constructor để validate input
     */
    public LoginRequestDTO {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được null hoặc trống");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được null hoặc trống");
        }
    }
}
