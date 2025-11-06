package com.learning.server.service.impl;

import com.learning.server.dto.websocket.MessageResponseDTO;
import com.learning.server.entity.Message;
import com.learning.server.entity.User;
import com.learning.server.repository.MessageReactionRepository;
import com.learning.server.repository.MessageRepository;
import com.learning.server.repository.UserRepository;
import com.learning.server.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation của MessageService
 * Tuân thủ quy tắc ServiceImpl trong coding instructions
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageReactionRepository reactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public MessageResponseDTO saveMessage(Long senderId, Long receiverId, String content) {
        return saveMessage(senderId, receiverId, content, Message.MessageType.TEXT, null);
    }

    @Override
    @Transactional
    public MessageResponseDTO saveMessage(Long senderId, Long receiverId, String content, 
                                        Message.MessageType messageType, String imageUrl) {
        // Validate users exist
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));

        // Tạo và lưu message
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content != null ? content : "");
        message.setMessageType(messageType);
        message.setImageUrl(imageUrl);
        message.setIsRead(false);
        
        Message savedMessage = messageRepository.save(message);

        return convertToResponseDTO(savedMessage, sender.getDisplayName());
    }

    @Override
    public List<MessageResponseDTO> getChatHistory(Long currentUserId, Long otherUserId) {
        // Validate users exist
        userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User hiện tại không tồn tại"));
        userRepository.findById(otherUserId)
                .orElseThrow(() -> new IllegalArgumentException("User khác không tồn tại"));

        List<Message> messages = messageRepository.findChatHistory(currentUserId, otherUserId);

        return messages.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageResponseDTO> getUnreadMessages(Long userId) {
        // Validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));

        List<Message> messages = messageRepository.findUnreadMessages(userId);

        return messages.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markMessageAsRead(Long messageId, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Tin nhắn không tồn tại"));

        // Chỉ receiver mới có thể đánh dấu tin nhắn đã đọc
        if (!message.getReceiver().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Không có quyền đánh dấu tin nhắn này đã đọc");
        }

        message.setIsRead(true);
        messageRepository.save(message);

    }

    @Override
    @Transactional
    public void markAllMessagesAsRead(Long senderId, Long receiverId) {
        // Validate users exist
        userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));
        userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));

        messageRepository.markMessagesAsRead(senderId, receiverId);
    }

    @Override
    public Long countUnreadMessages(Long senderId, Long receiverId) {
        // Validate users exist
        userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));
        userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Người nhận không tồn tại"));

        return messageRepository.countUnreadMessages(senderId, receiverId);
    }

    @Override
    public MessageResponseDTO getLatestMessage(Long userId1, Long userId2) {
        // Validate users exist
        userRepository.findById(userId1)
                .orElseThrow(() -> new IllegalArgumentException("User 1 không tồn tại"));
        userRepository.findById(userId2)
                .orElseThrow(() -> new IllegalArgumentException("User 2 không tồn tại"));

        List<Message> messages = messageRepository.findLatestMessages(userId1, userId2);

        if (messages.isEmpty()) {
            return null;
        }

        return convertToResponseDTO(messages.get(0));
    }

    /**
     * Convert Message entity to MessageResponseDTO
     */
    private MessageResponseDTO convertToResponseDTO(Message message) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Người gửi không tồn tại"));

        return convertToResponseDTO(message, sender.getDisplayName());
    }

    /**
     * Convert Message entity to MessageResponseDTO with sender display name
     */
    private MessageResponseDTO convertToResponseDTO(Message message, String senderDisplayName) {
        // Get reaction counts for this message
        Map<String, Integer> reactions = getReactionCounts(message.getId());
        
        return new MessageResponseDTO(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getContent(),
                message.getSentAt(),
                message.getIsRead(),
                senderDisplayName,
                message.getMessageType() != null ? message.getMessageType().name() : "TEXT",
                message.getImageUrl(),
                reactions
        );
    }

    /**
     * Get reaction counts for a message
     */
    private Map<String, Integer> getReactionCounts(Long messageId) {
        List<Object[]> results = reactionRepository.countReactionsByMessageId(messageId);
        Map<String, Integer> counts = new HashMap<>();
        
        for (Object[] result : results) {
            String emoji = (String) result[0];
            Long count = (Long) result[1];
            counts.put(emoji, count.intValue());
        }
        
        return counts;
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Tin nhắn không tồn tại"));

        // Chỉ người gửi hoặc người nhận mới có thể xóa
        if (!message.getSenderId().equals(currentUserId) && 
            !message.getReceiverId().equals(currentUserId)) {
            throw new IllegalArgumentException("Không có quyền xóa tin nhắn này");
        }

        // Soft delete - đánh dấu là đã xóa
        message.setIsDeleted(true);
        messageRepository.save(message);
    }
}
