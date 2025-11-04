package com.learning.server.controller;

import com.learning.server.common.ApiResponse;
import com.learning.server.dto.auth.LoginRequestDTO;
import com.learning.server.dto.auth.LoginResponseDTO;
import com.learning.server.dto.auth.RegisterRequestDTO;
import com.learning.server.dto.auth.UserResponseDTO;
import com.learning.server.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * RestController xử lý các API liên quan đến authentication
 * Tuân thủ quy tắc RestController trong coding instructions
 */
@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * API đăng ký user mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            log.info("Request đăng ký mới từ email: {}", registerRequest.email());

            UserResponseDTO userResponse = authService.register(registerRequest);

            return new ResponseEntity<>(
                ApiResponse.success("Đăng ký thành công", userResponse),
                HttpStatus.CREATED
            );
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi đăng ký: {}", e.getMessage());
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.BAD_REQUEST
            );
        } catch (Exception e) {
            log.error("Lỗi server khi đăng ký: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                ApiResponse.error("Lỗi server nội bộ"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * API đăng nhập
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            log.info("Request đăng nhập từ email: {}", loginRequest.email());

            LoginResponseDTO loginResponse = authService.login(loginRequest);

            return new ResponseEntity<>(
                ApiResponse.success("Đăng nhập thành công", loginResponse),
                HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi đăng nhập: {}", e.getMessage());
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.UNAUTHORIZED
            );
        } catch (Exception e) {
            log.error("Lỗi server khi đăng nhập: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                ApiResponse.error("Lỗi server nội bộ"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * API lấy thông tin user hiện tại (yêu cầu authentication)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser() {
        try {
            // Lấy user ID từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>(
                    ApiResponse.error("Chưa đăng nhập"),
                    HttpStatus.UNAUTHORIZED
                );
            }

            Long userId = (Long) authentication.getPrincipal();
            log.debug("Request lấy thông tin user hiện tại, ID: {}", userId);

            UserResponseDTO userResponse = authService.getCurrentUser(userId);

            return new ResponseEntity<>(
                ApiResponse.success("Lấy thông tin thành công", userResponse),
                HttpStatus.OK
            );
        } catch (IllegalArgumentException e) {
            log.warn("Lỗi lấy thông tin user: {}", e.getMessage());
            return new ResponseEntity<>(
                ApiResponse.error(e.getMessage()),
                HttpStatus.NOT_FOUND
            );
        } catch (Exception e) {
            log.error("Lỗi server khi lấy thông tin user: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                ApiResponse.error("Lỗi server nội bộ"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * API logout (chỉ cập nhật last seen)
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        try {
            // Lấy user ID từ SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return new ResponseEntity<>(
                    ApiResponse.error("Chưa đăng nhập"),
                    HttpStatus.UNAUTHORIZED
                );
            }

            Long userId = (Long) authentication.getPrincipal();
            log.info("User logout, ID: {}", userId);

            // Cập nhật last seen
            authService.updateLastSeen(userId);

            return new ResponseEntity<>(
                ApiResponse.success("Đăng xuất thành công"),
                HttpStatus.OK
            );
        } catch (Exception e) {
            log.error("Lỗi server khi logout: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                ApiResponse.error("Lỗi server nội bộ"),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
