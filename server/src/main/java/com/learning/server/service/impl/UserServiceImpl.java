package com.learning.server.service.impl;

import com.learning.server.dto.auth.UserResponseDTO;
import com.learning.server.entity.User;
import com.learning.server.repository.UserRepository;
import com.learning.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của UserService
 * Tuân thủ quy tắc ServiceImpl trong coding instructions
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        return convertToResponseDTO(user);
    }

    @Override
    public List<UserResponseDTO> searchUsersByDisplayName(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query tìm kiếm không được để trống");
        }

        List<User> users = userRepository.findByDisplayNameContainingIgnoreCase(query.trim());

        return users.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponseDTO> searchUsersByEmailOrDisplayName(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query tìm kiếm không được để trống");
        }

        List<User> users = userRepository.findByEmailOrDisplayNameContainingIgnoreCase(query.trim());

        return users.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDTO getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        return convertToResponseDTO(user);
    }

    @Override
    @Transactional
    public void updateLastSeen(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * Convert User entity to UserResponseDTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getCreatedAt(),
                user.getLastSeen()
        );
    }
}
