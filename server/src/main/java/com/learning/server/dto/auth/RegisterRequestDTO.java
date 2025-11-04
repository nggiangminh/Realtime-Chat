package com.learning.server.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO cho register request
 * Tuân thủ quy tắc DTOs là record type trong coding instructions
 */
public record RegisterRequestDTO(
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    String email,

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    String password,

    @NotBlank(message = "Tên hiển thị không được để trống")
    @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
    String displayName
) {
    /**
     * Compact canonical constructor để validate input
     */
    public RegisterRequestDTO {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email không được null hoặc trống");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu không được null hoặc trống");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Tên hiển thị không được null hoặc trống");
        }
    }
}
