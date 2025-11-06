package com.learning.server.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity Message - đại diện cho bảng messages trong database
 * Tuân thủ quy tắc Entity trong coding instructions
 */
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    @NotNull(message = "ID người gửi không được để trống")
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    @NotNull(message = "ID người nhận không được để trống")
    private Long receiverId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "message_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * Enum cho loại tin nhắn
     */
    public enum MessageType {
        TEXT,
        IMAGE
    }

    // Lazy loading relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", insertable = false, updatable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", insertable = false, updatable = false)
    private User receiver;

    /**
     * Tự động set thời gian gửi khi persist entity
     */
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (messageType == null) {
            messageType = MessageType.TEXT;
        }
    }

    /**
     * Constructor để tạo Message mới (không có id và timestamps)
     */
    public Message(Long senderId, Long receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.isRead = false;
    }
}
