package com.learning.server.controller;

import com.learning.server.common.ApiResponse;
import com.learning.server.dto.websocket.MessageResponseDTO;
import com.learning.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho Message operations
 * Tuân thủ quy tắc RestController trong coding instructions
 */
@RestController
@RequestMapping("/api/messages")
@Slf4j
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * Lấy tổng quan tất cả conversations của user hiện tại
     * GET /api/messages
     * Mở rộng từ design để support conversation list
     */
    @GetMapping
    public ResponseEntity<List<MessageResponseDTO>> getAllConversations(Authentication authentication) {
        try {
            log.info("Lấy tất cả conversations của user");

            // Lấy current user ID từ JWT authentication
            Long currentUserId = getCurrentUserId(authentication);

            List<MessageResponseDTO> conversations = messageService.getUnreadMessages(currentUserId);

            log.info("Lấy conversations thành công: {} conversations", conversations.size());
            return ResponseEntity.ok(conversations);

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation khi lấy conversations: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy conversations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Lấy lịch sử chat với user cụ thể
     * GET /api/messages/{userId}
     * Trả về ApiResponse để đồng nhất với các endpoint khác
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<MessageResponseDTO>>> getChatHistory(
            @PathVariable Long userId,
            Authentication authentication) {

        try {
            log.info("Lấy lịch sử chat với user: {}", userId);

            // Lấy current user ID từ JWT authentication
            Long currentUserId = getCurrentUserId(authentication);

            // Validate không thể lấy lịch sử chat với chính mình
            if (currentUserId.equals(userId)) {
                log.warn("User {} trying to get chat history with themselves", currentUserId);
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("ERROR", "Không thể lấy lịch sử chat với chính mình", null));
            }

            List<MessageResponseDTO> messages = messageService.getChatHistory(currentUserId, userId);

            log.info("Lấy lịch sử chat thành công: {} tin nhắn", messages.size());
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Lấy lịch sử chat thành công", messages));

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation khi lấy lịch sử chat: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", "Lỗi validation: " + e.getMessage(), null));
        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử chat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>("ERROR", "Lỗi server: " + e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách tin nhắn chưa đọc
     * GET /api/messages/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<MessageResponseDTO>> getUnreadMessages(Authentication authentication) {
        try {
            log.info("Lấy danh sách tin nhắn chưa đọc");

            // Lấy current user ID từ JWT authentication
            Long currentUserId = getCurrentUserId(authentication);

            List<MessageResponseDTO> messages = messageService.getUnreadMessages(currentUserId);

            log.info("Lấy tin nhắn chưa đọc thành công: {} tin nhắn", messages.size());
            return ResponseEntity.ok(messages);

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation khi lấy tin nhắn chưa đọc: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn chưa đọc: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     * PUT /api/messages/{messageId}/read
     * Tuân thủ design: trả về 200 OK không có body
     */
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable Long messageId,
            Authentication authentication) {
        try {
            log.info("Đánh dấu tin nhắn {} đã đọc", messageId);

            // Lấy current user ID để verify quyền đánh dấu đã đọc
            Long currentUserId = getCurrentUserId(authentication);

            messageService.markMessageAsRead(messageId, currentUserId);

            log.info("Đánh dấu tin nhắn {} đã đọc thành công", messageId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation khi đánh dấu tin nhắn đã đọc: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tin nhắn đã đọc: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Đánh dấu tất cả tin nhắn từ user cụ thể là đã đọc
     * PUT /api/messages/read-all/{senderId}
     */
    @PutMapping("/read-all/{senderId}")
    public ResponseEntity<Void> markAllMessagesAsRead(
            @PathVariable Long senderId,
            Authentication authentication) {

        try {
            log.info("Đánh dấu tất cả tin nhắn từ user {} đã đọc", senderId);

            // Lấy current user ID từ JWT authentication
            Long currentUserId = getCurrentUserId(authentication);

            // Validate không thể đánh dấu tin nhắn của chính mình
            if (currentUserId.equals(senderId)) {
                log.warn("User {} trying to mark their own messages as read", currentUserId);
                return ResponseEntity.badRequest().build();
            }

            messageService.markAllMessagesAsRead(senderId, currentUserId);

            log.info("Đánh dấu tất cả tin nhắn từ user {} đã đọc thành công", senderId);
            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation khi đánh dấu tất cả tin nhắn đã đọc: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tất cả tin nhắn đã đọc: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Helper method để lấy current user ID từ Authentication
     * Tuân thủ quy tắc trong coding instructions
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("User chưa được authentication");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        } else {
            throw new IllegalArgumentException("Invalid principal type: " + principal.getClass() +
                ", expected Long (userId). Principal value: " + principal);
        }
    }

    /**
     * Đếm số tin nhắn chưa đọc từ user cụ thể
     * GET /api/messages/unread-count/{senderId}
     */
    @GetMapping("/unread-count/{senderId}")
    public ResponseEntity<ApiResponse<Long>> countUnreadMessages(
            @PathVariable Long senderId,
            Authentication authentication) {

        try {
            log.info("Đếm tin nhắn chưa đọc từ user: {}", senderId);

            // TODO: Get current user ID from authentication
            Long currentUserId = 1L; // This should come from authentication

            Long count = messageService.countUnreadMessages(senderId, currentUserId);

            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Đếm tin nhắn chưa đọc thành công", count));

        } catch (Exception e) {
            log.error("Lỗi khi đếm tin nhắn chưa đọc: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", "Lỗi khi đếm tin nhắn chưa đọc: " + e.getMessage(), null));
        }
    }

    /**
     * Xóa tin nhắn (soft delete)
     * DELETE /api/messages/{messageId}
     * Cả người gửi và người nhận đều không thấy tin nhắn sau khi xóa
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {

        try {
            log.info("Xóa tin nhắn: {}", messageId);

            // Lấy current user ID từ JWT authentication
            Long currentUserId = getCurrentUserId(authentication);

            messageService.deleteMessage(messageId, currentUserId);

            log.info("Xóa tin nhắn {} thành công", messageId);
            return ResponseEntity.ok(new ApiResponse<>("SUCCESS", "Xóa tin nhắn thành công", null));

        } catch (IllegalArgumentException e) {
            log.warn("Lỗi validation khi xóa tin nhắn: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>("ERROR", e.getMessage(), null));
        } catch (Exception e) {
            log.error("Lỗi khi xóa tin nhắn: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>("ERROR", "Lỗi server: " + e.getMessage(), null));
        }
    }
}
