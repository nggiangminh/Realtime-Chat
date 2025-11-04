package com.learning.server.service.impl;

import com.learning.server.dto.auth.LoginRequestDTO;
import com.learning.server.dto.auth.LoginResponseDTO;
import com.learning.server.dto.auth.RegisterRequestDTO;
import com.learning.server.dto.auth.UserResponseDTO;
import com.learning.server.entity.User;
import com.learning.server.repository.UserRepository;
import com.learning.server.security.JwtTokenProvider;
import com.learning.server.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation của AuthService
 * Tuân thủ quy tắc Service layer trong coding instructions
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Override
    @Transactional
    public UserResponseDTO register(RegisterRequestDTO registerRequest) {
        log.info("Bắt đầu đăng ký user với email: {}", registerRequest.email());

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new IllegalArgumentException("Email đã được sử dụng: " + registerRequest.email());
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(registerRequest.password());

        // Tạo user mới
        User user = new User(
            registerRequest.email(),
            hashedPassword,
            registerRequest.displayName()
        );

        // Lưu vào database
        User savedUser = userRepository.save(user);

        log.info("Đăng ký thành công user với ID: {}", savedUser.getId());

        // Trả về DTO
        return convertToUserResponseDTO(savedUser);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        log.info("Bắt đầu đăng nhập user với email: {}", loginRequest.email());

        // Tìm user theo email
        User user = userRepository.findByEmail(loginRequest.email())
            .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không đúng"));

        // Kiểm tra password
        if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }

        // Cập nhật last seen
        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        // Tạo JWT token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        log.info("Đăng nhập thành công user với ID: {}", user.getId());

        // Trả về response
        return new LoginResponseDTO(token, convertToUserResponseDTO(user));
    }

    @Override
    public UserResponseDTO getCurrentUser(Long userId) {
        log.debug("Lấy thông tin user hiện tại với ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + userId));

        return convertToUserResponseDTO(user);
    }

    @Override
    @Transactional
    public void updateLastSeen(Long userId) {
        log.debug("Cập nhật last seen cho user ID: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với ID: " + userId));

        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Convert User entity sang UserResponseDTO
     */
    private UserResponseDTO convertToUserResponseDTO(User user) {
        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getDisplayName(),
            user.getCreatedAt(),
            user.getLastSeen()
        );
    }
}
