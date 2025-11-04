package com.learning.server.controller;

import com.learning.server.common.ApiResponse;
import com.learning.server.dto.auth.UserResponseDTO;
import com.learning.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho User operations
 * Tuân thủ quy tắc RestController trong coding instructions
 */
@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Lấy danh sách tất cả users (trừ current user)
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> getAllUsers(Authentication authentication) {
        try {
            log.info("Lấy danh sách tất cả users");

            List<UserResponseDTO> users = userService.getAllUsers();

            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Lấy danh sách users thành công", users));

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", "Lỗi khi lấy danh sách users: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy thông tin user theo ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long userId) {
        try {
            log.info("Lấy thông tin user: {}", userId);

            UserResponseDTO user = userService.getUserById(userId);

            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Lấy thông tin user thành công", user));

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin user: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", "Lỗi khi lấy thông tin user: " + e.getMessage(), null));
        }
    }

    /**
     * Tìm kiếm users theo tên hiển thị
     * GET /api/users/search?q={query}
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> searchUsers(@RequestParam String q) {
        try {
            log.info("Tìm kiếm users với query: {}", q);

            List<UserResponseDTO> users = userService.searchUsersByDisplayName(q);

            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Tìm kiếm users thành công", users));

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm users: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", "Lỗi khi tìm kiếm users: " + e.getMessage(), null));
        }
    }
}
