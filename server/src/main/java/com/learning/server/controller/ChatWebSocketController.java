package com.learning.server.controller;

import com.learning.server.dto.websocket.ChatMessageDTO;
import com.learning.server.dto.websocket.MessageResponseDTO;
import com.learning.server.dto.websocket.TypingNotificationDTO;
import com.learning.server.dto.websocket.UserStatusDTO;
import com.learning.server.entity.User;
import com.learning.server.repository.UserRepository;
import com.learning.server.security.JwtTokenProvider;
import com.learning.server.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * WebSocket Controller để xử lý real-time chat
 * Tuân thủ thiết kế trong design.md
 */
@Controller
@Slf4j
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Xử lý gửi tin nhắn qua WebSocket
     * Mapping: /app/chat.sendMessage
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, Principal principal) {
        try {
            log.info("Nhận tin nhắn từ user: {}, tới user: {}", principal.getName(), chatMessage.receiverId());

            // Lấy thông tin sender từ JWT token
            String senderEmail = principal.getName();
            User sender = userRepository.findByEmail(senderEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));

            // Validate receiver exists
            User receiver = userRepository.findById(chatMessage.receiverId())
                    .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));

            // Kiểm tra không gửi tin nhắn cho chính mình
            if (sender.getId().equals(chatMessage.receiverId())) {
                throw new IllegalArgumentException("Không thể gửi tin nhắn cho chính mình");
            }

            // Lưu tin nhắn vào database
            MessageResponseDTO savedMessage = messageService.saveMessage(
                    sender.getId(),
                    chatMessage.receiverId(),
                    chatMessage.content()
            );

            // Gửi tin nhắn tới người nhận qua user-specific queue
            messagingTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/queue/messages",
                    savedMessage
            );

            // Gửi confirmation cho người gửi
            messagingTemplate.convertAndSendToUser(
                    sender.getEmail(),
                    "/queue/messages",
                    savedMessage
            );

            log.info("Đã gửi tin nhắn thành công: {}", savedMessage.id());

        } catch (Exception e) {
            log.error("Lỗi khi gửi tin nhắn: {}", e.getMessage(), e);

            // Gửi lỗi về cho client
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    "Lỗi khi gửi tin nhắn: " + e.getMessage()
            );
        }
    }

    /**
     * Xử lý thông báo typing
     * Mapping: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(@Payload TypingNotificationDTO typingNotification, Principal principal) {
        try {
            log.debug("Nhận thông báo typing từ user: {}, tới user: {}, isTyping: {}",
                    principal.getName(), typingNotification.receiverId(), typingNotification.isTyping());

            // Lấy thông tin sender
            String senderEmail = principal.getName();
            User sender = userRepository.findByEmail(senderEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));

            // Validate receiver exists
            User receiver = userRepository.findById(typingNotification.receiverId())
                    .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));

            // Tạo typing response với thông tin sender
            var typingResponse = new TypingResponseDTO(
                    sender.getId(),
                    sender.getDisplayName(),
                    typingNotification.isTyping()
            );

            // Gửi thông báo typing tới người nhận
            messagingTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/queue/typing",
                    typingResponse
            );

            log.debug("Đã gửi thông báo typing thành công");

        } catch (Exception e) {
            log.error("Lỗi khi xử lý thông báo typing: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý khi user connect (online)
     */
    public void handleUserConnect(String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

            // Cập nhật last seen
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);

            // Broadcast user online status
            UserStatusDTO statusDTO = new UserStatusDTO(
                    user.getId(),
                    user.getDisplayName(),
                    UserStatusDTO.UserStatus.ONLINE
            );

            messagingTemplate.convertAndSend("/topic/users/status", statusDTO);

            log.info("User {} đã online", userEmail);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý user connect: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý khi user disconnect (offline)
     */
    public void handleUserDisconnect(String userEmail) {
        try {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

            // Cập nhật last seen
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);

            // Broadcast user offline status
            UserStatusDTO statusDTO = new UserStatusDTO(
                    user.getId(),
                    user.getDisplayName(),
                    UserStatusDTO.UserStatus.OFFLINE
            );

            messagingTemplate.convertAndSend("/topic/users/status", statusDTO);

            log.info("User {} đã offline", userEmail);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý user disconnect: {}", e.getMessage(), e);
        }
    }

    /**
     * Inner DTO class cho typing response
     */
    public record TypingResponseDTO(
            Long senderId,
            String senderDisplayName,
            Boolean isTyping
    ) {}
}
