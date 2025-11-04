package com.learning.server.service;

import com.learning.server.dto.auth.UserResponseDTO;

import java.util.List;

/**
 * Service interface cho User operations
 * Tuân thủ quy tắc Service trong coding instructions
 */
public interface UserService {

    /**
     * Lấy danh sách tất cả users
     */
    List<UserResponseDTO> getAllUsers();

    /**
     * Lấy thông tin user theo ID
     */
    UserResponseDTO getUserById(Long userId);

    /**
     * Tìm kiếm users theo tên hiển thị
     */
    List<UserResponseDTO> searchUsersByDisplayName(String query);

    /**
     * Lấy thông tin user theo email
     */
    UserResponseDTO getUserByEmail(String email);

    /**
     * Cập nhật last seen của user
     */
    void updateLastSeen(Long userId);
}
