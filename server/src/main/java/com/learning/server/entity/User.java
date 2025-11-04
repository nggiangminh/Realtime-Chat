package com.learning.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity User - đại diện cho bảng users trong database
 * Tuân thủ quy tắc Entity trong coding instructions
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Email(message = "Email không hợp lệ")
    @NotEmpty(message = "Email không được để trống")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    private String email;

    @Column(nullable = false)
    @NotEmpty(message = "Mật khẩu không được để trống")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
    private String password;

    @Column(name = "display_name", nullable = false)
    @NotEmpty(message = "Tên hiển thị không được để trống")
    @Size(max = 100, message = "Tên hiển thị không được vượt quá 100 ký tự")
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    /**
     * Tự động set thời gian tạo khi persist entity
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Constructor để tạo User mới (không có id và timestamps)
     */
    public User(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }
}
