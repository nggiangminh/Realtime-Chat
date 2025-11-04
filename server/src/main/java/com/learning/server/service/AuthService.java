package com.learning.server.service;

import com.learning.server.dto.auth.LoginRequestDTO;
import com.learning.server.dto.auth.LoginResponseDTO;
import com.learning.server.dto.auth.RegisterRequestDTO;
import com.learning.server.dto.auth.UserResponseDTO;

/**
 * Service interface cho authentication
 * Tuân thủ quy tắc Service layer trong coding instructions
 */
public interface AuthService {

    /**
     * Đăng ký user mới
     * @param registerRequest thông tin đăng ký
     * @return UserResponseDTO thông tin user đã tạo
     */
    UserResponseDTO register(RegisterRequestDTO registerRequest);

    /**
     * Đăng nhập user
     * @param loginRequest thông tin đăng nhập
     * @return LoginResponseDTO chứa token và thông tin user
     */
    LoginResponseDTO login(LoginRequestDTO loginRequest);

    /**
     * Lấy thông tin user hiện tại từ token
     * @param userId ID của user
     * @return UserResponseDTO thông tin user
     */
    UserResponseDTO getCurrentUser(Long userId);

    /**
     * Cập nhật last seen cho user
     * @param userId ID của user
     */
    void updateLastSeen(Long userId);
}
